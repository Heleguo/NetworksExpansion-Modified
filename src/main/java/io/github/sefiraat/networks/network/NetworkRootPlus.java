package io.github.sefiraat.networks.network;

import com.balugaq.netex.api.data.StorageUnitData;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.networks.advanced.AdvancedGreedyBlock;
import io.github.sefiraat.networks.managers.ExperimentalFeatureManager;
import io.github.sefiraat.networks.network.barrel.InfinityBarrel;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.network.stackcaches.ItemStackCache;
import io.github.sefiraat.networks.slimefun.network.NetworkGreedyBlock;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.utils.itemstack.ItemStackWrapper;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class NetworkRootPlus extends NetworkRoot {
    protected NetworkRootPlus(@NotNull Location location, @NotNull NodeType type, int maxNodes) {
        super(location, type, maxNodes);
    }
    @Override
    public ItemStack getItemStack(@Nonnull ItemRequest itemRequest) {
        if(enableGetAsync&& ExperimentalFeatureManager.getInstance().isEnableRootGetItemStackAsync()){
            return getItemStackRewrite(itemRequest);
        }else {
            return super.getItemStack(itemRequest);
        }
    }
    @Override
    public void addItemStack(ItemStack itemStack) {
        if(enableAddAsync){
            addItemStackAsync(itemStack);
        }else {
            super.addItemStack(itemStack);
        }
    }
    //private byte[][] cellMenuSlotLock= IntStream.range(0,63).mapToObj(i->new byte[0]).toArray(byte[][]::new);
    private static final boolean enableGetAsync=true;
    private static final boolean enableAddAsync=true;
    private static Constructor<NetworkRoot> constructor;
    //this method should be synchronized,otherwise adding inner sync locks will decrease performance
    private ConcurrentHashMap<Location, ReentrantLock> chargeEditing= new ConcurrentHashMap<>();
    public void removeRootPower(long power){
        if(ExperimentalFeatureManager.getInstance().isEnableAsyncRootPower()){
            this.rootPower -= power;
            CompletableFuture.runAsync(()->{
                int removed = 0;
                for (Location node : getPowerNodes()) {
                    var blockData=StorageCacheUtils.getBlock(node);
                    if(blockData==null)continue;

                    if(blockData.isPendingRemove()){
                        continue;
                    }else if(!blockData.isDataLoaded()){
                        StorageCacheUtils.requestLoad(blockData);
                        continue;
                    }else{
                        var lock=chargeEditing.computeIfAbsent(blockData.getLocation(),(loc)->new ReentrantLock());
                        lock.lock();
                        try{
                            String val=blockData.getData("energy-charge");
                            if(val!=null){
                                int charge=Integer.parseInt(val);
                                if (charge <= 0) {
                                    continue;
                                }
                                final int toRemove = (int) Math.min(power - removed, charge);
                                blockData.setData("energy-charge",String.valueOf(charge-toRemove));
                                removed = removed + toRemove;
                            }
                        }catch (NumberFormatException e){
                        }finally {
                            lock.unlock();
                        }
                        if (removed >= power) {
                            return;
                        }
                    }

                }
            });
        }else {
            super.removeRootPower(power);
        }
    }
//    public synchronized ItemStack getItemStackAsync(@Nonnull ItemRequest itemRequest) {
//        return getItemStackRewrite(itemRequest);
//    }
    public ItemStack getItemStackRewrite(@Nonnull ItemRequest request) {
        if (request.getAmount() <= 0) {
            return null;
        }
        ItemStack stackToReturn = null;
        int totalAmount=0;
        requestItem:{
            // Barrels first
            var barrels=getOutputAbleBarrels().get(request.getItemType());
            if(barrels!=null){
                for (BarrelIdentity barrelIdentity :barrels) {
                    if (barrelIdentity.getItemStack() == null || !StackUtils.itemsMatch(request, barrelIdentity)) {
                        continue;
                    }

                    boolean infinity = barrelIdentity instanceof InfinityBarrel;
                    //todo cut this shit
                    synchronized (barrelIdentity){
                        final ItemStack fetched = barrelIdentity.requestItem(request);
                        if (fetched == null || fetched.getType() == Material.AIR || (infinity && fetched.getAmount() == 1)) {
                            continue;
                        }
                        // Stack is null, so we can fill it here
                        if (stackToReturn == null) {
                            stackToReturn = fetched.clone();
                            totalAmount=0;
                        }
                        final int fetchedAmount=fetched.getAmount();
                        final int preserveAmount = infinity ? fetchedAmount - 1 :fetchedAmount;
                        final int requestAmount=request.getAmount();
                        if (requestAmount <= preserveAmount) {
                            totalAmount+=requestAmount;
                            //re getAmount in case of Async
                            fetched.setAmount(fetched.getAmount() - requestAmount);
                            break requestItem;
                        } else {
                            totalAmount+=preserveAmount;
                            request.receiveAmount(preserveAmount);
                            //setAmount for Infinity fetchItem
                            //so what kind of shit would do this?
                            fetched.setAmount(fetched.getAmount() - preserveAmount);
                        }
                    }
                }
            }


            // Units
            for (StorageUnitData cache : getOutputAbleCargoStorageUnitDatas().keySet()) {
                //we ensure that requestItem is multi-thread safe
                ItemStack take = cache.requestItem(request);
                if (take != null) {
                    if (stackToReturn == null) {
                        stackToReturn = take.clone();
                        totalAmount=0;
                    }
                    totalAmount+=take.getAmount();
                    request.receiveAmount(take.getAmount());

                    if (request.getAmount() <= 0) {
                        break requestItem;
                    }
                }
            }

            // Cells
            for (BlockMenu blockMenu : getCellMenus()) {
                int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
                byte[][] slotLock=cellMenuSlotLock.computeIfAbsent(blockMenu,k->{
                    byte[][] locks = new byte[54][];
                    Arrays.setAll(locks, i -> new byte[0]);
                    return locks;
                });
                for (int slot : slots) {
                    synchronized (slotLock[slot]) {
                        final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                        if (itemStack == null
                                || itemStack.getType() == Material.AIR
                                || !StackUtils.itemsMatch(request, itemStack)
                        ) {
                            continue;
                        }

                        // Mark the Cell as dirty otherwise the changes will not save on shutdown
                        blockMenu.markDirty();

                        // If the return stack is null, we need to set it up
                        if (stackToReturn == null) {
                            stackToReturn = itemStack.clone();
                        }
                        int requestedAmount=request.getAmount();
                        int itemAmount=itemStack.getAmount();
                        if (requestedAmount <= itemAmount) {
                            // We can't take more than this stack. Level to request amount, remove items and then return
                            totalAmount+=requestedAmount;
                            itemStack.setAmount(itemStack.getAmount() - requestedAmount);
                            break requestItem;
                        } else {
                            // We can take more than what is here, consume before trying to take more
                            totalAmount+=itemAmount;
                            request.receiveAmount(itemAmount);
                            itemStack.setAmount(0);
                        }
                    }
                }
            }

            // Crafters
            for (BlockMenu blockMenu : getCrafterOutputs()) {
                int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
                var lock=greedyBlockOperationLockMap.computeIfAbsent(blockMenu,i->new ReentrantLock());
                lock.lock();
                try{
                    for (int slot : slots) {
                        final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                        if (itemStack == null || itemStack.getType() == Material.AIR || !StackUtils.itemsMatch(
                                request,
                                itemStack
                        )) {
                            continue;
                        }

                        // Stack is null, so we can fill it here
                        if (stackToReturn == null) {
                            stackToReturn = itemStack.clone();
                            totalAmount=0;
                        }

                        int requestedAmount=request.getAmount();
                        int itemAmount=itemStack.getAmount();
                        if (requestedAmount <= itemAmount) {
                            // We can't take more than this stack. Level to request amount, remove items and then return
                            totalAmount+=requestedAmount;
                            itemStack.setAmount(itemStack.getAmount() - requestedAmount);
                            break requestItem;
                        } else {
                            // We can take more than what is here, consume before trying to take more
                            totalAmount+=itemAmount;
                            request.receiveAmount(itemAmount);
                            itemStack.setAmount(0);
                        }
                    }
                }finally {
                    lock.unlock();
                }
            }

            for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {
                int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
                var lock=greedyBlockOperationLockMap.computeIfAbsent(blockMenu,i->new ReentrantLock());
                lock.lock();
                try{
                    for (int slot : slots) {
                        final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                        if (itemStack == null || itemStack.getType() == Material.AIR || !StackUtils.itemsMatch(
                                request,
                                itemStack
                        )) {
                            continue;
                        }

                        // Stack is null, so we can fill it here
                        if (stackToReturn == null) {
                            stackToReturn = itemStack.clone();
                            totalAmount=0;
                        }

                        int requestedAmount=request.getAmount();
                        int itemAmount=itemStack.getAmount();
                        if (requestedAmount <= itemAmount) {
                            // We can't take more than this stack. Level to request amount, remove items and then return
                            totalAmount+=requestedAmount;
                            itemStack.setAmount(itemStack.getAmount() - requestedAmount);
                            break requestItem;
                        } else {
                            // We can take more than what is here, consume before trying to take more
                            totalAmount+=itemAmount;
                            request.receiveAmount(itemAmount);
                            itemStack.setAmount(0);
                        }
                    }
                }finally {
                    lock.unlock();
                }
            }

            // Greedy Blocks
            for (BlockMenu blockMenu : getGreedyBlockMenus()) {
                int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
                var lock=greedyBlockOperationLockMap.computeIfAbsent(blockMenu,i->new ReentrantLock());
                lock.lock();
                try{
                    final ItemStack itemStack = blockMenu.getItemInSlot(slots[0]);
                    if (itemStack == null
                            || itemStack.getType() == Material.AIR
                            || !StackUtils.itemsMatch(request, itemStack)
                    ) {
                        continue;
                    }

                    // Mark the Cell as dirty otherwise the changes will not save on shutdown
                    blockMenu.markDirty();

                    // If the return stack is null, we need to set it up
                    if (stackToReturn == null) {
                        stackToReturn = itemStack.clone();
                        totalAmount=0;
                    }

                    int requestedAmount=request.getAmount();
                    int itemAmount=itemStack.getAmount();
                    if (requestedAmount <= itemAmount) {
                        // We can't take more than this stack. Level to request amount, remove items and then return
                        totalAmount+=requestedAmount;
                        itemStack.setAmount(itemStack.getAmount() - requestedAmount);
                        break requestItem;
                    } else {
                        // We can take more than what is here, consume before trying to take more
                        totalAmount+=itemAmount;
                        request.receiveAmount(itemAmount);
                        itemStack.setAmount(0);
                    }
                }finally {
                    lock.unlock();
                }
            }
        }
        //end ,setAmount and return
        if(stackToReturn == null||totalAmount<=0){
            return null;
        }
        stackToReturn.setAmount(totalAmount);
        return stackToReturn;
    }
    /**
     * add ItemStack to network
     * this method should be multi-thread safe
     * @param incoming
     */
    private ConcurrentHashMap<BlockMenu,ReentrantLock> greedyBlockOperationLockMap =new ConcurrentHashMap<>();
    public void addItemStackAsync(@Nonnull ItemStack incoming) {
        //incomingCache cached the meta of incoming,and the getAmount / setAmount is sync
        ItemStackCache incomingCache = ItemRequest.of(incoming,0);
        for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {

            final ItemStack template = blockMenu.getItemInSlot(AdvancedGreedyBlock.TEMPLATE_SLOT);

            if (template == null || template.getType() == Material.AIR || !StackUtils.itemsMatch(incomingCache, template)) {
                continue;
            }
            blockMenu.markDirty();
            var lock= greedyBlockOperationLockMap.computeIfAbsent(blockMenu, k->new ReentrantLock());
            lock.lock();
            try{
                BlockMenuUtil.pushItem(blockMenu, incomingCache,false, ADVANCED_GREEDY_BLOCK_AVAILABLE_SLOTS);
            }finally {
                lock.unlock();
            }
            // Given we have found a match, it doesn't matter if the item moved or not, we will not bring it in
            return;
        }
        // Run for matching greedy blocks
        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            final ItemStack template = blockMenu.getItemInSlot(NetworkGreedyBlock.TEMPLATE_SLOT);
            if (template == null || template.getType() == Material.AIR || !StackUtils.itemsMatch(incomingCache, template)) {
                continue;
            }
            blockMenu.markDirty();
            var lock= greedyBlockOperationLockMap.computeIfAbsent(blockMenu, k->new ReentrantLock());
            lock.lock();
            try{
                BlockMenuUtil.pushItem(blockMenu, incomingCache,false, GREEDY_BLOCK_AVAILABLE_SLOTS[0]);
            }finally {
                lock.unlock();
            }
            // Given we have found a match, it doesn't matter if the item moved or not, we will not bring it in
            return;
        }

        // Run for matching barrels
        var barrels=getInputAbleBarrels().get(incomingCache.getItemType());
        if(barrels!=null){
            for (BarrelIdentity barrelIdentity :barrels) {
                if (StackUtils.itemsMatch(barrelIdentity, incomingCache)) {
                    // barrel Identity should realize multi-thread safe somehow
                    barrelIdentity.depositItemStack(incoming);
                    // All distributed, can escape
                    if (incoming.getAmount() == 0) {
                        return;
                    }
                }
            }
        }


        for (StorageUnitData cache : getInputAbleCargoStorageUnitDatas().keySet()) {
            //storageUnitData should realize multi-thread safe somehow
            cache.depositItemStack(incomingCache, true);
            if (incoming.getAmount() == 0) {
                return;
            }
        }

        for (BlockMenu blockMenu : getCellMenus()) {
            blockMenu.markDirty();
            //multi-thread safe method of cell Menus
            pushCellAsync(blockMenu,incomingCache);
            if (incoming.getAmount() == 0) {
                return;
            }
        }
    }
    ConcurrentHashMap<BlockMenu,byte[][]> cellMenuSlotLock=new ConcurrentHashMap<>();
    public void pushCellAsync(BlockMenu cellMenu,ItemStackCache item){
        Material material = item.getItemType();
        int maxSize=material.getMaxStackSize();
        int leftAmount = item.getItemAmount();
        ItemStack sample=null;
        for (int slot : CELL_AVAILABLE_SLOTS) {
            if (leftAmount <= 0) {
                break;
            }
            byte[][] slotLock=cellMenuSlotLock.computeIfAbsent(cellMenu,k-> {
                byte[][] locks = new byte[54][];
                Arrays.setAll(locks, i -> new byte[0]);
                return locks;
            });
            synchronized (slotLock[slot]) {
                ItemStack existing = cellMenu.getItemInSlot(slot);

                if (existing == null || existing.getType() == Material.AIR) {
                    int received = Math.min(leftAmount, maxSize);
                    if(sample==null){
                        sample=StackUtils.getAsQuantity(item.getItemStack(),received);
                    }else {
                        sample.setAmount(received);
                    }
                    cellMenu.replaceExistingItem(slot,sample,false);
                    leftAmount -= received;
                    // item.setItemAmount();
                } else {
                    int existingAmount = existing.getAmount();
                    if (existingAmount >= maxSize) {
                        continue;
                    }
                    if (!StackUtils.itemsMatch(item, existing)) {
                        continue;
                    }
                    int received = Math.max(0, Math.min(maxSize - existingAmount, leftAmount));
                    leftAmount -= received;
                    existing.setAmount(existingAmount + received);
                }
            }
        }
        item.setItemAmount(leftAmount);
    }
}
