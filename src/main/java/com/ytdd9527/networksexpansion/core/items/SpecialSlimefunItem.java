package com.ytdd9527.networksexpansion.core.items;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.factories.MachineRecipeFactory;
import com.balugaq.netex.api.interfaces.RecipeItem;
import com.balugaq.netex.utils.LocationUtil;
import io.github.sefiraat.networks.Networks;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * We may add something soon
 *
 * @author Final_ROOT
 * @since 2.0
 */
public abstract class SpecialSlimefunItem extends SlimefunItem {
    protected static final Map<UUID, Set<Location>> subscribedLocations = new HashMap<>();
    public SpecialSlimefunItem(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item, @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    public SpecialSlimefunItem(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item, @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe, @Nullable ItemStack recipeOutput) {
        super(itemGroup, item, recipeType, recipe, recipeOutput);
    }

    protected SpecialSlimefunItem(@Nonnull ItemGroup itemGroup, @Nonnull ItemStack item, @Nonnull String id, @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe) {
        super(itemGroup, item, id, recipeType, recipe);
    }

    @Override
    public void register(@Nonnull SlimefunAddon addon) {
        super.register(addon);
        if (this instanceof RecipeItem recipeItem) {
            int delay = recipeItem.getRegisterRecipeDelay();
            if (delay > 0) {
                this.getAddon().getJavaPlugin().getServer().getScheduler().runTaskLater((Plugin) addon, () -> {
                    (recipeItem).registerDefaultRecipes();
                    MachineRecipeFactory.getInstance().initAdvancedRecipeMap(this.getId());
                }, delay);
            } else {
                (recipeItem).registerDefaultRecipes();
                MachineRecipeFactory.getInstance().initAdvancedRecipeMap(this.getId());
            }
        }
    }

    @Nonnull
    public SpecialSlimefunItem registerThis() {
        this.register(Networks.getInstance());
        return this;
    }

    public static void subscribe(Player player, Location location) {
        UUID key = player.getUniqueId();
        if (!subscribedLocations.containsKey(key)) {
            subscribedLocations.put(key, new HashSet<>());
        }
        subscribedLocations.get(key).add(location);
    }

    public static void unsubscribe(Player player, Location location) {
        UUID key = player.getUniqueId();
        if (subscribedLocations.containsKey(key)) {
            subscribedLocations.get(key).remove(location);
        }
    }

    public static boolean hasSubscribed(Player player, Location location) {
        UUID key = player.getUniqueId();
        if (subscribedLocations.containsKey(key)) {
            return subscribedLocations.get(key).contains(location);
        }
        return false;
    }

    public void sendFeedback(Location location, FeedbackType type) {
        for (UUID uuid : subscribedLocations.keySet()) {
            if (subscribedLocations.get(uuid).contains(location)) {
                Player player = Bukkit.getServer().getPlayer(uuid);
                if (player != null) {
                    sendFeedback(player, location, type.getMessage());
                }
            }
        }
    }

    public void sendFeedback(Player player, Location location, String message) {
        player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.debug.status_view"), LocationUtil.humanizeBlock(location), message));
    }

}
