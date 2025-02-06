package io.github.sefiraat.networks.slimefun.network;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.Networks;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;

public interface AdminDebuggable {
    Set<Player> VIEWERS = new HashSet<>();
    //String DEBUG_KEY = "network_debugging";
    HashSet<Location> DEBUG_LOCATIONS = new HashSet<>();
    default boolean isDebug(@Nonnull Location location) {
       // String debug = StorageCacheUtils.getData(location, DEBUG_KEY);
        return DEBUG_LOCATIONS.contains(location); //Boolean.parseBoolean(debug);
    }

    default void setDebug(@Nonnull Location location, boolean value) {
        if (value){
            DEBUG_LOCATIONS.add(location);
        }else {
            DEBUG_LOCATIONS.remove(location);
        }
       // StorageCacheUtils.setData(location, DEBUG_KEY, String.valueOf(value));
    }

    default void toggle(@Nonnull Location location, @Nonnull Player player) {
        final boolean isDebug = isDebug(location);
        final boolean nextState = !isDebug;
        setDebug(location, nextState);
        player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.debug.toggle-debug"), nextState));
        if (nextState) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.debug.enabled-debug"));
        }
    }

    default void sendDebugMessage0(@Nonnull Location location, @Nonnull Supplier<String> string) {
        if (isDebug(location)) {
            final String locationString = "W[" + location.getWorld().getName() + "] " +
                    "X[" + location.getBlockX() + "] " +
                    "Y[" + location.getBlockY() + "] " +
                    "Z[" + location.getBlockZ() + "] ";
            Networks.getInstance().getLogger().log(Level.INFO, String.format(Networks.getLocalizationService().getString("messages.debug.info"), locationString, string.get()));
            for (Player player : VIEWERS) {
                if (player.isOnline()) {
                    player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.debug.viewer-info"), locationString, string.get()));
                } else {
                    removeViewer(player);
                }
            }
        }
    }
    default void sendDebugMessage(@Nonnull Location location, @Nonnull Supplier<String>... string) {
        if(isDebug(location)) {
            for (Supplier<String> stringSupplier : string) {
                sendDebugMessage0(location, stringSupplier);
            }
        }
    }

    default void addViewer(@Nonnull Player player) {
        VIEWERS.add(player);
    }

    default void removeViewer(@Nonnull Player player) {
        VIEWERS.remove(player);
    }

    default boolean hasViewer(@Nonnull Player player) {
        return VIEWERS.contains(player);
    }
}
