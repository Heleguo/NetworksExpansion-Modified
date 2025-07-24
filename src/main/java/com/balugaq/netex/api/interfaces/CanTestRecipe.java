package com.balugaq.netex.api.interfaces;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface CanTestRecipe {
    static boolean testRecipe(@NotNull ItemStack[] input, @NotNull ItemStack[] recipe) {
        return false;
    }
}
