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
import java.util.stream.IntStream;

@SuppressWarnings({"deprecation", "unused"})
public class NetworksMain extends AbstractMainCommand {

    @Override
    public String permissionRequired() {
        return null;
    }
    public Player isPlayer(CommandSender sender, boolean sendMessage){
        if(sender instanceof Player player){
            return player;
        }else {
            if(sendMessage){
                sendMessage(sender,getErrorMessage(ErrorType.MUST_BE_PLAYER));
            }
            return null;
        }
    }
    @Override
    public void noPermission(CommandSender var1) {
        var1.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
    }
    private SubCommand mainCommand=new SubCommand("networks",new SimpleCommandArgs("_operation"),"")
            .setTabCompletor("_operation",()->getSubCommands().stream().map(SubCommand::getName).toList());

    private SubCommand fillQuantum=new SubCommand("fillquantum",new SimpleCommandArgs("num"),Networks.getLocalizationService().getStringList("messages.commands.example.fillquantum")){
        @Override
        public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
            Player player=isPlayer(var1,true);
            if(player!=null){

                if (!player.hasPermission("networks.admin") && !player.hasPermission("networks.commands.fillquantum")) {
                    player.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                    return true;
                }
                var re=parseInput(var4);
                String amount=re.getFirstValue().nextArg();
                if (amount==null) {
                    player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "amount"));
                    return true;
                }
                try {
                    int amountInt = Integer.parseInt(amount);
                    fillQuantum(player, amountInt);
                } catch (NumberFormatException e) {
                    player.sendMessage(getErrorMessage(ErrorType.INVALID_REQUIRED_ARGUMENT, "amount"));
                }
            }
            return true;
        }
    }
            .setTabCompletor("num",()->List.of("1","64","1919810","1145141919","2147483647"))
            .register(this);
    private SubCommand fixblueprint=new SubCommand("fixblueprint",new SimpleCommandArgs("keyInMeta"),Networks.getLocalizationService().getStringList("messages.commands.example.fixblueprint")){
        @Override
        public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
            Player player=isPlayer(var1,true);
            if(player!=null){
                if (!player.hasPermission("networks.admin") && !player.hasPermission("networks.commands.fixblueprint")) {
                    player.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                    return true;
                }
                var re=parseInput(var4);
                var input=re.getFirstValue();
                String args1=input.nextArg();
                if (args1==null) {
                    player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "keyInMeta"));
                    return true;
                }
                fixBlueprint(player, args1);
            }
            return true;
        }
    }
            .setTabCompletor("keyInMeta",()->List.of("networks-changed"))
            .register(this);
    private SubCommand setquantum=new SubCommand("setquantum",new SimpleCommandArgs("num"),Networks.getLocalizationService().getStringList("messages.commands.example.setquantum")){
        @Override
        public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
            Player player=isPlayer(var1,true);
            if(player!=null){
                if (!player.hasPermission("networks.admin") && !player.hasPermission("networks.commands.setquantum")) {
                    player.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                    return true;
                }
                var re=parseInput(var4);
                var input=re.getFirstValue();
                String args1=input.nextArg();
                if (args1==null) {
                    player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "amount"));
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args1);
                    setQuantum(player, amount);
                } catch (NumberFormatException e) {
                    player.sendMessage(getErrorMessage(ErrorType.INVALID_REQUIRED_ARGUMENT, "amount"));
                }
                return true;
            }
            return true;
        }
    }
            .setTabCompletor("num",()->List.of("1","64","1919810","1145141919","2147483647"))
            .register(this);
    private SubCommand addstorageitem=new SubCommand("addstorageitem",new SimpleCommandArgs("num"),Networks.getLocalizationService().getStringList("messages.commands.example.addstorageitem")){
        @Override
        public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
            Player player=isPlayer(var1,true);
            if(player!=null){
                if (!player.hasPermission("networks.admin") && !player.hasPermission("networks.commands.addstorageitem")) {
                    player.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                    return true;
                }
                var re=parseInput(var4);
                var input=re.getFirstValue();
                String args1=input.nextArg();
                if (args1==null) {
                    player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "amount"));
                    return true;
                }

                try {
                    int amount = Integer.parseInt(args1);
                    addStorageItem(player, amount);
                } catch (NumberFormatException e) {
                    player.sendMessage(getErrorMessage(ErrorType.INVALID_REQUIRED_ARGUMENT, "amount"));
                }
            }
            return true;
        }
    }
            .setTabCompletor("num",()->List.of("1","64","1919810","1145141919","2147483647"))
            .register(this);
    private SubCommand reducestorageitem=new SubCommand("reducestorageitem",new SimpleCommandArgs("num"),Networks.getLocalizationService().getStringList("messages.commands.example.reducestorageitem")){
        @Override
        public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
            Player player=isPlayer(var1,true);
            if(player!=null){
                if (!player.hasPermission("networks.admin") && !player.hasPermission("networks.commands.reducestorageitem")) {
                    player.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                    return true;
                }
                var re=parseInput(var4);
                var input=re.getFirstValue();
                String args1=input.nextArg();
                if (args1==null) {
                    player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "amount"));
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args1);
                    reduceStorageItem(player, amount);
                } catch (NumberFormatException e) {
                    player.sendMessage(getErrorMessage(ErrorType.INVALID_REQUIRED_ARGUMENT, "amount"));
                }
            }
            return true;
        }
    }
            .setTabCompletor("num",()->List.of("1","64","1919810","1145141919","2147483647"))
            .register(this);
    private SubCommand setcontainerid=new SubCommand("setcontainerid",new SimpleCommandArgs("id"),Networks.getLocalizationService().getStringList("messages.commands.example.setcontainerid")){
        public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
            Player player=isPlayer(var1,true);
            if(player!=null){
                if (!player.hasPermission("networks.admin") && !player.hasPermission("networks.commands.setcontainerid")) {
                    player.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                    return true;
                }
                var re=parseInput(var4);
                var input=re.getFirstValue();
                String args1=input.nextArg();
                if (args1==null) {
                    player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "containerId"));
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args1);
                    setContainerId(player, amount);
                } catch (NumberFormatException e) {
                    player.sendMessage(getErrorMessage(ErrorType.INVALID_REQUIRED_ARGUMENT, "containerId"));
                }
            }
            return true;
        }
    }
            .setTabCompletor("num",()->List.of("1","64","1919810","1145141919","2147483647"))
            .register(this);

    private SubCommand helpCommand=new SubCommand("help",new SimpleCommandArgs("mainCommand"),Networks.getLocalizationService().getStringList("messages.commands.example.help")){
        public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
            if (var1.isOp()) {
            help(var1,parseInput(var4).getFirstValue().nextArg());
            }else {
                var1.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
            }
            return true;
        }
    }
            .setTabCompletor("mainCommand",()->getSubCommands().stream().map(SubCommand::getName).toList())
            .register( this);
    private SubCommand worldedit=new SubCommand("worldedit", new SimpleCommandArgs(),Networks.getLocalizationService().getStringList("messages.commands.example.worldedit"))
            .setCommandExecutor(new WorldEditMain(this  ))
            .register(this);
    private SubCommand updateItem=new SubCommand("updateitem",new SimpleCommandArgs(),Networks.getLocalizationService().getStringList("messages.commands.example.updateitem")){
        public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
            Player player=isPlayer(var1,true);
            if(player!=null){
                if (!player.hasPermission("networks.admin") && !player.hasPermission("networks.commands.updateitem")) {
                    player.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                    return true;
                }

                updateItem(player);
            }
            return true;
        }
    }
            .register(this);
    private SubCommand getstorageitem=new SubCommand("getstorageitem",new SimpleCommandArgs("slot"),Networks.getLocalizationService().getStringList("messages.commands.example.getstorageitem")){
        public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
            Player player=isPlayer(var1,true);
            if(player!=null){
                if (!player.hasPermission("networks.admin") && !player.hasPermission("networks.commands.getstorageitem")) {
                    player.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                    return true;
                }
                String args1=parseInput(var4).getFirstValue().nextArg();
                if (args1==null) {
                    player.sendMessage(getErrorMessage(ErrorType.MISSING_REQUIRED_ARGUMENT, "slot"));
                    return true;
                }
                try {
                    int slot = Integer.parseInt(args1);
                    getStorageItem(player, slot);
                } catch (NumberFormatException e) {
                    player.sendMessage(getErrorMessage(ErrorType.INVALID_REQUIRED_ARGUMENT, "slot"));
                }
            }
            return true;
        }
    }
            .setTabCompletor("slot",()-> IntStream.range(0,35).mapToObj(String::valueOf).toList())
            .register(this);
    private SubCommand viewlog=new SubCommand("viewlog",new SimpleCommandArgs(),Networks.getLocalizationService().getStringList("messages.commands.example.viewlog")){
        public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
            Player player=isPlayer(var1,true);
            if(player!=null){
                if (!player.hasPermission("networks.admin") && !player.hasPermission("networks.commands.viewlog")) {
                    player.sendMessage(getErrorMessage(ErrorType.NO_PERMISSION));
                    return true;
                }

                viewLog(player);
            }
            return true;
        }
    }
            .register(this);
    private SubCommand experimental=new SubCommand("experimental",new SimpleCommandArgs(),"禁止使用该指令!")
            .setCommandExecutor(ExperimentalFeatureManager.getInstance())
            .register(this);
    void showHelpCommand(CommandSender sender){
        for (String message : Networks.getLocalizationService().getStringList("messages.commands.help")) {
            sender.sendMessage(message);
        }
    }
    public void help(CommandSender sender, String mainCommand) {
        if (mainCommand == null) {
            showHelpCommand(sender);
            return;
        }
        else {
            SubCommand command = this.getSubCommand(mainCommand);
            if (command != null) {
                for (String message : command.getHelp()) {
                    sender.sendMessage(message);
                }
            } else {
                for (String message : Networks.getLocalizationService().getStringList("messages.commands.example.unknown-command")) {
                    sender.sendMessage(message);
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
    public static String locationToString(Location l) {
        if (l == null) {
            return Networks.getLocalizationService().getString("icons.drawer.location_error.unknown");
        }
        if (l.getWorld() == null) {
            return Networks.getLocalizationService().getString("icons.drawer.location_error.unknown");
        }
        return l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
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

    public static String getErrorMessage(ErrorType errorType) {
        return getErrorMessage(errorType, null);
    }

    public static String getErrorMessage(ErrorType errorType, String argument) {
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
