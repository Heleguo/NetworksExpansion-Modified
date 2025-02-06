package com.balugaq.netex.api.helpers;

import com.balugaq.netex.api.enums.MinecraftVersion;
import com.google.common.base.Preconditions;
import io.github.sefiraat.networks.Networks;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import javax.annotation.Nonnull;

@SuppressWarnings("deprecation")
public final class ItemStackHelper {
    private static final MinecraftVersion MC_VERSION = Networks.getInstance().getMCVersion();
    private static final boolean IS_1_20_5 = MC_VERSION.isAtLeast(MinecraftVersion.MC1_20_5);
    private static final boolean IS_1_21 = MC_VERSION.isAtLeast(MinecraftVersion.MC1_21);
    private static final String NULL_ITEMSTACK_MESSAGE = "物品不能为空";

    private ItemStackHelper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
//
//    @Nonnull
//    public static String getDisplayName(@Nonnull ItemStack item) {
//        Preconditions.checkArgument(item != null, NULL_ITEMSTACK_MESSAGE);
//        return item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : getName(item);
//    }

    @Nonnull
    public static String getName(@Nonnull ItemStack item) {
        Preconditions.checkArgument(item != null, NULL_ITEMSTACK_MESSAGE);
        if (MinecraftTag.POTION_WITH_TIPPED_ARROW.isTagged(item)) {
            String potion = "unknown";
            try {
                if (IS_1_20_5) {
                    PotionType type = ((PotionMeta) item.getItemMeta()).getBasePotionType();
                    if (type != null) {
                        potion = type.name().toLowerCase();
                    }
                } else {
                    PotionData data = ((PotionMeta) item.getItemMeta()).getBasePotionData();
                    if (data != null) {
                        potion = data.getType().name().toLowerCase();
                    }
                }
            } catch (Throwable ignored) {
            }
            String material = MaterialHelper.getKey(item.getType());
            return LanguageHelper.getLangOrKey(material + ".effect." + potion);
        } else {
            return item.getType() != Material.PLAYER_HEAD && item.getType() != Material.PLAYER_WALL_HEAD ? MaterialHelper.getName(item.getType()) : getPlayerSkullName(item);
        }
    }

    @Nonnull
    private static String getPlayerSkullName(@Nonnull ItemStack skull) {
        Preconditions.checkArgument(skull != null, "物品不能为空");
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        return meta != null && meta.hasOwner() ? String.format(LanguageHelper.getLangOrKey("block_minecraft_player_head_named"), meta.getOwningPlayer().getName()) : LanguageHelper.getLangOrKey("block_minecraft_player_head");
    }
}
