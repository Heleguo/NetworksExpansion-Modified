package com.ytdd9527.networksexpansion.core.items.machines;

import com.balugaq.netex.utils.algorithms.MenuWithData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractAdvancedAutoCrafter extends AbstractAutoCrafter implements MenuWithData {
    public AbstractAdvancedAutoCrafter(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int chargePerCraft,
            boolean withholding
    ) {
        super(itemGroup, item, recipeType, recipe, chargePerCraft,withholding);
        this.craftlimit=64;
    }

    public abstract boolean getRecipeTester(ItemStack[] inputs, ItemStack[] recipe);

    public boolean canTestVanillaRecipe() {
        return false;
    }
}
