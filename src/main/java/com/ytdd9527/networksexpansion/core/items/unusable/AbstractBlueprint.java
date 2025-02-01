package com.ytdd9527.networksexpansion.core.items.unusable;

import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.stackcaches.BlueprintInstance;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.Theme;
import io.github.sefiraat.networks.utils.datatypes.DataTypeMethods;
import io.github.sefiraat.networks.utils.datatypes.PersistentCraftingBlueprintType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.DistinctiveItem;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBlueprint extends UnusableSlimefunItem implements DistinctiveItem {

    public AbstractBlueprint(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @ParametersAreNonnullByDefault
    public static void setBlueprint(ItemStack blueprint, ItemStack[] recipe, ItemStack output) {
        final ItemMeta itemMeta = blueprint.getItemMeta();
        DataTypeMethods.setCustom(itemMeta, Keys.BLUEPRINT_INSTANCE, PersistentCraftingBlueprintType.TYPE, new BlueprintInstance(recipe, output));
        List<String> lore = new ArrayList<>();

        lore.add(Networks.getLocalizationService().getString("messages.blueprint.title"));

        for (ItemStack item : recipe) {
            if (item == null) {
                lore.add(Networks.getLocalizationService().getString("messages.blueprint.empty"));
                continue;
            }
            lore.add(Theme.PASSIVE + ChatColor.stripColor(ItemStackHelper.getDisplayName(item)));
        }

        lore.add("");
        lore.add(Networks.getLocalizationService().getString("messages.blueprint.output"));

        lore.add(Theme.PASSIVE + ChatColor.stripColor(ItemStackHelper.getDisplayName(output)));

        itemMeta.setLore(lore);

        blueprint.setItemMeta(itemMeta);
    }

    /*
     * Fix https://github.com/Sefiraat/Networks/issues/201
     */
    @Override
    public boolean canStack(ItemMeta meta1, ItemMeta meta2) {
        return meta1.getPersistentDataContainer().equals(meta2.getPersistentDataContainer());
    }
}
