package com.balugaq.netex.utils;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.managers.ExperimentalFeatureManager;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.network.stackcaches.ItemStackCache;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.N;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public class TransportUtil {
    public static int sendLimitedItemToRoot(NetworkRoot root, ItemStack item, int limit) {
        int itemAmount = item.getAmount();
        ItemStack sample = StackUtils.getLimitedRequest(item, limit);
        int expectedAmount = sample.getAmount();
        root.addItemStack(sample);
        int sendedAmount =expectedAmount - sample.getAmount();
        item.setAmount(itemAmount - sendedAmount);
        limit -= sendedAmount;
        return limit;
    }
    public static void fetchItemAndPush(NetworkRoot root, BlockMenu blockMenu, ItemRequest itemRequest, ToIntFunction<ItemStack> matchAmount, int limit, boolean breakAfterFirstMatch,boolean breakWhenNoMatch, int... slots){
        int freeSpace = 0;
        int maxStackSize=itemRequest.getMaxStackSize();
        if(maxStackSize<=0)return;
        limit=Math.min(limit,breakAfterFirstMatch?maxStackSize:(slots.length*maxStackSize));
        List<Integer> matchedSlots=new ArrayList<>(slots.length);
        for (int slot : slots) {
            int match=matchAmount.applyAsInt(blockMenu.getItemInSlot(slot));
            if (match > 0) {
                freeSpace += match;
                matchedSlots.add(slot);
                if(breakAfterFirstMatch) {
                    //first stop mode
                    break;
                }
            }else if(breakWhenNoMatch) {
                //if match Amount<=0 in this slot,then next slots probably no match
                break;
            }
            if(freeSpace >= limit){
                break;
            }
        }
        if (freeSpace <= 0||matchedSlots.isEmpty()) {
            return;
        }
        itemRequest.setAmount(Math.min(freeSpace, limit));

        final ItemStack retrieved = root.getItemStack(itemRequest);
        if (retrieved != null && retrieved.getType() != Material.AIR) {
            //BlockMenuUtil.pushItem(blockMenu, retrieved, slots);
            BlockMenuUtil.pushItemAlreadyMatched(blockMenu, retrieved, matchedSlots);
        }
    }
    public static void fetchItemAndPush(NetworkRoot root, BlockMenu blockMenu, ItemRequest itemRequest, ToIntFunction<ItemStack> matchAmount, int limit, boolean breakAfterFirstMatch, int... slots) {
        fetchItemAndPush(root, blockMenu, itemRequest, matchAmount, limit, breakAfterFirstMatch, false, slots);
    }
    public static void fetchItemAndPushSnapShot(NetworkRoot root, BlockMenuUtil.BlockMenuSnapShot snapShot, ItemRequest itemRequest, ToIntFunction<ItemStackCache> matchAmount, int limit, boolean breakAfterFirstMatch,int... slots) {
        //long start=System.nanoTime();
        int freeSpace = 0;
        int maxStackSize=itemRequest.getMaxStackSize();
        if(maxStackSize<=0)return;
        limit=Math.min(limit,breakAfterFirstMatch?maxStackSize:(slots.length*maxStackSize));
        List<Integer> matchedSlots=new ArrayList<>(slots.length);
        for (int slot : slots) {
            int match=matchAmount.applyAsInt(snapShot.getItemInSlot(slot));
            if (match > 0) {
                freeSpace += match;
                matchedSlots.add(slot);
                if(breakAfterFirstMatch) {
                    break;
                }
            }
            if(freeSpace >= limit){
                break;
            }
        }
        //long end=System.nanoTime();
        //ExperimentalFeatureManager.getInstance().sendTimings(start,end,()->"TransportUtils:match slots %s");
        if (freeSpace <= 0||matchedSlots.isEmpty()) {
            return;
        }
        itemRequest.setAmount(Math.min(freeSpace, limit));
        //long a=System.nanoTime();
        final ItemStack retrieved = root.getItemStack(itemRequest);
        //long d=System.nanoTime();
        //ExperimentalFeatureManager.getInstance().sendTimings(a,d,()->"TransportUtils:getItemStack %s");
        if (retrieved != null && retrieved.getType() != Material.AIR) {
            //BlockMenuUtil.pushItem(blockMenu, retrieved, slots);
            //a=System.nanoTime();
            snapShot.pushItemAlreadyMatched(retrieved, matchedSlots);
            //d=System.nanoTime();
            //ExperimentalFeatureManager.getInstance().sendTimings(a,d,()->"TransportUtils:pushItemMatched %s");
        }
    }
    public static int commonMatch(ItemStack itemStack,ItemRequest itemRequest){
        int maxStackSize=itemRequest.getMaxStackSize();
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
    }
    public static int commonMatchCache(@Nonnull ItemStackCache itemStack, ItemRequest itemRequest){
        int maxStackSize=itemRequest.getMaxStackSize();
        if (itemStack.getItemStack() == null || itemStack.getItemType() == Material.AIR) {
            return maxStackSize;
        } else {
            if (itemStack.getItemAmount() >= maxStackSize) {
                return 0;
            }
            if (StackUtils.itemsMatch(itemRequest, itemStack)) {
                return maxStackSize - itemStack.getItemAmount();
            }
            return 0;
        }
    }
    public static void outPower(@Nonnull Location targetLocation, @Nonnull NetworkRoot root, int rate) {
        final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(targetLocation);
        if (!(slimefunItem instanceof EnergyNetComponent component) || slimefunItem instanceof NetworkObject) {
            return;
        }
        if(!component.isChargeable()){
            return;
        }
        final int capacity = component.getCapacity();
        final int chargeInt = component.getCharge(targetLocation);
        final int space = capacity - chargeInt;
        if (space <= 0) {
            return;
        }
        final int possibleGeneration = Math.min(rate, space);

        final long power = root.getRootPower();
        if (power <= 0) {
            return;
        }
        final int gen = power < possibleGeneration ? (int) power : possibleGeneration;
        component.addCharge(targetLocation, gen);
        root.removeRootPower(gen);
    }
}
