package io.github.sefiraat.networks;

import io.github.bakedlibs.dough.blocks.ChunkPosition;
import io.github.sefiraat.networks.network.NetworkNode;
import io.github.sefiraat.networks.network.NodeDefinition;
import lombok.experimental.UtilityClass;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class NetworkStorage {
    private static final Map<ChunkPosition, Set<Location>> ALL_NETWORK_OBJECTS_BY_CHUNK = new ConcurrentHashMap<>();
    private static final Map<Location, NodeDefinition> ALL_NETWORK_OBJECTS = new ConcurrentHashMap<>();

    public static void removeNode(Location location) {
        final NodeDefinition nodeDefinition = ALL_NETWORK_OBJECTS.remove(location);

        if (nodeDefinition == null) {
            return;
        }

        final NetworkNode node = nodeDefinition.getNode();

        if (node == null) {
            return;
        }

        for (NetworkNode childNode : nodeDefinition.getNode().getChildrenNodes()) {
            removeNode(childNode.getNodePosition());
        }
    }

    public static boolean containsKey(Location location) {
        return ALL_NETWORK_OBJECTS.containsKey(location);
    }

    public static NodeDefinition getNode(Location location) {
        return ALL_NETWORK_OBJECTS.get(location);
    }

    public static void registerNode(Location location, NodeDefinition nodeDefinition) {
        ALL_NETWORK_OBJECTS.put(location, nodeDefinition);
        ChunkPosition unionKey = new ChunkPosition(location);
        Set<Location> locations = ALL_NETWORK_OBJECTS_BY_CHUNK.computeIfAbsent(unionKey,(k)-> new HashSet<>());
        locations.add(location);
        //ALL_NETWORK_OBJECTS_BY_CHUNK.put(unionKey, locations);
    }

    public static void unregisterChunk(Chunk chunk) {
        ChunkPosition chunkPosition = new ChunkPosition(chunk);
        Set<Location> locations = ALL_NETWORK_OBJECTS_BY_CHUNK.remove(chunkPosition);
        if (locations == null) {
            return;
        }
        for (Location location : locations) {
            removeNode(location);
        }
       // ALL_NETWORK_OBJECTS_BY_CHUNK.remove(chunkPosition);
    }

}
