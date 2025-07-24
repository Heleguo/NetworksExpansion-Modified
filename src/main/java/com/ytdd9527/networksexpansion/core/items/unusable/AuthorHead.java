package com.ytdd9527.networksexpansion.core.items.unusable;

import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import com.ytdd9527.networksexpansion.implementation.ExpansionRecipes;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AuthorHead extends SpecialSlimefunItem {

    public AuthorHead(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item) {
        super(itemGroup, item, RecipeType.NULL, ExpansionRecipes.NULL);
    }

    public AuthorHead(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull ItemStack recipeOutput) {
        super(itemGroup, item, RecipeType.NULL, ExpansionRecipes.NULL, recipeOutput);
    }

    protected AuthorHead(@NotNull ItemGroup itemGroup, @NotNull ItemStack item, @NotNull String id) {
        super(itemGroup, item, id, RecipeType.NULL, ExpansionRecipes.NULL);
    }
}
