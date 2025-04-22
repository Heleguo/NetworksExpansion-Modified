package io.github.sefiraat.networks.network;

import com.balugaq.netex.api.data.StorageUnitData;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.networks.advanced.AdvancedGreedyBlock;
import io.github.sefiraat.networks.NetworkAsyncUtil;
import io.github.sefiraat.networks.managers.ExperimentalFeatureManager;
import io.github.sefiraat.networks.network.barrel.InfinityBarrel;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.network.stackcaches.ItemStackCache;
import io.github.sefiraat.networks.slimefun.network.NetworkGreedyBlock;
import io.github.sefiraat.networks.utils.StackUtils;
import me.matl114.matlib.common.lang.annotations.Note;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class NetworkRootPlus extends NetworkRoot {
    protected NetworkRootPlus(@NotNull Location location, @NotNull NodeType type, int maxNodes) {
        super(location, type, maxNodes);
    }

    @Override
    public void addItemStack(ItemStack itemStack) {
//        if(enableAddAsync){
            addItemStackAsync(itemStack);
//        }else {
//            super.addItemStack(itemStack);
//        }
    }

//    private static final boolean enableGetAsync=true;
//    private static final boolean enableAddAsync=true;
//    private static Constructor<NetworkRoot> constructor;
    //this method should be synchronized,otherwise adding inner sync locks will decrease performance
    private static final ConcurrentHashMap<Location, ReentrantLock> chargeEditing= new ConcurrentHashMap<>();
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

    protected ItemStack fetchFromBarrels(ItemRequest request, BarrelIdentity barrelIdentity, ItemStack stackToReturn, boolean bypassCheck){
        if (barrelIdentity.getItemStack() == null || !StackUtils.itemsMatch(request, barrelIdentity)) {
            return null;
        }

        final ItemStack fetched = barrelIdentity.requestItemExact(request);
        if (fetched == null || fetched.getType() == Material.AIR) {
            return null;
        }
        if (stackToReturn == null) {
            stackToReturn = fetched.clone();
            stackToReturn.setAmount(0);
        }
        final int preserveAmount =  fetched.getAmount() ;
        if (request.getAmount() <= preserveAmount) {
            stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
            request.receiveAll();
        } else {
            stackToReturn.setAmount(stackToReturn.getAmount() + preserveAmount);
            request.receiveAmount(preserveAmount);
        }
        return stackToReturn;
    }

    @Override
    protected ItemStack getFromBlockMenu(ItemRequest request, ItemStack stackToReturn, BlockMenu blockMenu) {
        return NetworkAsyncUtil.getInstance().ensureLocation(blockMenu.getLocation(), ()->super.getFromBlockMenu(request, stackToReturn, blockMenu));
    }


    /**
     * add ItemStack to network
     * this method should be multi-thread safe
     * @param incoming
     */
    public void addItemStackAsync(@Nonnull ItemStack incoming) {
        //incomingCache cached the meta of incoming,and the getAmount / setAmount is sync
        ItemStackCache incomingCache = ItemRequest.of(incoming,0);
        for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {

            final ItemStack template = blockMenu.getItemInSlot(AdvancedGreedyBlock.TEMPLATE_SLOT);

            if (template == null || template.getType() == Material.AIR || !StackUtils.itemsMatch(incomingCache, template)) {
                continue;
            }
            blockMenu.markDirty();
            NetworkAsyncUtil.getInstance().ensureLocation(blockMenu.getLocation(), ()->
                BlockMenuUtil.pushItem(blockMenu, incomingCache.getItemStack(), ADVANCED_GREEDY_BLOCK_AVAILABLE_SLOTS)
            );
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
            NetworkAsyncUtil.getInstance().ensureLocation(blockMenu.getLocation(), ()->
                BlockMenuUtil.pushItem(blockMenu, incomingCache.getItemStack(), GREEDY_BLOCK_AVAILABLE_SLOTS[0])
            );
            // Given we have found a match, it doesn't matter if the item moved or not, we will not bring it in
            return;
        }

        // Run for matching barrels
        var barrels= getMaterial2InputAbleBarrels().get(incomingCache.getItemType());
        if(barrels!=null){
            for (BarrelIdentity barrelIdentity :barrels.values()) {
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


        for (StorageUnitData cache : getInputAbleCargoStorageUnitDatas0().values()) {
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

    public void pushCellAsync(BlockMenu cellMenu,ItemStackCache item){
        Material material = item.getItemType();
        int maxSize=material.getMaxStackSize();
        NetworkAsyncUtil.getInstance().ensureLocation(cellMenu.getLocation(),()->{
            int leftAmount = item.getItemAmount();
            ItemStack sample=null;
            for (int slot : CELL_AVAILABLE_SLOTS) {
                if (leftAmount <= 0) {
                    break;
                }
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
                } else {
                    int existingAmount = existing.getAmount();
                    if (existingAmount >= maxSize) {
                        continue;
                    }
                    if (!StackUtils.itemsMatch(item, existing)) {
                        continue;
                    }

                    existing = cellMenu.getItemInSlot(slot);
                    int received = Math.max(0, Math.min(maxSize - existingAmount, leftAmount));
                    leftAmount -= received;
                    existing.setAmount(existingAmount + received);
                }
            }
            item.setItemAmount(leftAmount);
        });


    }
}
