package com.balugaq.netex.api.data;

import com.balugaq.netex.api.enums.StorageUnitType;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import com.ytdd9527.networksexpansion.utils.databases.DataStorage;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.utils.StackUtils;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

@ToString
public class StorageUnitData {

    private final int id;
    private final OfflinePlayer owner;
    private final Map<Integer, ItemContainer> storedItems;
    private boolean isPlaced;
    private StorageUnitType sizeType;
    private Location lastLocation;
    private byte[] mapLock=new byte[0];
    private byte[][] containerLock= IntStream.range(0,54).mapToObj(i->new byte[0]).toArray(byte[][]::new);
    public StorageUnitData(int id, String ownerUUID, StorageUnitType sizeType, boolean isPlaced, Location lastLocation) {
        this(id, Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)), sizeType, isPlaced, lastLocation, new HashMap<>());
    }

    public StorageUnitData(int id, String ownerUUID, StorageUnitType sizeType, boolean isPlaced, Location lastLocation, Map<Integer, ItemContainer> storedItems) {
        this(id, Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)), sizeType, isPlaced, lastLocation, storedItems);
    }

    public StorageUnitData(int id, OfflinePlayer owner, StorageUnitType sizeType, boolean isPlaced, Location lastLocation, Map<Integer, ItemContainer> storedItems) {
        this.id = id;
        this.owner = owner;
        this.sizeType = sizeType;
        this.isPlaced = isPlaced;
        this.lastLocation = lastLocation;
        this.storedItems = storedItems;
    }

    public static boolean isBlacklisted(@Nonnull ItemStack itemStack) {
        // if item is air, it's blacklisted
        if (itemStack.getType() == Material.AIR) {
            return true;
        }
        // if item has invalid durability, it's blacklisted
        if (itemStack.getType().getMaxDurability() < 0) {
            return true;
        }
        // if item is a shulker box, it's blacklisted
        if (Tag.SHULKER_BOXES.isTagged(itemStack.getType())) {
            return true;
        }
        // if item is a bundle, it's blacklisted
        if (itemStack.getType() == Material.BUNDLE) {
            return true;
        }

        return false;
    }

    /**
     * Add item to unit, the amount will be the item stack amount
     *
     * @param item: item will be added
     * @return the amount actual added
     */
    public int addStoredItem(ItemStack item, boolean contentLocked) {
        return addStoredItem(item, item.getAmount(), contentLocked, false);
    }

    public int addStoredItem(ItemStack item, boolean contentLocked, boolean force) {
        return addStoredItem(item, item.getAmount(), contentLocked, force);
    }

    public int addStoredItem(ItemStack item, int amount, boolean contentLocked) {
        return addStoredItem(item, amount, contentLocked, false);
    }

    /**
     * Add item to unit
     * this method should be multi-thread safe
     *
     * @param item:   item will be added
     * @param amount: amount will be added
     * @return the amount actual added
     */

    public int addStoredItem(ItemStack item, int amount, boolean contentLocked, boolean force) {
        int add = 0;
        boolean isVoidExcess = NetworksDrawer.isVoidExcess(getLastLocation());
        int index=0;
        for (ItemContainer each : getStoredItems()) {
            if (each.isSimilar(item)) {
                // Found existing one, add amount
                synchronized (containerLock[index]) {
                    add = Math.min(amount, sizeType.getEachMaxSize() - each.getAmount());
                    if (add > 0||!isVoidExcess) {
                        each.addAmount(add);
                        DataStorage.setStoredAmount(id, each.getId(), each.getAmount());
                    } else {
                        item.setAmount(0);
                        return add;
                    }
                    return add;
                }
            }
            index+=1;
        }
        // isforce?
        if (!force) {
            // If in content locked mode, no new input allowed
            if (contentLocked || NetworksDrawer.isLocked(getLastLocation())) return 0;
        }
        // Not found, new one
        synchronized (mapLock){
            if (storedItems.size() < sizeType.getMaxItemCount()) {
                add = Math.min(amount, sizeType.getEachMaxSize());
                synchronized (containerLock[index]) {
                    int itemId = DataStorage.getItemId(item);
                    storedItems.put(itemId, new ItemContainer(itemId, item, add));
                    DataStorage.addStoredItem(id, itemId, add);
                    return add;
                }
            }
        }
        return add;
    }

    public int getId() {
        return id;
    }

    public boolean isPlaced() {
        return isPlaced;
    }

    public synchronized void setPlaced(boolean isPlaced) {
        if (this.isPlaced != isPlaced) {
            this.isPlaced = isPlaced;
            DataStorage.setContainerStatus(id, isPlaced);
        }
    }

    public StorageUnitType getSizeType() {
        return sizeType;
    }

    public synchronized void setSizeType(StorageUnitType sizeType) {
        if (this.sizeType != sizeType) {
            this.sizeType = sizeType;
            DataStorage.setContainerSizeType(id, sizeType);
        }
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public synchronized void setLastLocation(Location lastLocation) {
        if (this.lastLocation != lastLocation) {
            this.lastLocation = lastLocation;
            DataStorage.setContainerLocation(id, lastLocation);
        }
    }

    public void removeItem(int itemId) {
        synchronized (mapLock){
            if (storedItems.remove(itemId) != null) {
                DataStorage.deleteStoredItem(id, itemId);
            }
        }
    }

    public void setItemAmount(int itemId, int amount) {
        if (amount < 0) {
            // Directly remove
            removeItem(itemId);
            return;
        }
        ItemContainer container;
        synchronized (mapLock){
            container = storedItems.get(itemId);
        }
        if (container != null) {
            container.setAmount(amount);
            DataStorage.setStoredAmount(id, itemId, amount);
        }
    }

    public void removeAmount(int itemId, int amount) {
        ItemContainer container;
        synchronized (mapLock){
            container = storedItems.get(itemId);
        }
        if (container != null) {
            container.removeAmount(amount);
            if (container.getAmount() <= 0 && !NetworksDrawer.isLocked(getLastLocation())) {
                removeItem(itemId);
                return;
            }
            DataStorage.setStoredAmount(id, itemId, container.getAmount());
        }
    }

    public int getStoredTypeCount() {
        synchronized (mapLock){
            return storedItems.size();
        }
    }

    public int getTotalAmount() {
        int re = 0;
        for (ItemContainer each : getStoredItemArray()) {
            re += each.getAmount();
        }
        return re;
    }

    public List<ItemContainer> getStoredItems() {
        synchronized (mapLock){
            return new ArrayList<>(storedItems.values());
        }
    }
    public ItemContainer[] getStoredItemArray(){
        synchronized (mapLock){
            return storedItems.values().toArray(ItemContainer[]::new);
        }
    }

    public OfflinePlayer getOwner() {
        return owner;
    }

    @Nullable
    public ItemStack requestItem(@Nonnull ItemRequest itemRequest) {
        ItemStack item = itemRequest.getItemStack();
        if (item == null) {
            return null;
        }

        int amount = itemRequest.getAmount();
        for (ItemContainer itemContainer : getStoredItemArray()) {
            int containerAmount = itemContainer.getAmount();
            if (StackUtils.itemsMatch(itemContainer.getSample(), item)) {
                int take = Math.min(amount, containerAmount);
                if (take <= 0) {
                    break;
                }
                itemContainer.removeAmount(take);
                DataStorage.setStoredAmount(id, itemContainer.getId(), itemContainer.getAmount());
                ItemStack clone = item.clone();
                clone.setAmount(take);
                return clone;
            }
        }
        return null;
    }

    public void depositItemStack(@Nonnull ItemStack[] itemsToDeposit, boolean contentLocked) {
        for (ItemStack item : itemsToDeposit) {
            depositItemStack(item, contentLocked);
        }
    }

    public void depositItemStack(ItemStack itemsToDeposit, boolean contentLocked, boolean force) {
        if (itemsToDeposit == null || isBlacklisted(itemsToDeposit)) {
            return;
        }
        int actualAdded = addStoredItem(itemsToDeposit, itemsToDeposit.getAmount(), contentLocked, force);
        itemsToDeposit.setAmount(itemsToDeposit.getAmount() - actualAdded);
    }

    public void depositItemStack(ItemStack item, boolean contentLocked) {
        depositItemStack(item, contentLocked, false);
    }
}