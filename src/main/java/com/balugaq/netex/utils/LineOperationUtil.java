package com.balugaq.netex.utils;

import com.balugaq.netex.api.enums.TransportMode;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.managers.ExperimentalFeatureManager;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import lombok.experimental.UtilityClass;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import static com.balugaq.netex.utils.TransportUtil.fetchItemAndPush;
import static com.balugaq.netex.utils.TransportUtil.sendLimitedItemToRoot;

@UtilityClass
public class LineOperationUtil {
    public static void doOperation(Location startLocation, BlockFace direction, int limit, Consumer<BlockMenu> consumer) {
        doOperation(startLocation, direction, limit, false, true, consumer);
    }

    public static void doOperation(Location startLocation, BlockFace direction, int limit, boolean skipNoMenu, Consumer<BlockMenu> consumer) {
        doOperation(startLocation, direction, limit, skipNoMenu, true, consumer);
    }

    public static void doOperation(Location startLocation, BlockFace direction, int limit, boolean skipNoMenu, boolean optimizeExperience, Consumer<BlockMenu> consumer) {
        doOperation(startLocation, direction, limit, skipNoMenu, optimizeExperience, false, consumer);
    }
    public static void doOperation(Location startLocation, BlockFace direction, int limit, boolean skipNoMenu, boolean optimizeExperience,boolean parallel, Consumer<BlockMenu> consumer) {
        if(parallel&&ExperimentalFeatureManager.getInstance().isEnableParallelLineOperation()){
            doOperationParallel(startLocation, direction, limit, skipNoMenu, optimizeExperience, consumer);
        }else {
            doOperationOrdinal(startLocation, direction, limit, skipNoMenu, optimizeExperience, consumer);
        }
    }
    public static void doOperation(Location startLocation, BlockFace direction, int limit, boolean skipNoMenu,Predicate<Location> stopLocation, Consumer<Location> consumer) {
        doOperation(startLocation, direction, limit, skipNoMenu, true,stopLocation, consumer);
    }
    public static void doOperation(Location startLocation, BlockFace direction, int limit, boolean skipNoMenu, boolean optimizeExperience, Predicate<Location> stopLocation, Consumer<Location> consumer) {
        doOperation(startLocation, direction, limit, skipNoMenu, optimizeExperience, false,stopLocation, consumer);
    }
    public static void doOperation(Location startLocation, BlockFace direction, int limit,boolean skipNotPass, boolean optimizeExperience,boolean parallel,Predicate<Location> stopLocation, Consumer<Location> consumer) {
//        if(false&&parallel&&ExperimentalFeatureManager.getInstance().isEnableParallelLineOperation()){
//            //doOperationParallel(startLocation, direction, limit, skipNoMenu, optimizeExperience, consumer);
//        }else {
            doOperationOrdinal(startLocation, direction, limit,skipNotPass,optimizeExperience,stopLocation, consumer);
        //}
    }
    //menu operation
    public static void doOperationOrdinal(Location startLocation,BlockFace direction,int limit,boolean skipNoMenu,boolean optimizeExperience, Consumer<BlockMenu> consumer){
        Location location = startLocation.clone();
        int finalLimit = limit;
        if (optimizeExperience) {
            finalLimit += 1;
        }
        Vector directionVec = direction.getDirection();
        for (int i = 0; i < finalLimit; i++) {
            location.add(directionVec);
            final BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
            if (blockMenu == null) {
                if (skipNoMenu) {
                    continue;
                } else {
                    return;
                }
            }
            consumer.accept(blockMenu);
        }
    }
    //just location
    public static void doOperationOrdinal(Location startLocation,BlockFace direction,int limit,boolean skipNotPass,boolean optimizeExperience,Predicate<Location> stopLocation, Consumer<Location> consumer){
        Location location = startLocation.clone();
        int finalLimit = limit;
        if (optimizeExperience) {
            finalLimit += 1;
        }
        Vector directionVec = direction.getDirection();
        for (int i = 0; i < finalLimit; i++) {
            location.add(directionVec);
            //final BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
            if (!stopLocation.test(location)) {
                if (skipNotPass) {
                    continue;
                } else {
                    return;
                }
            }
            consumer.accept(location);
        }
    }
    public static void doOperationParallel(Location startLocation,BlockFace direction,int limit,boolean skipNoMenu,boolean optimizeExperience, Consumer<BlockMenu> consumer) {
        Location location = startLocation.clone();
        int finalLimit = limit;
        if (optimizeExperience) {
            finalLimit += 1;
        }
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Vector directionVec = direction.getDirection();
        for (int i = 0; i < finalLimit; i++) {
            location.add(directionVec);
            final BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
            if (blockMenu == null) {
                if (skipNoMenu) {
                    continue;
                }else {
                    break;
                }
            }
            futures.add(CompletableFuture.runAsync(() -> {
                try{
                    consumer.accept(blockMenu);
                }catch (Throwable e) {
                    e.printStackTrace();
                }
            }));
        }
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
    }

    public static void grabItem(
            @Nonnull NetworkRoot root,
            @Nonnull BlockMenu blockMenu,
            @Nonnull TransportMode transportMode,
            int limitQuantity
    ) {
        final int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(blockMenu, ItemTransportFlow.WITHDRAW, null);

        int limit = limitQuantity;
        switch (transportMode) {
            case NONE, NONNULL_ONLY -> {
                /*
                 * Grab all the items.
                 */
                for (int slot : slots) {
                    final ItemStack item = blockMenu.getItemInSlot(slot);
                    if (item != null && item.getType() != Material.AIR) {
                        //final int exceptedReceive = Math.min(item.getAmount(), limit);
                        limit=sendLimitedItemToRoot(root, item, limit);
                        if (limit <= 0) {
                            break;
                        }
                    }
                }
            }
            case NULL_ONLY -> {
                /*
                 * Nothing to do.
                 */
            }
            case FIRST_ONLY -> {
                /*
                 * Grab the first item only.
                 */
                if (slots.length > 0) {
                    final ItemStack item = blockMenu.getItemInSlot(slots[0]);
                    if (item != null && item.getType() != Material.AIR) {
                        limit=sendLimitedItemToRoot(root, item, limit);
                        if (limit <= 0) {
                            break;
                        }
                    }
                }
            }
            case LAST_ONLY -> {
                /*
                 * Grab the last item only.
                 */
                if (slots.length > 0) {
                    final ItemStack item = blockMenu.getItemInSlot(slots[slots.length - 1]);
                    if (item != null && item.getType() != Material.AIR) {
                        limit=sendLimitedItemToRoot(root, item, limit);
                        if (limit <= 0) {
                            break;
                        }
                    }
                }
            }
            case FIRST_STOP -> {
                /*
                 * Grab the first non-null item only.
                 */
                for (int slot : slots) {
                    final ItemStack item = blockMenu.getItemInSlot(slot);
                    if (item != null && item.getType() != Material.AIR) {
                        limit=sendLimitedItemToRoot(root, item, limit);
                        break;
                    }
                }
            }
            case LAZY -> {
                /*
                 * When it's first item is non-null, we will grab all the items.
                 */
                if (slots.length > 0) {
                    final ItemStack delta = blockMenu.getItemInSlot(slots[0]);
                    if (delta != null && delta.getType() != Material.AIR) {
                        for (int slot : slots) {
                            ItemStack item = blockMenu.getItemInSlot(slot);
                            if (item != null && item.getType() != Material.AIR) {
                                limit=sendLimitedItemToRoot(root, item, limit);
                                if (limit <= 0) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return;
    }

    public static void pushItem(
            @Nonnull NetworkRoot root,
            @Nonnull BlockMenu blockMenu,
            @Nonnull List<ItemStack> clones,
            @Nonnull TransportMode transportMode,
            int limitQuantity
    ) {
        for (ItemStack clone : clones) {
            pushItem(root, blockMenu, clone, transportMode, limitQuantity);
        }
    }


    public static void pushItem(
            @Nonnull NetworkRoot root,
            @Nonnull BlockMenu blockMenu,
            @Nonnull ItemStack clone,
            @Nonnull TransportMode transportMode,
            int limitQuantity
    ) {
        final int maxStackSize=clone.getMaxStackSize();
        final ItemRequest itemRequest = ItemRequest.of(clone,maxStackSize);
        final int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(blockMenu, ItemTransportFlow.INSERT, clone);
        switch (transportMode) {
            case NONE -> {
                fetchItemAndPush(root,blockMenu,itemRequest,(itemStack)->{
                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                        return maxStackSize;
                    } else {
                        if (itemStack.getAmount() >= maxStackSize) {
                            return 0;
                        }
                        if (StackUtils.itemsMatch(itemRequest, itemStack)) {
                            return maxStackSize - itemStack.getAmount();
                        }
                        return 0;
                    }
                },limitQuantity,false,slots);
            }

            case NULL_ONLY -> {
               // int free = limitQuantity;
                fetchItemAndPush(root,blockMenu,itemRequest,(itemStack)->
                    itemStack==null?maxStackSize:0,limitQuantity,false,slots
                );
            }

            case NONNULL_ONLY -> {
                fetchItemAndPush(root,blockMenu,itemRequest,(itemStack)->{
                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                        return 0;
                    } else {
                        if (itemStack.getAmount() >= maxStackSize) {
                            return 0;
                        }
                        if (StackUtils.itemsMatch(itemRequest, itemStack)) {
                            return maxStackSize - itemStack.getAmount();
                        }
                        return 0;
                    }
                },limitQuantity,false,slots);
            }
            case FIRST_ONLY -> {
                //int free = limitQuantity;
                if (slots.length == 0) {
                    break;
                }
                final int slot = slots[0];
                fetchItemAndPush(root,blockMenu,itemRequest,(itemStack)->{
                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                        return maxStackSize;
                    } else {
                        if (itemStack.getAmount() >= maxStackSize) {
                            return 0;
                        }
                        if (StackUtils.itemsMatch(itemRequest, itemStack)) {
                            return maxStackSize - itemStack.getAmount();
                        }
                        return 0;
                    }
                },limitQuantity,true,slot);
            }
            case LAST_ONLY -> {
                if (slots.length == 0) {
                    break;
                }
                final int slot = slots[slots.length-1];
                fetchItemAndPush(root,blockMenu,itemRequest,(itemStack)->{
                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                        return maxStackSize;
                    } else {
                        if (itemStack.getAmount() >= maxStackSize) {
                            return 0;
                        }
                        if (StackUtils.itemsMatch(itemRequest, itemStack)) {
                            return maxStackSize - itemStack.getAmount();
                        }
                        return 0;
                    }
                },limitQuantity,true,slot);
            }
            case FIRST_STOP -> {
                fetchItemAndPush(root,blockMenu,itemRequest,(itemStack)->{
                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                        return maxStackSize;
                    } else {
                        if (itemStack.getAmount() >= maxStackSize) {
                            return 0;
                        }
                        if (StackUtils.itemsMatch(itemRequest, itemStack)) {
                            return maxStackSize - itemStack.getAmount();
                        }
                        return 0;
                    }
                },limitQuantity,true,slots);
            }
            case LAZY -> {
                if (slots.length > 0) {
                    final ItemStack delta = blockMenu.getItemInSlot(slots[0]);
                    if (delta == null || delta.getType() == Material.AIR) {
                        fetchItemAndPush(root,blockMenu,itemRequest,(itemStack)->{
                            if (itemStack == null || itemStack.getType() == Material.AIR) {
                                return maxStackSize;
                            } else {
                                if (itemStack.getAmount() >= maxStackSize) {
                                    return 0;
                                }
                                if (StackUtils.itemsMatch(itemRequest, itemStack)) {
                                    return maxStackSize - itemStack.getAmount();
                                }
                                return 0;
                            }
                        },limitQuantity,false,slots);
                    }
                }
            }
        }
    }


}
