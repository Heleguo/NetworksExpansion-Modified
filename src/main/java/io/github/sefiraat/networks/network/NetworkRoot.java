package io.github.sefiraat.networks.network;

import com.balugaq.netex.api.data.ItemContainer;
import com.balugaq.netex.api.data.ItemFlowRecord;
import com.balugaq.netex.api.data.StorageUnitData;
import com.balugaq.netex.api.enums.StorageType;
import com.balugaq.netex.api.events.NetworkRootLocateStorageEvent;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.balugaq.netex.utils.NetworksVersionedParticle;
import com.google.common.collect.Streams;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.networks.advanced.AdvancedGreedyBlock;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import io.github.mooy1.infinityexpansion.items.storage.StorageCache;
import io.github.mooy1.infinityexpansion.items.storage.StorageUnit;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.managers.ExperimentalFeatureManager;
import io.github.sefiraat.networks.network.barrel.FluffyBarrel;
import io.github.sefiraat.networks.network.barrel.InfinityBarrel;
import io.github.sefiraat.networks.network.barrel.NetworkStorage;
import io.github.sefiraat.networks.network.stackcaches.*;
import io.github.sefiraat.networks.slimefun.network.NetworkCell;
import io.github.sefiraat.networks.slimefun.network.NetworkDirectional;
import io.github.sefiraat.networks.slimefun.network.NetworkGreedyBlock;
import io.github.sefiraat.networks.slimefun.network.NetworkPowerNode;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.ncbpfluffybear.fluffymachines.items.Barrel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import me.matl114.matlib.algorithms.dataStructures.struct.Pair;
import me.matl114.matlib.common.lang.annotations.Note;
import me.matl114.matlib.nmsUtils.inventory.ItemCacheHashMap;
import me.matl114.matlib.nmsUtils.inventory.ItemHashCache;
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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
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
        return cellsMap.keySet();
    }

    @Getter
    private final Map<Location, SlimefunBlockData> cellsMap = new ConcurrentHashMap<>();

    @Getter
    private final Set<Location> grabbers = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<Location> pushers = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<Location> purgers = ConcurrentHashMap.newKeySet();

    public Set<Location> getCrafters(){
        return craftersMap;
    }

    private final Set<Location> craftersMap = ConcurrentHashMap.newKeySet(); //Map<Location, SlimefunBlockData> craftersMap = new ConcurrentHashMap<>();

    private final Map<Location, SlimefunBlockData> availableCraftersMap = new ConcurrentHashMap<>();

    @Getter
    private final Set<Location> powerNodes = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<Location> powerDisplays = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<Location> encoders = ConcurrentHashMap.newKeySet();

    public Set<Location> getGreedyBlocks(){
        return greedyBlocksMap.keySet();
    }
    @Getter
    private final Map<Location, SlimefunBlockData> greedyBlocksMap = new ConcurrentHashMap<>();
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
        return advancedGreedyBlocksMap.keySet();
    }
    @Getter
    private final Map<Location, SlimefunBlockData> advancedGreedyBlocksMap = new ConcurrentHashMap<>();
    @Getter
    private final Set<Location> advancedPurgers = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<Location> advancedVacuums = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<Location> lineTransferVanillaPushers = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<Location> lineTransferVanillaGrabbers = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<Location> inputOnlyMonitors = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<Location> outputOnlyMonitors = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<Location> linePowerOutlets = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<Location> decoders = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<Location> quantumManagers = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<Location> drawerManagers = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<Location> crafterManagers = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<Location> itemFlowViewers = ConcurrentHashMap.newKeySet();

    @Deprecated
    private final boolean progressing = false;

    @Getter
    private final int maxNodes;

    @Getter
    private final boolean recordFlow = false;

    @Getter
    private final @Nullable ItemFlowRecord itemFlowRecord = null;

    @Getter
    private @Nullable Location controller = null;

    @Getter
    private boolean isOverburdened = false;

    @Deprecated
    public Collection<BarrelIdentity> getBarrels(){
        getMapBarrels();
        return barrels==null?Set.of():barrels;
    }
    private Collection<BarrelIdentity> barrels = null;
    public Collection<BarrelIdentity> getInputAbleBarrels(){
        getMapInputAbleBarrels();
        return inputAbleBarrels==null?Set.of():inputAbleBarrels;
    }
    private Collection<BarrelIdentity> inputAbleBarrels = null;
    public Collection<BarrelIdentity> getOutputAbleBarrels(){
        getMapOutputAbleBarrels();
        return outputAbleBarrels==null?Set.of():outputAbleBarrels;
    }
    private Collection<BarrelIdentity> outputAbleBarrels = null;

    private Map<Location, BarrelIdentity> material2Barrels = null;
    private Map<Location, BarrelIdentity> material2InputAbleBarrels = null;
    private Map<Location, BarrelIdentity> material2OutputAbleBarrels = null;
    @Deprecated
    private Map<Location, StorageUnitData> cargoStorageUnitDatas = null;

    private Map<Location, StorageUnitData> inputAbleCargoStorageUnitDatas = null;

    private Map<Location, StorageUnitData> outputAbleCargoStorageUnitDatas = null;

    private ItemCacheHashMap<ItemHashCache, Object> inputableStorageIdentities = null;

    private ItemCacheHashMap<ItemHashCache, Object> outputableStorageIdentities = null;
    @Getter
    @Setter
    private boolean ready = false;

    @Getter
    protected long rootPower = 0;

    @Setter
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

    }

    public static void addPersistentAccessHistory(Location location, Location accessLocation){

    }
    public static void addCacheMiss(Location location, Location accessLocation){

    }

    public static void minusCacheMiss(Location location, Location accessLocation){

    }

    public static Map<Location, Integer> getPersistentAccessHistory(Location location){
        return Map.of();
    }

    public static void removePersistentAccessHistory(Location location){

    }

    public static void removePersistentAccessHistory(Location location, Location accessLocation){

    }

    public static void addCountObservingAccessHistory(Location location, Location accessLocation){

    }

    public static Map<Location, Integer> getCountObservingAccessHistory(Location location){
        return Map.of();
    }

    public static void removeCountObservingAccessHistory(Location location){

    }

    public static void removeCountObservingAccessHistory(Location location, Location accessLocation){

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
                    cellsMap.put(location, blockMenu);
                }
            }
            case GRABBER -> grabbers.add(location);
            case PUSHER -> pushers.add(location);
            case PURGER -> purgers.add(location);
            case CRAFTER -> {
                craftersMap.add(location);
                var block =  StorageCacheUtils.getBlock(location);
                if(block != null && !block.isPendingRemove() && block.getBlockMenu()!= null && block.getBlockMenu().getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW).length != 0){
                    availableCraftersMap.put(location, block);
                }
            }
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
                    greedyBlocksMap.put(location, blockMenu);
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
                    advancedGreedyBlocksMap.put(location, blockMenu);
                }
            }
            case ADVANCED_PURGER -> advancedPurgers.add(location);
            case ADVANCED_VACUUM -> advancedVacuums.add(location);
            case TRANSFER -> transfers.add(location);
            case TRANSFER_PUSHER -> transferPushers.add(location);
            case TRANSFER_GRABBER -> transferGrabbers.add(location);
            case LINE_TRANSFER_VANILLA_GRABBER -> lineTransferVanillaGrabbers.add(location);
            case LINE_TRANSFER_VANILLA_PUSHER -> lineTransferVanillaPushers.add(location);
            case INPUT_ONLY_MONITOR -> inputOnlyMonitors.add(location);
            case OUTPUT_ONLY_MONITOR -> outputOnlyMonitors.add(location);
            case LINE_POWER_OUTLET -> linePowerOutlets.add(location);
            case DECODER -> decoders.add(location);
            case QUANTUM_MANAGER -> quantumManagers.add(location);
            case DRAWER_MANAGER -> drawerManagers.add(location);
            case CRAFTER_MANAGER -> crafterManagers.add(location);
            case FLOW_VIEWER -> itemFlowViewers.add(location);
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
                        loc.getWorld()
                                .spawnParticle(
                                        NetworksVersionedParticle.EXPLOSION,
                                        loc.clone().add(x, y, z),
                                        0);
                    }
                }
            }
        }
        this.isOverburdened = overburdened;
    }

    @Nonnull
    public Map<ItemStack, Long> getAllNetworkItemsLongType() {
        if(!ready){
            handleAsync();
            return (Map<ItemStack, Long>) EMPTY_MAP;
        }
        final Map<ItemStack, Long> itemStacks = new HashMap<>();

        // Barrels
        for(var barrelIdentity: getMapOutputAbleBarrels().values()){
            itemStacks.compute(barrelIdentity.getItemStack(),(i,num)->num==null?(long)barrelIdentity.getAmount():(long)num+barrelIdentity.getAmount() );
        }

        // Cargo storage units
        Map<Location, StorageUnitData> cacheMap = getOutputAbleCargoStorageUnitDatas0();
        for (StorageUnitData cache : cacheMap.values()) {
            for (ItemContainer itemContainer : cache.getStoredItems()) {
                itemStacks.compute(itemContainer.getSample(),(i,num)->num==null?(long)itemContainer.getAmount():(long)num+itemContainer.getAmount());
            }
        }

        for (SlimefunBlockData data : getAdvancedGreedyBlocksMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
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

        for (SlimefunBlockData data : getGreedyBlocksMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            final ItemStack itemStack = blockMenu.getItemInSlot(slots[0]);
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            final ItemStack clone = StackUtils.getAsQuantity(itemStack, 1);
            itemStacks.compute(clone,(i,num)->num==null?(long)itemStack.getAmount():(long)num+itemStack.getAmount());
        }

        for (SlimefunBlockData data : availableCraftersMap.values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
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

        for (SlimefunBlockData data : getCellsMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
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
        if(!ready){
            handleAsync();
            return  (Map<ItemStack, Integer>) EMPTY_MAP;
        }
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
    protected static final Map<?,?> EMPTY_MAP = Map.of();
    @Deprecated
    private synchronized Map<Location, BarrelIdentity> getMapBarrels0(){
        if (this.material2Barrels != null) {
            return this.material2Barrels;
        }
        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Map<Location, BarrelIdentity> barrelSet =new Reference2ReferenceOpenHashMap<>();

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
                    barrelSet.put(testLocation, infinityBarrel);
                }
                continue;
            } else if (Networks.getSupportedPluginManager().isFluffyMachines() && slimefunItem instanceof Barrel barrel) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final FluffyBarrel fluffyBarrel = getFluffyBarrel(menu, barrel);
                if (fluffyBarrel != null) {
                    barrelSet.put(testLocation, fluffyBarrel);
                }
                continue;
            } else if (slimefunItem instanceof NetworkQuantumStorage) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final NetworkStorage storage = getNetworkStorage(menu);
                if (storage != null) {
                    barrelSet.put(testLocation, storage);
                }
            }
        }

        this.material2Barrels = barrelSet;
        this.barrels = Collections.unmodifiableCollection(barrelSet.values());
        NetworkRootLocateStorageEvent event = new NetworkRootLocateStorageEvent(this, StorageType.BARREL, true, true, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return barrelSet;
    }
    @Deprecated
    @Nonnull
    protected Map<Location, BarrelIdentity> getMapBarrels() {
        if(!ready){
            handleAsync();
            return (Map<Location, BarrelIdentity>) EMPTY_MAP;
        }
        if (this.material2Barrels != null) {
            return this.material2Barrels;
        }
        return getMapBarrels0();
    }

    @org.jetbrains.annotations.Nullable
    public static BarrelIdentity getBarrel(@NotNull Location barrelLocation, boolean includeEmpty) {
        var barrel = getBarrel(barrelLocation);
        if(barrel != null && includeEmpty && barrel.getAmount() <= 0L){
            return null;
        }return barrel;
    }

    @org.jetbrains.annotations.Nullable
    public static BarrelIdentity getBarrel(@NotNull Location barrelLocation) {
        SlimefunItem item = StorageCacheUtils.getSfItem(barrelLocation);
        BlockMenu menu = StorageCacheUtils.getMenu(barrelLocation);
        if (menu == null) {
            return null;
        }

        if (item instanceof NetworkQuantumStorage) {
            return getNetworkStorage(menu);
        } else if (Networks.getSupportedPluginManager().isFluffyMachines() && item instanceof Barrel barrel) {
            return getFluffyBarrel(menu, barrel);
        } else if (Networks.getSupportedPluginManager().isInfinityExpansion() && item instanceof StorageUnit storageUnit) {
            return getInfinityBarrel(menu, storageUnit);
        } else {
            return null;
        }
    }



    public synchronized Map<StorageUnitData, Location> getCargoStorageUnitDatas(){
        Map map = getCargoStorageUnitDatas0();
        return map.isEmpty()?map: ((BidiMap)map).inverseBidiMap();
    }
    protected synchronized Map<Location, StorageUnitData> getCargoStorageUnitDatas00(){
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
    @Nonnull
    protected Map<Location, StorageUnitData> getCargoStorageUnitDatas0() {
        if(!ready){
            handleAsync();
            return (Map<Location, StorageUnitData>) EMPTY_MAP;
        }
        if (this.cargoStorageUnitDatas != null) {
            return this.cargoStorageUnitDatas;
        }
        return getCargoStorageUnitDatas00();
    }

    @Nullable
    private static InfinityBarrel getInfinityBarrel(@Nonnull BlockMenu blockMenu, @Nonnull StorageUnit storageUnit) {
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
    private static FluffyBarrel getFluffyBarrel(@Nonnull BlockMenu blockMenu, @Nonnull Barrel barrel) {
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
    public static NetworkStorage getNetworkStorage(@Nonnull BlockMenu blockMenu) {

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
    public static StorageUnitData getCargoStorageUnitData(@Nonnull BlockMenu blockMenu) {
        return NetworksDrawer.getStorageData(blockMenu.getLocation());
    }

    @Nullable
    public static StorageUnitData getCargoStorageUnitData(@Nonnull Location location) {
        return NetworksDrawer.getStorageData(location);
    }

    @Nonnull
    @Deprecated(forRemoval = true)
    public Set<BlockMenu> getCellMenus() {
        return this.cellsMap.values()
            .stream()
            .filter(d->!d.isPendingRemove())
            .map(SlimefunBlockData::getBlockMenu)
            .collect(Collectors.toUnmodifiableSet());
//        final Set<BlockMenu> menus = new HashSet<>();
//        for (Location cellLocation : this.cellsMap) {
//            BlockMenu menu = StorageCacheUtils.getMenu(cellLocation);
//            if (menu != null) {
//                menus.add(menu);
//            }
//        }
//        return menus;
    }


    @Nonnull
    @Deprecated(forRemoval = true)
    public Set<BlockMenu> getCrafterOutputs() {

        return this.craftersMap
            .stream()
            .map(StorageCacheUtils::getBlock)
            .filter(d-> d!= null &&  !d.isPendingRemove())
            .map(SlimefunBlockData::getBlockMenu)
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableSet());
//        final Set<BlockMenu> menus = new HashSet<>();
//        for (Location location : this.craftersMap) {
//            BlockMenu menu = StorageCacheUtils.getMenu(location);
//            if (menu != null) {
//                menus.add(menu);
//            }
//        }
//        return menus;
    }

    @Nonnull
    @Deprecated(forRemoval = true)
    public Set<BlockMenu> getGreedyBlockMenus() {
        return this.greedyBlocksMap.values()
            .stream()
            .filter(d->!d.isPendingRemove())
            .map(SlimefunBlockData::getBlockMenu)
            .collect(Collectors.toUnmodifiableSet());
//        final Set<BlockMenu> menus = new HashSet<>();
//        for (var data : this.greedyBlocksMap.values()) {
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
    @Deprecated(forRemoval = true)
    public Set<BlockMenu> getAdvancedGreedyBlockMenus() {
        return this.advancedGreedyBlocksMap.values()
            .stream()
            .filter(d->!d.isPendingRemove())
            .map(SlimefunBlockData::getBlockMenu)
            .collect(Collectors.toUnmodifiableSet());
//        final Set<BlockMenu> menus = new HashSet<>();
//        for (var data : this.advancedGreedyBlocksMap.values()) {
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
        ItemStack stackToReturn0 ;

        for (BarrelIdentity barrelIdentity : getMapOutputAbleBarrels().values()) {
            if(request.getAmount() <= 0 ){
                break;
            }
            stackToReturn0 = fetchFromBarrels(request, barrelIdentity, stackToReturn, false);
            if(stackToReturn0 != null){
                stackToReturn = stackToReturn0;
            }

        }

        return stackToReturn;
    }
    @Note("should check ItemStack return == INVALID, if so, abandon the prefetch info")
    protected final ItemStack prefetchFromBarrels(ItemRequest request, Location loc, ItemStack stackToReturn, boolean bypassCheck){
        if(request.getAmount() <= 0){
            return stackToReturn;
        }
        var barrel = getMapOutputAbleBarrels().get(loc);
        if(barrel == null || barrel.getItemStack() == null || !( bypassCheck || StackUtils.itemsMatch(request, barrel))){
            return PREFETCH_INVALID;
        }else {
            //we check the barrel at above, so check is no longer needed
            return fetchFromBarrels(request, barrel, stackToReturn, true);
        }
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
        final int preserveAmount = infinity ? fetched.getAmount() - 1 : fetched.getAmount();

        if (stackToReturn == null) {
            stackToReturn = fetched.clone();
            stackToReturn.setAmount(0);
        }

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

    protected ItemStack getFromStorageIdentities(ItemRequest request, ItemStack stackToReturn, Object what){
        if(request.getAmount() <= 0){
            return stackToReturn;
        }
        if(what instanceof BarrelIdentity barrel){
            ItemStack stackToReturn0 = fetchFromBarrels(request, barrel, stackToReturn, true);
            return stackToReturn0 == null ? stackToReturn : stackToReturn0;
        }else if (what instanceof Pair containerData){
            ItemContainer container = (ItemContainer)containerData.getB();
            ItemStack take = StorageUnitData.requestItemContainer(container, request, request.getAmount(), true);
            if (take != null) {
                if (stackToReturn == null) {
                    stackToReturn = take;
                } else {
                    stackToReturn.setAmount(stackToReturn.getAmount() + take.getAmount());
                }
                request.receiveAmount(take.getAmount());
            }
            return stackToReturn;
        }else if(what instanceof ArrayList<?> multiple){
            ItemStack stackToReturn0 ;
            for (var object: multiple){
                stackToReturn0 = getFromStorageIdentities(request, stackToReturn, object);
                if(stackToReturn0 != null){
                    stackToReturn = stackToReturn0;
                }
                if(request.getAmount() <= 0){
                    break;
                }
            }
            return stackToReturn;
        }
        return stackToReturn;
    }
    protected static final Pair NULL_PAIR = Pair.of(null,null);
    protected Pair<StorageIndex, ItemStack> prefetchInternal(ItemRequest request){
        if(request.getItemStack() == null){
            return NULL_PAIR;
        }

        var storageIdentity = getOutputableStorageIdentities().get(request);
        if(storageIdentity != null) {
            return prefetchInternal0(request, storageIdentity, null);
        }
        return NULL_PAIR;
    }
    protected Pair<StorageIndex, ItemStack> prefetchInternal0(ItemRequest request, Object storageIdentity, ItemStack stackToReturn) {
        ItemStack stackToReturn0;
        if(storageIdentity instanceof BarrelIdentity barrel){
            stackToReturn0 = prefetchFromBarrels(request, barrel.getLocation(), stackToReturn, true);
            //in barrels, we should prefer barrel with more item, only when satisfy request can we return , otherwise this will perform as simply goes through the whole cache
            if(stackToReturn0 == PREFETCH_INVALID){
                return stackToReturn == null? NULL_PAIR: Pair.of(null, stackToReturn);
            }else {
                stackToReturn = stackToReturn0;
            }
            if(request.getAmount() <=0){
                return Pair.of(new LocationIndex(barrel.getLocation().clone(), StorageSource.QUANTUM), stackToReturn);
            }else {
                return Pair.of(null, stackToReturn);
            }
        }
        else if (storageIdentity instanceof Pair containerData){
            StorageUnitData units = (StorageUnitData)containerData.getA();
            Integer internalIndex = units.getPrefetchInfo(request);
            if(internalIndex != null){
                Location loc = getOutputAbleCargoStorageUnitDatas().get(units);
                if(loc != null){
                    stackToReturn0 = prefetchFromStorages(request, loc, internalIndex, stackToReturn, true);
                    if(stackToReturn0 == PREFETCH_INVALID){
                        return stackToReturn == null? NULL_PAIR: Pair.of(null, stackToReturn);
                    }else {
                        stackToReturn = stackToReturn0;
                    }
                    if(request.getAmount() <= 0){
                        return Pair.of(new StorageUnitDataIndex(loc.clone(), internalIndex), stackToReturn);
                    }else {
                        return Pair.of(null, stackToReturn);
                    }
                }
            }
            return stackToReturn == null? NULL_PAIR: Pair.of(null, stackToReturn);
        }else if(storageIdentity instanceof ArrayList<?> multiple){
            for (var object: multiple){
                var re = prefetchInternal0(request, object, stackToReturn);
                stackToReturn0 = re.getB();
                if(stackToReturn0 != null){
                    stackToReturn = stackToReturn0;
                }
                if(request.getAmount() <= 0){
                    return Pair.of(re.getA(), stackToReturn);
                }
            }
        }
        return stackToReturn == null ? NULL_PAIR: Pair.of(null, stackToReturn);
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
                request.receiveAmount(take.getAmount());

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
                        stackToReturn = take;
                    } else {
                        stackToReturn.setAmount(stackToReturn.getAmount() + take.getAmount());
                    }
                    request.receiveAmount(take.getAmount());

                }
                return stackToReturn;
            }
        }
        return PREFETCH_INVALID;
    }
    protected final ItemStack getFromCrafters(ItemRequest request, ItemStack stackToReturn){
        for (SlimefunBlockData data: availableCraftersMap.values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
            stackToReturn = getFromBlockMenu(request, stackToReturn, blockMenu);
            if(request.getAmount() <= 0){
                return stackToReturn;
            }
        }
        return stackToReturn;
    }


    protected final ItemStack getFromGreedyBlocks(ItemRequest request, ItemStack stackToReturn){
        for (SlimefunBlockData data : getAdvancedGreedyBlocksMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
            stackToReturn = getFromBlockMenu(request, stackToReturn, blockMenu);
            if(request.getAmount() <= 0){
                return stackToReturn;
            }
        }
        // Greedy Blocks
        for (SlimefunBlockData data : getGreedyBlocksMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
            stackToReturn =  getFromBlockMenu(request, stackToReturn, blockMenu);
            if(request.getAmount() <= 0){
                return stackToReturn;
            }
        }
        return stackToReturn;
    }
    protected final ItemStack getFromShit(ItemRequest request, ItemStack stackToReturn){
        for (SlimefunBlockData data : getCellsMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
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
        if(!ready){
            handleAsync();
            return null;
        }
        ItemStack stackToReturn = null;
        if (request.getAmount() <= 0) {
            return stackToReturn;
        }
        // Barrels first

        Object val = getOutputableStorageIdentities().get(request);
        if(val != null){
            stackToReturn = getFromStorageIdentities(request, stackToReturn, val);
        }

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

        if (stackToReturn == null || stackToReturn.getAmount() == 0) {
            return null;
        }
        return stackToReturn;
    }
    public final ItemStack getItemStack0(Location loc, @Nonnull ItemRequest request){
        return getItemStack( request );
    }



    //will not change the request's amount
    //todo need cache
    protected long containsStorageIdentities(Object ival){
        long found = 0;
        if(ival instanceof BarrelIdentity barrelIdentity){
            if (barrelIdentity instanceof InfinityBarrel) {
                ItemStack itemStack = barrelIdentity.getItemStack();
                if (itemStack.getMaxStackSize() > 1) {
                    found += barrelIdentity.getAmount() - 2;
                }
            } else {
                found += barrelIdentity.getAmount();
            }
        }else if (ival instanceof Pair<?,?> containerData){
            ItemContainer container = (ItemContainer)containerData.getB();
            found += container.getAmount();
        }else if (ival instanceof ArrayList<?> multiple){
            for (var obj: multiple){
                found += containsStorageIdentities(obj);
            }
        }
        return found;
    }
    public boolean contains(@Nonnull ItemRequest request) {
        if(!ready){
            handleAsync();
            return false;
        }
        long found = 0;
        int requestAmount=request.getAmount();
        Object val = getOutputableStorageIdentities().get(request);
        if(val != null){
            found += containsStorageIdentities(val);
            if(found > (long) (requestAmount)){
                return true;
            }
        }

        // Crafters
        for (SlimefunBlockData data : availableCraftersMap.values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
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

        for (SlimefunBlockData data : getAdvancedGreedyBlocksMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
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
        for (SlimefunBlockData data : getGreedyBlocksMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
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
        for (SlimefunBlockData data : getCellsMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
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
    @Deprecated(forRemoval = true)
    public int getAmount(@Nonnull ItemStack itemStack) {
        if(!ready){
            handleAsync();
            return 0;
        }
        ItemStack itemStackCache= itemStack;
        long totalAmount = 0;
        for (SlimefunBlockData data : getAdvancedGreedyBlocksMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack inputSlotItem = blockMenu.getItemInSlot(slot);
                if (inputSlotItem != null && StackUtils.itemsMatch(inputSlotItem, itemStackCache)) {
                    totalAmount += inputSlotItem.getAmount();
                }
            }
        }

        for (SlimefunBlockData data : getGreedyBlocksMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            ItemStack inputSlotItem = blockMenu.getItemInSlot(slots[0]);
            if (inputSlotItem != null && StackUtils.itemsMatch(inputSlotItem, itemStackCache)) {
                totalAmount += inputSlotItem.getAmount();
            }
        }

        for (BarrelIdentity barrelIdentity :  getMapOutputAbleBarrels().values()) {
            if (StackUtils.itemsMatch(barrelIdentity, itemStackCache)) {
                totalAmount += barrelIdentity.getAmount();
                if (barrelIdentity instanceof InfinityBarrel) {
                    totalAmount -= 2;
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

        for (SlimefunBlockData data : getCellsMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
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
    @Deprecated(forRemoval = true)
    public HashMap<ItemStack, Long> getAmount(@Nonnull Set<ItemStack> itemStacks) {
        if(!ready){
            handleAsync();;
            return new HashMap<>();
        }
        HashMap<ItemStack, Long> totalAmounts = new HashMap<>();
        Set<ItemStack> itemStackCaches=itemStacks;
        for (SlimefunBlockData data : getAdvancedGreedyBlocksMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu menu = data.getBlockMenu();
            if(menu == null){
                continue;
            }
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

        for (SlimefunBlockData data : getGreedyBlocksMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
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

        for (BarrelIdentity barrelIdentity : getMapOutputAbleBarrels().values()) {
            for (ItemStack itemStack : itemStackCaches) {
                if (StackUtils.itemsMatch(barrelIdentity, itemStack)) {
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

        for (SlimefunBlockData data : getCellsMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
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
    protected boolean addItemStackGreedyBlocks(ItemStack incomingCache){
        for (SlimefunBlockData data : getAdvancedGreedyBlocksMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
            final ItemStack template = blockMenu.getItemInSlot(AdvancedGreedyBlock.TEMPLATE_SLOT);

            if (template == null || template.getType() == Material.AIR || !StackUtils.itemsMatch(incomingCache, template)) {
                continue;
            }

            blockMenu.markDirty();
            BlockMenuUtil.pushItem(blockMenu, incomingCache, ADVANCED_GREEDY_BLOCK_AVAILABLE_SLOTS);
            // Given we have found a match, it doesn't matter if the item moved or not, we will not bring it in
            return true;
        }
        for (SlimefunBlockData data : getGreedyBlocksMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
            final ItemStack template = blockMenu.getItemInSlot(NetworkGreedyBlock.TEMPLATE_SLOT);

            if (template == null || template.getType() == Material.AIR || !StackUtils.itemsMatch(incomingCache, template)) {
                continue;
            }

            blockMenu.markDirty();
            BlockMenuUtil.pushItem(blockMenu, incomingCache, GREEDY_BLOCK_AVAILABLE_SLOTS[0]);
            // Given we have found a match, it doesn't matter if the item moved or not, we will not bring it in
            return true;
        }
        return false;
    }
    protected boolean addItemStackStorageIdentities(ItemStack incoming, Object val){
        if(val instanceof BarrelIdentity barrel){
            barrel.depositItemStackExact(incoming);
            return incoming.getAmount() <= 0;
        }else if (val instanceof Pair<?,?> unitData){
            StorageUnitData unit = (StorageUnitData) unitData.getA();
            ItemContainer container = (ItemContainer) unitData.getB();
            Integer value = unit.depositItemContainer(container, incoming, incoming.getAmount(), true);
            if(value != null){
                int deposited = value;
                incoming.setAmount(incoming.getAmount() - deposited);
            }
            return incoming.getAmount() <= 0;
        }else if(val instanceof ArrayList<?> list){
            for (Object object: list){
                if(addItemStackStorageIdentities(incoming, object)){
                    return true;
                }
            }
            return false;
        }else {
            return incoming.getAmount() <= 0;
        }
    }
    @Note("we make async fix in NetworkRootPlus, so this is deprecated")
    protected void addItemStackCells(ItemStack incoming){
        for (SlimefunBlockData data : getCellsMap().values()) {
            if(data.isPendingRemove()){
                continue;
            }
            BlockMenu blockMenu = data.getBlockMenu();
            if(blockMenu == null){
                continue;
            }
            blockMenu.markDirty();
            BlockMenuUtil.pushItem(blockMenu, incoming, CELL_AVAILABLE_SLOTS);
            if (incoming.getAmount() == 0) {
                return;
            }
        }
    }


    public final void addItemStack(@Nonnull ItemStack incoming) {
        if(!ready){
            handleAsync();
            return ;
        }
        ItemStack incomingCache = incoming;// new ItemStackCache(incoming);//  ItemStackCache.of(incoming);
        if(addItemStackGreedyBlocks(incomingCache)){
            return;
        }
        Object val = getInputableStorageIdentities().get(ItemStackCache.of(incoming));
        if(val != null){
            if(addItemStackStorageIdentities(incoming, val)){
                return;
            }
        }
        addItemStackCells(incoming);
        // Run for matching barrels
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
    @Deprecated(forRemoval = true)
    public List<ItemStack> getItemStacks(@Nonnull List<ItemRequest> itemRequests) {
        if(!ready){
            handleAsync();
            return List.of();
        }
        List<ItemStack> retrievedItems = new ArrayList<>();

        for (ItemRequest request : itemRequests) {
            ItemStack retrieved = getItemStack(request);
            if (retrieved != null) {
                retrievedItems.add(retrieved);
            }
        }
        return retrievedItems;
    }

    @NotNull
    public List<ItemStack> getItemStacks0(@NotNull Location location, @NotNull List<ItemRequest> itemRequests){
        return getItemStacks(itemRequests);
    }

    @NotNull public List<BarrelIdentity> getBarrels(
        @NotNull Predicate<BarrelIdentity> filter,
        NetworkRootLocateStorageEvent.Strategy strategy,
        boolean includeEmpty) {
        List<BarrelIdentity> barrelList = Streams.concat(
            getInputAbleBarrels().stream(),
            getOutputAbleBarrels().stream()
        )
            .distinct()
            .filter(i-> includeEmpty || i.getAmount() > 0)
            .filter(filter)
            .collect(Collectors.toCollection(ArrayList::new));
        NetworkRootLocateStorageEvent event =
            new NetworkRootLocateStorageEvent(this, StorageType.BARREL, strategy, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return barrelList;
    }

    @NotNull public Map<StorageUnitData, Location> getCargoStorageUnitDatas(
        NetworkRootLocateStorageEvent.Strategy strategy, boolean includeEmpty) {
        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Map<StorageUnitData, Location> dataSet = new HashMap<>();

        final Set<Location> monitor = new HashSet<>();
        monitor.addAll(this.inputOnlyMonitors);
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
                    dataSet.put(data, testLocation);
                }
            }
        }

        NetworkRootLocateStorageEvent event =
            new NetworkRootLocateStorageEvent(this, StorageType.DRAWER, strategy, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return dataSet;
    }

    protected synchronized Map<Location, BarrelIdentity> getMapInputAbleBarrels0(){
        if (this.material2InputAbleBarrels != null) {
            return this.material2InputAbleBarrels;
        }
        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Map<Location, BarrelIdentity> barrelSet =new Object2ObjectOpenHashMap<>();

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
                    barrelSet.put(testLocation, infinityBarrel);
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
                    barrelSet.put(testLocation, fluffyBarrel);
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
                    barrelSet.put(testLocation, storage);
                }
            }
        }

        this.material2InputAbleBarrels = barrelSet;
        this.inputAbleBarrels = Collections.unmodifiableCollection(barrelSet.values());
        NetworkRootLocateStorageEvent event = new NetworkRootLocateStorageEvent(this, StorageType.BARREL, true, false, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return barrelSet;
    }

    @Nonnull
    protected Map<Location, BarrelIdentity> getMapInputAbleBarrels() {
        if(!ready){
            handleAsync();
            return (Map<Location, BarrelIdentity>) EMPTY_MAP;
        }
        if (this.material2InputAbleBarrels != null) {
            return this.material2InputAbleBarrels;
        }

        return getMapInputAbleBarrels0();
    }
    protected synchronized Map<Location, BarrelIdentity> getMapOutputAbleBarrels0(){
        if (this.material2OutputAbleBarrels != null) {
            return this.material2OutputAbleBarrels;
        }
        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Map<Location, BarrelIdentity> barrelSet = new Reference2ReferenceOpenHashMap<>();

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
                    barrelSet.put(testLocation, infinityBarrel);
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
                    barrelSet.put(testLocation, fluffyBarrel);
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
                    barrelSet.put(testLocation, storage);
                }
            }
        }

        this.material2OutputAbleBarrels = barrelSet;
        this.outputAbleBarrels = Collections.unmodifiableCollection(barrelSet.values());
        NetworkRootLocateStorageEvent event = new NetworkRootLocateStorageEvent(this, StorageType.BARREL, false, true, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return barrelSet;
    }
    @Nonnull
    protected Map<Location, BarrelIdentity> getMapOutputAbleBarrels() {
        if(!ready){
            handleAsync();
            return (Map<Location, BarrelIdentity>) EMPTY_MAP;
        }
        if (this.material2OutputAbleBarrels != null) {
            return this.material2OutputAbleBarrels;
        }
      //  Networks.getInstance().getLogger().info("Initialize "+nodePosition+" root output barrels uid "+locUniqueId+ " status "+ready);
//        Preconditions.checkArgument(ready);
        return getMapOutputAbleBarrels0();

    }
    public synchronized Map<StorageUnitData,Location> getInputAbleCargoStorageUnitDatas(){
        Map map = getInputAbleCargoStorageUnitDatas0();
        return map.isEmpty()?map:((BidiMap)map).inverseBidiMap();
    }
    public synchronized Map<Location, StorageUnitData> getInputAbleCargoStorageUnitDatas00(){
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
    @Nonnull
    public Map<Location, StorageUnitData> getInputAbleCargoStorageUnitDatas0() {
        if(!ready){
            handleAsync();
            return (Map<Location, StorageUnitData>) EMPTY_MAP;
        }
        if (this.inputAbleCargoStorageUnitDatas != null) {
            return this.inputAbleCargoStorageUnitDatas;
        }
        return getInputAbleCargoStorageUnitDatas00();
    }
    public void handleAsync(){
        //Networks.getInstance().getLogger().info("Network is not ready! ");
    }
    public Map<StorageUnitData, Location> getOutputAbleCargoStorageUnitDatas(){
        Map map = getOutputAbleCargoStorageUnitDatas0();
        return map.isEmpty()?map:((BidiMap)map).inverseBidiMap();
    }
    protected synchronized Map<Location, StorageUnitData> getOutputAbleCargoStorageUnitDatas00(){
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
    @Nonnull
    protected Map<Location, StorageUnitData> getOutputAbleCargoStorageUnitDatas0() {
        if(!ready){
            handleAsync();
            return Map.of();
        }
        if (this.outputAbleCargoStorageUnitDatas != null) {
            return this.outputAbleCargoStorageUnitDatas;
        }
        return getOutputAbleCargoStorageUnitDatas00();
    }
    protected synchronized ItemCacheHashMap<ItemHashCache, Object> getInputableStorageIdentities0(){
        if(this.inputableStorageIdentities != null){
            return this.inputableStorageIdentities;
        }
        var a= getMapInputAbleBarrels0();
        var b= getInputAbleCargoStorageUnitDatas00();
        ItemCacheHashMap<ItemHashCache, Object> cacheMap = new ItemCacheHashMap<>(a.size()+ b.size() +32, ItemStackCacheStrategy.INSTANCE);
        for (var entry: a.values()){
            if(entry.getItemStack() != null && entry.getItemStack().getType() != Material.AIR){
                cacheMap.compute(entry,(ignoredKey, val)->NetworkRoot.addOrCreateList(entry, val));
            }
        }
        for (var unit: b.values()){
            for(var itemContainer: unit.getStoredItems()){
                if(itemContainer.getItemStack() != null && itemContainer.getItemStack().getType() != Material.AIR){
                cacheMap.compute(itemContainer,(ignoredKey, val)-> NetworkRoot.addOrCreatListForStorageUnit(unit, itemContainer, val));
                }
            }
        }
        this.inputableStorageIdentities = cacheMap;
        return this.inputableStorageIdentities;
    }

    protected ItemCacheHashMap<ItemHashCache, Object> getInputableStorageIdentities(){
        if(!ready){
            handleAsync();
            return new ItemCacheHashMap<>(false);
        }
        if(this.inputableStorageIdentities != null){
            return this.inputableStorageIdentities;
        }
        return getInputableStorageIdentities0();
    }
    protected synchronized ItemCacheHashMap<ItemHashCache, Object> getOutputableStorageIdentities0(){
        if(this.outputableStorageIdentities != null){
            return this.outputableStorageIdentities;
        }
        var a= getMapOutputAbleBarrels0();
        var b= getOutputAbleCargoStorageUnitDatas00();
        ItemCacheHashMap<ItemHashCache, Object> cacheMap = new ItemCacheHashMap<>(a.size()+ b.size() +32, ItemStackCacheStrategy.INSTANCE);
        for (var entry: a.values()){
            if(entry.getItemStack() != null && entry.getItemStack().getType() != Material.AIR){
                cacheMap.compute(entry,(ignoredKey, val)->NetworkRoot.addOrCreateList(entry, val));
            }
        }
        for (var unit: b.values()){
            for(var itemContainer: unit.getStoredItems()){
                if(itemContainer.getItemStack() != null && itemContainer.getItemStack().getType() != Material.AIR){
                    cacheMap.compute(itemContainer,(ignoredKey, val)-> NetworkRoot.addOrCreatListForStorageUnit(unit, itemContainer, val));
                }
            }
        }
        this.outputableStorageIdentities = cacheMap;
        return this.outputableStorageIdentities;
    }
    protected ItemCacheHashMap<ItemHashCache, Object> getOutputableStorageIdentities(){
        if(!ready){
            handleAsync();
            return new ItemCacheHashMap<>(false);
        }
        if(this.outputableStorageIdentities != null){
            return this.outputableStorageIdentities;
        }
        return getOutputableStorageIdentities0();
    }
    protected static Object addOrCreateList(BarrelIdentity e, Object i) {
        if(i == null){
            return e;
        }else if(i instanceof ArrayList array){
            array.add(e);
            return array;
        }else {
            var list = new ArrayList<>(4);
            list.add(i);
            list.add(e);
            return list;
        }
    }
    protected static Object addOrCreatListForStorageUnit(StorageUnitData unit, ItemContainer e, Object i){
        if(i == null){
            return Pair.of(unit, e);
        }else if(i instanceof ArrayList array){
            array.add(Pair.of(unit, e));
            return array;
        }else {
            var list = new ArrayList<>(4);
            list.add(i);
            list.add(Pair.of(unit, e));
            return list;
        }
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

        getMapInputAbleBarrels0();
        getMapOutputAbleBarrels0();
        getInputAbleCargoStorageUnitDatas00();
        getOutputAbleCargoStorageUnitDatas00();
        getInputableStorageIdentities0();
        getOutputableStorageIdentities0();
    }
    public boolean refreshRootItems() {
        resetRootItems();
        initRootItems();
        return true;
    }

    @org.jetbrains.annotations.Nullable
    public BarrelIdentity accessInputAbleBarrel(Location barrelLocation) {
        return getMapInputAbleBarrels().get(barrelLocation);
    }

    @org.jetbrains.annotations.Nullable
    public BarrelIdentity accessOutputAbleBarrel(Location barrelLocation) {
        return getMapOutputAbleBarrels().get(barrelLocation);
    }

    @org.jetbrains.annotations.Nullable
    public StorageUnitData accessInputAbleDrawerData(Location drawerLocation) {
        return accessInputAbleCargoStorageUnitData(drawerLocation);
    }

    @org.jetbrains.annotations.Nullable
    public StorageUnitData accessOutputAbleDrawerData(Location drawerLocation) {
        return accessOutputAbleCargoStorageUnitData(drawerLocation);
    }

    @org.jetbrains.annotations.Nullable
    public StorageUnitData accessInputAbleCargoStorageUnitData(Location storageUnitLocation) {
        return getInputAbleCargoStorageUnitDatas0().get(storageUnitLocation);
    }

    @org.jetbrains.annotations.Nullable
    public StorageUnitData accessOutputAbleCargoStorageUnitData(Location storageUnitLocation) {
        return getOutputAbleCargoStorageUnitDatas0().get(storageUnitLocation);
    }

    @org.jetbrains.annotations.Nullable
    public ItemStack requestItem(@NotNull Location accessor, @NotNull ItemRequest request) {
        return getItemStack0(accessor, request);
    }

    @org.jetbrains.annotations.Nullable
    public ItemStack requestItem(@NotNull Location accessor, @NotNull ItemStack itemStack) {
        return requestItem(accessor, new ItemRequest(itemStack, itemStack.getAmount()));
    }

    public void tryRecord(@NotNull Location accessor, @NotNull ItemRequest request) {
//        if (recordFlow && itemFlowRecord != null) {
//            itemFlowRecord.addAction(accessor, request);
//        }
    }

    public void addItem(@NotNull Location accessor, @NotNull ItemStack incoming){
        addItemStack(incoming);
    }

    public void tryRecord(@NotNull Location accessor, @org.jetbrains.annotations.Nullable ItemStack before, int after) {
//        if (recordFlow && itemFlowRecord != null && before != null) {
//            itemFlowRecord.addAction(accessor, before, after);
//        }
    }

    public void addItemStack0(@NotNull Location accessor, @NotNull ItemStack incoming){
        addItemStack(incoming);
    }

    public boolean allowAccessInput(@NotNull Location accessor) {
        return true;
    }

    public boolean allowAccessOutput(@NotNull Location accessor) {
        return true;
    }

    public void addTransportInputMiss(@NotNull Location location) {

    }

    public void addTransportOutputMiss(@NotNull Location location) {
    }

    public void reduceTransportInputMiss(@NotNull Location location) {

    }

    public void reduceTransportOutputMiss(@NotNull Location location) {

    }

    public void controlAccessInput(@NotNull Location accessor) {

    }

    public void controlAccessOutput(@NotNull Location accessor) {

    }

    public void uncontrolAccessInput(@NotNull Location accessor) {
    }

    public void uncontrolAccessOutput(@NotNull Location accessor) {
    }

    public int getCellsSize() {
        return getCellsMap().size();
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
        public static PusherPrefetcherInfo DEFAULT = new PusherPrefetcherInfo(){
            @Override
            public ItemStack getItemStackWithPrefetch(NetworkRoot root, ItemRequest request) {
                if(!root.ready){
                    return null;
                }
                return root.getItemStack(request);
            }
        };
        /**
         * this method is synchronized because of prefetchInfo
         * luckly, we have the optimization of grabbing only once in linear push operation
         * @param root
         * @param request
         * @return
         */
        public ItemStack getItemStackWithPrefetch(NetworkRoot root, ItemRequest request){
            if(!root.ready){
                return null;
            }
            if( !ExperimentalFeatureManager.getInstance().isEnablePrefetchPusher()){
                return root.getItemStack(request);
            }
            return root.getItemStack(request);
//            int endCode;
//            ItemStack stackToReturn = null;
//            synchronized (this){
//                if(prefetchInfo == null){
//                    var prefetching = root.prefetchInternal(request);
//                    stackToReturn = prefetching.getB();
//                    prefetchInfo = prefetching.getA();
//                    endCode = 0;
//                }else{
//                    ItemStack stackToReturn0;
//                    switch (prefetchInfo.source()){
//                        case QUANTUM:
//                            stackToReturn0 = root.prefetchFromBarrels(request, prefetchInfo.loc(), stackToReturn, false);
//                            if(stackToReturn0 == PREFETCH_INVALID || request.getAmount() > 0){
//                                //if this index do not satisfy the request, then we consider it as a NOT-ENOUGH index, we deserves better index, so search for it
//                                prefetchInfo = null;
//                                endCode = 1;
//                            }else {
//                                //request is satisfied, continue
//                                stackToReturn = stackToReturn0;
//                                endCode = 2;
//                                //return root.getItemStack0(request, stackToReturn, 1);
//                            }
//                            break;
//                        case STORAGE_DATA:
//                            stackToReturn0 = root.prefetchFromStorages(request, prefetchInfo.loc() , ((StorageUnitDataIndex)prefetchInfo).internalIndex(), stackToReturn, false);
//                            //wew suppose that it should be single source item
//                            if(stackToReturn0 == PREFETCH_INVALID){
//                                prefetchInfo = null;
//                                endCode = 3;
//                            }else {
//                                stackToReturn = stackToReturn0;
//                                endCode = 4;
//                            }
//                            break;
//                        default:
//                            prefetchInfo = null;
//                            endCode = 5;
//                            break;
//                    }
//                }
//            }
//            return switch (endCode){
//                case 0->root.getItemStack0(request, stackToReturn, prefetchInfo == null ? StorageSource.UNKNOWN.ordinal():(prefetchInfo.source().ordinal()+1));
//                case 1, 3, 5 ->getItemStackWithPrefetch(root, request);
//                case 2->stackToReturn;
//                case 4->root.getItemStack0(request, stackToReturn, 2);
//                default -> null;
//            };

        }
    }
}