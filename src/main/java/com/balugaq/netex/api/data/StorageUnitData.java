package com.balugaq.netex.api.data;

import com.balugaq.netex.api.enums.StorageUnitType;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import com.ytdd9527.networksexpansion.utils.databases.DataStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.network.stackcaches.ItemStackCache;
import io.github.sefiraat.networks.utils.StackUtils;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import lombok.Getter;
import lombok.ToString;
import me.matl114.matlib.algorithms.dataStructures.struct.Pair;
import me.matl114.matlib.nmsUtils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

@SuppressWarnings("UnusedAssignment")
@ToString
public class StorageUnitData {

    @Getter
    private final int id;

    @Getter
    private final OfflinePlayer owner;

    private final SyncedHashMap<Integer, ItemContainer> storedItems;
    //a view of storedItems using Material as filter
    private final Map<Material, Int2ObjectArrayMap<ItemContainer>> material2Containers;
    private boolean isPlaced;

    @Getter
    private StorageUnitType sizeType;

    @Getter
    private Location lastLocation;

    private final byte[] mapLock=new byte[0];

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

    public StorageUnitData(
            int id,  String ownerUUID, StorageUnitType sizeType, boolean isPlaced, Location lastLocation) {
        this(
                id,
                Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)),
                sizeType,
                isPlaced,
                lastLocation,
                new SyncedHashMap<>());
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
        this.material2Containers = new Reference2ReferenceOpenHashMap<>();
        this.storedItems.setSynced((key,value)->{
            synchronized(mapLock) {
                this.material2Containers.computeIfAbsent(value.getItemType(),k -> new Int2ObjectArrayMap<>()).put((int)key, value);
            }
        },(key,value)->{
            if (value != null) {
                var material=value.getItemType();
                var sets= material2Containers.get(material);
                if(sets != null) {
                    sets.remove((int)key);
//                    sets.removeIf(i->i.getId()==value.getId());
                    if(sets.isEmpty()){
                        material2Containers.remove(material);
                    }
                }

            }
        });
        for(var container : storedItems.entrySet()) {
            material2Containers.computeIfAbsent(container.getValue().getItemType(), k -> new Int2ObjectArrayMap<>()).put((int)container.getKey(), container.getValue());
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
//    public int addStoredItem(ItemStack item, int amount, boolean contentLocked, boolean force){
//        return addStoredItem(ItemStackCache.of(item),amount,contentLocked,force);
//    }
    public int addStoredItem(ItemStack item, int amount, boolean contentLocked, boolean force) {
        if(item == null ){
            return 0;
        }
        int add = 0;

        var containers=viewStoredItemByMaterial(item.getType());
        if(containers!=null){
            for (ItemContainer each : containers.values()) {
                Integer val = depositItemContainer(each, item, amount, false);
                if(val != null){
                    return val;
                }
            }
        }
        // isforce?
        if (!force) {
            // If in content locked mode, no new input allowed
            if (contentLocked || NetworksDrawer.isLocked(getLastLocation())) return 0;
        }
        // Not found, new one, this action should be synchronized ,as it is not frequently called
        synchronized (mapLock){
            if (storedItems.size() < sizeType.getMaxItemCount()) {
                add = Math.min(amount, sizeType.getEachMaxSize());
                ItemStack cleanItem = ItemUtils.copyStack(item);
                int itemId = DataStorage.getItemId(cleanItem);
                var container = new ItemContainer(itemId, cleanItem, add, this.id);
                storedItems.put(itemId, container);
                material2Containers.computeIfAbsent(container.getItemType(), k -> new Int2ObjectArrayMap<>()).put(itemId, container);
                DataStorage.addStoredItem(id, itemId, add);
                return add;
            }
        }
        return add;
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

    public synchronized void setSizeType(StorageUnitType sizeType) {
        if (this.sizeType != sizeType) {
            this.sizeType = sizeType;
            DataStorage.setContainerSizeType(id, sizeType);
        }
    }

    public synchronized void setLastLocation(Location lastLocation) {
        if (this.lastLocation != lastLocation) {
            this.lastLocation = lastLocation;
            DataStorage.setContainerLocation(id, lastLocation);
        }
    }

    public void removeItem(int itemId) {
        synchronized (mapLock){
            if (storedItems.remove(itemId) != null)
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

    public long getTotalAmountLong() {
        long re = 0;
        for (ItemContainer each : getStoredItemArray()) {
            re += each.getAmount();
        }
        return re;
    }

    @Deprecated
    public @NotNull List<ItemContainer> getStoredItems() {
        return copyStoredItems();
    }

    public @NotNull List<ItemContainer> copyStoredItems() {
        synchronized (mapLock){
            return new ArrayList<>(storedItems.values());
        }
    }

    public @NotNull Collection<ItemContainer> getStoredItemsDirectly() {
        return storedItems.values();
    }

    public @NotNull Map<Integer, ItemContainer> copyStoredItemsMap() {
        synchronized (mapLock){
            return new HashMap<>(storedItems);
        }
    }

    public Map<Integer, ItemContainer> getStoredItemsMap() {
        return storedItems;
    }

    public ItemContainer[] getStoredItemArray(){
        synchronized (mapLock){
            return storedItems.values().toArray(ItemContainer[]::new);
        }
    }
    public Int2ObjectArrayMap<ItemContainer> viewStoredItemByMaterial(Material material){
        synchronized (mapLock){
            return material2Containers.get(material);
        }
    }

    public OfflinePlayer getOwner() {
        return owner;
    }
    private static final ItemStack EMPTY = new ItemStack(Material.AIR);
    @Deprecated
    @Nullable
    public ItemStack requestItem(@Nonnull ItemRequest itemRequest) {

        if (itemRequest.getItemStack() == null) {
            return null;
        }

        int amount = itemRequest.getAmount();
        var containers=viewStoredItemByMaterial(itemRequest.getItemType());
        if(containers != null) {
            find_loop:
            for (ItemContainer itemContainer : containers.values()) {
                ItemStack stack = requestItemContainer(itemContainer, itemRequest, amount, false);
                if(stack == EMPTY) {
                    break ;
                }else if(stack != null){
                    return stack;
                }
            }
        }
        return null;
    }
    public static ItemStack requestItemContainer(ItemContainer itemContainer, ItemRequest itemRequest, int amount, boolean bypassCheck){
        if (bypassCheck || itemContainer.isSimilar(itemRequest)) {
            int take;
            int oldAmount, newAmount;
            do{
                oldAmount = itemContainer.amount;
                take = Math.min(amount, oldAmount);
                if(take <= 0){
                    return EMPTY;
                }
                newAmount = oldAmount - take;
            }while (!ItemContainer.ATOMIC_AMOUNT_HANDLE.compareAndSet((ItemContainer)itemContainer, (int)oldAmount, (int)newAmount));
            DataStorage.setStoredAmount(itemContainer.getStorageUnitId(), itemContainer.getId(), itemContainer.amount);
            ItemStack clone = itemRequest.getItemStack().clone();
            clone.setAmount(take);
            return clone;
        }
        return null;
    }
    @Nullable
    public Integer depositItemContainer(ItemContainer each, ItemStack item, int amount, boolean bypassCheck){
        if (each.getStorageUnitId() == this.id && (bypassCheck || each.isSimilar(item)) ) {
            int add;
            boolean isVoidExcess = NetworksDrawer.isVoidExcess(getLastLocation());
            // Found existing one, add amount
            int oldAmount,newAmount;
            do{
                oldAmount = each.amount;
                add = Math.min(amount, sizeType.getEachMaxSize() - oldAmount);
                if(add > 0 || !isVoidExcess){
                    newAmount = oldAmount + add;
                }else {
                    return item.getAmount();
                }

            }while (!ItemContainer.ATOMIC_AMOUNT_HANDLE.compareAndSet((ItemContainer)each,(int)oldAmount, (int)newAmount));
            DataStorage.setStoredAmount(id, each.getId(), each.amount);
            return add;
        }
        return null;
    }

    public Integer getPrefetchInfo(ItemRequest itemRequest){
        ItemStack item = itemRequest.getItemStack();
        if(item == null){
            return null;
        }
        var containers=viewStoredItemByMaterial(itemRequest.getItemType());
        if(containers != null) {
            for (var itemContainerEntry : containers.int2ObjectEntrySet()) {
                var ic = itemContainerEntry.getValue();
                if (ic.isSimilar(itemRequest)) {
                    return itemContainerEntry.getIntKey();
                }
            }
        }
        return null;
    }
    @Nullable
    public ItemStack requestItem0(@NotNull Location accessor, @NotNull ItemRequest itemRequest) {
        return requestItem0(accessor, itemRequest, true);
    }

    @Nullable
    public ItemStack requestItem0(@NotNull Location accessor, @NotNull ItemRequest itemRequest, boolean contentLocked){
        return requestItem(itemRequest);
    }


    private static final Pair<ItemStack,Integer> NULL_REQUEST = Pair.of(null, null);
    @Nonnull
    public Pair<ItemStack,Integer> requestViaIndex(ItemRequest itemRequest, int index, boolean bypassCheck){
        ItemContainer container = storedItems.get(index);
        int amount = itemRequest.getAmount();
        ItemStack request = requestItemContainer(container, itemRequest, amount, bypassCheck);
        return request == EMPTY? Pair.of(null, index) : (request == null? Pair.of(null,null): Pair.of(request, index));
    }

    public void depositItemStack0(
        @NotNull Location accessor, @NotNull Map.Entry<ItemStack, Integer> entry, boolean contentLocked) {
        ItemStack item = StackUtils.getAsQuantity(entry.getKey(), entry.getValue());
        depositItemStack0(accessor, item, contentLocked);
        int leftover = item.getAmount();
        entry.setValue(leftover);
    }
    public void depositItemStack0(
        @NotNull Location accessor, @NotNull Map<ItemStack, Integer> itemsToDeposit, boolean contentLocked) {
        for (Map.Entry<ItemStack, Integer> entry : itemsToDeposit.entrySet()) {
            depositItemStack0(accessor, entry, contentLocked);
        }
    }


    public void depositItemStack(@Nonnull ItemStack[] itemsToDeposit, boolean contentLocked) {
        for (ItemStack item : itemsToDeposit) {
            depositItemStack(item, contentLocked);
        }
    }
    public void depositItemStack0(
        @NotNull Location accessor, @NotNull ItemStack @NotNull [] itemsToDeposit, boolean contentLocked) {
        for (ItemStack item : itemsToDeposit) {
            depositItemStack0(accessor, item, contentLocked);
        }
    }

    public void depositItemStack0(
        @NotNull Location accessor, @Nullable ItemStack itemsToDeposit, boolean contentLocked, boolean force) {
        if (itemsToDeposit == null || isBlacklisted(itemsToDeposit)) {
            return;
        }
        int actualAdded = addStoredItem0(accessor, itemsToDeposit, itemsToDeposit.getAmount(), contentLocked, force);
        if (actualAdded > 0) {
            itemsToDeposit.setAmount(itemsToDeposit.getAmount() - actualAdded);
        }
    }

    public void depositItemStack0(@NotNull Location accessor, ItemStack item, boolean contentLocked) {
        depositItemStack0(accessor, item, contentLocked, false);
    }

    public int addStoredItem0(
        Location accessor, @NotNull ItemStack item, int amount, boolean contentLocked, boolean force){
        return addStoredItem(item, amount, contentLocked, force);
    }

    public void depositItemStack(ItemStack itemsToDeposit, boolean contentLocked, boolean force) {
        if (itemsToDeposit == null || isBlacklisted(itemsToDeposit)) {
            return;
        }
        int actualAdded = addStoredItem(itemsToDeposit, itemsToDeposit.getAmount(), contentLocked, force);
        itemsToDeposit.setAmount(itemsToDeposit.getAmount() - actualAdded);
    }
    public void depositItemStack(ItemStackCache itemsToDeposit, boolean contentLocked, boolean force){
        ItemStack depositeItem = itemsToDeposit.getItemStack();
        if(depositeItem==null||isBlacklisted(depositeItem)){
            return;
        }
        int actualAdded=addStoredItem(itemsToDeposit.getItemStack(),itemsToDeposit.getItemAmount(), contentLocked, force);
        depositeItem.setAmount(depositeItem.getAmount() - actualAdded);
    }
    public void depositItemStack(ItemStackCache item, boolean contentLocked) {
        depositItemStack(item, contentLocked, false);
    }
    public void depositItemStack(ItemStack item, boolean contentLocked) {
        depositItemStack(item, contentLocked, false);
    }
}