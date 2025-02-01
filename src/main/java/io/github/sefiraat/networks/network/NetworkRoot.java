package io.github.sefiraat.networks.network;

import com.balugaq.netex.api.data.ItemContainer;
import com.balugaq.netex.api.data.StorageUnitData;
import com.balugaq.netex.api.enums.StorageType;
import com.balugaq.netex.api.events.NetworkRootLocateStorageEvent;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.balugaq.netex.utils.NetworksVersionedParticle;
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
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.network.stackcaches.ItemStackCache;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkCell;
import io.github.sefiraat.networks.slimefun.network.NetworkDirectional;
import io.github.sefiraat.networks.slimefun.network.NetworkGreedyBlock;
import io.github.sefiraat.networks.slimefun.network.NetworkPowerNode;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.ncbpfluffybear.fluffymachines.items.Barrel;
import lombok.Getter;
import lombok.Setter;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
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
import java.util.stream.IntStream;

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
    @Getter
    private final Set<Location> cells = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> grabbers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> pushers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> purgers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> crafters = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> powerNodes = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> powerDisplays = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> encoders = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> greedyBlocks = ConcurrentHashMap.newKeySet();
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
    @Getter
    private final Set<Location> advancedGreedyBlocks = ConcurrentHashMap.newKeySet();
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
    @Getter
    private Set<BarrelIdentity> barrels = null;
    @Getter
    private Set<BarrelIdentity> inputAbleBarrels = null;
    @Getter
    private Set<BarrelIdentity> outputAbleBarrels = null;

    private Map< Material,Set<BarrelIdentity>> material2Barrels = null;
    private Map< Material,Set<BarrelIdentity>> material2InputAbleBarrels = null;
    private Map< Material,Set<BarrelIdentity>> material2OutputAbleBarrels = null;

    @Deprecated
    @Getter
    private Map<StorageUnitData, Location> cargoStorageUnitDatas = null;
    @Getter
    private Map<StorageUnitData, Location> inputAbleCargoStorageUnitDatas = null;
    @Getter
    private Map<StorageUnitData, Location> outputAbleCargoStorageUnitDatas = null;
    @Getter
    @Setter
    private boolean ready = false;
    private static final ConcurrentHashMap<Location, AtomicInteger> rootCounters = new ConcurrentHashMap<>();
    @Getter
    private int locUniqueId;



    @Getter
    private long rootPower = 0;

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
                BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
                if (blockMenu == null) {
                    return;
                }
                if (Arrays.equals(blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW), CELL_AVAILABLE_SLOTS)) {
                    cells.add(location);
                }
            }
            case GRABBER -> grabbers.add(location);
            case PUSHER -> pushers.add(location);
            case PURGER -> purgers.add(location);
            case CRAFTER -> crafters.add(location);
            case POWER_NODE -> powerNodes.add(location);
            case POWER_DISPLAY -> powerDisplays.add(location);
            case ENCODER -> encoders.add(location);
            case GREEDY_BLOCK -> {
                /*
                 * Fix https://github.com/Sefiraat/Networks/issues/211
                 */
                BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
                if (blockMenu == null) {
                    return;
                }
                if (Arrays.equals(blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW), GREEDY_BLOCK_AVAILABLE_SLOTS)) {
                    greedyBlocks.add(location);
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
                BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
                if (blockMenu == null) {
                    return;
                }
                if (Arrays.equals(blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW), ADVANCED_GREEDY_BLOCK_AVAILABLE_SLOTS)) {
                    advancedGreedyBlocks.add(location);
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
        for(var barrels:getOutputAbleBarrels().values()){
            for (BarrelIdentity barrelIdentity : barrels) {
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
        Map<StorageUnitData, Location> cacheMap = getOutputAbleCargoStorageUnitDatas();
        for (StorageUnitData cache : cacheMap.keySet()) {
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
    protected synchronized Map<Material,Set<BarrelIdentity>> getBarrels() {
        if(!ready){
            handleAsync();
            return Map.of();
        }
        if (this.material2Barrels != null) {
            return this.material2Barrels;
        }

        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Map<Material,Set<BarrelIdentity>> barrelSet =new EnumMap<>(Material.class);

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
                    barrelSet.computeIfAbsent(infinityBarrel.getItemType(),(i)->ConcurrentHashMap.newKeySet()).add(infinityBarrel);
                }
                continue;
            } else if (Networks.getSupportedPluginManager().isFluffyMachines() && slimefunItem instanceof Barrel barrel) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final FluffyBarrel fluffyBarrel = getFluffyBarrel(menu, barrel);
                if (fluffyBarrel != null) {
                    barrelSet.computeIfAbsent(fluffyBarrel.getItemType(),(i)->ConcurrentHashMap.newKeySet()).add(fluffyBarrel);
                }
                continue;
            } else if (slimefunItem instanceof NetworkQuantumStorage) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final NetworkStorage storage = getNetworkStorage(menu);
                if (storage != null) {
                    barrelSet.computeIfAbsent(storage.getItemType(),(i)->ConcurrentHashMap.newKeySet()).add(storage);
                }
            }
        }

        this.material2Barrels = barrelSet;
        Set<BarrelIdentity> barrels = ConcurrentHashMap.newKeySet();
        barrelSet.forEach((key,value)->barrels.addAll(value));
        this.barrels = barrels;
        NetworkRootLocateStorageEvent event = new NetworkRootLocateStorageEvent(this, StorageType.BARREL, true, true, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return barrelSet;
    }

    @Deprecated
    @Nonnull
    public synchronized Map<StorageUnitData, Location> getCargoStorageUnitDatas() {
        if(!ready){
            handleAsync();
            return Map.of();
        }
        if (this.cargoStorageUnitDatas != null) {
            return this.cargoStorageUnitDatas;
        }

        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Map<StorageUnitData, Location> dataSet = new HashMap<>();

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
                    dataSet.put(data, testLocation);
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
        final Set<BlockMenu> menus = new HashSet<>();
        for (Location cellLocation : this.cells) {
            BlockMenu menu = StorageCacheUtils.getMenu(cellLocation);
            if (menu != null) {
                menus.add(menu);
            }
        }
        return menus;
    }

    @Nonnull
    public Set<BlockMenu> getCrafterOutputs() {
        final Set<BlockMenu> menus = new HashSet<>();
        for (Location location : this.crafters) {
            BlockMenu menu = StorageCacheUtils.getMenu(location);
            if (menu != null) {
                menus.add(menu);
            }
        }
        return menus;
    }

    @Nonnull
    public Set<BlockMenu> getGreedyBlockMenus() {
        final Set<BlockMenu> menus = new HashSet<>();
        for (Location location : this.greedyBlocks) {
            BlockMenu menu = StorageCacheUtils.getMenu(location);
            if (menu != null) {
                menus.add(menu);
            }
        }
        return menus;
    }

    @Nonnull
    public Set<BlockMenu> getAdvancedGreedyBlockMenus() {
        final Set<BlockMenu> menus = new HashSet<>();
        for (Location location : this.advancedGreedyBlocks) {
            BlockMenu menu = StorageCacheUtils.getMenu(location);
            if (menu != null) {
                menus.add(menu);
            }
        }
        return menus;
    }

    @Nullable
    public ItemStack getItemStack(@Nonnull ItemRequest request) {
        ItemStack stackToReturn = null;
        if (request.getAmount() <= 0) {
            return null;
        }

        // Barrels first
        var barrels=getOutputAbleBarrels().get(request.getItemType());
        if(barrels != null) {
            for (BarrelIdentity barrelIdentity :barrels) {
                if (barrelIdentity.getItemStack() == null || !StackUtils.itemsMatch(request, barrelIdentity)) {
                    continue;
                }

                boolean infinity = barrelIdentity instanceof InfinityBarrel;
                final ItemStack fetched = barrelIdentity.requestItem(request);
                if (fetched == null || fetched.getType() == Material.AIR || (infinity && fetched.getAmount() == 1)) {
                    continue;
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
                    return stackToReturn;
                } else {
                    stackToReturn.setAmount(stackToReturn.getAmount() + preserveAmount);
                    request.receiveAmount(preserveAmount);
                    fetched.setAmount(fetched.getAmount() - preserveAmount);
                }
            }
        }


        // Units
        for (StorageUnitData cache : getOutputAbleCargoStorageUnitDatas().keySet()) {
            ItemStack take = cache.requestItem(request);
            if (take != null) {
                if (stackToReturn == null) {
                    stackToReturn = take.clone();
                } else {
                    stackToReturn.setAmount(stackToReturn.getAmount() + take.getAmount());
                }
                request.receiveAmount(stackToReturn.getAmount());

                if (request.getAmount() <= 0) {
                    return stackToReturn;
                }
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

                // Mark the Cell as dirty otherwise the changes will not save on shutdown
                blockMenu.markDirty();

                // If the return stack is null, we need to set it up
                if (stackToReturn == null) {
                    stackToReturn = itemStack.clone();
                    stackToReturn.setAmount(0);
                }

                if (request.getAmount() <= itemStack.getAmount()) {
                    // We can't take more than this stack. Level to request amount, remove items and then return
                    stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                    itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                    return stackToReturn;
                } else {
                    // We can take more than what is here, consume before trying to take more
                    stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                    request.receiveAmount(itemStack.getAmount());
                    itemStack.setAmount(0);
                }
            }
        }

        // Crafters
        for (BlockMenu blockMenu : getCrafterOutputs()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null || itemStack.getType() == Material.AIR || !StackUtils.itemsMatch(
                        request,
                        itemStack
                )) {
                    continue;
                }

                // Stack is null, so we can fill it here
                if (stackToReturn == null) {
                    stackToReturn = itemStack.clone();
                    stackToReturn.setAmount(0);
                }

                if (request.getAmount() <= itemStack.getAmount()) {
                    stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                    itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                    return stackToReturn;
                } else {
                    stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                    request.receiveAmount(itemStack.getAmount());
                    itemStack.setAmount(0);
                }
            }
        }

        for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null || itemStack.getType() == Material.AIR || !StackUtils.itemsMatch(
                        request,
                        itemStack
                )) {
                    continue;
                }

                // Stack is null, so we can fill it here
                if (stackToReturn == null) {
                    stackToReturn = itemStack.clone();
                    stackToReturn.setAmount(0);
                }

                if (request.getAmount() <= itemStack.getAmount()) {
                    stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                    itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                    return stackToReturn;
                } else {
                    stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                    request.receiveAmount(itemStack.getAmount());
                    itemStack.setAmount(0);
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

            // Mark the Cell as dirty otherwise the changes will not save on shutdown
            blockMenu.markDirty();

            // If the return stack is null, we need to set it up
            if (stackToReturn == null) {
                stackToReturn = itemStack.clone();
                stackToReturn.setAmount(0);
            }

            if (request.getAmount() <= itemStack.getAmount()) {
                // We can't take more than this stack. Level to request amount, remove items and then return
                stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                return stackToReturn;
            } else {
                // We can take more than what is here, consume before trying to take more
                stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                request.receiveAmount(itemStack.getAmount());
                itemStack.setAmount(0);
            }
        }

        if (stackToReturn == null || stackToReturn.getAmount() == 0) {
            return null;
        }

        return stackToReturn;
    }
    //will not change the request's amount
    //todo need cache
    public boolean contains(@Nonnull ItemRequest request) {

        long found = 0;
        int requestAmount=request.getAmount();
        // Barrels
        var barrels=getOutputAbleBarrels().get(request.getItemType());
        if(barrels!=null){
            for (BarrelIdentity barrelIdentity : barrels) {
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

        Map<StorageUnitData, Location> cacheMap = getOutputAbleCargoStorageUnitDatas();
        for (StorageUnitData cache : cacheMap.keySet()) {
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
        ItemStackCache itemStackCache=ItemStackCache.of(itemStack);
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
        var barrels=getOutputAbleBarrels().get(itemStack.getType());
        if(barrels!=null){
            for (BarrelIdentity barrelIdentity : barrels) {
                if (StackUtils.itemsMatch(barrelIdentity, itemStackCache)) {
                    totalAmount += barrelIdentity.getAmount();
                    if (barrelIdentity instanceof InfinityBarrel) {
                        totalAmount -= 2;
                    }
                }
            }
        }
        Map<StorageUnitData, Location> cacheMap = getOutputAbleCargoStorageUnitDatas();
        for (StorageUnitData cache : cacheMap.keySet()) {
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
        Set<ItemStackCache> itemStackCaches=itemStacks.stream().map(ItemStackCache::of).collect(Collectors.toSet());
        for (BlockMenu menu : getAdvancedGreedyBlockMenus()) {
            int[] slots = menu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack inputSlotItem = menu.getItemInSlot(slot);
                if (inputSlotItem != null) {
                    for (ItemStackCache itemStack : itemStackCaches) {
                        if (StackUtils.itemsMatch(inputSlotItem, itemStack)) {
                            totalAmounts.compute(itemStack.getItemStack(),(i,oldValue)->oldValue==null?(long)( inputSlotItem.getAmount()):oldValue+inputSlotItem.getAmount());
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
                for (ItemStackCache itemStack : itemStackCaches) {
                    if (StackUtils.itemsMatch(inputSlotItem, itemStack)) {
                        totalAmounts.compute(itemStack.getItemStack(),(i,oldValue)->oldValue==null?(long)(inputSlotItem.getAmount()):oldValue+inputSlotItem.getAmount());
                        //totalAmounts.put(itemStack, totalAmounts.getOrDefault(itemStack, 0L) + inputSlotItem.getAmount());
                    }
                }
            }
        }
        for (var entry:getOutputAbleBarrels().entrySet()){
            if(entry.getValue()==null||entry.getValue().isEmpty())continue;
            for (BarrelIdentity barrelIdentity : entry.getValue()) {
                for (ItemStackCache itemStack : itemStackCaches) {
                    if (itemStack.getItemType()==entry.getKey()&& StackUtils.itemsMatch(barrelIdentity, itemStack)) {
                        long totalAmount = barrelIdentity.getAmount();
                        if (barrelIdentity instanceof InfinityBarrel) {
                            totalAmount -= 2;
                        }
                        final long total=totalAmount;
                        totalAmounts.compute(itemStack.getItemStack(),(i,oldValue)->oldValue==null?(long)total:oldValue+total);
                       // totalAmounts.put(itemStack, totalAmounts.getOrDefault(itemStack, 0L) + totalAmount);
                    }
                }
            }
        }
        Map<StorageUnitData, Location> cacheMap = getOutputAbleCargoStorageUnitDatas();
        for (StorageUnitData cache : cacheMap.keySet()) {
            final List<ItemContainer> storedItems = cache.getStoredItems();
            for (ItemContainer itemContainer : storedItems) {
                for (ItemStackCache itemStack : itemStackCaches) {
                    if (StackUtils.itemsMatch(itemContainer.getSample(), itemStack)) {
                        long totalAmount = itemContainer.getAmount();
                        totalAmounts.compute(itemStack.getItemStack(),(i,oldValue)->oldValue==null?(long)totalAmount:oldValue+totalAmount);
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
                    for (ItemStackCache itemStack : itemStackCaches) {
                        if (StackUtils.itemsMatch(cellItem, itemStack)) {
                            totalAmounts.compute(itemStack.getItemStack(),(i,oldValue)->oldValue==null?(long)cellItem.getAmount():oldValue+cellItem.getAmount());
//                            totalAmounts.put(itemStack.getItemStack(), totalAmounts.getOrDefault(itemStack, 0L) + cellItem.getAmount());
                        }
                    }
                }
            }
        }

        return totalAmounts;
    }

    public void addItemStack(@Nonnull ItemStack incoming) {
        ItemStackCache incomingCache = ItemStackCache.of(incoming);
        for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {
            final ItemStack template = blockMenu.getItemInSlot(AdvancedGreedyBlock.TEMPLATE_SLOT);

            if (template == null || template.getType() == Material.AIR || !StackUtils.itemsMatch(incomingCache, template)) {
                continue;
            }

            blockMenu.markDirty();
            BlockMenuUtil.pushItem(blockMenu, incomingCache,false, ADVANCED_GREEDY_BLOCK_AVAILABLE_SLOTS);
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
            BlockMenuUtil.pushItem(blockMenu, incomingCache,false, GREEDY_BLOCK_AVAILABLE_SLOTS[0]);
            // Given we have found a match, it doesn't matter if the item moved or not, we will not bring it in
            return;
        }


        // Run for matching barrels
        var barrels=getInputAbleBarrels().get(incoming.getType());
        if(barrels!=null){
            for (BarrelIdentity barrelIdentity : barrels) {
                if (StackUtils.itemsMatch(barrelIdentity, incomingCache)) {
                    barrelIdentity.depositItemStack(incoming);

                    // All distributed, can escape
                    if (incoming.getAmount() == 0) {
                        return;
                    }
                }
            }
        }

        for (StorageUnitData cache : getInputAbleCargoStorageUnitDatas().keySet()) {
            cache.depositItemStack(incoming, true);

            if (incoming.getAmount() == 0) {
                return;
            }
        }

        for (BlockMenu blockMenu : getCellMenus()) {
            blockMenu.markDirty();
            BlockMenuUtil.pushItem(blockMenu, incomingCache,false, CELL_AVAILABLE_SLOTS);
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
    protected synchronized Map<Material,Set<BarrelIdentity>> getInputAbleBarrels() {
        if(!ready){
            handleAsync();
            return Map.of();
        }
        if (this.material2InputAbleBarrels != null) {
            return this.material2InputAbleBarrels;
        }

        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Map<Material,Set<BarrelIdentity>> barrelSet =new EnumMap<>(Material.class);

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
                    barrelSet.computeIfAbsent(infinityBarrel.getItemType(),(i)->ConcurrentHashMap.newKeySet()).add(infinityBarrel);
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
                    barrelSet.computeIfAbsent(fluffyBarrel.getItemType(),(i)->ConcurrentHashMap.newKeySet()).add(fluffyBarrel);
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
                    barrelSet.computeIfAbsent(storage.getItemType(),(i)->ConcurrentHashMap.newKeySet()).add(storage);
                }
            }
        }

        this.material2InputAbleBarrels = barrelSet;
        Set<BarrelIdentity> barrels = ConcurrentHashMap.newKeySet();
        barrelSet.forEach((key,value)->barrels.addAll(value));
        this.inputAbleBarrels = barrels;
        NetworkRootLocateStorageEvent event = new NetworkRootLocateStorageEvent(this, StorageType.BARREL, true, false, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return barrelSet;
    }

    @Nonnull
    protected synchronized Map<Material,Set<BarrelIdentity>> getOutputAbleBarrels() {
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
        final Map<Material,Set<BarrelIdentity>> barrelSet = new EnumMap<>(Material.class);

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
                    barrelSet.computeIfAbsent(infinityBarrel.getItemType(),(i)->ConcurrentHashMap.newKeySet()).add(infinityBarrel);
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
                    barrelSet.computeIfAbsent(fluffyBarrel.getItemType(),(i)->ConcurrentHashMap.newKeySet()).add(fluffyBarrel);
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
                    barrelSet.computeIfAbsent(storage.getItemType(),(i)->ConcurrentHashMap.newKeySet()).add(storage);
                }
            }
        }

        this.material2OutputAbleBarrels = barrelSet;
        Set<BarrelIdentity> barrels = ConcurrentHashMap.newKeySet();
        barrelSet.forEach((key,value)->barrels.addAll(value));
        this.outputAbleBarrels = barrels;
        NetworkRootLocateStorageEvent event = new NetworkRootLocateStorageEvent(this, StorageType.BARREL, false, true, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return barrelSet;
    }

    @Nonnull
    public synchronized Map<StorageUnitData, Location> getInputAbleCargoStorageUnitDatas() {
        if(!ready){
            handleAsync();
            return Map.of();
        }
        if (this.inputAbleCargoStorageUnitDatas != null) {
            return this.inputAbleCargoStorageUnitDatas;
        }

        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Map<StorageUnitData, Location> dataSet = new HashMap<>();

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
                    dataSet.put(data, testLocation);
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
    @Nonnull
    public synchronized Map<StorageUnitData, Location> getOutputAbleCargoStorageUnitDatas() {
        if(!ready){
            handleAsync();
            return Map.of();
        }
        if (this.outputAbleCargoStorageUnitDatas != null) {
            return this.outputAbleCargoStorageUnitDatas;
        }

        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Map<StorageUnitData, Location> dataSet = new HashMap<>();

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
                    dataSet.put(data, testLocation);
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
        this.cargoStorageUnitDatas = null;
        this.inputAbleBarrels = null;
        this.outputAbleBarrels = null;
        this.inputAbleCargoStorageUnitDatas = null;
        this.outputAbleCargoStorageUnitDatas = null;
    }
    public void initRootItems(){
        getBarrels();
        getCargoStorageUnitDatas();
        getInputAbleBarrels();
        getOutputAbleBarrels();
        getInputAbleCargoStorageUnitDatas();
        getOutputAbleCargoStorageUnitDatas();
    }
    public boolean refreshRootItems() {
        resetRootItems();
        initRootItems();
        return true;
    }
}