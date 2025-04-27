package io.github.sefiraat.networks.network;

import com.balugaq.netex.api.data.StorageUnitData;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
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


//    private static final boolean enableGetAsync=true;
//    private static final boolean enableAddAsync=true;
//    private static Constructor<NetworkRoot> constructor;
    //this method should be synchronized,otherwise adding inner sync locks will decrease performance
    private static final ConcurrentHashMap<Location, ReentrantLock> chargeEditing= new ConcurrentHashMap<>();
    public void removeRootPower(long power){
        if(ExperimentalFeatureManager.getInstance().isEnableAsyncRootPower()){
            this.rootPower -= power;
            NetworkAsyncUtil.getInstance().submitParallel(()->{
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
        if (barrelIdentity.getItemStack() == null || !(bypassCheck || StackUtils.itemsMatch(request, barrelIdentity))) {
            return null;
        }

        final ItemStack fetched = barrelIdentity.requestItemExact(request);
        if (fetched == null || fetched.getType() == Material.AIR) {
            return null;
        }
        final int preserveAmount =  fetched.getAmount() ;
        if (stackToReturn == null) {
            //already cloned ,
            stackToReturn = fetched;
            stackToReturn.setAmount(0);
        }

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
        //fix deadLock from acquiring lock
        return NetworkAsyncUtil.getInstance().ensureRootLocation(blockMenu.getLocation(), ()->super.getFromBlockMenu(request, stackToReturn, blockMenu));
    }

    @Override
    protected boolean addItemStackGreedyBlocks(ItemStack incomingCache) {
        for (SlimefunBlockData data : getAdvancedGreedyBlocksMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
            final ItemStack template = blockMenu.getItemInSlot(AdvancedGreedyBlock.TEMPLATE_SLOT);

            if (template == null || template.getType() == Material.AIR || !StackUtils.itemsMatch(incomingCache, template)) {
                continue;
            }
            blockMenu.markDirty();
            NetworkAsyncUtil.getInstance().ensureRootLocation( blockMenu.getLocation(), ()->
                BlockMenuUtil.pushItem(blockMenu, incomingCache, ADVANCED_GREEDY_BLOCK_AVAILABLE_SLOTS)
            );
            // Given we have found a match, it doesn't matter if the item moved or not, we will not bring it in
            return true;
        }
        // Run for matching greedy blocks
        for (SlimefunBlockData data : getGreedyBlocksMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
            final ItemStack template = blockMenu.getItemInSlot(NetworkGreedyBlock.TEMPLATE_SLOT);
            if (template == null || template.getType() == Material.AIR || !StackUtils.itemsMatch(incomingCache, template)) {
                continue;
            }
            blockMenu.markDirty();
            NetworkAsyncUtil.getInstance().ensureRootLocation(blockMenu.getLocation(), ()->
                BlockMenuUtil.pushItem(blockMenu, incomingCache, GREEDY_BLOCK_AVAILABLE_SLOTS[0])
            );
            // Given we have found a match, it doesn't matter if the item moved or not, we will not bring it in
            return true;
        }
        return false;
    }

    @Override
    protected void addItemStackCells(ItemStack incoming) {
        for (SlimefunBlockData data : getCellsMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
            blockMenu.markDirty();
            //multi-thread safe method of cell Menus
            pushCellAsync(blockMenu,incoming);
            if (incoming.getAmount() == 0) {
                return;
            }
        }
    }

    public void pushCellAsync(BlockMenu cellMenu,ItemStack item){
        NetworkAsyncUtil.getInstance().ensureRootLocation(cellMenu.getLocation(),()->{
            BlockMenuUtil.pushItem(cellMenu, item, CELL_AVAILABLE_SLOTS);
        });
    }
}
