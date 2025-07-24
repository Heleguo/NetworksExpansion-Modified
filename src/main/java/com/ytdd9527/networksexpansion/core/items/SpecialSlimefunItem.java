package com.ytdd9527.networksexpansion.core.items;

import com.balugaq.netex.api.factories.MachineRecipeFactory;
import com.balugaq.netex.api.interfaces.FeedbackSendable;
import com.balugaq.netex.api.interfaces.RecipeItem;
import io.github.sefiraat.networks.Networks;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * We may add something soon
 *
 * @author Final_ROOT
 * @author baluagq
 * @since 2.0
 */
public abstract class SpecialSlimefunItem extends SlimefunItem implements FeedbackSendable {
    public SpecialSlimefunItem(
            @NotNull ItemGroup itemGroup,
            @NotNull SlimefunItemStack item,
            @NotNull RecipeType recipeType,
            @NotNull ItemStack @NotNull [] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    public SpecialSlimefunItem(
            @NotNull ItemGroup itemGroup,
            @NotNull SlimefunItemStack item,
            @NotNull RecipeType recipeType,
            @NotNull ItemStack @NotNull [] recipe,
            @Nullable ItemStack recipeOutput) {
        super(itemGroup, item, recipeType, recipe, recipeOutput);
    }

    protected SpecialSlimefunItem(
            @NotNull ItemGroup itemGroup,
            @NotNull ItemStack item,
            @NotNull String id,
            @NotNull RecipeType recipeType,
            @NotNull ItemStack @NotNull [] recipe) {
        super(itemGroup, item, id, recipeType, recipe);
    }

    @Override
    public void register(@NotNull SlimefunAddon addon) {
        super.register(addon);
        if (this instanceof RecipeItem recipeItem) {
            int delay = recipeItem.getRegisterRecipeDelay();
            if (delay > 0) {
                this.getAddon()
                        .getJavaPlugin()
                        .getServer()
                        .getScheduler()
                        .runTaskLater(
                                (Plugin) addon,
                                () -> {
                                    (recipeItem).registerDefaultRecipes();
                                    MachineRecipeFactory.getInstance().initAdvancedRecipeMap(this.getId());
                                },
                                delay);
            } else {
                (recipeItem).registerDefaultRecipes();
                MachineRecipeFactory.getInstance().initAdvancedRecipeMap(this.getId());
            }
        }
    }

    @NotNull public SpecialSlimefunItem registerThis() {
        this.register(Networks.getInstance());
        return this;
    }
}
