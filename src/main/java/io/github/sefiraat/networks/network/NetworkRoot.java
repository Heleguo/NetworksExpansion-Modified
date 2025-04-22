package io.github.sefiraat.networks.network;

import com.balugaq.netex.api.data.ItemContainer;
import com.balugaq.netex.api.data.StorageUnitData;
import com.balugaq.netex.api.enums.StorageType;
import com.balugaq.netex.api.events.NetworkRootLocateStorageEvent;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.balugaq.netex.utils.NetworksVersionedParticle;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.machines.AbstractAutoCrafter;
import com.ytdd9527.networksexpansion.implementation.machines.networks.advanced.AdvancedGreedyBlock;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import io.github.mooy1.infinityexpansion.items.storage.StorageCache;
import io.github.mooy1.infinityexpansion.items.storage.StorageUnit;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.managers.ExperimentalFeatureManager;
import io.github.sefiraat.networks.network.barrel.FluffyBarrel;
import io.github.sefiraat.networks.network.barrel.InfinityBarrel;
import io.github.sefiraat.networks.network.barrel.NetworkStorage;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkCell;
import io.github.sefiraat.networks.slimefun.network.NetworkDirectional;
import io.github.sefiraat.networks.slimefun.network.NetworkGreedyBlock;
import io.github.sefiraat.networks.slimefun.network.NetworkPowerNode;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.ncbpfluffybear.fluffymachines.items.Barrel;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import me.matl114.matlib.algorithms.dataStructures.struct.Pair;
import me.matl114.matlib.common.lang.annotations.NotCompleted;
import me.matl114.matlib.common.lang.annotations.Note;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class NetworkRoot extends NetworkNode {
    @Getter
    private final long CREATED_TIME = System.currentTimeMillis();
    @Getter
    private final Set<Location> nodeLocations = new HashSet<>();
    protected final int[] CELL_AVAILABLE_SLOTS = NetworkCell.SLOTS.stream().mapToInt(i -> i).toArray();
    protected final int[] GREEDY_BLOCK_AVAILABLE_SLOTS = new int[]{NetworkGreedyBlock.INPUT_SLOT};
    final int[] ADVANCED_GREEDY_BLOCK_AVAILABLE_SLOTS = AdvancedGreedyBlock.INPUT_SLOTS;
    @Getter
    private final Set<Location> bridges = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> monitors = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> importers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> exporters = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> grids = ConcurrentHashMap.newKeySet();
    public Set<Location> getCells(){
        return cells.keySet();
    }
    private final Map<Location, SlimefunBlockData> cells = new ConcurrentHashMap<>();
    @Getter
    private final Set<Location> grabbers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> pushers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> purgers = ConcurrentHashMap.newKeySet();
    public Set<Location> getCrafters(){
        return crafters.keySet();
    }
    private final Map<Location, SlimefunBlockData> crafters = new ConcurrentHashMap<>();
    @Getter
    private final Set<Location> powerNodes = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> powerDisplays = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> encoders = ConcurrentHashMap.newKeySet();

    public Set<Location> getGreedyBlocks(){
        return greedyBlocks.keySet();
    }
    private final Map<Location, SlimefunBlockData> greedyBlocks = new ConcurrentHashMap<>();
    @Getter
    private final Set<Location> cutters = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> pasters = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> vacuums = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> wirelessTransmitters = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> wirelessReceivers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> powerOutlets = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> transferPushers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> transferGrabbers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> transfers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> advancedImporters = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> advancedExporters = ConcurrentHashMap.newKeySet();
    public Set<Location> getAdvancedGreedyBlocks(){
        return advancedGreedyBlocks.keySet();
    }
    @Getter
    private final Map<Location, SlimefunBlockData> advancedGreedyBlocks = new ConcurrentHashMap<>();
    @Getter
    private final Set<Location> advancedPurgers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> lineTransferVanillaPushers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> lineTransferVanillaGrabbers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> inputOnlyMonitors = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> outputOnlyMonitors = ConcurrentHashMap.newKeySet();
    @Getter
    private Location controller = null;
    @Deprecated
    private boolean progressing = false;
    @Getter
    private int maxNodes;
    @Getter
    private boolean isOverburdened = false;
    @Deprecated
    public Set<BarrelIdentity> getBarrels(){
        getMaterial2Barrels();
        return barrels==null?Set.of():barrels;
    }
    private Set<BarrelIdentity> barrels = null;
    public Set<BarrelIdentity> getInputAbleBarrels(){
        getMaterial2InputAbleBarrels();
        return inputAbleBarrels==null?Set.of():inputAbleBarrels;
    }
    private Set<BarrelIdentity> inputAbleBarrels = null;
    public Set<BarrelIdentity> getOutputAbleBarrels(){
        getMaterial2OutputAbleBarrels();
        return outputAbleBarrels==null?Set.of():outputAbleBarrels;
    }
    private Set<BarrelIdentity> outputAbleBarrels = null;

    private Map< Material,Map<Location, BarrelIdentity>> material2Barrels = null;
    private Map< Material,Map<Location, BarrelIdentity>> material2InputAbleBarrels = null;
    private Map< Material,Map<Location, BarrelIdentity>> material2OutputAbleBarrels = null;
    @Deprecated
    private Map<Location, StorageUnitData> cargoStorageUnitDatas = null;

    private Map<Location, StorageUnitData> inputAbleCargoStorageUnitDatas = null;

    private Map<Location, StorageUnitData> outputAbleCargoStorageUnitDatas = null;
    @Getter
    @Setter
    private boolean ready = false;
    private static final ConcurrentHashMap<Location, AtomicInteger> rootCounters = new ConcurrentHashMap<>();
    @Getter
    private int locUniqueId;



    @Getter
    protected long rootPower = 0;

    @Getter
    private boolean displayParticles = false;
    public static NetworkRoot newInstance(Location location,NodeType type,int maxNodes){
        if(ExperimentalFeatureManager.getInstance().isEnableAsyncSafeNetworkRoot()){
            return new NetworkRootPlus(location,type,maxNodes);
        }else {
            return new NetworkRoot(location,type,maxNodes);
        }
    }
    protected NetworkRoot getRootUpdateInternal() {
        //for root
        return this.root;
    }
    @Nonnull
    public NetworkRoot getRoot(){
        return this;
    }
    protected NetworkRoot(@Nonnull Location location, @Nonnull NodeType type, int maxNodes) {
        super(location, type);
        this.maxNodes = maxNodes;
        this.root = this;

        registerNode(location, type);
        locUniqueId = rootCounters.computeIfAbsent(location, k -> new AtomicInteger()).incrementAndGet();
    }

    public void registerNode(@Nonnull Location location, @Nonnull NodeType type) {
        nodeLocations.add(location);
        switch (type) {
            case CONTROLLER -> this.controller = location;
            case BRIDGE -> bridges.add(location);
            case STORAGE_MONITOR -> monitors.add(location);
            case IMPORT -> importers.add(location);
            case EXPORT -> exporters.add(location);
            case GRID -> grids.add(location);
            case CELL -> {
                /*
                 * Fix https://github.com/Sefiraat/Networks/issues/211
                 */
                var blockMenu = StorageCacheUtils.getBlock(location);
                if (blockMenu == null || blockMenu.getBlockMenu() == null) {
                    return;
                }
                if (Arrays.equals(blockMenu.getBlockMenu().getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW), CELL_AVAILABLE_SLOTS)) {
                    cells.put(location, blockMenu);
                }
            }
            case GRABBER -> grabbers.add(location);
            case PUSHER -> pushers.add(location);
            case PURGER -> purgers.add(location);
            case CRAFTER -> crafters.put(location,StorageCacheUtils.getBlock(location));
            case POWER_NODE -> powerNodes.add(location);
            case POWER_DISPLAY -> powerDisplays.add(location);
            case ENCODER -> encoders.add(location);
            case GREEDY_BLOCK -> {
                /*
                 * Fix https://github.com/Sefiraat/Networks/issues/211
                 */
                var blockMenu = StorageCacheUtils.getBlock(location);
                if (blockMenu == null || blockMenu.getBlockMenu() == null) {
                    return;
                }
                if (Arrays.equals(blockMenu.getBlockMenu().getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW), GREEDY_BLOCK_AVAILABLE_SLOTS)) {
                    greedyBlocks.put(location, blockMenu);
                }
            }
            case CUTTER -> cutters.add(location);
            case PASTER -> pasters.add(location);
            case VACUUM -> vacuums.add(location);
            case WIRELESS_TRANSMITTER -> wirelessTransmitters.add(location);
            case WIRELESS_RECEIVER -> wirelessReceivers.add(location);
            case POWER_OUTLET -> powerOutlets.add(location);
            // from networks expansion
            case ADVANCED_IMPORT -> advancedImporters.add(location);
            case ADVANCED_EXPORT -> advancedExporters.add(location);
            case ADVANCED_GREEDY_BLOCK -> {
                /*
                 * Fix https://github.com/Sefiraat/Networks/issues/211
                 */
                var blockMenu = StorageCacheUtils.getBlock(location);
                if (blockMenu == null || blockMenu.getBlockMenu() == null) {
                    return;
                }
                if (Arrays.equals(blockMenu.getBlockMenu().getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW), ADVANCED_GREEDY_BLOCK_AVAILABLE_SLOTS)) {
                    advancedGreedyBlocks.put(location, blockMenu);
                }
            }
            case ADVANCED_PURGER -> advancedPurgers.add(location);
            case TRANSFER -> transfers.add(location);
            case TRANSFER_PUSHER -> transferPushers.add(location);
            case TRANSFER_GRABBER -> transferGrabbers.add(location);
            case LINE_TRANSFER_VANILLA_GRABBER -> lineTransferVanillaGrabbers.add(location);
            case LINE_TRANSFER_VANILLA_PUSHER -> lineTransferVanillaPushers.add(location);
            case INPUT_ONLY_MONITOR -> inputOnlyMonitors.add(location);
            case OUTPUT_ONLY_MONITOR -> outputOnlyMonitors.add(location);
        }
    }

    public int getNodeCount() {
        return this.nodeLocations.size();
    }

    public boolean isOverburdened() {
        return isOverburdened;
    }

    public void setOverburdened(boolean overburdened) {
        if (overburdened && !isOverburdened) {
            final Location loc = this.nodePosition.clone();
            for (int x = 0; x <= 1; x++) {
                for (int y = 0; y <= 1; y++) {
                    for (int z = 0; z <= 1; z++) {
                        loc.getWorld().spawnParticle(NetworksVersionedParticle.EXPLOSION, loc.clone().add(x, y, z), 0);
                    }
                }
            }
        }
        this.isOverburdened = overburdened;
    }

    @Nonnull
    public Map<ItemStack, Long> getAllNetworkItemsLongType() {
        final Map<ItemStack, Long> itemStacks = new HashMap<>();

        // Barrels
        for(var barrels: getMaterial2OutputAbleBarrels().values()){
            for (BarrelIdentity barrelIdentity : barrels.values()) {
                itemStacks.compute(barrelIdentity.getItemStack(),(i,num)->num==null?(long)barrelIdentity.getAmount():(long)num+barrelIdentity.getAmount() );
    //            final Long currentAmount = itemStacks.get(barrelIdentity.getItemStack());
    //            final long newAmount;
    //            if (currentAmount == null) {
    //                newAmount = barrelIdentity.getAmount();
    //            } else {
    //                long newLong = currentAmount + barrelIdentity.getAmount();
    //                if (newLong < 0) {
    //                    newAmount = 0;
    //                } else {
    //                    newAmount = currentAmount + barrelIdentity.getAmount();
    //                }
    //            }
    //            itemStacks.put(barrelIdentity.getItemStack(), newAmount);
            }
        }

        // Cargo storage units
        Map<Location, StorageUnitData> cacheMap = getOutputAbleCargoStorageUnitDatas0();
        for (StorageUnitData cache : cacheMap.values()) {
            for (ItemContainer itemContainer : cache.getStoredItems()) {
                itemStacks.compute(itemContainer.getSample(),(i,num)->num==null?(long)itemContainer.getAmount():(long)num+itemContainer.getAmount());
            }
        }

        for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    continue;
                }
                final ItemStack clone = StackUtils.getAsQuantity(itemStack, 1);
                itemStacks.compute(clone,(i,num)->num==null?(long)itemStack.getAmount():(long)num+itemStack.getAmount());
            }
        }

        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            final ItemStack itemStack = blockMenu.getItemInSlot(slots[0]);
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            final ItemStack clone = StackUtils.getAsQuantity(itemStack, 1);
            itemStacks.compute(clone,(i,num)->num==null?(long)itemStack.getAmount():(long)num+itemStack.getAmount());
        }

        for (BlockMenu blockMenu : getCrafterOutputs()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    continue;
                }
                final ItemStack clone = StackUtils.getAsQuantity(itemStack, 1);
                itemStacks.compute(clone,(i,num)->num==null?(long)itemStack.getAmount():(long)num+itemStack.getAmount());
            }
        }

        for (BlockMenu blockMenu : getCellMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    final ItemStack clone = StackUtils.getAsQuantity(itemStack, 1);
                    itemStacks.compute(clone,(i,num)->num==null?(long)itemStack.getAmount():(long)num+itemStack.getAmount());
                }
            }
        }
        return itemStacks;
    }

    @Deprecated
    public Map<ItemStack, Integer> getAllNetworkItems() {
        final Map<ItemStack, Long> itemStacks = getAllNetworkItemsLongType();
        Map<ItemStack,Integer> itemStackMap = itemStacks.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,entry->{
            Long num = entry.getValue();
            if(num>Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }else if(num<0){
                return 0;
            }else {
                return Math.toIntExact(num);
            }
        }));
        return itemStackMap;
        // Barrels

    }

    @Deprecated
    @Nonnull
    protected synchronized Map<Material,Map<Location, BarrelIdentity>> getMaterial2Barrels() {
        if(!ready){
            handleAsync();
            return Map.of();
        }
        if (this.material2Barrels != null) {
            return this.material2Barrels;
        }

        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Map<Material,Map<Location, BarrelIdentity>> barrelSet =new Reference2ReferenceOpenHashMap<>();

        for (Location cellLocation : this.monitors) {
            final BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);

            if (face == null) {
                continue;
            }

            final Location testLocation = cellLocation.clone().add(face.getDirection());

            if (addedLocations.contains(testLocation)) {
                continue;
            } else {
                addedLocations.add(testLocation);
            }

            final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(testLocation);

            if (Networks.getSupportedPluginManager()
                    .isInfinityExpansion() && slimefunItem instanceof StorageUnit unit) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final InfinityBarrel infinityBarrel = getInfinityBarrel(menu, unit);
                if (infinityBarrel != null) {
                    barrelSet.computeIfAbsent(infinityBarrel.getItemType(),(i)->new ConcurrentHashMap<>()).put(cellLocation.clone(), infinityBarrel);
                }
                continue;
            } else if (Networks.getSupportedPluginManager().isFluffyMachines() && slimefunItem instanceof Barrel barrel) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final FluffyBarrel fluffyBarrel = getFluffyBarrel(menu, barrel);
                if (fluffyBarrel != null) {
                    barrelSet.computeIfAbsent(fluffyBarrel.getItemType(),(i)->new ConcurrentHashMap<>()).put(cellLocation.clone(), fluffyBarrel);
                }
                continue;
            } else if (slimefunItem instanceof NetworkQuantumStorage) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final NetworkStorage storage = getNetworkStorage(menu);
                if (storage != null) {
                    barrelSet.computeIfAbsent(storage.getItemType(),(i)->new ConcurrentHashMap<>()).put(cellLocation.clone(), storage);
                }
            }
        }

        this.material2Barrels = barrelSet;
        Set<BarrelIdentity> barrels = ConcurrentHashMap.newKeySet();
        barrelSet.forEach((key,value)->barrels.addAll(value.values()));
        this.barrels = barrels;
        NetworkRootLocateStorageEvent event = new NetworkRootLocateStorageEvent(this, StorageType.BARREL, true, true, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return barrelSet;
    }
    public synchronized Map<StorageUnitData, Location> getCargoStorageUnitDatas(){
        Map map = getCargoStorageUnitDatas0();
        return map.isEmpty()?map: ((BidiMap)map).inverseBidiMap();
    }

    @Nonnull
    protected synchronized Map<Location, StorageUnitData> getCargoStorageUnitDatas0() {
        if(!ready){
            handleAsync();
            return Map.of();
        }
        if (this.cargoStorageUnitDatas != null) {
            return this.cargoStorageUnitDatas;
        }

        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Map<Location, StorageUnitData> dataSet = new DualHashBidiMap<>();

        for (Location cellLocation : this.monitors) {
            final BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);

            if (face == null) {
                continue;
            }

            final Location testLocation = cellLocation.clone().add(face.getDirection());

            if (addedLocations.contains(testLocation)) {
                continue;
            } else {
                addedLocations.add(testLocation);
            }

            final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(testLocation);

            if (slimefunItem instanceof NetworksDrawer) {
                final StorageUnitData data = getCargoStorageUnitData(testLocation);
                if (data != null) {
                    dataSet.put(testLocation, data);
                }
            }
        }

        this.cargoStorageUnitDatas = dataSet;
        NetworkRootLocateStorageEvent event = new NetworkRootLocateStorageEvent(this, StorageType.DRAWER, true, true, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return dataSet;
    }

    @Nullable
    private InfinityBarrel getInfinityBarrel(@Nonnull BlockMenu blockMenu, @Nonnull StorageUnit storageUnit) {
        final ItemStack itemStack = blockMenu.getItemInSlot(16);
        final var data = StorageCacheUtils.getBlock(blockMenu.getLocation());
        if (data == null) {
            return null;
        }
        final String storedString = data.getData("stored");

        if (storedString == null) {
            return null;
        }

        final int storedInt = Integer.parseInt(storedString);

        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }


        final StorageCache cache = storageUnit.getCache(blockMenu.getLocation());

        if (cache == null) {
            return null;
        }

        final ItemStack clone = itemStack.clone();
        clone.setAmount(1);

        return new InfinityBarrel(
                blockMenu.getLocation(),
                clone,
                storedInt + itemStack.getAmount(),
                cache
        );
    }

    @Nullable
    private synchronized FluffyBarrel getFluffyBarrel(@Nonnull BlockMenu blockMenu, @Nonnull Barrel barrel) {
        Block block = blockMenu.getBlock();
        ItemStack itemStack;
        try {
            itemStack = barrel.getStoredItem(block);
        } catch (NullPointerException ignored) {
            return null;
        }

        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }

        final ItemStack clone = itemStack.clone();

        int stored = barrel.getStored(block);

        if (stored <= 0) {
            return null;
        }
        int limit = barrel.getCapacity(block);
        boolean voidExcess = Boolean.parseBoolean(StorageCacheUtils.getData(blockMenu.getLocation(), "trash"));

        return new FluffyBarrel(
                blockMenu.getLocation(),
                clone,
                stored,
                limit,
                voidExcess
        );
    }

    @Nullable
    private NetworkStorage getNetworkStorage(@Nonnull BlockMenu blockMenu) {

        final QuantumCache cache = NetworkQuantumStorage.getCaches().get(blockMenu.getLocation());

        if (cache == null || cache.getItemStack() == null) {
            return null;
        }

        final ItemStack output = blockMenu.getItemInSlot(NetworkQuantumStorage.OUTPUT_SLOT);
        final ItemStack itemStack = cache.getItemStack();
        long storedInt = cache.getAmount();
        if (output != null && output.getType() != Material.AIR && StackUtils.itemsMatch(cache, output)) {
            storedInt = storedInt + output.getAmount();
        }

        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }

        final ItemStack clone = itemStack.clone();
        clone.setAmount(1);

        return new NetworkStorage(
                blockMenu.getLocation(),
                clone,
                cache,
                storedInt
        );
    }

    @Nullable
    private StorageUnitData getCargoStorageUnitData(@Nonnull BlockMenu blockMenu) {
        return NetworksDrawer.getStorageData(blockMenu.getLocation());
    }

    @Nullable
    private StorageUnitData getCargoStorageUnitData(@Nonnull Location location) {
        return NetworksDrawer.getStorageData(location);
    }

    @Nonnull
    public Set<BlockMenu> getCellMenus() {
        return this.cells.values()
            .stream()
            .filter(d->!d.isPendingRemove())
            .map(SlimefunBlockData::getBlockMenu)
            .collect(Collectors.toUnmodifiableSet());
//        final Set<BlockMenu> menus = new HashSet<>();
//        for (Location cellLocation : this.cells) {
//            BlockMenu menu = StorageCacheUtils.getMenu(cellLocation);
//            if (menu != null) {
//                menus.add(menu);
//            }
//        }
//        return menus;
    }

    @Nonnull
    public Set<BlockMenu> getCrafterOutputs() {
        return this.crafters.values()
            .stream()
            .filter(d-> d!= null &&  !d.isPendingRemove())
            .map(SlimefunBlockData::getBlockMenu)
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableSet());
//        final Set<BlockMenu> menus = new HashSet<>();
//        for (Location location : this.crafters) {
//            BlockMenu menu = StorageCacheUtils.getMenu(location);
//            if (menu != null) {
//                menus.add(menu);
//            }
//        }
//        return menus;
    }

    @Nonnull
    public Set<BlockMenu> getGreedyBlockMenus() {
        return this.greedyBlocks.values()
            .stream()
            .filter(d->!d.isPendingRemove())
            .map(SlimefunBlockData::getBlockMenu)
            .collect(Collectors.toUnmodifiableSet());
//        final Set<BlockMenu> menus = new HashSet<>();
//        for (var data : this.greedyBlocks.values()) {
//            if(!data.isPendingRemove()){
//                menus.add(data.getBlockMenu());
//            }
////            BlockMenu menu = StorageCacheUtils.getMenu(location);
////            if (menu != null) {
////                menus.add(menu);
////            }
//        }
//        return menus;
    }

    @Nonnull
    public Set<BlockMenu> getAdvancedGreedyBlockMenus() {
        return this.advancedGreedyBlocks.values()
            .stream()
            .filter(d->!d.isPendingRemove())
            .map(SlimefunBlockData::getBlockMenu)
            .collect(Collectors.toUnmodifiableSet());
//        final Set<BlockMenu> menus = new HashSet<>();
//        for (var data : this.advancedGreedyBlocks.values()) {
//            if(!data.isPendingRemove()){
//                menus.add(data.getBlockMenu());
//            }
////            BlockMenu menu = StorageCacheUtils.getMenu(location);
////            if (menu != null) {
////                menus.add(menu);
////            }
//        }
//        return menus;
    }
    protected static final ItemStack PREFETCH_INVALID = new ItemStack(Material.AIR);
    protected final ItemStack getFromBarrels(ItemRequest request, ItemStack stackToReturn){
        var barrels= getMaterial2OutputAbleBarrels().get(request.getItemType());
        ItemStack stackToReturn0 ;
        if(barrels != null) {
            for (BarrelIdentity barrelIdentity :barrels.values()) {
                stackToReturn0 = fetchFromBarrels(request, barrelIdentity, stackToReturn, false);
                if(stackToReturn0 != null){
                    stackToReturn = stackToReturn0;
                }
            }
        }
        return stackToReturn;
    }
    @Note("should check ItemStack return == INVALID, if so, abandon the prefetch info")
    protected final ItemStack prefetchFromBarrels(ItemRequest request, Location loc, ItemStack stackToReturn, boolean bypassCheck){
        var barrels= getMaterial2OutputAbleBarrels().get(request.getItemType());
        if(barrels != null) {
            var barrel = barrels.get(loc);
            if(barrel == null || barrel.getItemStack() == null || !( bypassCheck || StackUtils.itemsMatch(request, barrel))){
                return PREFETCH_INVALID;
            }else {
                //we check the barrel at above, so check is no longer needed
                return fetchFromBarrels(request, barrel, stackToReturn, true);
            }
        }
        return PREFETCH_INVALID;
    }
    @Note("the core logic of fetching item from a barrel, return a null means fetch failure, but fetch failure can also return stackToReturn as a default choice")
    protected ItemStack fetchFromBarrels(ItemRequest request, BarrelIdentity barrel, ItemStack stackToReturn, boolean bypassCheck){
        if (barrel.getItemStack() == null || !( bypassCheck || StackUtils.itemsMatch(request, barrel))) {
            return null;
        }
        boolean infinity = barrel instanceof InfinityBarrel;
        final ItemStack fetched = barrel.requestItem(request);
        if (fetched == null || fetched.getType() == Material.AIR || (infinity && fetched.getAmount() == 1)) {
            return stackToReturn;
        }
        // Stack is null, so we can fill it here
        if (stackToReturn == null) {
            stackToReturn = fetched.clone();
            stackToReturn.setAmount(0);
        }
        final int preserveAmount = infinity ? fetched.getAmount() - 1 : fetched.getAmount();

        if (request.getAmount() <= preserveAmount) {
            stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
            fetched.setAmount(fetched.getAmount() - request.getAmount());
            request.receiveAll();
        } else {
            stackToReturn.setAmount(stackToReturn.getAmount() + preserveAmount);
            request.receiveAmount(preserveAmount);
            fetched.setAmount(fetched.getAmount() - preserveAmount);
        }
        return stackToReturn;
    }

    @Note("should check ItemStack return == INVALID, if so, abandon the prefetch info")
    protected ItemStack getFromStorages(ItemRequest request, ItemStack stackToReturn){
        for (StorageUnitData cache : getOutputAbleCargoStorageUnitDatas0().values()) {
            ItemStack take = cache.requestItem(request);
            if (take != null) {
                if (stackToReturn == null) {
                    stackToReturn = take.clone();
                } else {
                    stackToReturn.setAmount(stackToReturn.getAmount() + take.getAmount());
                }
                request.receiveAmount(stackToReturn.getAmount());

                if (request.getAmount() <= 0) {
                    break;
                }
            }
        }
        return stackToReturn;
    }
    protected ItemStack prefetchFromStorages(ItemRequest request, Location loc, int index, ItemStack stackToReturn, boolean bypassCheck){
        StorageUnitData cache = getOutputAbleCargoStorageUnitDatas0().get(loc);
        if(cache != null){
            var result = cache.requestViaIndex(request,index, bypassCheck);
            if(result.getB() == null){
                return PREFETCH_INVALID;
            }else {
                ItemStack take = result.getA();
                if (take != null) {
                    if (stackToReturn == null) {
                        stackToReturn = take.clone();
                    } else {
                        stackToReturn.setAmount(stackToReturn.getAmount() + take.getAmount());
                    }
                    request.receiveAmount(stackToReturn.getAmount());

                }
                return stackToReturn;
            }
        }
        return PREFETCH_INVALID;
    }
    protected final ItemStack getFromCrafters(ItemRequest request, ItemStack stackToReturn){
        for (BlockMenu blockMenu : getCrafterOutputs()) {
            stackToReturn = getFromBlockMenu(request, stackToReturn, blockMenu);
            if(request.getAmount() <= 0){
                return stackToReturn;
            }
        }
        return stackToReturn;
    }
    @NotCompleted
    protected ItemStack prefetchFromStorages(ItemRequest request, Location loc, ItemStack stackToReturn){
        return null;
    }

    protected final ItemStack getFromGreedyBlocks(ItemRequest request, ItemStack stackToReturn){
        for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {
            stackToReturn = getFromBlockMenu(request, stackToReturn, blockMenu);
            if(request.getAmount() <= 0){
                return stackToReturn;
            }
        }
        // Greedy Blocks
        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            stackToReturn =  getFromBlockMenu(request, stackToReturn, blockMenu);
            if(request.getAmount() <= 0){
                return stackToReturn;
            }
        }
        return stackToReturn;
    }
    protected final ItemStack getFromShit(ItemRequest request, ItemStack stackToReturn){
        for (BlockMenu blockMenu : getCellMenus()) {
            stackToReturn = getFromBlockMenu(request, stackToReturn, blockMenu);
            if(request.getAmount() <= 0){
                return stackToReturn;
            }
        }
        return stackToReturn;
    }

    protected ItemStack getFromBlockMenu(ItemRequest request, ItemStack stackToReturn, BlockMenu blockMenu){
        int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
        for (int slot : slots) {
            final ItemStack itemStack = blockMenu.getItemInSlot(slot);
            if (itemStack == null
                || itemStack.getType() == Material.AIR
                || !StackUtils.itemsMatch(request, itemStack)
            ) {
                continue;
            }
            // If the return stack is null, we need to set it up
            if (stackToReturn == null) {
                stackToReturn = itemStack.clone();
                stackToReturn.setAmount(0);
            }

            if (request.getAmount() <= itemStack.getAmount()) {
                // We can't take more than this stack. Level to request amount, remove items and then return
                stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                request.receiveAll();
                return stackToReturn;
            } else {
                // We can take more than what is here, consume before trying to take more
                stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                request.receiveAmount(itemStack.getAmount());
                itemStack.setAmount(0);
            }
        }
        return stackToReturn;
    }


    public final ItemStack getItemStack(@Nonnull ItemRequest request){
        return getItemStack0(request, null,0);
    }
    @Nullable
    protected final ItemStack getItemStack0(@Nonnull ItemRequest request, ItemStack stackToReturn, int entryPoint) {
        //return stackToReturn, to avoid passing stackToReturn notnull and return null
        if (request.getAmount() <= 0) {
            return stackToReturn;
        }
        // Barrels first
        switch (entryPoint){
            case 0:
                stackToReturn = getFromBarrels(request, stackToReturn);
            case 1:
                if(request.getAmount() <= 0){
                    return stackToReturn;
                }
                stackToReturn = getFromStorages(request, stackToReturn);
            default:
                if(request.getAmount() <= 0){
                    return stackToReturn;
                }
                stackToReturn = getFromGreedyBlocks(request, stackToReturn);
                // Cells
                if(request.getAmount() <= 0){
                    return stackToReturn;
                }
                stackToReturn = getFromCrafters(request, stackToReturn);
                if(request.getAmount() <= 0){
                    return stackToReturn;
                }
                stackToReturn = getFromShit(request, stackToReturn);
        }
        if (stackToReturn == null || stackToReturn.getAmount() == 0) {
            return null;
        }
        return stackToReturn;
    }
    protected Pair<StorageIndex, ItemStack> prefetchInternal(ItemRequest request){
        if(request.getItemStack() == null){
            return Pair.of(null, null);
        }
        var barrels= getMaterial2OutputAbleBarrels().get(request.getItemType());
        ItemStack stackToReturn = null;
        ItemStack stackToReturn0;
        if(barrels != null) {
            for (var barrel: barrels.entrySet()){
                if(StackUtils.itemsMatch(request, barrel.getValue())){
                    //return Pair.of( new LocationIndex(barrel.getKey().clone(),StorageSource.QUANTUM), ;
                     stackToReturn0 = prefetchFromBarrels(request, barrel.getKey(), stackToReturn, true);
                    //in barrels, we should prefer barrel with more item, only when satisfy request can we return , otherwise this will perform as simply goes through the whole cache
                    if(stackToReturn0 == PREFETCH_INVALID){
                        continue;
                    }else {
                        stackToReturn = stackToReturn0;
                    }
                    if(request.getAmount() <=0)
                        return Pair.of(new LocationIndex(barrel.getKey().clone(), StorageSource.QUANTUM), stackToReturn);
                }
            }
        }
        var storageUnits = getOutputAbleCargoStorageUnitDatas0();
        for (var units: storageUnits.entrySet()){
            Integer internalIndex = units.getValue().getPrefetchInfo(request);
            if(internalIndex != null){
                stackToReturn0 = prefetchFromStorages(request,units.getKey(), internalIndex, stackToReturn, true);
                if(stackToReturn0 == PREFETCH_INVALID){
                    continue;
                }else {
                    stackToReturn = stackToReturn0;
                }
                return Pair.of(new StorageUnitDataIndex(units.getKey(), internalIndex), stackToReturn);
            }
        }
        return Pair.of(null, stackToReturn);
    }

    //will not change the request's amount
    //todo need cache
    public boolean contains(@Nonnull ItemRequest request) {

        long found = 0;
        int requestAmount=request.getAmount();
        // Barrels
        var barrels= getMaterial2OutputAbleBarrels().get(request.getItemType());
        if(barrels!=null){
            for (BarrelIdentity barrelIdentity : barrels.values()) {
                final ItemStack itemStack = barrelIdentity.getItemStack();

                if (itemStack == null || !StackUtils.itemsMatch(request, barrelIdentity)) {
                    continue;
                }

                if (barrelIdentity instanceof InfinityBarrel) {
                    if (itemStack.getMaxStackSize() > 1) {
                        found += barrelIdentity.getAmount() - 2;
                    }
                } else {
                    found += barrelIdentity.getAmount();
                }

                // Escape if found all we need
                if (found >= requestAmount) {
                    return true;
                }
            }
        }

        // Crafters
        for (BlockMenu blockMenu : getCrafterOutputs()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null
                        || itemStack.getType() == Material.AIR
                        || !StackUtils.itemsMatch(request, itemStack)
                ) {
                    continue;
                }

                found += itemStack.getAmount();

                // Escape if found all we need
                if (found >= requestAmount) {
                    return true;
                }
            }
        }

        Map<Location, StorageUnitData> cacheMap = getOutputAbleCargoStorageUnitDatas0();
        for (StorageUnitData cache : cacheMap.values()) {
            final List<ItemContainer> storedItems = cache.getStoredItems();
            for (ItemContainer itemContainer : storedItems) {
                final ItemStack itemStack = itemContainer.getSample();
                if (itemStack == null
                        || itemStack.getType() == Material.AIR
                        || !StackUtils.itemsMatch(request, itemStack)
                ) {
                    continue;
                }

                int amount = itemContainer.getAmount();
                found += amount;


                // Escape if found all we need
                if (found >= requestAmount) {
                    return true;
                }
            }
        }

        for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null
                        || itemStack.getType() == Material.AIR
                        || !StackUtils.itemsMatch(request, itemStack)
                ) {
                    continue;
                }

                found += itemStack.getAmount();

                // Escape if found all we need
                if (found >= requestAmount) {
                    return true;
                }
            }
        }

        // Greedy Blocks
        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            final ItemStack itemStack = blockMenu.getItemInSlot(slots[0]);
            if (itemStack == null
                    || itemStack.getType() == Material.AIR
                    || !StackUtils.itemsMatch(request, itemStack)
            ) {
                continue;
            }

            found += itemStack.getAmount();

            // Escape if found all we need
            if (found >= requestAmount) {
                return true;
            }
        }

        // Cells
        for (BlockMenu blockMenu : getCellMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null
                        || itemStack.getType() == Material.AIR
                        || !StackUtils.itemsMatch(request, itemStack)
                ) {
                    continue;
                }

                found += itemStack.getAmount();

                // Escape if found all we need
                if (found >= requestAmount) {
                    return true;
                }
            }
        }

        return false;
    }

    public int getAmount(@Nonnull ItemStack itemStack) {
        ItemStack itemStackCache= itemStack;
        long totalAmount = 0;
        for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack inputSlotItem = blockMenu.getItemInSlot(slot);
                if (inputSlotItem != null && StackUtils.itemsMatch(inputSlotItem, itemStackCache)) {
                    totalAmount += inputSlotItem.getAmount();
                }
            }
        }

        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            ItemStack inputSlotItem = blockMenu.getItemInSlot(slots[0]);
            if (inputSlotItem != null && StackUtils.itemsMatch(inputSlotItem, itemStackCache)) {
                totalAmount += inputSlotItem.getAmount();
            }
        }
        var barrels= getMaterial2OutputAbleBarrels().get(itemStack.getType());
        if(barrels!=null){
            for (BarrelIdentity barrelIdentity : barrels.values()) {
                if (StackUtils.itemsMatch(barrelIdentity, itemStackCache)) {
                    totalAmount += barrelIdentity.getAmount();
                    if (barrelIdentity instanceof InfinityBarrel) {
                        totalAmount -= 2;
                    }
                }
            }
        }
        Map<Location, StorageUnitData> cacheMap = getOutputAbleCargoStorageUnitDatas0();
        for (StorageUnitData cache : cacheMap.values()) {
            final List<ItemContainer> storedItems = cache.getStoredItems();
            for (ItemContainer itemContainer : storedItems) {
                if (StackUtils.itemsMatch(itemContainer.getSample(), itemStackCache)) {
                    totalAmount += itemContainer.getAmount();
                }
            }
        }

        for (BlockMenu blockMenu : getCellMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack cellItem = blockMenu.getItemInSlot(slot);
                if (cellItem != null && StackUtils.itemsMatch(cellItem, itemStackCache)) {
                    totalAmount += cellItem.getAmount();
                }
            }
        }
        if (totalAmount > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return (int) totalAmount;
        }
    }

    public HashMap<ItemStack, Long> getAmount(@Nonnull Set<ItemStack> itemStacks) {
        HashMap<ItemStack, Long> totalAmounts = new HashMap<>();
        Set<ItemStack> itemStackCaches=itemStacks;//itemStacks.stream().collect(Collectors.toSet());
        for (BlockMenu menu : getAdvancedGreedyBlockMenus()) {
            int[] slots = menu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack inputSlotItem = menu.getItemInSlot(slot);
                if (inputSlotItem != null) {
                    for (ItemStack itemStack : itemStackCaches) {
                        if (StackUtils.itemsMatch(inputSlotItem, itemStack)) {
                            totalAmounts.compute(itemStack,(i,oldValue)->oldValue==null?(long)( inputSlotItem.getAmount()):oldValue+inputSlotItem.getAmount());
//                            totalAmounts.put(itemStack.getItemStack(), totalAmounts.getOrDefault(itemStack, 0L) + inputSlotItem.getAmount());
                        }
                    }
                }
            }
        }

        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            ItemStack inputSlotItem = blockMenu.getItemInSlot(slots[0]);
            if (inputSlotItem != null) {
                for (ItemStack itemStack : itemStackCaches) {
                    if (StackUtils.itemsMatch(inputSlotItem, itemStack)) {
                        totalAmounts.compute(itemStack,(i,oldValue)->oldValue==null?(long)(inputSlotItem.getAmount()):oldValue+inputSlotItem.getAmount());
                        //totalAmounts.put(itemStack, totalAmounts.getOrDefault(itemStack, 0L) + inputSlotItem.getAmount());
                    }
                }
            }
        }
        for (var entry: getMaterial2OutputAbleBarrels().entrySet()){
            if(entry.getValue()==null||entry.getValue().isEmpty())continue;
            for (BarrelIdentity barrelIdentity : entry.getValue().values()) {
                for (ItemStack itemStack : itemStackCaches) {
                    if (itemStack.getType()==entry.getKey()&& StackUtils.itemsMatch(barrelIdentity, itemStack)) {
                        long totalAmount = barrelIdentity.getAmount();
                        if (barrelIdentity instanceof InfinityBarrel) {
                            totalAmount -= 2;
                        }
                        final long total=totalAmount;
                        totalAmounts.compute(itemStack,(i,oldValue)->oldValue==null?(long)total:oldValue+total);
                       // totalAmounts.put(itemStack, totalAmounts.getOrDefault(itemStack, 0L) + totalAmount);
                    }
                }
            }
        }
        var cacheMap = getOutputAbleCargoStorageUnitDatas0();
        for (StorageUnitData cache : cacheMap.values()) {
            final List<ItemContainer> storedItems = cache.getStoredItems();
            for (ItemContainer itemContainer : storedItems) {
                for (ItemStack itemStack : itemStackCaches) {
                    if (StackUtils.itemsMatch(itemContainer.getSample(), itemStack)) {
                        long totalAmount = itemContainer.getAmount();
                        totalAmounts.compute(itemStack,(i,oldValue)->oldValue==null?(long)totalAmount:oldValue+totalAmount);
//                        totalAmounts.put(itemStack, totalAmounts.getOrDefault(itemStack, 0L) + totalAmount);
                    }
                }
            }
        }

        for (BlockMenu blockMenu : getCellMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack cellItem = blockMenu.getItemInSlot(slot);
                if (cellItem != null) {
                    for (ItemStack itemStack : itemStackCaches) {
                        if (StackUtils.itemsMatch(cellItem, itemStack)) {
                            totalAmounts.compute(itemStack,(i,oldValue)->oldValue==null?(long)cellItem.getAmount():oldValue+cellItem.getAmount());
//                            totalAmounts.put(itemStack.getItemStack(), totalAmounts.getOrDefault(itemStack, 0L) + cellItem.getAmount());
                        }
                    }
                }
            }
        }

        return totalAmounts;
    }
    @Note("we make async fix in NetworkRootPlus, so this is deprecated")
    public void addItemStack(@Nonnull ItemStack incoming) {
        ItemStack incomingCache = incoming;// new ItemStackCache(incoming);//  ItemStackCache.of(incoming);
        for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {
            final ItemStack template = blockMenu.getItemInSlot(AdvancedGreedyBlock.TEMPLATE_SLOT);

            if (template == null || template.getType() == Material.AIR || !StackUtils.itemsMatch(incomingCache, template)) {
                continue;
            }

            blockMenu.markDirty();
            BlockMenuUtil.pushItem(blockMenu, incomingCache, ADVANCED_GREEDY_BLOCK_AVAILABLE_SLOTS);
            // Given we have found a match, it doesn't matter if the item moved or not, we will not bring it in
            return;
        }

        // Run for matching greedy blocks
        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            final ItemStack template = blockMenu.getItemInSlot(NetworkGreedyBlock.TEMPLATE_SLOT);

            if (template == null || template.getType() == Material.AIR || !StackUtils.itemsMatch(incomingCache, template)) {
                continue;
            }

            blockMenu.markDirty();
            BlockMenuUtil.pushItem(blockMenu, incomingCache, GREEDY_BLOCK_AVAILABLE_SLOTS[0]);
            // Given we have found a match, it doesn't matter if the item moved or not, we will not bring it in
            return;
        }


        // Run for matching barrels
        var barrels= getMaterial2InputAbleBarrels().get(incoming.getType());
        if(barrels!=null){
            for (BarrelIdentity barrelIdentity : barrels.values()) {
                if (StackUtils.itemsMatch(barrelIdentity, incomingCache)) {
                    barrelIdentity.depositItemStack(incoming);

                    // All distributed, can escape
                    if (incoming.getAmount() == 0) {
                        return;
                    }
                }
            }
        }

        for (StorageUnitData cache : getInputAbleCargoStorageUnitDatas0().values()) {
            cache.depositItemStack(incoming, true);

            if (incoming.getAmount() == 0) {
                return;
            }
        }

        for (BlockMenu blockMenu : getCellMenus()) {
            blockMenu.markDirty();
            BlockMenuUtil.pushItem(blockMenu, incomingCache, CELL_AVAILABLE_SLOTS);
            if (incoming.getAmount() == 0) {
                return;
            }
        }
    }

    @Override
    public long retrieveBlockCharge() {
        return 0;
    }

    public void setRootPower(long power) {
        this.rootPower = power;
    }

    public void addRootPower(long power) {
        this.rootPower += power;
    }

    public void removeRootPower(long power) {
        int removed = 0;
        for (Location node : powerNodes) {
            final SlimefunItem item = StorageCacheUtils.getSfItem(node);
            if (item instanceof NetworkPowerNode powerNode) {
                final int charge = powerNode.getCharge(node);
                if (charge <= 0) {
                    continue;
                }
                final int toRemove = (int) Math.min(power - removed, charge);
                powerNode.removeCharge(node, toRemove);
                this.rootPower -= power;
                removed = removed + toRemove;
            }
            if (removed >= power) {
                return;
            }
        }
    }

    public void setDisplayParticles(boolean displayParticles) {
        this.displayParticles = displayParticles;
    }

    @Nonnull
    public List<ItemStack> getItemStacks(@Nonnull List<ItemRequest> itemRequests) {
        List<ItemStack> retrievedItems = new ArrayList<>();

        for (ItemRequest request : itemRequests) {
            ItemStack retrieved = getItemStack(request);
            if (retrieved != null) {
                retrievedItems.add(retrieved);
            }
        }
        return retrievedItems;
    }

    @Nonnull
    protected synchronized Map<Material,Map<Location, BarrelIdentity>> getMaterial2InputAbleBarrels() {
        if(!ready){
            handleAsync();
            return Map.of();
        }
        if (this.material2InputAbleBarrels != null) {
            return this.material2InputAbleBarrels;
        }

        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Map<Material,Map<Location, BarrelIdentity>> barrelSet =new Reference2ReferenceOpenHashMap<>();

        final Set<Location> monitor = new HashSet<>();
        monitor.addAll(this.inputOnlyMonitors);
        monitor.addAll(this.monitors);
        for (Location cellLocation : monitor) {
            final BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);

            if (face == null) {
                continue;
            }

            final Location testLocation = cellLocation.clone().add(face.getDirection());

            if (addedLocations.contains(testLocation)) {
                continue;
            } else {
                addedLocations.add(testLocation);
            }

            final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(testLocation);

            if (Networks.getSupportedPluginManager().isInfinityExpansion() && slimefunItem instanceof StorageUnit unit) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final InfinityBarrel infinityBarrel = getInfinityBarrel(menu, unit);
                if (infinityBarrel != null) {
                    barrelSet.computeIfAbsent(infinityBarrel.getItemType(),(i)->new ConcurrentHashMap<>()).put(cellLocation.clone(), infinityBarrel);
                }
                continue;
            }
            if (Networks.getSupportedPluginManager().isFluffyMachines() && slimefunItem instanceof Barrel barrel) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final FluffyBarrel fluffyBarrel = getFluffyBarrel(menu, barrel);
                if (fluffyBarrel != null) {
                    barrelSet.computeIfAbsent(fluffyBarrel.getItemType(),(i)->new ConcurrentHashMap<>()).put(cellLocation.clone(), fluffyBarrel);
                }
                continue;
            }
            if (slimefunItem instanceof NetworkQuantumStorage) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final NetworkStorage storage = getNetworkStorage(menu);
                if (storage != null) {
                    barrelSet.computeIfAbsent(storage.getItemType(),(i)->new ConcurrentHashMap<>()).put(cellLocation.clone(), storage);
                }
            }
        }

        this.material2InputAbleBarrels = barrelSet;
        Set<BarrelIdentity> barrels = ConcurrentHashMap.newKeySet();
        barrelSet.forEach((key,value)->barrels.addAll(value.values()));
        this.inputAbleBarrels = barrels;
        NetworkRootLocateStorageEvent event = new NetworkRootLocateStorageEvent(this, StorageType.BARREL, true, false, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return barrelSet;
    }

    @Nonnull
    protected synchronized Map<Material,Map<Location, BarrelIdentity>> getMaterial2OutputAbleBarrels() {
        if(!ready){
            handleAsync();
            return Map.of();
        }
        if (this.material2OutputAbleBarrels != null) {
            return this.material2OutputAbleBarrels;
        }
      //  Networks.getInstance().getLogger().info("Initialize "+nodePosition+" root output barrels uid "+locUniqueId+ " status "+ready);
//        Preconditions.checkArgument(ready);

        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Map<Material,Map<Location, BarrelIdentity>> barrelSet = new Reference2ReferenceOpenHashMap<>();

        final Set<Location> monitor = new HashSet<>();
        monitor.addAll(this.outputOnlyMonitors);
        monitor.addAll(this.monitors);
//        Networks.getInstance().getLogger().info("find "+nodePosition+" monitor "+monitor.size()+" "+locUniqueId);
        for (Location cellLocation : monitor) {
            final BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);

            if (face == null) {
                continue;
            }

            final Location testLocation = cellLocation.clone().add(face.getDirection());

            if (addedLocations.contains(testLocation)) {
                continue;
            } else {
                addedLocations.add(testLocation);
            }

            final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(testLocation);

            if (Networks.getSupportedPluginManager().isInfinityExpansion() && slimefunItem instanceof StorageUnit unit) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final InfinityBarrel infinityBarrel = getInfinityBarrel(menu, unit);
                if (infinityBarrel != null) {
                    barrelSet.computeIfAbsent(infinityBarrel.getItemType(),(i)->new ConcurrentHashMap<>()).put(cellLocation.clone(), infinityBarrel);
                }
                continue;
            }
            if (Networks.getSupportedPluginManager().isFluffyMachines() && slimefunItem instanceof Barrel barrel) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final FluffyBarrel fluffyBarrel = getFluffyBarrel(menu, barrel);
                if (fluffyBarrel != null) {
                    barrelSet.computeIfAbsent(fluffyBarrel.getItemType(),(i)->new ConcurrentHashMap<>()).put(cellLocation.clone(), fluffyBarrel);
                }
                continue;
            }
            if (slimefunItem instanceof NetworkQuantumStorage) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final NetworkStorage storage = getNetworkStorage(menu);
                if (storage != null) {
                    barrelSet.computeIfAbsent(storage.getItemType(),(i)->new ConcurrentHashMap<>()).put(cellLocation.clone(), storage);
                }
            }
        }

        this.material2OutputAbleBarrels = barrelSet;
        Set<BarrelIdentity> barrels = ConcurrentHashMap.newKeySet();
        barrelSet.forEach((key,value)->barrels.addAll(value.values()));
        this.outputAbleBarrels = barrels;
        NetworkRootLocateStorageEvent event = new NetworkRootLocateStorageEvent(this, StorageType.BARREL, false, true, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return barrelSet;
    }
    public synchronized Map<StorageUnitData,Location> getInputAbleCargoStorageUnitDatas(){
        Map map = getInputAbleCargoStorageUnitDatas0();
        return map.isEmpty()?map:((BidiMap)map).inverseBidiMap();
    }
    @Nonnull
    public synchronized Map<Location, StorageUnitData> getInputAbleCargoStorageUnitDatas0() {
        if(!ready){
            handleAsync();
            return Map.of();
        }
        if (this.inputAbleCargoStorageUnitDatas != null) {
            return this.inputAbleCargoStorageUnitDatas;
        }

        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Map<Location, StorageUnitData> dataSet = new DualHashBidiMap<>();

        final Set<Location> monitor = new HashSet<>();
        monitor.addAll(this.inputOnlyMonitors);
        monitor.addAll(this.monitors);
        for (Location cellLocation : monitor) {
            final BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);

            if (face == null) {
                continue;
            }

            final Location testLocation = cellLocation.clone().add(face.getDirection());

            if (addedLocations.contains(testLocation)) {
                continue;
            } else {
                addedLocations.add(testLocation);
            }

            final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(testLocation);

            if (slimefunItem instanceof NetworksDrawer) {
                final StorageUnitData data = getCargoStorageUnitData(testLocation);
                if (data != null) {
                    dataSet.put(testLocation, data);
                }
            }
        }

        this.inputAbleCargoStorageUnitDatas = dataSet;
        NetworkRootLocateStorageEvent event = new NetworkRootLocateStorageEvent(this, StorageType.DRAWER, true, false, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return dataSet;
    }
    public void handleAsync(){
        //Networks.getInstance().getLogger().info("Network is not ready! ");
    }
    public synchronized Map<StorageUnitData, Location> getOutputAbleCargoStorageUnitDatas(){
        Map map = getOutputAbleCargoStorageUnitDatas0();
        return map.isEmpty()?map:((BidiMap)map).inverseBidiMap();
    }
    @Nonnull
    protected synchronized Map<Location, StorageUnitData> getOutputAbleCargoStorageUnitDatas0() {
        if(!ready){
            handleAsync();
            return Map.of();
        }
        if (this.outputAbleCargoStorageUnitDatas != null) {
            return this.outputAbleCargoStorageUnitDatas;
        }

        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Map<Location, StorageUnitData> dataSet = new DualHashBidiMap<>();

        final Set<Location> monitor = new HashSet<>();
        monitor.addAll(this.outputOnlyMonitors);
        monitor.addAll(this.monitors);
        for (Location cellLocation : monitor) {
            final BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);

            if (face == null) {
                continue;
            }

            final Location testLocation = cellLocation.clone().add(face.getDirection());

            if (addedLocations.contains(testLocation)) {
                continue;
            } else {
                addedLocations.add(testLocation);
            }

            final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(testLocation);

            if (slimefunItem instanceof NetworksDrawer) {
                final StorageUnitData data = getCargoStorageUnitData(testLocation);
                if (data != null) {
                    dataSet.put(testLocation, data);
                }
            }
        }

        this.outputAbleCargoStorageUnitDatas = dataSet;
        NetworkRootLocateStorageEvent event = new NetworkRootLocateStorageEvent(this, StorageType.DRAWER, false, true, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return dataSet;
    }
    public void resetRootItems(){
        this.barrels = null;
        this.material2Barrels = null;
        this.cargoStorageUnitDatas = null;
        this.inputAbleBarrels = null;
        this.material2InputAbleBarrels = null;
        this.outputAbleBarrels = null;
        this.material2OutputAbleBarrels = null;
        this.inputAbleCargoStorageUnitDatas = null;
        this.outputAbleCargoStorageUnitDatas = null;
    }
    public void initRootItems(){
        getMaterial2Barrels();
        getCargoStorageUnitDatas0();
        getMaterial2InputAbleBarrels();
        getMaterial2OutputAbleBarrels();
        getInputAbleCargoStorageUnitDatas0();
        getOutputAbleCargoStorageUnitDatas0();
    }
    public boolean refreshRootItems() {
        resetRootItems();
        initRootItems();
        return true;
    }
    protected static enum StorageSource{
        QUANTUM,
        STORAGE_DATA,
        UNKNOWN;
        //shit , we decided to delete these shit
    }
    protected static interface StorageIndex{
        StorageSource source();
        Location loc();
    }
    protected static record LocationIndex(Location loc, StorageSource source) implements StorageIndex{
    }
    protected static record StorageUnitDataIndex(Location loc, int internalIndex) implements StorageIndex{

        @Override
        public StorageSource source() {
            return StorageSource.STORAGE_DATA;
        }
    }

    public static class PusherPrefetcherInfo {
        public PusherPrefetcherInfo(){

        }
        protected StorageIndex prefetchInfo;

        /**
         * this method is synchronized because of prefetchInfo
         * luckly, we have the optimization of grabbing only once in linear push operation
         * @param root
         * @param request
         * @return
         */
        public synchronized ItemStack getItemStackWithPrefetch(NetworkRoot root, ItemRequest request){
            ItemStack stackToReturn = null;
            if(prefetchInfo == null){
                var prefetching = root.prefetchInternal(request);
                stackToReturn = prefetching.getB();
                prefetchInfo = prefetching.getA();
                return root.getItemStack0(request, stackToReturn, prefetchInfo == null ? StorageSource.UNKNOWN.ordinal():(prefetchInfo.source().ordinal()+1));
            }else{
                ItemStack stackToReturn0;
                switch (prefetchInfo.source()){
                    case QUANTUM:
                        stackToReturn0 = root.prefetchFromBarrels(request, prefetchInfo.loc(), stackToReturn, false);
                        if(stackToReturn0 == PREFETCH_INVALID || request.getAmount() > 0){
                            //if this index do not satisfy the request, then we consider it as a NOT-ENOUGH index, we deserves better index, so search for it
                            prefetchInfo = null;
                            return getItemStackWithPrefetch(root, request);
                        }else {
                            //request is satisfied, continue
                            stackToReturn = stackToReturn0;
                            return stackToReturn;
                            //return root.getItemStack0(request, stackToReturn, 1);
                        }
                    case STORAGE_DATA:
                        stackToReturn0 = root.prefetchFromStorages(request, prefetchInfo.loc() , ((StorageUnitDataIndex)prefetchInfo).internalIndex(), stackToReturn, false);
                        //wew suppose that it should be single source item
                        if(stackToReturn0 == PREFETCH_INVALID){
                            prefetchInfo = null;
                            return getItemStackWithPrefetch(root, request);
                        }else {
                            stackToReturn = stackToReturn0;
                            return root.getItemStack0(request, stackToReturn, 2);
                        }
                    default:
                        prefetchInfo = null;
                        return getItemStackWithPrefetch(root, request);
                }
            }
        }
    }
    @NotCompleted
    public static class PusherItemStream {
        private NetworkRoot root;
        ItemRequest request;
        private int type;
        private Iterator sourcePointer;
        private Object sourceData;
        public boolean hasMore(){
            return type != -1;
        }
        private void iterForNextData(){
            switch (type){
                case 0:
                    checkbarrel:{
                        if(sourcePointer == null){
                            var barrels = root.getMaterial2OutputAbleBarrels().get(request.getItemType());
                            if(barrels == null){
                                break checkbarrel;
                            }
                            sourcePointer = barrels.values().iterator();
                        }
                        while (sourcePointer.hasNext()){
                            BarrelIdentity barrel  = (BarrelIdentity) sourcePointer.next();
                            if(barrel.getItemStack() != null && StackUtils.itemsMatch(barrel, request)){
                                sourceData = barrel;
                                return;
                            }
                        }
                    }
                    sourcePointer = null;
                    type = 1;
                case 1:
                    if(sourcePointer == null){
                        sourcePointer = root.getOutputAbleCargoStorageUnitDatas0().values().iterator();
                    }
                    while (sourcePointer.hasNext()){
                        StorageUnitData unit = (StorageUnitData) sourcePointer.next();
                        Integer indexed =  unit.getPrefetchInfo(this.request);
                        if(indexed != null){
                            sourceData = Pair.of(unit, indexed);
                            return;
                        }
                    }
                    sourcePointer = null;
                    type = 2;
                case 2:
                    if(sourcePointer == null){
                        sourcePointer = root.advancedGreedyBlocks.values().iterator();
                    }
                    while (sourcePointer.hasNext()){
                        SlimefunBlockData data = (SlimefunBlockData) sourcePointer.next();
                        if(data.isPendingRemove())continue;
                        BlockMenu menu = data.getBlockMenu();
                        if(menu != null){
                            sourceData = menu;
                            return;
                        }
                    }
                    sourcePointer = null;
                    type = 3;
                case 3:
                    if(sourcePointer == null){
                        sourcePointer = root.greedyBlocks.values().iterator();
                    }
                    while (sourcePointer.hasNext()){
                        SlimefunBlockData data = (SlimefunBlockData) sourcePointer.next();
                        if(data.isPendingRemove())continue;
                        BlockMenu menu = data.getBlockMenu();
                        if(menu != null){
                            sourceData = menu;
                            return;
                        }
                    }
                    sourcePointer = null;
                    type = 4;
                case 4:
                    if(sourcePointer == null){
                        sourcePointer = root.crafters.values().iterator();
                    }
                    while (sourcePointer.hasNext()){
                        SlimefunBlockData data = (SlimefunBlockData) sourcePointer.next();
                        if(data.isPendingRemove())continue;
                        BlockMenu menu = data.getBlockMenu();
                        if(menu != null){
                            sourceData = menu;
                            return;
                        }
                    }
                    sourcePointer = null;
                    type = 5;
                case 5:
                    if(sourcePointer == null){
                        sourcePointer = root.cells.values().iterator();
                    }
                    while (sourcePointer.hasNext()){
                        SlimefunBlockData data = (SlimefunBlockData) sourcePointer.next();
                        if(data.isPendingRemove())continue;
                        BlockMenu menu = data.getBlockMenu();
                        if(menu != null){
                            sourceData = menu;
                            return;
                        }
                    }
                    sourcePointer = null;
                    type = -1;
                default:
                    sourceData = null;
                    return;
            }
        }
        public ItemStack nextItem(int amount){
            ItemStack stackToReturn = null;
            ItemStack stackToReturn0;
            int received = 0;
            if(sourceData == null){
                iterForNextData();
            }
            do{
                switch (type){
                    case 0:
                        BarrelIdentity identity = (BarrelIdentity) sourceData;
                        stackToReturn0 = root.fetchFromBarrels(request, identity, stackToReturn, true);
                        if(stackToReturn0 != null){
                            stackToReturn = stackToReturn0;
                            received = stackToReturn.getAmount();
                            if(received >= amount){
                                return stackToReturn;
                            }
                        }
                        //re find data
                        break;
                    case 1:
                        Pair<StorageUnitData, Integer> ipair = (Pair<StorageUnitData, Integer>) sourceData;
                        stackToReturn0 = ipair.getA().requestViaIndex(request, ipair.getB(), true).getA();
                        if(stackToReturn0 != null){
                            if(stackToReturn == null){
                                stackToReturn = stackToReturn0;
                            }else {
                                stackToReturn.setAmount(stackToReturn.getAmount() + stackToReturn0.getAmount());
                            }
                            received = stackToReturn.getAmount();
                            if(received >= amount){
                                return stackToReturn;
                            }
                        }
                        //refind data
                        break;
                    case -1:
                        //end, no more
                        return stackToReturn;
                    default:
                        //else type, all menus, use menu logic
                        if(sourceData instanceof BlockMenu menu){
                            stackToReturn0 = root.getFromBlockMenu(request, stackToReturn, menu);
                            if(stackToReturn0 != null){
                                stackToReturn = stackToReturn0;
                                received = stackToReturn.getAmount();
                                if(received >= amount){
                                    return stackToReturn;
                                }
                            }
                        }
                }
                iterForNextData();
            }while (amount > received && type != -1);
            return stackToReturn;
        }
    }
}