package com.balugaq.netex.api.interfaces;

import com.balugaq.netex.api.enums.FeedbackType;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SoftCellBannable extends FeedbackSendable {
    boolean SOFT_CELL_BAN = Networks.getConfigManager().getSoftCellBan();
    int SOFT_CELL_BAN_THRESHOLD = Networks.getConfigManager().getSoftCellBanThreshold();

    default boolean checkSoftCellBan(@NotNull BlockMenu blockMenu, NodeDefinition definition) {
        return checkSoftCellBan(blockMenu.getLocation(), definition);
    }

    default boolean checkSoftCellBan(@NotNull BlockMenu blockMenu, @NotNull NetworkRoot root) {
        return checkSoftCellBan(blockMenu.getLocation(), root);
    }

    default boolean checkSoftCellBan(@NotNull Location location, @Nullable NodeDefinition definition) {
        if (definition != null && definition.getNode() != null) {
            return checkSoftCellBan(location, definition.getNode().getRoot());
        } else {
            return true; // true to pass the execution
        }
    }

    default boolean checkSoftCellBan(@NotNull Location location, @NotNull NetworkRoot root) {
        if (SOFT_CELL_BAN && root.getCellsSize() > SOFT_CELL_BAN_THRESHOLD) {
            sendFeedback(location, FeedbackType.SOFT_CELL_BANNED);
            return true; // true to pass the execution
        }

        return false; // false to continue the execution
    }
}
