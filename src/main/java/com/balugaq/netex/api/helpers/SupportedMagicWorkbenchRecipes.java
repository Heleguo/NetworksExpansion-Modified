package com.balugaq.netex.api.helpers;

import com.balugaq.netex.api.interfaces.CanTestRecipe;
import com.balugaq.netex.api.interfaces.HasRecipes;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.multiblocks.MultiBlockMachine;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.implementation.items.backpacks.SlimefunBackpack;
import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public final class SupportedMagicWorkbenchRecipes implements HasRecipes, CanTestRecipe {

    private static final Map<ItemStack[], ItemStack> RECIPES = new HashMap<>();

    static {
        String id = SlimefunItems.MAGIC_WORKBENCH.getItemId();
        SlimefunItem recipeTypeItem = SlimefunItem.getById(id);
        if (recipeTypeItem != null && recipeTypeItem instanceof MultiBlockMachine mb) {
            boolean isInput = true;
            ItemStack[] input = null;
            ItemStack[] output = null;
            for (ItemStack[] recipe : mb.getRecipes()) {
                if (isInput) {
                    input = recipe;
                } else {
                    output = recipe;
                    if (input.length != 9) {
                        ItemStack[] newInput = new ItemStack[9];
                        for (int i = 0; i < 9; i++) {
                            if (i < input.length) {
                                newInput[i] = input[i];
                            } else {
                                newInput[i] = null;
                            }
                        }
                        input = newInput;
                    }
                    RECIPES.put(input, output[0]);
                }
                isInput = !isInput;
            }
        }
        for (SlimefunItem item : Slimefun.getRegistry().getEnabledSlimefunItems()) {
            RecipeType recipeType = item.getRecipeType();
            if ((recipeType == RecipeType.MAGIC_WORKBENCH) && allowedRecipe(item)) {
                ItemStack[] itemStacks = new ItemStack[9];
                int i = 0;
                for (ItemStack itemStack : item.getRecipe()) {
                    if (itemStack == null) {
                        itemStacks[i] = null;
                    } else {
                        itemStacks[i] = ItemStackUtil.getCleanItem(itemStack);
                    }
                    if (++i >= 9) {
                        break;
                    }
                }
                SupportedMagicWorkbenchRecipes.addRecipe(itemStacks, item.getRecipeOutput());
            }
        }
    }

    public static Map<ItemStack[], ItemStack> getRecipes() {
        return RECIPES;
    }

    public static void addRecipe(@Nonnull ItemStack[] input, @Nonnull ItemStack output) {
        RECIPES.put(input, output);
    }

    public static boolean testRecipe(@Nonnull ItemStack[] input, @Nonnull ItemStack[] recipe) {
        for (int test = 0; test < recipe.length; test++) {
            if (!StackUtils.itemsMatch(input[test], recipe[test])) {
                return false;
            }
        }
        return true;
    }

    public static boolean allowedRecipe(@Nonnull SlimefunItem item) {
        return !(item instanceof SlimefunBackpack);
    }

}
