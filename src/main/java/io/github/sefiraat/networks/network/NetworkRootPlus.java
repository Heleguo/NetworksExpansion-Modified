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
import java.util.stream.IntStream;

public class NetworkRootPlus extends NetworkRoot {
    protected NetworkRootPlus(@NotNull Location location, @NotNull NodeType type, int maxNodes) {
        super(location, type, maxNodes);
    }
    @Override
    public ItemStack getItemStack(@Nonnull ItemRequest itemRequest) {
        if(ExperimentalFeatureManager.getInstance().isEnableRootGetItemStackAsync()){
            return getItemStackAsync(itemRequest);
        }else {
            return super.getItemStack(itemRequest);
        }
    }
    @Override
    public void addItemStack(ItemStack itemStack) {
        if(ExperimentalFeatureManager.getInstance().isEnableRootAddItemStackAsync()){
            addItemStackAsync(itemStack);
        }else {
            super.addItemStack(itemStack);
        }
    }
    private byte[][] cellMenuSlotLock= IntStream.range(0,63).mapToObj(i->new byte[0]).toArray(byte[][]::new);

    /**
     * getItemStack from network ,this method should be multi-thread safe
     * @param request
     * @return
     */
    @Nullable
    public ItemStack getItemStackAsync(@Nonnull ItemRequest request) {
        ItemStack stackToReturn = null;
        if (request.getAmount() <= 0) {
            return null;
        }

        // Barrels first
        //most of our modern network-based Factory are made of barrels and StorageUnits
        for (BarrelIdentity barrelIdentity : getOutputAbleBarrels()) {
            boolean infinity;
            final ItemStack fetched;
            if (barrelIdentity.getItemStack() == null || !StackUtils.itemsMatch(request, barrelIdentity)) {
                continue;
            }
            infinity = barrelIdentity instanceof InfinityBarrel;
            synchronized (barrelIdentity){
                fetched = barrelIdentity.requestItem(request);
            }
            if (fetched == null || fetched.getType() == Material.AIR || (infinity && fetched.getAmount() == 1)) {
                continue;
            }
            // Stack is null, so we can fill it here
            if (stackToReturn == null) {
                stackToReturn = fetched.clone();
                stackToReturn.setAmount(0);
            }

            final int preserveAmount = infinity ? fetched.getAmount() - 1 : fetched.getAmount();

            if (request.getAmount() <= preserveAmount) {
                stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                fetched.setAmount(fetched.getAmount() - request.getAmount());
                return stackToReturn;
            } else {
                stackToReturn.setAmount(stackToReturn.getAmount() + preserveAmount);
                request.receiveAmount(preserveAmount);
                fetched.setAmount(fetched.getAmount() - preserveAmount);
            }
        }

        // Units
        for (StorageUnitData cache : getOutputAbleCargoStorageUnitDatas().keySet()) {
            final ItemStack take;
            synchronized (cache){
                take= cache.requestItem(request);
            }
            if (take != null) {
                if (stackToReturn == null) {
                    stackToReturn = take.clone();
                } else {
                    stackToReturn.setAmount(stackToReturn.getAmount() + take.getAmount());
                }
                request.receiveAmount(stackToReturn.getAmount());

                if (request.getAmount() <= 0) {
                    return stackToReturn;
                }
            }
        }

        // Cells
        for (BlockMenu blockMenu : getCellMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                synchronized (cellMenuSlotLock[slot]){
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
                        stackToReturn.setAmount(0);
                    }

                    if (request.getAmount() <= itemStack.getAmount()) {
                        // We can't take more than this stack. Level to request amount, remove items and then return
                        stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                        itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                        return stackToReturn;
                    } else {
                        // We can take more than what is here, consume before trying to take more
                        stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                        request.receiveAmount(itemStack.getAmount());
                        itemStack.setAmount(0);
                    }
                }
            }
        }

        // Crafters
        for (BlockMenu blockMenu : getCrafterOutputs()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                synchronized (blockMenu){
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
                        stackToReturn.setAmount(0);
                    }

                    if (request.getAmount() <= itemStack.getAmount()) {
                        stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                        itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                        return stackToReturn;
                    } else {
                        stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                        request.receiveAmount(itemStack.getAmount());
                        itemStack.setAmount(0);
                    }
                }
            }
        }

        for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            synchronized (blockMenu){
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
                        stackToReturn.setAmount(0);
                    }

                    if (request.getAmount() <= itemStack.getAmount()) {
                        stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                        itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                        return stackToReturn;
                    } else {
                        stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                        request.receiveAmount(itemStack.getAmount());
                        itemStack.setAmount(0);
                    }
                }
            }
        }

        // Greedy Blocks
        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            synchronized (blockMenu){
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
                    stackToReturn.setAmount(0);
                }

                if (request.getAmount() <= itemStack.getAmount()) {
                    // We can't take more than this stack. Level to request amount, remove items and then return
                    stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                    itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                    return stackToReturn;
                } else {
                    // We can take more than what is here, consume before trying to take more
                    stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                    request.receiveAmount(itemStack.getAmount());
                    itemStack.setAmount(0);
                }
            }
        }

        if (stackToReturn == null || stackToReturn.getAmount() == 0) {
            return null;
        }

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
                synchronized (barrelIdentity) {
                    barrelIdentity.depositItemStack(incoming);
                }
                // All distributed, can escape
                if (incoming.getAmount() == 0) {
                    return;
                }
            }
        }

        for (StorageUnitData cache : getInputAbleCargoStorageUnitDatas().keySet()) {
            synchronized (cache) {
                cache.depositItemStack(incoming, true);
            }
            if (incoming.getAmount() == 0) {
                return;
            }
        }

        for (BlockMenu blockMenu : getCellMenus()) {
            blockMenu.markDirty();
            synchronized (blockMenu) {
                BlockMenuUtil.pushItem(blockMenu, incomingCache,false, CELL_AVAILABLE_SLOTS);
            }
            if (incoming.getAmount() == 0) {
                return;
            }
        }
    }
}
