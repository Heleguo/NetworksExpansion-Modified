package io.github.sefiraat.networks.commands;

import com.balugaq.netex.api.data.ItemContainer;
import com.balugaq.netex.api.data.StorageUnitData;
import com.balugaq.netex.api.enums.ErrorType;
import com.balugaq.netex.api.helpers.ItemStackHelper;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.unusable.AbstractBlueprint;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import com.ytdd9527.networksexpansion.utils.WorldUtils;
import io.github.bakedlibs.dough.collections.Pair;
import io.github.bakedlibs.dough.skins.PlayerHead;
import io.github.bakedlibs.dough.skins.PlayerSkin;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.managers.ExperimentalFeatureManager;
import io.github.sefiraat.networks.network.stackcaches.BlueprintInstance;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.NetworksSlimefunItemStacks;
import io.github.sefiraat.networks.slimefun.network.AdminDebuggable;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.sefiraat.networks.utils.datatypes.DataTypeMethods;
import io.github.sefiraat.networks.utils.datatypes.PersistentCraftingBlueprintType;
import io.github.sefiraat.networks.utils.datatypes.PersistentQuantumStorageType;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.ChunkPosition;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@SuppressWarnings({"deprecation", "unused"})
public class NetworksMain implements TabExecutor {
    private static final Map<UUID, Pair<Location, Location>> SELECTED_POS = new HashMap<>();

    public static Location getPos1(Player p) {
        if (SELECTED_POS.get(p.getUniqueId()) == null) {
            return null;
        }
        return SELECTED_POS.get(p.getUniqueId()).getFirstValue();
    }

    public static Location getPos2(Player p) {
        if (SELECTED_POS.get(p.getUniqueId()) == null) {
            return null;
        }
        return SELECTED_POS.get(p.getUniqueId()).getSecondValue();
    }

    public static void setPos1(Player p, Location pos) {
        SELECTED_POS.put(p.getUniqueId(), new Pair<>(pos, getPos2(p)));
    }

    public static void setPos2(Player p, Location pos) {
        SELECTED_POS.put(p.getUniqueId(), new Pair<>(getPos1(p), pos));
    }

    public static String locationToString(Location l) {
        if (l == null) {
            return Networks.getLocalizationService().getString("icons.drawer.location_error.unknown");
        }
        if (l.getWorld() == null) {
            return Networks.getLocalizationService().getString("icons.drawer.location_error.unknown");
        }
        return l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
    }

    public static long locationRange(Location pos1, Location pos2) {
        if (pos1 == null || pos2 == null) {
            return 0;
        }

        final int downX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        final int upX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        final int downY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        final int upY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        final int downZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        final int upZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        return (long) (Math.abs(upX - downX) + 1) * (Math.abs(upY - downY) + 1) * (Math.abs(upZ - downZ) + 1);
    }

    private static void doWorldEdit(Location pos1, Location pos2, Consumer<Location> consumer) {
        if (pos1 == null || pos2 == null) {
            return;
        }
        final int downX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        final int upX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        final int downY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        final int upY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        final int downZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        final int upZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        for (int x = downX; x <= upX; x++) {
            for (int y = downY; y <= upY; y++) {
                for (int z = downZ; z <= upZ; z++) {
                    consumer.accept(new Location(pos1.getWorld(), x, y, z));
                }
            }
        }
    }

    public static void viewLog(Player player) {
        final Block targetBlock = player.getTargetBlockExact(8, FluidCollisionMode.NEVER);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-admin-debuggable"));
            return;
        }

        final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(targetBlock.getLocation());
        if (slimefunItem == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-admin-debuggable"));
            return;
        }

        if (!(slimefunItem instanceof AdminDebuggable debuggable)) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-admin-debuggable"));
            return;
        }

        if (debuggable.hasViewer(player)) {
            debuggable.removeViewer(player);
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.viewer-removed"));
        } else {
            debuggable.addViewer(player);
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.viewer-added"));
        }
    }

    public static void setQuantum(Player player, int amount) {
        final Block targetBlock = player.getTargetBlockExact(8, FluidCollisionMode.NEVER);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-look-at-quantum-storage"));
            return;
        }

        final ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-hand-item"));
            return;
        }

        final SlimefunBlockData blockData = StorageCacheUtils.getBlock(targetBlock.getLocation());
        if (blockData == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-look-at-quantum-storage"));
            return;
        }

        final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(targetBlock.getLocation());
        if (slimefunItem == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-look-at-quantum-storage"));
            return;
        }

        final Location targetLocation = targetBlock.getLocation();
        final ItemStack clone = itemInHand.clone();
        if (!(slimefunItem instanceof NetworkQuantumStorage)) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.invalid-quantum-storage"));
            return;
        }

        final BlockMenu blockMenu = StorageCacheUtils.getMenu(targetLocation);
        if (blockMenu == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.invalid-quantum-storage"));
            return;
        }

        NetworkQuantumStorage.setItem(blockMenu, clone, amount);
        final QuantumCache cache = NetworkQuantumStorage.getCaches().get(blockMenu.getLocation());

        clone.setAmount(1);
        cache.setItemStack(clone);
        cache.setAmount(amount);
        NetworkQuantumStorage.updateDisplayItem(blockMenu, cache);
        NetworkQuantumStorage.syncBlock(blockMenu.getLocation(), cache);
        NetworkQuantumStorage.getCaches().put(blockMenu.getLocation(), cache);
    }

    private static void addStorageItem(Player player, int amount) {
        final Block targetBlock = player.getTargetBlockExact(8, FluidCollisionMode.NEVER);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-look-at-drawer"));
            return;
        }

        final ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-hand-item"));
            return;
        }

        final SlimefunBlockData blockData = StorageCacheUtils.getBlock(targetBlock.getLocation());
        if (blockData == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-look-at-drawer"));
            return;
        }

        final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(targetBlock.getLocation());
        if (slimefunItem == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-look-at-drawer"));
            return;
        }

        if (!(slimefunItem instanceof NetworksDrawer)) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-look-at-drawer"));
        }

        final Location targetLocation = targetBlock.getLocation();
        final ItemStack clone = itemInHand.clone();
        final StorageUnitData data = NetworksDrawer.getStorageData(targetLocation);

        if (data == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.invalid-drawer"));
            return;
        }

        clone.setAmount(amount);
        data.depositItemStack(clone, false);
        NetworksDrawer.setStorageData(targetLocation, data);
        player.sendMessage(Networks.getLocalizationService().getString("messages.commands.updated-drawer"));
    }

    private static void reduceStorageItem(Player player, int amount) {
        final Block targetBlock = player.getTargetBlockExact(8, FluidCollisionMode.NEVER);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-look-at-drawer"));
            return;
        }

        final ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-hand-item"));
            return;
        }

        final SlimefunBlockData blockData = StorageCacheUtils.getBlock(targetBlock.getLocation());
        if (blockData == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-look-at-drawer"));
            return;
        }

        final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(targetBlock.getLocation());
        if (slimefunItem == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-look-at-drawer"));
            return;
        }

        if (!(slimefunItem instanceof NetworksDrawer)) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-look-at-drawer"));
        }

        final Location targetLocation = targetBlock.getLocation();
        final ItemStack clone = itemInHand.clone();
        final StorageUnitData data = NetworksDrawer.getStorageData(targetLocation);

        if (data == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.invalid-drawer"));
            return;
        }

        clone.setAmount(1);
        data.requestItem(new ItemRequest(clone, amount));
        NetworksDrawer.setStorageData(targetLocation, data);
        player.sendMessage(Networks.getLocalizationService().getString("messages.commands.updated-drawer"));
    }

    public static void setContainerId(Player player, int containerId) {
        final Block targetBlock = player.getTargetBlockExact(8, FluidCollisionMode.NEVER);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-look-at-drawer"));
            return;
        }

        final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(targetBlock.getLocation());
        if (slimefunItem == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-look-at-drawer"));
            return;
        }

        if (!(slimefunItem instanceof NetworksDrawer)) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-look-at-drawer"));
            return;
        }

        final Location location = targetBlock.getLocation();

        player.sendMessage(Networks.getLocalizationService().getString("messages.commands.wait-for-data"));
        NetworksDrawer.requestData(location, containerId);
        player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.commands.set-container-id"), locationToString(location), containerId));
    }


    public static void worldeditPos1(Player player) {
        Block targetBlock = player.getTargetBlockExact(8, FluidCollisionMode.NEVER);
        if (targetBlock == null) {
            targetBlock = player.getLocation().getBlock();
        }

        worldeditPos1(player, targetBlock.getLocation());
    }

    public static void worldeditPos1(Player player, Location location) {
        setPos1(player, location);
        if (getPos2(player) == null) {
            player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.commands.worldedit.set-pos1"), locationToString(getPos1(player))));
        } else {
            player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.commands.worldedit.set-pos1-with-blocks"), locationToString(getPos1(player)), locationRange(getPos1(player), getPos2(player))));
        }
    }

    public static void worldeditPos2(Player player) {
        Block targetBlock = player.getTargetBlockExact(8, FluidCollisionMode.NEVER);
        if (targetBlock == null) {
            targetBlock = player.getLocation().getBlock();
        }

        worldeditPos2(player, targetBlock.getLocation());
    }

    public static void worldeditPos2(Player player, Location location) {
        setPos2(player, location);
        if (getPos1(player) == null) {
            player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.commands.worldedit.set-pos2"), locationToString(getPos2(player))));
        } else {
            player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.commands.worldedit.set-pos2-with-blocks"), locationToString(getPos1(player)), locationRange(getPos1(player), getPos2(player))));
        }
    }

    public static void worldeditClone(Player player) {
        worldeditClone(player, false);
    }

    public static void worldeditClone(Player player, boolean overrideData) {
        if (getPos1(player) == null || getPos2(player) == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.worldedit.must-select-area"));
            return;
        }

        if (!Objects.equals(getPos1(player).getWorld().getUID(), getPos2(player).getWorld().getUID())) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.worldedit.must-select-same-world"));
            return;
        }

        player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.commands.worldedit.pasting-block"), locationToString(getPos1(player)), locationToString(getPos2(player))));
        final long currentMillSeconds = System.currentTimeMillis();

        final AtomicInteger count = new AtomicInteger();
        final Location playerLocation = player.getLocation();
        final ItemStack itemInHand = player.getItemInHand();

        final Location pos1 = getPos1(player);
        final int dx = playerLocation.getBlockX() - pos1.getBlockX();
        final int dy = playerLocation.getBlockY() - pos1.getBlockY();
        final int dz = playerLocation.getBlockZ() - pos1.getBlockZ();

        final Map<ChunkPosition, Set<Location>> tickingBlocks = Slimefun.getTickerTask().getLocations();

        Bukkit.getScheduler().runTask(Networks.getInstance(), () -> {
            doWorldEdit(getPos1(player), getPos2(player), (fromLocation -> {
                final Block fromBlock = fromLocation.getBlock();
                final Block toBlock = playerLocation.getWorld().getBlockAt(fromLocation.getBlockX() + dx, fromLocation.getBlockY() + dy, fromLocation.getBlockZ() + dz);
                final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(fromLocation);
                final Location toLocation = toBlock.getLocation();

                // Block Data
                WorldUtils.copyBlockState(fromBlock.getState(), toBlock);

                // Count means successful pasting block data. Not including Slimefun data.
                count.addAndGet(1);

                // Slimefun Data
                if (slimefunItem == null) {
                    return;
                }

                // Call Handler
                slimefunItem.callItemHandler(BlockPlaceHandler.class, handler -> handler.onPlayerPlace(
                        new BlockPlaceEvent(
                                toBlock,
                                toBlock.getState(),
                                toBlock.getRelative(BlockFace.SOUTH),
                                itemInHand,
                                player,
                                true
                        )
                ));

                SlimefunBlockData fromSlimefunBlockData = Slimefun.getDatabaseManager().getBlockDataController().getBlockData(fromLocation);
                if (overrideData) {
                    Slimefun.getDatabaseManager().getBlockDataController().removeBlock(toLocation);
                }

                boolean ticking = false;
                ChunkPosition chunkPosition = new ChunkPosition(fromLocation);
                if (tickingBlocks.containsKey(chunkPosition)) {
                    if (tickingBlocks.get(chunkPosition).contains(fromLocation)) {
                        ticking = true;
                    }
                }

                if (StorageCacheUtils.hasBlock(toLocation)) {
                    return;
                }

                // Slimefun Block
                Slimefun.getDatabaseManager().getBlockDataController().createBlock(toLocation, slimefunItem.getId());
                SlimefunBlockData toSlimefunBlockData = Slimefun.getDatabaseManager().getBlockDataController().getBlockData(toLocation);

                // SlimefunBlockData
                if (fromSlimefunBlockData == null || toSlimefunBlockData == null) {
                    return;
                }

                Map<String, String> data = fromSlimefunBlockData.getAllData();
                for (String key : data.keySet()) {
                    toSlimefunBlockData.setData(key, data.get(key));
                }

                // BlockMenu
                final BlockMenu fromMenu = fromSlimefunBlockData.getBlockMenu();
                final BlockMenu toMenu = toSlimefunBlockData.getBlockMenu();

                if (fromMenu == null || toMenu == null) {
                    return;
                }

                ItemStack[] contents = fromMenu.getContents();
                for (int i = 0; i < contents.length; i++) {
                    if (contents[i] != null) {
                        toMenu.getInventory().setItem(i, contents[i].clone());
                    }
                }

                // Ticking
                if (!ticking) {
                    Slimefun.getTickerTask().disableTicker(toLocation);
                }
            }));
            player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.commands.worldedit.paste-done"), count, System.currentTimeMillis() - currentMillSeconds));
        });
    }

    public static void worldeditPaste(Player player, String sfid) {
        worldeditPaste(player, sfid, false, false);
    }

    public static void worldeditPaste(Player player, String sfid, boolean overrideData) {
        worldeditPaste(player, sfid, overrideData, false);
    }

    public static void worldeditPaste(Player player, String sfid, boolean overrideData, boolean force) {
        final SlimefunItem sfItem = SlimefunItem.getById(sfid);

        if (getPos1(player) == null || getPos2(player) == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.worldedit.must-select-area"));
            return;
        }

        if (!Objects.equals(getPos1(player).getWorld().getUID(), getPos2(player).getWorld().getUID())) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.worldedit.must-select-same-world"));
            return;
        }

        if (sfItem == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.worldedit.invalid-slimefun-block-id"));
            return;
        }

        if (!sfItem.getItem().getType().isBlock()) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.worldedit.invalid-slimefun-block-id"));
            return;
        }

        if (sfItem.getItem().getType() == Material.AIR) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.worldedit.not-a-placeable-block"));
            return;
        }

        if (!force && sfItem instanceof NotPlaceable) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.worldedit.not-placeable-block"));
            return;
        }

        player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.commands.worldedit.pasting-block"), locationToString(getPos1(player)), locationToString(getPos2(player))));
        final long currentMillSeconds = System.currentTimeMillis();

        final AtomicInteger count = new AtomicInteger();
        final Material t = sfItem.getItem().getType();
        final ItemStack itemStack = sfItem.getItem();
        PlayerSkin skin0 = null;
        boolean isHead0 = false;
        final PlayerSkin skin;
        final boolean isHead;
        if (itemStack.getType() == Material.PLAYER_HEAD || itemStack.getType() == Material.PLAYER_WALL_HEAD) {
            if (itemStack instanceof SlimefunItemStack sfis) {
                Optional<String> texture = sfis.getSkullTexture();
                if (texture.isPresent()) {
                    skin0 = PlayerSkin.fromBase64(texture.get());
                    isHead0 = true;
                }
            }
        }
        skin = skin0;
        isHead = isHead0;

        doWorldEdit(getPos1(player), getPos2(player), (location -> {
            final Block targetBlock = location.getBlock();
            sfItem.callItemHandler(BlockPlaceHandler.class, h -> h.onPlayerPlace(
                    new BlockPlaceEvent(
                            targetBlock,
                            targetBlock.getState(),
                            targetBlock.getRelative(BlockFace.DOWN),
                            itemStack,
                            player,
                            true
                    )
            ));
            if (overrideData) {
                Slimefun.getDatabaseManager().getBlockDataController().removeBlock(location);
            }
            if (!StorageCacheUtils.hasBlock(location)) {
                targetBlock.setType(t);
                if (isHead) {
                    PlayerHead.setSkin(targetBlock, skin, false);
                }
                Slimefun.getDatabaseManager().getBlockDataController().createBlock(location, sfid);
            }
            count.addAndGet(1);
        }));

        player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.commands.worldedit.paste-done"), count, System.currentTimeMillis() - currentMillSeconds));
    }

    public static void worldeditClear(Player player, boolean callHandler, boolean skipVanilla) {
        if (getPos1(player) == null || getPos2(player) == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.worldedit.must-select-area"));
            return;
        }

        if (!Objects.equals(getPos1(player).getWorld().getUID(), getPos2(player).getWorld().getUID())) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.worldedit.must-select-same-world"));
            return;
        }

        player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.commands.worldedit.clearing-area"), locationToString(getPos1(player)), locationToString(getPos2(player))));
        final long currentMillSeconds = System.currentTimeMillis();

        final AtomicInteger count = new AtomicInteger();
        doWorldEdit(getPos1(player), getPos2(player), (location -> {
            final Block targetBlock = getPos1(player).getWorld().getBlockAt(location);
            if (StorageCacheUtils.hasBlock(location)) {
                SlimefunItem item = StorageCacheUtils.getSfItem(location);
                if (callHandler) {
                    item.callItemHandler(BlockBreakHandler.class, handler -> handler.onPlayerBreak(
                            new BlockBreakEvent(targetBlock, player),
                            new ItemStack(Material.AIR),
                            new ArrayList<>()
                    ));
                }
                targetBlock.setType(Material.AIR);
            }
            Slimefun.getDatabaseManager().getBlockDataController().removeBlock(location);
            if (!skipVanilla) {
                targetBlock.setType(Material.AIR);
            }
            count.addAndGet(1);
        }));

        player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.commands.worldedit.clear-done"), count, System.currentTimeMillis() - currentMillSeconds));
    }

    public static void worldeditBlockMenuSetSlot(Player player, int slot) {
        if (getPos1(player) == null || getPos2(player) == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.worldedit.must-select-area"));
            return;
        }

        if (!Objects.equals(getPos1(player).getWorld().getUID(), getPos2(player).getWorld().getUID())) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.worldedit.must-select-same-world"));
            return;
        }

        if (!(0 <= slot && slot <= 53)) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.worldedit.invalid-slot"));
            return;
        }

        final ItemStack hand = player.getInventory().getItemInMainHand();

        player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.commands.worldedit.set-slot"), slot, ItemStackHelper.getDisplayName(hand)));
        final long currentMillSeconds = System.currentTimeMillis();

        final AtomicInteger count = new AtomicInteger();
        doWorldEdit(getPos1(player), getPos2(player), (location -> {
            final BlockMenu menu = StorageCacheUtils.getMenu(location);
            if (menu != null) {
                menu.replaceExistingItem(slot, hand);
            }
            count.addAndGet(1);
        }));

        player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.commands.worldedit.set-slot-done"), slot, System.currentTimeMillis() - currentMillSeconds));
    }

    public static void worldeditBlockInfoAdd(Player player, String key, String value) {
        if (getPos1(player) == null || getPos2(player) == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.worldedit.must-select-area"));
            return;
        }

        if (!Objects.equals(getPos1(player).getWorld().getUID(), getPos2(player).getWorld().getUID())) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.worldedit.must-select-same-world"));
            return;
        }

        player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.commands.worldedit.setting-info"), key, value));
        final long currentMillSeconds = System.currentTimeMillis();

        final AtomicInteger count = new AtomicInteger();
        doWorldEdit(getPos1(player), getPos2(player), (location -> {
            if (StorageCacheUtils.getBlock(location) != null) {
                StorageCacheUtils.setData(location, key, value);
                count.addAndGet(1);
            }
        }));

        player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.commands.worldedit.setting-info"), key, System.currentTimeMillis() - currentMillSeconds));
    }

    public static void worldeditBlockInfoRemove(Player player, String key) {
        if (getPos1(player) == null || getPos2(player) == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.worldedit.must-select-area"));
            return;
        }

        if (!Objects.equals(getPos1(player).getWorld().getUID(), getPos2(player).getWorld().getUID())) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.worldedit.must-select-same-world"));
            return;
        }

        player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.commands.worldedit.removing-info"), key));
        final long currentMillSeconds = System.currentTimeMillis();

        final AtomicInteger count = new AtomicInteger();
        doWorldEdit(getPos1(player), getPos2(player), (location -> {
            if (StorageCacheUtils.getBlock(location) != null) {
                StorageCacheUtils.removeData(location, key);
                count.addAndGet(1);
            }
        }));
        player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.commands.worldedit.removing-info"), key, System.currentTimeMillis() - currentMillSeconds));
    }

    private static void updateItem(Player player) {
        final ItemStack itemInHand = player.getInventory().getItemInMainHand();
        final SlimefunItem slimefunItem = SlimefunItem.getByItem(itemInHand);
        if (slimefunItem == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.not-a-slimefun-item"));
            return;
        }

        final String currentId = slimefunItem.getId();
        if (slimefunItem instanceof NetworksDrawer) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.cannot-update-cargo-storage-unit"));
        } else if (slimefunItem instanceof NetworkQuantumStorage) {
            final ItemMeta meta = itemInHand.getItemMeta();
            final QuantumCache quantumCache = DataTypeMethods.getCustom(
                    meta,
                    Keys.QUANTUM_STORAGE_INSTANCE,
                    PersistentQuantumStorageType.TYPE
            );

            if (quantumCache == null || quantumCache.getItemStack() == null) {
                itemInHand.setItemMeta(SlimefunItem.getById(currentId).getItem().getItemMeta());
                player.sendMessage(Networks.getLocalizationService().getString("messages.commands.updated-item"));
                return;
            }

            final ItemStack stored = quantumCache.getItemStack();
            final SlimefunItem sfi = SlimefunItem.getByItem(stored);
            if (sfi != null) {
                final String quantumStoredId = sfi.getId();
                stored.setItemMeta(SlimefunItem.getById(quantumStoredId).getItem().getItemMeta());
                player.sendMessage(Networks.getLocalizationService().getString("messages.commands.updated-item-in-quantum-storage"));
            }
            DataTypeMethods.setCustom(meta, Keys.QUANTUM_STORAGE_INSTANCE, PersistentQuantumStorageType.TYPE, quantumCache);
            quantumCache.updateMetaLore(meta);
            itemInHand.setItemMeta(meta);
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.updated-item"));
        } else {
            itemInHand.setItemMeta(SlimefunItem.getById(currentId).getItem().getItemMeta());
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.updated-item"));
        }
    }

    public static void getStorageItem(Player player, int slot) {
        final Block targetBlock = player.getTargetBlockExact(8, FluidCollisionMode.NEVER);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-look-at-drawer"));
            return;
        }

        final SlimefunBlockData blockData = StorageCacheUtils.getBlock(targetBlock.getLocation());
        if (blockData == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-look-at-drawer"));
            return;
        }

        final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(targetBlock.getLocation());
        if (slimefunItem == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-look-at-drawer"));
            return;
        }

        if (!(slimefunItem instanceof NetworksDrawer)) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-look-at-drawer"));
        }

        final Location targetLocation = targetBlock.getLocation();
        final StorageUnitData data = NetworksDrawer.getStorageData(targetLocation);

        if (data == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.invalid-drawer"));
            return;
        }

        final List<ItemContainer> stored = data.getStoredItems();
        if (slot >= stored.size()) {
            player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.commands.invalid-slot"), stored.size() - 1));
        } else {
            final ItemStack stack = stored.get(slot).getSample();
            if (stack == null || stack.getType() == Material.AIR) {
                player.sendMessage(Networks.getLocalizationService().getString("messages.commands.empty-slot"));
                return;
            }

            player.getInventory().addItem(StackUtils.getAsQuantity(stack, 1));
        }
    }

    public static void help(CommandSender sender, String mainCommand) {
        if (mainCommand == null) {
            for (String message : Networks.getLocalizationService().getStringList("messages.commands.help")) {
                sender.sendMessage(message);
            }
            return;
        }
        switch (mainCommand.toLowerCase(Locale.ROOT)) {
            case "help" -> {
                for (String message : Networks.getLocalizationService().getStringList("messages.commands.example.help")) {
                    sender.sendMessage(message);
                }
            }
            case "fillquantum" -> {
                for (String message : Networks.getLocalizationService().getStringList("messages.commands.example.fillquantum")) {
                    sender.sendMessage(message);
                }
            }
            case "fixblueprint" -> {
                for (String message : Networks.getLocalizationService().getStringList("messages.commands.example.fixblueprint")) {
                    sender.sendMessage(message);
                }
            }
            case "addstorageitem" -> {
                for (String message : Networks.getLocalizationService().getStringList("messages.commands.example.addstorageitem")) {
                    sender.sendMessage(message);
                }
            }
            case "reducestorageitem" -> {
                for (String message : Networks.getLocalizationService().getStringList("messages.commands.example.reducestorageitem")) {
                    sender.sendMessage(message);
                }
            }
            case "setquantum" -> {
                for (String message : Networks.getLocalizationService().getStringList("messages.commands.example.setquantum")) {
                    sender.sendMessage(message);
                }
            }
            case "setcontainerid" -> {
                for (String message : Networks.getLocalizationService().getStringList("messages.commands.example.setcontainerid")) {
                    sender.sendMessage(message);
                }
            }
            case "getstorageitem" -> {
                for (String message : Networks.getLocalizationService().getStringList("messages.commands.example.getstorageitem")) {
                    sender.sendMessage(message);
                }
            }
            case "worldedit" -> {
                for (String message : Networks.getLocalizationService().getStringList("messages.commands.example.worldedit")) {
                    sender.sendMessage(message);
                }
            }
            case "updateitem" -> {
                for (String message : Networks.getLocalizationService().getStringList("messages.commands.example.updateitem")) {
                    sender.sendMessage(message);
                }
            }
            case "viewlog" -> {
                for (String message : Networks.getLocalizationService().getStringList("messages.commands.example.viewlog")) {
                    sender.sendMessage(message);
                }
            }
            default -> {
                for (String message : Networks.getLocalizationService().getStringList("messages.commands.example.unknown-command")) {
                    sender.sendMessage(message);
                }
            }
        }
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (args.length == 0) {
            help(sender, null);
            return true;
        }
        switch (args[0]) {
            case "fillquantum", "fixblueprint", "addstorageitem", "reducestorageitem", "setquantum", "setcontainerid" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(getErrorMessage(ErrorType.MUST_BE_PLAYER));
                    return false;
                }
            }
            case "help" -> {

            }
        }

        // Player or console
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "help" -> {
                if (sender.isOp()) {
                    if (args.length >= 2) {
                        help(sender, args[1]);
                    } else {
                        help(sender, null);
                    }
                } else {
                    sender.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                }
                return true;
            }
        }

        // Player only
        if (sender instanceof Player player) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "fillquantum" -> {
                    if (!player.hasPermission("networks.admin") && !player.hasPermission("networks.commands.fillquantum")) {
                        player.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                        return true;
                    }

                    if (args.length <= 1) {
                        player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "amount"));
                        return true;
                    }

                    try {
                        int amount = Integer.parseInt(args[1]);
                        fillQuantum(player, amount);
                    } catch (NumberFormatException e) {
                        player.sendMessage(getErrorMessage(ErrorType.INVALID_REQUIRED_ARGUMENT, "amount"));
                    }

                    return true;
                }

                case "fixblueprint" -> {
                    if (!player.hasPermission("networks.admin") && !player.hasPermission("networks.commands.fixblueprint")) {
                        player.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                        return true;
                    }

                    if (args.length <= 1) {
                        player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "keyInMeta"));
                        return true;
                    }

                    String before = args[1];
                    fixBlueprint(player, before);
                    return true;
                }

                case "setquantum" -> {
                    if (!player.hasPermission("networks.admin") && !player.hasPermission("networks.commands.setquantum")) {
                        player.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                        return true;
                    }

                    if (args.length <= 1) {
                        player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "amount"));
                        return true;
                    }

                    try {
                        int amount = Integer.parseInt(args[1]);
                        setQuantum(player, amount);
                    } catch (NumberFormatException e) {
                        player.sendMessage(getErrorMessage(ErrorType.INVALID_REQUIRED_ARGUMENT, "amount"));
                    }

                    return true;
                }
                case "addstorageitem" -> {
                    if (!player.hasPermission("networks.admin") && !player.hasPermission("networks.commands.addstorageitem")) {
                        player.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                        return true;
                    }

                    if (args.length <= 1) {
                        player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "amount"));
                        return true;
                    }

                    try {
                        int amount = Integer.parseInt(args[1]);
                        addStorageItem(player, amount);
                    } catch (NumberFormatException e) {
                        player.sendMessage(getErrorMessage(ErrorType.INVALID_REQUIRED_ARGUMENT, "amount"));
                    }

                    return true;
                }

                case "reducestorageitem" -> {
                    if (!player.hasPermission("networks.admin") && !player.hasPermission("networks.commands.reducestorageitem")) {
                        player.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                        return true;
                    }

                    if (args.length <= 1) {
                        player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "amount"));
                        return true;
                    }

                    try {
                        int amount = Integer.parseInt(args[1]);
                        reduceStorageItem(player, amount);
                    } catch (NumberFormatException e) {
                        player.sendMessage(getErrorMessage(ErrorType.INVALID_REQUIRED_ARGUMENT, "amount"));
                    }

                    return true;
                }

                case "setcontainerid" -> {
                    if (!player.hasPermission("networks.admin") && !player.hasPermission("networks.commands.setcontainerid")) {
                        player.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                        return true;
                    }

                    if (args.length <= 1) {
                        player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "containerId"));
                        return true;
                    }

                    try {
                        int containerId = Integer.parseInt(args[1]);
                        setContainerId(player, containerId);
                    } catch (NumberFormatException e) {
                        player.sendMessage(getErrorMessage(ErrorType.INVALID_REQUIRED_ARGUMENT, "containerId"));
                    }

                    return true;
                }

                case "worldedit" -> {
                    if (!player.hasPermission("networks.admin") && !player.hasPermission("networks.commands.worldedit.*")) {
                        player.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                        return true;
                    }

                    if (args.length <= 1) {
                        player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "subCommand"));
                        return true;
                    }

                    switch (args[1].toLowerCase(Locale.ROOT)) {
                        case "pos1" -> {
                            worldeditPos1(player);
                        }
                        case "pos2" -> {
                            worldeditPos2(player);
                        }

                        case "clear" -> {
                            switch (args.length) {
                                case 4 -> {
                                    try {
                                        boolean callHandler = Boolean.parseBoolean(args[2]);
                                        boolean skipVanilla = Boolean.parseBoolean(args[3]);
                                        worldeditClear(player, callHandler, skipVanilla);
                                    } catch (NumberFormatException e) {
                                        player.sendMessage(getErrorMessage(ErrorType.INVALID_REQUIRED_ARGUMENT, "callHandler / skipVanilla"));
                                    }
                                }
                                case 3 -> {
                                    try {
                                        boolean callHandler = Boolean.parseBoolean(args[2]);
                                        worldeditClear(player, callHandler, true);
                                    } catch (NumberFormatException e) {
                                        player.sendMessage(getErrorMessage(ErrorType.INVALID_REQUIRED_ARGUMENT, "callHandler"));
                                    }
                                }
                                default -> {
                                    worldeditClear(player, true, true);
                                }
                            }
                        }

                        case "clone" -> {
                            if (args.length <= 2) {
                                worldeditClone(player);
                            } else if (args.length <= 3) {
                                worldeditClone(player, "override".equalsIgnoreCase(args[2]));
                            }
                        }

                        case "paste" -> {
                            if (args.length <= 2) {
                                player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "sfId"));
                                return true;
                            }
                            boolean overrideData = false;
                            boolean force = false;
                            switch (args.length) {
                                case 5 -> {
                                    if ("override".equalsIgnoreCase(args[3])) {
                                        overrideData = true;
                                    }
                                    force = Boolean.parseBoolean(args[4]);
                                }
                                case 4 -> {
                                    if ("override".equalsIgnoreCase(args[3])) {
                                        overrideData = true;
                                    }
                                }
                            }
                            worldeditPaste(player, args[2], overrideData, force);
                        }

                        case "blockmenu" -> {
                            if (args.length <= 2) {
                                player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "subCommand"));
                                return true;
                            }

                            switch (args[2].toLowerCase(Locale.ROOT)) {
                                case "setslot" -> {
                                    if (args.length <= 3) {
                                        player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "slot"));
                                        return true;
                                    }

                                    try {
                                        int slot = Integer.parseInt(args[3]);
                                        worldeditBlockMenuSetSlot(player, slot);
                                    } catch (NumberFormatException e) {
                                        player.sendMessage(getErrorMessage(ErrorType.INVALID_REQUIRED_ARGUMENT, "slot"));
                                    }
                                }

                                default -> {
                                    player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "subCommand"));
                                }
                            }
                        }

                        case "blockinfo" -> {
                            if (args.length <= 2) {
                                player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "subCommand"));
                                return true;
                            }

                            switch (args[2].toLowerCase(Locale.ROOT)) {
                                case "add", "set" -> {
                                    switch (args.length) {
                                        case 3 -> {
                                            player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "key"));
                                        }
                                        case 4 -> {
                                            player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "value"));
                                        }
                                        case 5 -> {
                                            String key = args[3];
                                            String value = args[4];
                                            worldeditBlockInfoAdd(player, key, value);
                                        }
                                    }
                                }
                                case "remove" -> {
                                    if (args.length <= 3) {
                                        player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "value"));
                                        return true;
                                    }

                                    String value = args[3];
                                    worldeditBlockInfoRemove(player, value);
                                }

                                default -> {
                                    player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "subCommand"));
                                }
                            }
                        }
                    }
                }

                case "updateitem" -> {
                    if (!player.hasPermission("networks.admin") && !player.hasPermission("networks.commands.updateitem")) {
                        player.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                        return true;
                    }

                    updateItem(player);
                    return true;
                }

                case "getstorageitem" -> {
                    if (!player.hasPermission("networks.admin") && !player.hasPermission("networks.commands.getstorageitem")) {
                        player.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                        return true;
                    }

                    if (args.length <= 2) {
                        player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "slot"));
                        return true;
                    }

                    try {
                        int slot = Integer.parseInt(args[1]);
                        getStorageItem(player, slot);
                    } catch (NumberFormatException e) {
                        player.sendMessage(getErrorMessage(ErrorType.INVALID_REQUIRED_ARGUMENT, "slot"));
                    }

                    return true;
                }

                case "viewlog" -> {
                    if (!player.hasPermission("networks.admin") && !player.hasPermission("networks.commands.viewlog")) {
                        player.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                        return true;
                    }

                    viewLog(player);
                    return true;
                }
                case "experimental"->{
                    if (!player.hasPermission("networks.admin") && !player.hasPermission("networks.commands.experimental")) {
                        player.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                        return true;
                    }

                    return ExperimentalFeatureManager.getInstance().onCommand(sender,command,label, Arrays.copyOfRange(args,1,args.length));
                }


                default -> {
                    help(player, null);
                }
            }
        }
        // We always return true, even if the command was not executed, so that the help message is not shown.
        return true;
    }

    public void fillQuantum(Player player, int amount) {
        final ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack.getType() == Material.AIR) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.no-item-in-hand"));
            return;
        }

        SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);

        if (!(slimefunItem instanceof NetworkQuantumStorage)) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-hand-quantum-storage"));
            return;
        }

        ItemMeta meta = itemStack.getItemMeta();
        final QuantumCache quantumCache = DataTypeMethods.getCustom(
                meta,
                Keys.QUANTUM_STORAGE_INSTANCE,
                PersistentQuantumStorageType.TYPE
        );

        if (quantumCache == null || quantumCache.getItemStack() == null) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.no-set-item"));
            return;
        }

        quantumCache.setAmount(amount);
        DataTypeMethods.setCustom(meta, Keys.QUANTUM_STORAGE_INSTANCE, PersistentQuantumStorageType.TYPE, quantumCache);
        quantumCache.updateMetaLore(meta);
        itemStack.setItemMeta(meta);
        player.sendMessage(Networks.getLocalizationService().getString("messages.commands.updated-quantum-storage"));
    }

    // change "networks-changed:recipe" -> "networks:recipe"
    public void fixBlueprint(Player player, String before) {
        ItemStack blueprint = player.getInventory().getItemInMainHand();
        if (blueprint.getType() == Material.AIR) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-hand-blueprint"));
            return;
        }

        final SlimefunItem item = SlimefunItem.getByItem(blueprint);

        if (!(item instanceof AbstractBlueprint)) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.must-hand-blueprint"));
            return;
        }

        ItemMeta blueprintMeta = blueprint.getItemMeta();

        final Optional<BlueprintInstance> optional = DataTypeMethods.getOptionalCustom(
                blueprintMeta,
                new NamespacedKey(before, Keys.BLUEPRINT_INSTANCE.getKey()),
                PersistentCraftingBlueprintType.TYPE
        );

        if (optional.isEmpty()) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.commands.invalid-blueprint"));
            return;
        }

        BlueprintInstance instance = optional.get();

        ItemStack fix = NetworksSlimefunItemStacks.CRAFTING_BLUEPRINT.clone();
        AbstractBlueprint.setBlueprint(fix, instance.getRecipeItems(), instance.getItemStack());

        blueprint.setItemMeta(fix.getItemMeta());

        player.sendMessage(Networks.getLocalizationService().getString("messages.commands.fixed-blueprint"));

    }

    @Override
    public @Nullable List<String> onTabComplete(
            @Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(args.length > 0&&"experimental".equals(args[0])) {
            return ExperimentalFeatureManager.getInstance().onTabComplete(sender,command,label, Arrays.copyOfRange(args,1,args.length));
        }
        List<String> raw = onTabCompleteRaw(sender, args);
        return StringUtil.copyPartialMatches(args[args.length - 1], raw, new ArrayList<>());
    }

    public @Nonnull List<String> onTabCompleteRaw(@Nonnull CommandSender sender, @Nonnull String[] args) {

        switch (args.length) {
            case 1 -> {
                return List.of(
                        "addStorageItem",
                        "fillQuantum",
                        "fixBlueprint",
                        "getStorageItem",
                        "help",
                        "reduceStorageItem",
                        "setContainerId",
                        "setQuantum",
                        "updateItem",
                        "viewLog",
                        "worldedit",
                        "experimental"
                );
            }
            case 2 -> {
                return switch (args[0].toLowerCase(Locale.ROOT)) {
                    // case "help", "updateitem" -> List.of();
                    case "getstorageitem" -> List.of("<slot>");
                    case "fillquantum", "addstorageitem", "reducestorageitem", "setquantum" -> List.of("<amount>");
                    case "fixblueprint" -> List.of("<keyInMeta>");
                    case "setcontainerid" -> List.of("<containerId>");
                    case "worldedit" -> List.of("pos1", "pos2", "paste", "clear", "clone", "blockmenu", "blockinfo");
                    default -> List.of();
                };
            }
            case 3 -> {
                if (args[0].equalsIgnoreCase("worldedit")) {
                    return switch (args[1]) {
                        // case "pos1", "pos2" -> List.of();
                        case "paste" -> Slimefun.getRegistry().getAllSlimefunItems()
                                .stream()
                                .filter(sfItem -> sfItem.getItem().getType().isBlock())
                                .map(SlimefunItem::getId)
                                .toList();
                        case "blockinfo" -> List.of("add", "remove", "set");
                        case "blockmenu" -> List.of("setSlot");
                        case "clear" -> List.of("true", "false");
                        default -> List.of();
                    };
                }
            }
            case 4 -> {
                if (args[0].equalsIgnoreCase("worldedit")) {
                    return switch (args[1].toLowerCase(Locale.ROOT)) {
                        case "paste" -> List.of("override", "keep");
                        case "blockmenu" -> switch (args[2].toLowerCase(Locale.ROOT)) {
                            case "setslot" ->
                                    List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53");
                            default -> List.of();
                        };
                        case "clear" -> List.of("true", "false");
                        default -> List.of();
                    };
                }
            }
            case 5 -> {
                if (args[0].equalsIgnoreCase("worldedit")) {
                    if (args[1].equalsIgnoreCase("paste")) {
                        return List.of("true", "false");
                    }
                }
            }
        }

        return new ArrayList<>();
    }

    public String getErrorMessage(ErrorType errorType) {
        return getErrorMessage(errorType, null);
    }

    public String getErrorMessage(ErrorType errorType, String argument) {
        return switch (errorType) {
            case NO_PERMISSION -> Networks.getLocalizationService().getString("messages.commands.no-permission");
            case NO_ITEM_IN_HAND -> Networks.getLocalizationService().getString("messages.commands.no-item-in-hand");
            case MISSING_REQUIRED_ARGUMENT ->
                    String.format(Networks.getLocalizationService().getString("messages.commands.missing-required-argument"), argument);
            case INVALID_REQUIRED_ARGUMENT ->
                    String.format(Networks.getLocalizationService().getString("messages.commands.invalid-required-argument"), argument);
            case MUST_BE_PLAYER -> Networks.getLocalizationService().getString("messages.commands.must-be-player");
            default -> Networks.getLocalizationService().getString("messages.commands.unknown-error");
        };
    }
}
