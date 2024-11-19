package io.github.sefiraat.networks.listeners;

import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.utils.NetworkUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.text.MessageFormat;

/*
 * Fix https://github.com/Sefiraat/Networks/issues/188
 * Fix https://github.com/Sefiraat/Networks/issues/192
 * Fix https://github.com/ytdd9527/NetworksExpansion/issues/119
 */
public class SyncListener implements Listener {
    private static final String S1 = "Listened BlockBreakEvent at {0}";
    private static final String S2 = "Listened BlockPlaceEvent at {0}";
    private static final String S3 = "Listened ChunkUnloadEvent at world: {0}, x: {1}, z: {2}";

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Networks.getInstance().debug(()->MessageFormat.format(S1, e.getBlock().getLocation()));
        NetworkUtils.clearNetwork(e.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Networks.getInstance().debug(()->MessageFormat.format(S2, e.getBlock().getLocation()));
        NetworkUtils.clearNetwork(e.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent e) {
        Networks.getInstance().debug(()->MessageFormat.format(S3, e.getWorld().getName(), e.getChunk().getX(), e.getChunk().getZ()));
        NetworkStorage.unregisterChunk(e.getChunk());
    }
}