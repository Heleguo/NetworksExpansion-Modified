package com.balugaq.netex.api.events;

import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class NetworksBlockPlaceEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final @NotNull Block block;
    private final @NotNull Player player;
    private boolean cancelled = false;

    public NetworksBlockPlaceEvent(@NotNull Block theBlock, @NotNull Player player) {
        this.block = theBlock;
        this.player = player;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
