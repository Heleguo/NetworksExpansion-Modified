package com.balugaq.netex.utils;

import io.github.sefiraat.networks.utils.StackUtils;
import java.util.HashMap;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
@UtilityClass
public class InventoryUtil {
    public static @NotNull HashMap<Integer, ItemStack> addItem(@NotNull Player player, ItemStack... toAdd) {
        HashMap<Integer, ItemStack> result = addItem(player.getInventory(), toAdd);
        player.updateInventory();
        return result;
    }

    public static @NotNull HashMap<Integer, ItemStack> addItem(@NotNull InventoryHolder holder, ItemStack... toAdds) {
        return addItem(holder.getInventory(), toAdds);
    }
    //fixme nms implements
    public static @NotNull HashMap<Integer, ItemStack> addItem(
            @NotNull Inventory inventory, ItemStack @NotNull ... toAdds) {
        HashMap<Integer, ItemStack> leftover = new HashMap<>();
        ItemStack[] storage = inventory.getStorageContents();
        if (storage == null) {
            // Fallback
            return inventory.addItem(toAdds);
        }

        for (ItemStack toAdd : toAdds) {
            if (toAdd == null || toAdd.getType() == Material.AIR) {
                continue;
            }

            int index = firstSimilar(storage, toAdd);
            if (index == -1) {
                index = firstEmpty(storage);
                if (index == -1) {
                    leftover.put(inventory.firstEmpty(), toAdd);
                    continue;
                }
                storage[index] = toAdd.clone();
                toAdd.setAmount(0);
                toAdd.setType(Material.AIR);
            } else {
                ItemStack exist = storage[index];
                int existing = exist.getAmount();
                int maxStack = exist.getMaxStackSize();
                if (existing == maxStack) {
                    index = firstEmpty(storage);
                    if (index == -1) {
                        leftover.put(inventory.firstEmpty(), toAdd);
                        continue;
                    }
                    storage[index] = toAdd.clone();
                    toAdd.setAmount(0);
                    toAdd.setType(Material.AIR);
                } else if (existing < maxStack) {
                    int handled = Math.min(maxStack - existing, toAdd.getAmount());
                    exist.setAmount(existing + handled);
                    toAdd.setAmount(toAdd.getAmount() - handled);
                    if (toAdd.getAmount() != 0) {
                        leftover.put(index, toAdd);
                    }
                } else {
                    leftover.put(index, toAdd);
                }
            }
        }

        inventory.setStorageContents(storage);
        return leftover;
    }

    public static int firstSimilar(ItemStack @NotNull [] storage, ItemStack item) {
        return firstSimilar(storage, item, true);
    }

    public static int firstSimilar(ItemStack @NotNull [] storage, ItemStack item, boolean withoutAmount) {
        for (int i = 0; i < storage.length; i++) {
            if (StackUtils.itemsMatch(storage[i], item, true, !withoutAmount)) {
                return i;
            }
        }

        return -1;
    }

    public static int firstEmpty(ItemStack @NotNull [] storage) {
        for (int i = 0; i < storage.length; i++) {
            if (storage[i] == null || storage[i].getType() == Material.AIR) {
                return i;
            }
        }

        return -1;
    }
}
