package com.balugaq.netex.api.interfaces;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.utils.Lang;
import com.balugaq.netex.utils.LocationUtil;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface FeedbackSendable {
    Map<UUID, Set<Location>> SUBSCRIBED_LOCATIONS = new ConcurrentHashMap<>();

    static void subscribe(@NotNull Player player, Location location) {
        UUID key = player.getUniqueId();
        if (!SUBSCRIBED_LOCATIONS.containsKey(key)) {
            SUBSCRIBED_LOCATIONS.put(key, ConcurrentHashMap.newKeySet());
        }
        SUBSCRIBED_LOCATIONS.get(key).add(location);
    }

    static void unsubscribe(@NotNull Player player, Location location) {
        UUID key = player.getUniqueId();
        if (SUBSCRIBED_LOCATIONS.containsKey(key)) {
            SUBSCRIBED_LOCATIONS.get(key).remove(location);
        }
    }

    static boolean hasSubscribed(@NotNull Player player, Location location) {
        UUID key = player.getUniqueId();
        if (SUBSCRIBED_LOCATIONS.containsKey(key)) {
            return SUBSCRIBED_LOCATIONS.get(key).contains(location);
        }
        return false;
    }
    //todo async SubScribedFeedBack
    default void sendFeedback(@NotNull Location location, @NotNull FeedbackType type) {
        for (UUID uuid : SUBSCRIBED_LOCATIONS.keySet()) {
            if (SUBSCRIBED_LOCATIONS.get(uuid).contains(location)) {
                Player player = Bukkit.getServer().getPlayer(uuid);
                if (player != null) {
                    sendFeedback(player, location, type.getMessage());
                }
            }
        }
    }

    default void sendFeedback(@NotNull Player player, @NotNull Location location, String message) {
        player.sendMessage(String.format(
                Lang.getString("messages.debug.status_view"), LocationUtil.humanizeBlock(location), message));
    }

    static void sendFeedback0(@NotNull Location location, @NotNull FeedbackType type) {
        for (UUID uuid : SUBSCRIBED_LOCATIONS.keySet()) {
            if (SUBSCRIBED_LOCATIONS.get(uuid).contains(location)) {
                Player player = Bukkit.getServer().getPlayer(uuid);
                if (player != null) {
                    sendFeedback0(player, location, type.getMessage());
                }
            }
        }
    }

    static void sendFeedback0(@NotNull Player player, @NotNull Location location, String message) {
        player.sendMessage(String.format(
            Lang.getString("messages.debug.status_view"), LocationUtil.humanizeBlock(location), message));
    }
}
