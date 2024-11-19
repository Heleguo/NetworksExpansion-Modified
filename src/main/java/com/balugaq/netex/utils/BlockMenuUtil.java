package com.balugaq.netex.utils;

import io.github.sefiraat.networks.network.stackcaches.ItemStackCache;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import lombok.experimental.UtilityClass;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@UtilityClass
public class BlockMenuUtil {
    public static void pushItem(@Nonnull BlockMenu blockMenu, @Nullable ItemStack stack,int... slots) {
        pushItem(blockMenu,ItemStackCache.of(stack),false,slots);
    }
    @Nullable
    public static ItemStack pushItem(@Nonnull BlockMenu blockMenu, @Nonnull ItemStackCache item,boolean needReturn, int... slots) {
        if (item == null || item.getItemType() == Material.AIR) {
            throw new IllegalArgumentException("Cannot push null or AIR");
        }
        Material material = item.getItemType();
        int maxSize=material.getMaxStackSize();
        int leftAmount = item.getItemAmount();

        for (int slot : slots) {
            if (leftAmount <= 0) {
                break;
            }

            ItemStack existing = blockMenu.getItemInSlot(slot);

            if (existing == null || existing.getType() == Material.AIR) {
                int received = Math.min(leftAmount, maxSize);
                blockMenu.replaceExistingItem(slot, StackUtils.getAsQuantity(item, received));
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
        item.setItemAmount(leftAmount);

        if (needReturn&&leftAmount > 0) {
            return StackUtils.getAsQuantity(item, leftAmount);
        } else {
            return null;
        }
    }

    @Nonnull
    public static List<ItemStack> pushItem(@Nonnull BlockMenu blockMenu, @Nonnull ItemStack[] items, int... slots) {
        if (items == null || items.length == 0) {
            throw new IllegalArgumentException("Cannot push null or empty array");
        }

        List<ItemStack> listItems = Arrays.stream(items).filter(item->item != null && item.getType() != Material.AIR).toList();
//                new ArrayList<>();
//        for (ItemStack item : items) {
//            if (item != null && item.getType() != Material.AIR) {
//                listItems.add(item);
//            }
//        }
        return pushItem(blockMenu, listItems, slots);
    }

    @Nonnull
    public static List<ItemStack> pushItem(@Nonnull BlockMenu blockMenu, @Nonnull List<ItemStack> items, int... slots) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Cannot push null or empty list");
        }

        List<ItemStack> itemMap = new ArrayList<>(items.size());
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
                //ItemStack leftOver =
                ItemStackCache cache=ItemStackCache.of(item);
                pushItem(blockMenu, cache,false, slots);
                if (cache.getItemAmount() >0) {
                    itemMap.add(cache.getItemStack());
                }
            }
        }

        return itemMap;
    }

    public static boolean fits(@Nonnull BlockMenu blockMenu, @Nonnull ItemStack item, int... slots) {
        if (item == null || item.getType() == Material.AIR) {
            return true;
        }

        int incoming = item.getAmount();
        for (int slot : slots) {
            ItemStack stack = blockMenu.getItemInSlot(slot);

            if (stack == null || stack.getType() == Material.AIR) {
                incoming -= item.getMaxStackSize();
            } else if (stack.getMaxStackSize() > stack.getAmount() && StackUtils.itemsMatch(item, stack)) {
                incoming -= stack.getMaxStackSize() - stack.getAmount();
            }

            if (incoming <= 0) {
                return true;
            }
        }

        return false;
    }

    public static boolean fits(@Nonnull BlockMenu blockMenu, @Nonnull ItemStack[] items, int... slots) {
        if (items == null || items.length == 0) {
            return false;
        }

        List<ItemStack> listItems = new ArrayList<>();
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
                listItems.add(item.clone());
            }
        }

        return fits(blockMenu, listItems, slots);
    }

    public static boolean fits(@Nonnull BlockMenu blockMenu, @Nonnull List<ItemStack> items, int... slots) {
        if (items == null || items.isEmpty()) {
            return false;
        }

        List<ItemStack> cloneMenu = new ArrayList<>();
        for (int i = 0; i < 54; i++) {
            cloneMenu.add(null);
        }

        for (int slot : slots) {
            ItemStack stack = blockMenu.getItemInSlot(slot);
            if (stack != null && stack.getType() != Material.AIR) {
                cloneMenu.set(slot, stack.clone());
            } else {
                cloneMenu.set(slot, null);
            }
        }

        for (ItemStack rawItem : items) {
            ItemStack item = rawItem.clone();
            int leftAmount = item.getAmount();
            for (int slot : slots) {
                if (leftAmount <= 0) {
                    break;
                }

                ItemStack existing = cloneMenu.get(slot);

                if (existing == null || existing.getType() == Material.AIR) {
                    int received = Math.min(leftAmount, item.getMaxStackSize());
                    cloneMenu.set(slot, StackUtils.getAsQuantity(item, leftAmount));
                    leftAmount -= received;
                    item.setAmount(Math.max(0, leftAmount));
                } else {
                    int existingAmount = existing.getAmount();
                    if (existingAmount >= item.getMaxStackSize()) {
                        continue;
                    }

                    if (!StackUtils.itemsMatch(item, existing)) {
                        continue;
                    }

                    int received = Math.max(0, Math.min(item.getMaxStackSize() - existingAmount, leftAmount));
                    leftAmount -= received;
                    existing.setAmount(existingAmount + received);
                    item.setAmount(leftAmount);
                }
            }

            if (leftAmount > 0) {
                return false;
            }
        }

        return true;
    }
}
