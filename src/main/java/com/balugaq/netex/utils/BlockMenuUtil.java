package com.balugaq.netex.utils;

import io.github.sefiraat.networks.NetworkAsyncUtil;
import io.github.sefiraat.networks.network.stackcaches.ItemStackCache;
import io.github.sefiraat.networks.utils.StackUtils;
import com.balugaq.netex.utils.algorithms.DynamicArray;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.matl114.matlib.nmsUtils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@UtilityClass
public class BlockMenuUtil {
    public static ItemStack pushItem(@Nonnull BlockMenu blockMenu, @Nullable ItemStack stack,int... slots) {
        return ItemUtils.pushItem(blockMenu.getInventory(), stack, slots);
    }
    public static void pushItemAlreadyMatched(@Nonnull BlockMenu inv, @Nullable ItemStack stack, List<Integer> slots)
    {
        int[] slotArray = new int[slots.size()];// slots.stream().mapToInt(Integer::intValue).toArray();
        for (int i=0; i< slotArray.length; ++i){
            slotArray[i] = slots.get(i);
        }
        if(NetworkAsyncUtil.getInstance().isUseAsync()){
            pushItem(inv, stack, slotArray);
        }else {
            ItemUtils.pushItemWithoutMatch(inv.getInventory(), stack, slotArray);
        }

    }

    //todo: this method should be optimized
    //todo: this method is already optimized
    @Nonnull
    public static List<ItemStack> pushItem(@Nonnull BlockMenu blockMenu, @Nonnull ItemStack[] items, int... slots) {
        if (items == null || items.length == 0) {
            throw new IllegalArgumentException("Cannot push null or empty array");
        }
        ItemUtils.pushItem(blockMenu.getInventory(), slots, items);
        return Arrays.stream(items).filter(item->item != null && !item.getType().isAir() && item.getAmount() > 0).toList();
    }

    @Nonnull
    public static List<ItemStack> pushItem(@Nonnull BlockMenu blockMenu, @Nullable List<ItemStack> items, int... slots) {
        if (items ==null || items.isEmpty()) {
            throw new IllegalArgumentException("Cannot push null or empty list");
        }

        return pushItem( blockMenu, items.toArray(ItemStack[]::new), slots);
    }






    @Deprecated
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

    public static void consumeItem(@NotNull final BlockMenu blockMenu, @Range(from = 0, to = 64) final int slot) {
        consumeItem(blockMenu, slot, 1);
    }

    public static void consumeItem(
            @NotNull final BlockMenu blockMenu,
            @Range(from = 0, to = 53) final int slot,
            final boolean replaceConsumables) {
        consumeItem(blockMenu, slot, 1, replaceConsumables);
    }

    public static void consumeItem(
            @NotNull final BlockMenu blockMenu,
            @Range(from = 0, to = 53) final int slot,
            @Range(from = 0, to = 64) final int amount) {
        consumeItem(blockMenu, slot, amount, false);
    }

    @SuppressWarnings("deprecation")
    public static void consumeItem(
            @NotNull final BlockMenu blockMenu,
            @Range(from = 0, to = 53) final int slot,
            @Range(from = 0, to = 64) final int amount,
            final boolean replaceConsumables) {
        if (amount == 0) {
            return;
        }

        final ItemStack item = blockMenu.getItemInSlot(slot);
        if (item != null && item.getType() != Material.AIR) {
            if (replaceConsumables
                    && item.getAmount() == 1
                    && StackUtils.itemsMatch(item, new ItemStack(item.getType()))) {
                switch (item.getType()) {
                    case WATER_BUCKET,
                            LAVA_BUCKET,
                            MILK_BUCKET,
                            COD_BUCKET,
                            SALMON_BUCKET,
                            PUFFERFISH_BUCKET,
                            TROPICAL_FISH_BUCKET,
                            AXOLOTL_BUCKET,
                            POWDER_SNOW_BUCKET,
                            TADPOLE_BUCKET -> item.setType(Material.BUCKET);
                    case POTION, SPLASH_POTION, LINGERING_POTION, HONEY_BOTTLE, DRAGON_BREATH -> item.setType(
                            Material.GLASS_BOTTLE);
                    case MUSHROOM_STEW, BEETROOT_SOUP, RABBIT_STEW, SUSPICIOUS_STEW -> item.setType(Material.BOWL);
                    default -> item.setAmount(0);
                }
            } else {
                if (item.getAmount() <= amount) {
                    item.setAmount(0);
                } else {
                    item.setAmount(item.getAmount() - amount);
                }
            }
        }
    }
}
