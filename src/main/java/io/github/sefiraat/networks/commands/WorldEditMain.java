package io.github.sefiraat.networks.commands;

import com.balugaq.netex.api.enums.ErrorType;
import com.balugaq.netex.api.helpers.ItemStackHelper;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.utils.WorldUtils;
import io.github.bakedlibs.dough.collections.Pair;
import io.github.bakedlibs.dough.skins.PlayerHead;
import io.github.bakedlibs.dough.skins.PlayerSkin;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.managers.ExperimentalFeatureManager;
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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class WorldEditMain extends AbstractMainCommand{
    public WorldEditMain(NetworksMain parent) {
        this.parent=parent;
    }
    NetworksMain parent;
    @Override
    public String permissionRequired() {
        return "";
    }

    @Override
    public void noPermission(CommandSender var1) {
        var1.sendMessage(parent.getErrorMessage(ErrorType.NO_PERMISSION));
    }

    private SubCommand mainCommand=new SubCommand("worldedit",new SimpleCommandArgs("_operation"),"")
            .setTabCompletor("_operation",()->getSubCommands().stream().map(SubCommand::getName).toList());

    private SubCommand pos1Command=new SubCommand("pos1",new SimpleCommandArgs(),""){
        @Override
        public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
            worldeditPos1(isPlayer(var1,false));
            return true;
        }
    }
            .register(this);
    private SubCommand pos2Command=new SubCommand("pos2",new SimpleCommandArgs(),""){
        @Override
        public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
            worldeditPos2(isPlayer(var1,false));
            return true;
        }
    }
            .register(this);
    private SubCommand clearCommand=new SubCommand("clear",new SimpleCommandArgs("callHandler","skipVanilla"),""){
        public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
            var input=parseInput(var4).getFirstValue();
            String callHandler=input.nextArg();
            String skipVanilla=input.nextArg();
            worldeditClear(isPlayer(var1,false),"true".equals(callHandler),"true".equals(skipVanilla));
            return true;
        }
    }
            .setDefault("callHandler","true")
            .setDefault("skipVanilla","true")
            .setTabCompletor("callHandler",()->List.of("true","false"))
            .setTabCompletor("skipVanilla",()->List.of("true","false"))
            .register(this);
    private SubCommand cloneCommand=new SubCommand("clone",new SimpleCommandArgs("override"),""){
        public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
            var input=parseInput(var4).getFirstValue();
            String override=input.nextArg();
            worldeditClone(isPlayer(var1,false),"override".equalsIgnoreCase(override));
            return true;
        }
    }
            .setDefault("override","keep")
            .setTabCompletor("override",()->List.of("keep","override"))
            .register(this);
    private SubCommand pasteCommand=new SubCommand("paste",new SimpleCommandArgs("sfId","override","force"),""){
        @Override
        public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
            Player player=isPlayer(var1,false);
            var input=parseInput(var4).getFirstValue();
            String sfid=input.nextArg();
            if(sfid==null){
                sendMessage(player,parent.getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "sfId"));
            }else {
                String override=input.nextArg();
                String force=input.nextArg();
                worldeditPaste(player, sfid, "override".equalsIgnoreCase(override), "true".equalsIgnoreCase(force));
            }
            return true;
        }
    }
            .setTabCompletor("sfId",()->Slimefun.getRegistry().getAllSlimefunItems().stream().map(SlimefunItem::getId).toList())
            .setDefault("override","keep")
            .setTabCompletor("override",()->List.of("keep","override"))
            .setDefault("force","false")
            .setTabCompletor("force",()->List.of("true","false"))
            .register(this);
    private SubCommand blockMenuCommand=new SubCommand("blockmenu",new SimpleCommandArgs("operation","index"),""){
        @Override
        public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
            Player player=isPlayer(var1,false);
            var input=parseInput(var4).getFirstValue();
            String sfid=input.nextArg();
            if("setslot".equals(sfid)){
                String index=input.nextArg();
                if(index==null){
                    player.sendMessage(parent.getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "slot"));
                }else {
                    try{
                        int slot=Integer.parseInt(index);
                        worldeditBlockMenuSetSlot(player, slot);
                    }catch (Throwable e){
                        player.sendMessage(parent.getErrorMessage(ErrorType.INVALID_REQUIRED_ARGUMENT, "slot"));
                    }
                }
            }else{
                player.sendMessage(parent.getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "subCommand"));

            }
            return true;
        }
    }
            .setDefault("operation","setslot")
            .setTabCompletor("operation",()->List.of("setslot"))
            .setTabCompletor("index",()-> IntStream.range(0,54).mapToObj(String::valueOf).toList())
            .register(this);
    private SubCommand blockinfoCommand=new SubCommand("blockinfo",new SimpleCommandArgs(),"")
            .setCommandExecutor(
                    new AbstractMainCommand() {
                        public NetworksMain parent=WorldEditMain.this.parent;
                        @Override
                        public String permissionRequired() {
                            return null;
                        }
                        private SubCommand mainCommand=new SubCommand("blockinfo",new SimpleCommandArgs("operation"),"")
                                .setTabCompletor("operation",()->getSubCommands().stream().map(SubCommand::getName).toList());
                        private SubCommand addCommand=new SubCommand("add",new SimpleCommandArgs("key","value"),""){
                            @Override
                            public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
                                var input=parseInput(var4).getFirstValue();
                                String key=input.nextArg();
                                if(key==null){
                                    var1.sendMessage(parent.getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "key"));
                                    return true;
                                }
                                String value=input.nextArg();
                                if(value==null){
                                    var1.sendMessage(parent.getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "value"));
                                    return true;
                                }
                                worldeditBlockInfoAdd(isPlayer(var1,false), key, value);
                                return true;
                            }
                        }
                                .setTabCompletor("key",()->List.of("key"))
                                .setTabCompletor("value",()->List.of("value"))
                                .register(this);
                        private SubCommand setCommand=new SubCommand("set",new SimpleCommandArgs("key","value"),"")
                                .setCommandExecutor(this.addCommand)
                                .register(this);
                        private SubCommand removeCommand=new SubCommand("remove",new SimpleCommandArgs("key"),""){
                            public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
                                var input=parseInput(var4).getFirstValue();
                                String key=input.nextArg();
                                if(key==null){
                                    var1.sendMessage(parent.getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "value"));
                                }else {
                                    worldeditBlockInfoRemove(isPlayer(var1,false), key);
                                }
                                return true;
                            }
                        }
                                .setTabCompletor("key",()->List.of("key"))
                                .register(this);
                        public void noPermission(CommandSender var1){
                            var1.sendMessage(parent.getErrorMessage(ErrorType.NO_PERMISSION));
                        }
                        void showHelpCommand(CommandSender sender){
                            sender.sendMessage(parent.getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "subCommand"));
                        }
                    }
            )
            .register(this);
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
}
