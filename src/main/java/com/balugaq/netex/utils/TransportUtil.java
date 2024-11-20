package com.balugaq.netex.utils;

import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.utils.StackUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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
    public static void fetchItemAndPush(NetworkRoot root, BlockMenu blockMenu, ItemRequest itemRequest, ToIntFunction<ItemStack> matchAmount, int limit, boolean breakAfterFirstMatch, int... slots) {
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
                    break;
                }
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
            BlockMenuUtil.pushItemWithMatchedSlots(blockMenu, retrieved, matchedSlots);
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
}
