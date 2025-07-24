package io.github.sefiraat.networks.network;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.slimefun.network.NetworkController;
import io.github.sefiraat.networks.slimefun.network.NetworkPowerNode;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;

import java.lang.invoke.VarHandle;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.ToString;
import me.matl114.matlib.utils.reflect.ReflectUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

@ToString
public class NetworkNode {

    protected static final Set<BlockFace> VALID_FACES =
            EnumSet.of(BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);

    @Getter
    protected final Set<NetworkNode> childrenNodes = new HashSet<>();
    @Getter
    protected NetworkNode parent = null;
    protected volatile NetworkRoot root ;

    protected NetworkRoot newRoot ;
    protected Location nodePosition;
    protected NodeType nodeType;
    @Getter
    protected long power;
    //for child NetworkRoot
    protected NetworkNode(Location location, NodeType type) {
        this.nodePosition = location;
        this.nodeType = type;
        this.power = retrieveBlockCharge();
        this.newRoot = null;
    }
    public NetworkNode(Location location, NodeType type,@Nullable NetworkRoot history,@Nonnull NetworkRoot rootUpdate) {
        this.nodePosition = location;
        this.nodeType = type;
        this.power = retrieveBlockCharge();
        this.root = history;
        this.newRoot = rootUpdate;
    }

    public void addChild(@Nonnull NetworkNode child) {
        child.setParent(this);
        //if newRoot present. fetch new root ,register info into newRoot
        NetworkRoot rootUpdate = this.getRootUpdateInternal();
        //pass update to children
        rootUpdate.addRootPower(child.getPower());
        rootUpdate.registerNode(child.nodePosition, child.nodeType);
        this.childrenNodes.add(child);
    }

    @Nonnull
    public Location getNodePosition() {
        return nodePosition;
    }

    @Nonnull
    public NodeType getNodeType() {
        return nodeType;
    }

    public boolean networkContains(@NotNull NetworkNode networkNode) {
        return networkContains(networkNode.nodePosition);
    }

    public boolean networkContains(@NotNull Location location) {
        if (this.root == null) {
            return false;
        }

        return this.root.getNodeLocations().contains(location);
    }
    //获取可靠的root,
    private static final VarHandle ROOT_ATOMIC_UPDATE = ReflectUtils.getVarHandlePrivate(NetworkNode.class, "root").withInvokeExactBehavior();
    @Nonnull
    public NetworkRoot getRoot() {
        NetworkRoot currentRoot;
        NetworkRoot currentNewRoot;
        do{
            currentRoot = this.root;
            currentNewRoot = this.newRoot;
            if (!(currentRoot == null || (currentNewRoot != null && currentNewRoot.isReady()))) {
                return currentRoot;
            }
        }while (! ROOT_ATOMIC_UPDATE.compareAndSet((NetworkNode)this, (NetworkRoot)currentRoot, (NetworkRoot)currentNewRoot));
        this.newRoot = null;
        return currentNewRoot;
    }
    //获取最新版的root 可能是未完成的
    @Nonnull
    protected NetworkRoot getRootUpdateInternal() {
        return this.newRoot;
    }

//    protected void setRootInternal(@Nonnull NetworkRoot root) {
//        this.root = root;
//        this.newRoot = null;
//    }
    private void setRoot(NetworkRoot root) {
        this.newRoot = root;
    }

    private void setParent(NetworkNode parent) {
        this.parent = parent;
    }
    //in building period, should use getRootUpdate
    public void addAllChildren() {
        // Loop through all possible locations
        for (BlockFace face : VALID_FACES) {
            final Location testLocation = this.nodePosition.clone().add(face.getDirection());
            final NodeDefinition testDefinition = NetworkStorage.getNode(testLocation);

            if (testDefinition == null) {
                continue;
            }

            final NodeType testType = testDefinition.getType();

            // Kill additional controllers if it isn't the root
            if (testType == NodeType.CONTROLLER && !testLocation.equals(getRootUpdateInternal() .nodePosition)) {
                killAdditionalController(testLocation);
                continue;
            }

            // Check if it's in the network already and, if not, create a child node and propagate further.
            NetworkRoot rootUpdate = this.getRootUpdateInternal();
            if (testType != NodeType.CONTROLLER && !rootUpdate.getNodeLocations().contains(testLocation)) {
                if (rootUpdate .getNodeCount() >= rootUpdate .getMaxNodes()) {
                    rootUpdate .setOverburdened(true);
                    return;
                }
                NetworkRoot historyRoot ;
                if(testDefinition.getNode()!=null){
                    historyRoot = testDefinition.getNode().getRoot();
                }else{
                    historyRoot = null;
                }
                //move the root set in addChild to <init>
                final NetworkNode networkNode = new NetworkNode(testLocation, testType, historyRoot, rootUpdate);
                addChild(networkNode);
                networkNode.addAllChildren();
                testDefinition.setNode(networkNode);
                NetworkStorage.registerNode(testLocation, testDefinition);
            }
        }
    }

    private void killAdditionalController(@NotNull Location location) {
        SlimefunItem sfItem = StorageCacheUtils.getSfItem(location);
        if (sfItem != null) {
            Slimefun.getDatabaseManager().getBlockDataController().removeBlock(location);
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    // fix #99
                    NetworkController.wipeNetwork(location);
                    location.getWorld().dropItemNaturally(location, sfItem.getItem());
                    location.getBlock().setType(Material.AIR);
                }
            };
            runnable.runTask(Networks.getInstance());
            NetworkController.wipeNetwork(location);
        }
    }

    protected long retrieveBlockCharge() {
        if (this.nodeType == NodeType.POWER_NODE) {
            int blockCharge = 0;
            final SlimefunItem item = StorageCacheUtils.getSfItem(this.nodePosition);
            if (item instanceof NetworkPowerNode powerNode) {
                blockCharge = powerNode.getCharge(this.nodePosition);
            }
            return blockCharge;
        }
        return 0;
    }
}
