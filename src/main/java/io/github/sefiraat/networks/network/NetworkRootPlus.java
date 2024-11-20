package io.github.sefiraat.networks.network;

import com.balugaq.netex.api.data.StorageUnitData;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.ytdd9527.networksexpansion.implementation.machines.networks.advanced.AdvancedGreedyBlock;
import io.github.sefiraat.networks.managers.ExperimentalFeatureManager;
import io.github.sefiraat.networks.network.barrel.InfinityBarrel;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.network.stackcaches.ItemStackCache;
import io.github.sefiraat.networks.slimefun.network.NetworkGreedyBlock;
import io.github.sefiraat.networks.utils.StackUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.stream.IntStream;

public class NetworkRootPlus extends NetworkRoot {
    protected NetworkRootPlus(@NotNull Location location, @NotNull NodeType type, int maxNodes) {
        super(location, type, maxNodes);
    }
    @Override
    public ItemStack getItemStack(@Nonnull ItemRequest itemRequest) {
        return getItemStackRewrite(itemRequest);
//        if(enableGetAsync&& ExperimentalFeatureManager.getInstance().isEnableRootGetItemStackAsync()){
//            return getItemStackAsync(itemRequest);
//        }else {
//            return super.getItemStack(itemRequest);
//        }
    }
    @Override
    public void addItemStack(ItemStack itemStack) {
        if(enableAddAsync&&ExperimentalFeatureManager.getInstance().isEnableRootAddItemStackAsync()){
            addItemStackAsync(itemStack);
        }else {
            super.addItemStack(itemStack);
        }
    }
    private byte[][] cellMenuSlotLock= IntStream.range(0,63).mapToObj(i->new byte[0]).toArray(byte[][]::new);
    private static final boolean enableGetAsync=false;
    private static final boolean enableAddAsync=true;
    private static Constructor<NetworkRoot> constructor;
    public ItemStack getItemStackRewrite(@Nonnull ItemRequest request) {

        if (request.getAmount() <= 0) {
            return null;
        }
        ItemStack stackToReturn = null;
        int totalAmount=0;
        requestItem:{
            // Barrels first
            for (BarrelIdentity barrelIdentity : getOutputAbleBarrels()) {
                if (barrelIdentity.getItemStack() == null || !StackUtils.itemsMatch(request, barrelIdentity)) {
                    continue;
                }

                boolean infinity = barrelIdentity instanceof InfinityBarrel;
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

            // Units
            for (StorageUnitData cache : getOutputAbleCargoStorageUnitDatas().keySet()) {
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
                for (int slot : slots) {
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

            // Crafters
            for (BlockMenu blockMenu : getCrafterOutputs()) {
                int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
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
            }

            for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {
                int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
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
            }

            // Greedy Blocks
            for (BlockMenu blockMenu : getGreedyBlockMenus()) {
                int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
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
    public void addItemStackAsync(@Nonnull ItemStack incoming) {
        ItemStackCache incomingCache = ItemStackCache.of(incoming);
        for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {

            final ItemStack template = blockMenu.getItemInSlot(AdvancedGreedyBlock.TEMPLATE_SLOT);

            if (template == null || template.getType() == Material.AIR || !StackUtils.itemsMatch(incomingCache, template)) {
                continue;
            }
            blockMenu.markDirty();
            synchronized (blockMenu){
                BlockMenuUtil.pushItem(blockMenu, incomingCache,false, ADVANCED_GREEDY_BLOCK_AVAILABLE_SLOTS);
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
            synchronized (blockMenu){
                BlockMenuUtil.pushItem(blockMenu, incomingCache,false, GREEDY_BLOCK_AVAILABLE_SLOTS[0]);
            }
            // Given we have found a match, it doesn't matter if the item moved or not, we will not bring it in
            return;
        }


        // Run for matching barrels
        for (BarrelIdentity barrelIdentity : getInputAbleBarrels()) {
            if (StackUtils.itemsMatch(barrelIdentity, incomingCache)) {
                // barrel Identity should realize multi-thread safe somehow
                barrelIdentity.depositItemStack(incoming);
                // All distributed, can escape
                if (incoming.getAmount() == 0) {
                    return;
                }
            }
        }

        for (StorageUnitData cache : getInputAbleCargoStorageUnitDatas().keySet()) {
            //storageUnitData should realize multi-thread safe somehow
            cache.depositItemStack(incoming, true);
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
        int leftAmount = item.getItemAmount();

        for (int slot : CELL_AVAILABLE_SLOTS) {
            if (leftAmount <= 0) {
                break;
            }
            synchronized (cellMenuSlotLock[slot]) {
                ItemStack existing = cellMenu.getItemInSlot(slot);

                if (existing == null || existing.getType() == Material.AIR) {
                    int received = Math.min(leftAmount, maxSize);

                    cellMenu.replaceExistingItem(slot,item.getItemStack(),false);
                    existing=cellMenu.getItemInSlot(slot);
                    existing.setAmount(received);
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
