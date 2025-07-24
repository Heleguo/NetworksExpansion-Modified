package io.github.sefiraat.networks.network.stackcaches;

import lombok.Getter;
import me.matl114.matlib.nmsUtils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class BlueprintInstance extends ItemStackCache {

    @Getter
    private final ItemStack[] recipeItems;

    @Nullable private Recipe recipe = null;

    public BlueprintInstance(@Nonnull ItemStack[] recipeItems, @Nonnull ItemStack expectedOutput) {
        super(ItemUtils.cleanStack(expectedOutput));
        this.recipeItems = recipeItems;
    }

    @Nullable public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(@Nullable Recipe recipe) {
        this.recipe = recipe;
    }

    public void generateVanillaRecipe(@NotNull World world) {
        if (this.recipe == null) {
            this.recipe = Bukkit.getCraftingRecipe(this.recipeItems, world);
        }
    }
}
