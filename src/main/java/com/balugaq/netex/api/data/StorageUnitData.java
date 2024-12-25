package com.balugaq.netex.api.data;

import com.balugaq.netex.api.enums.StorageUnitType;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import com.ytdd9527.networksexpansion.utils.databases.DataStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.network.stackcaches.ItemStackCache;
import io.github.sefiraat.networks.utils.StackUtils;
import it.unimi.dsi.fastutil.Hash;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

@ToString
public class StorageUnitData {

    private final int id;
    private final OfflinePlayer owner;
    private final SyncedHashMap<Integer, ItemContainer> storedItems;
    //a view of storedItems using Material as filter
    private final Map<Material, Set<ItemContainer>> material2Containers;
    private boolean isPlaced;
    private StorageUnitType sizeType;
    private Location lastLocation;
    private final byte[] mapLock=new byte[0];
    //private byte[][] containerLock= IntStream.range(0,54).mapToObj(i->new byte[0]).toArray(byte[][]::new);
    public StorageUnitData(int id, String ownerUUID, StorageUnitType sizeType, boolean isPlaced, Location lastLocation) {
        this(id, Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)), sizeType, isPlaced, lastLocation, new SyncedHashMap<>());
    }
    public static class SyncedHashMap<T,W> extends HashMap<T,W>{
        BiConsumer<T,W> putListener=(w,r)->{};
        BiConsumer<Object,W> removeListener=(o,r)->{};
        public void setSynced(BiConsumer<T,W> putListener, BiConsumer<Object,W> removeListener){
            this.putListener = putListener;
            this.removeListener = removeListener;
        }
        @Override
        public W put(T key, W value) {
            this.putListener.accept(key, value);
            return super.put(key, value);
        }
        public W remove(Object key) {
            var removed=super.remove(key);
            removeListener.accept(key, removed);
            return removed;
        }

    }

    public StorageUnitData(int id, String ownerUUID, StorageUnitType sizeType, boolean isPlaced, Location lastLocation, SyncedHashMap<Integer, ItemContainer> storedItems) {
        this(id, Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)), sizeType, isPlaced, lastLocation, storedItems);
    }

    public StorageUnitData(int id, OfflinePlayer owner, StorageUnitType sizeType, boolean isPlaced, Location lastLocation, SyncedHashMap<Integer, ItemContainer> storedItems) {
        this.id = id;
        this.owner = owner;
        this.sizeType = sizeType;
        this.isPlaced = isPlaced;
        this.lastLocation = lastLocation;
        this.storedItems = storedItems;
        this.material2Containers = new EnumMap<>(Material.class);
        this.storedItems.setSynced((key,value)->{
            this.material2Containers.computeIfAbsent(value.getItemType(),k -> new HashSet<>()).add(value);
        },(key,value)->{
            if (value != null) {
                var material=value.getItemType();
                var sets= material2Containers.get(material);
                if(sets != null) {
                    sets.removeIf(i->i.getId()==value.getId());
                    if(sets.isEmpty()){
                        material2Containers.remove(material);
                    }
                }

            }
        });
        for(ItemContainer container : storedItems.values()) {
            material2Containers.computeIfAbsent(container.getItemType(), k -> new HashSet<>()).add(container);
        }

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
    public int addStoredItem(ItemStack item, int amount, boolean contentLocked, boolean force){
        return addStoredItem(ItemStackCache.of(item),amount,contentLocked,force);
    }
    public int addStoredItem(ItemStackCache item, int amount, boolean contentLocked, boolean force) {
        int add = 0;
        boolean isVoidExcess = NetworksDrawer.isVoidExcess(getLastLocation());
        var containers=viewStoredItemByMaterial(item.getItemType());
        if(containers!=null){
            for (ItemContainer each : containers) {
                if (each.isSimilar(item)) {
                    // Found existing one, add amount
                    int eachAmount;
                    synchronized (each) {
                        eachAmount = each.getAmount();
                        add = Math.min(amount, sizeType.getEachMaxSize() - eachAmount);
                        if (add > 0||!isVoidExcess) {
                            each.addAmount(add);
                            eachAmount+=add;
                        } else {
                            //force voidExcess
                            item.setItemAmount(0);
                            return add;
                        }
                    }
                    DataStorage.setStoredAmount(id, each.getId(), eachAmount);
                    return add;

                }
            }
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
                int itemId = DataStorage.getItemId(item.getItemStack());
                var container = new ItemContainer(itemId, item.getItemStack(), add);
                storedItems.put(itemId, container);
                material2Containers.computeIfAbsent(container.getItemType(), k -> new HashSet<>()).add(container);
                DataStorage.addStoredItem(id, itemId, add);
                return add;
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
            storedItems.remove(itemId);
            DataStorage.deleteStoredItem(id, itemId);
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
    public Set<ItemContainer> viewStoredItemByMaterial(Material material){
        synchronized (mapLock){
//            Networks.getInstance().getLogger().info(material2Containers.toString());
//            Networks.getInstance().getLogger().info(storedItems.toString());
            return material2Containers.get(material);
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
        var containers=viewStoredItemByMaterial(itemRequest.getItemType());
        if(containers != null) {
            for (ItemContainer itemContainer : containers) {
                if (itemContainer.isSimilar(itemRequest)) {
                    int containerAmount;
                    int take;
                    synchronized (itemContainer) {
                        containerAmount = itemContainer.getAmount();
                        take = Math.min(amount, containerAmount);
                        if (take <= 0) {
                            break;
                        }
                        itemContainer.removeAmount(take);
                    }

                    DataStorage.setStoredAmount(id, itemContainer.getId(), containerAmount-take);
                    ItemStack clone = item.clone();
                    clone.setAmount(take);
                    return clone;
                }
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
    public void depositItemStack(ItemStackCache itemsToDeposit, boolean contentLocked, boolean force){
        if(itemsToDeposit.getItemStack()==null||isBlacklisted(itemsToDeposit.getItemStack())){
            return;
        }
        int actualAdded=addStoredItem(itemsToDeposit,itemsToDeposit.getItemAmount(),contentLocked,force);
        itemsToDeposit.setItemAmount(itemsToDeposit.getItemAmount() - actualAdded);
    }
    public void depositItemStack(ItemStackCache item, boolean contentLocked) {
        depositItemStack(item, contentLocked, false);
    }
    public void depositItemStack(ItemStack item, boolean contentLocked) {
        depositItemStack(item, contentLocked, false);
    }
}