package io.github.sefiraat.networks.utils;

import io.github.mooy1.infinityexpansion.InfinityExpansion;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.managers.SupportedPluginManager;
import lombok.Data;
import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;

import javax.annotation.Nonnull;

@Data
@UtilityClass
public class Keys {
    public static final String NETWORKS_ID = "networks";
    public static final String NETWORKS_CHANGED_ID = "networks-changed";
    public static final NamespacedKey ON_COOLDOWN = newKey("cooldown");
    public static final NamespacedKey ON_COOLDOWN2 = customNewKey(NETWORKS_ID, "cooldown");
    public static final NamespacedKey ON_COOLDOWN3 = customNewKey(NETWORKS_CHANGED_ID, "cooldown");
    public static final NamespacedKey CARD_INSTANCE = newKey("ntw_card");
    public static final NamespacedKey CARD_INSTANCE2 = customNewKey(NETWORKS_ID, "ntw_card");
    public static final NamespacedKey CARD_INSTANCE3 = customNewKey(NETWORKS_CHANGED_ID, "ntw_card");
    public static final NamespacedKey QUANTUM_STORAGE_INSTANCE = newKey("quantum_storage");
    public static final NamespacedKey QUANTUM_STORAGE_INSTANCE2 = customNewKey(NETWORKS_ID, "quantum_storage");
    public static final NamespacedKey QUANTUM_STORAGE_INSTANCE3 = customNewKey(NETWORKS_CHANGED_ID, "quantum_storage");
    public static final NamespacedKey BLUEPRINT_INSTANCE = newKey("ntw_blueprint");
    public static final NamespacedKey BLUEPRINT_INSTANCE2 = customNewKey(NETWORKS_ID, "ntw_blueprint");
    public static final NamespacedKey BLUEPRINT_INSTANCE3 = customNewKey(NETWORKS_CHANGED_ID, "ntw_blueprint");
    public static final NamespacedKey FACE = newKey("face");
    public static final NamespacedKey FACE2 = customNewKey(NETWORKS_ID, "face");
    public static final NamespacedKey FACE3 = customNewKey(NETWORKS_CHANGED_ID, "face");
    public static final NamespacedKey ITEM = newKey("item");
    public static final NamespacedKey ITEM2 = customNewKey(NETWORKS_ID, "item");
    public static final NamespacedKey ITEM3 = customNewKey(NETWORKS_CHANGED_ID, "item");
    public static final NamespacedKey TARGET_LOCATION = Keys.newKey("target-location");
    public static final NamespacedKey TARGET_LOCATION2 = customNewKey(NETWORKS_ID, "target-location");
    public static final NamespacedKey TARGET_LOCATION3 = customNewKey(NETWORKS_CHANGED_ID, "target-location");

    public static final NamespacedKey AMOUNT = newKey("amount");
    public static final NamespacedKey TRANSFER_MODE = newKey("transfer_mode");
    public static final NamespacedKey STORAGE_UNIT_UPGRADE_TABLE = newKey("storage_upgrade_table");
    public static final NamespacedKey STORAGE_UNIT_UPGRADE_TABLE_MODEL = newKey("storage_upgrade_table_model");
    public static final NamespacedKey NETWORKSKEY = newKey("networkskey");
    public static final NamespacedKey ITEM_MOVER_ITEM = newKey("item_mover_item");
    public static final NamespacedKey ITEM_MOVER_AMOUNT = newKey("item_mover_amount");
    public static final NamespacedKey EXPANSION_WORKBENCH = newKey("expansion_workbench");
    public static final NamespacedKey EXPANSION_WORKBENCH_6x6 = newKey("expansion_workbench_6x6");

    public static NamespacedKey INFINITY_DISPLAY = null;

    static {
        if (SupportedPluginManager.getInstance().isInfinityExpansion()) {
            INFINITY_DISPLAY = InfinityExpansion.createKey("display");
        }
    }


    @Nonnull
    public static NamespacedKey newKey(@Nonnull String key) {
        return new NamespacedKey(Networks.getInstance(), key);
    }

    @Nonnull
    public static NamespacedKey customNewKey(@Nonnull String namespace, @Nonnull String key) {
        return new NamespacedKey(namespace, key);
    }
}
