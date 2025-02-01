package com.balugaq.netex.api.events;

import com.balugaq.netex.api.enums.StorageType;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.barrel.BarrelType;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class NetworkRootLocateStorageEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final NetworkRoot root;
    private final StorageType storageType;
    private final boolean inputAble;
    private final boolean outputAble;
    public NetworkRootLocateStorageEvent(NetworkRoot root, StorageType storageType, boolean inputAble, boolean outputAble, boolean isSync) {
        super(!isSync);
        this.root = root;
        this.storageType = storageType;
        this.inputAble = inputAble;
        this.outputAble = outputAble;
    }
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
