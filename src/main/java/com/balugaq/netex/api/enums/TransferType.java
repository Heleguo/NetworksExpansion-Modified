package com.balugaq.netex.api.enums;

import com.balugaq.netex.api.gui.GUI;
import com.balugaq.netex.utils.GUIs;
import io.github.sefiraat.networks.Networks;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public enum TransferType {
    ADVANCED_LINE_TRANSFER(GUIs.ALT, true),
    ADVANCED_LINE_TRANSFER_BEST_PUSHER(GUIs.ALTBP, true),
    ADVANCED_LINE_TRANSFER_GRABBER(GUIs.ALTG, true),
    ADVANCED_LINE_TRANSFER_MORE_PUSHER(GUIs.ALTMP, true),
    ADVANCED_LINE_TRANSFER_PUSHER(GUIs.ALTP, true),
    LINE_TRANSFER(GUIs.LT),
    LINE_TRANSFER_BEST_PUSHER(GUIs.LTBP),
    LINE_TRANSFER_GRABBER(GUIs.LTG),
    LINE_TRANSFER_MORE_PUSHER(GUIs.LTMP),
    LINE_TRANSFER_PUSHER(GUIs.LTP),
    ADVANCED_TRANSFER(GUIs.AT, true),
    ADVANCED_TRANSFER_BEST_PUSHER(GUIs.ATBP, true),
    ADVANCED_TRANSFER_GRABBER(GUIs.ATG, true),
    ADVANCED_TRANSFER_MORE_PUSHER(GUIs.ATMP, true),
    ADVANCED_TRANSFER_PUSHER(GUIs.ATP, true),
    TRANSFER(GUIs.T),
    TRANSFER_BEST_PUSHER(GUIs.TBP),
    TRANSFER_GRABBER(GUIs.TG),
    TRANSFER_MORE_PUSHER(GUIs.TMP),
    TRANSFER_PUSHER(GUIs.TP),
    LINE_TRANSFER_VANILLA_GRABBER(GUIs.LTVG, false),
    LINE_TRANSFER_VANILLA_PUSHER(GUIs.LTVP, false);

    private final @NotNull GUI gui;
    private final boolean advanced;

    TransferType(@NotNull GUI gui, boolean advanced) {
        this.gui = gui;
        this.advanced = advanced;
    }

    TransferType(@NotNull GUI gui) {
        this.gui = gui;
        this.advanced = false;
    }

    private final Map<String, Integer> config = new HashMap<>();

    public int config(@NotNull String key) {
        return config(key, 0);
    }

    public int config(@NotNull String key, int defaultValue) {
        if (config.containsKey(key)) {
            return config.get(key);
        }

        int v = Networks.getConfigManager().getInt("items.NTW_EXPANSION_" + name() + "." + key, defaultValue);
        config.put(key, v);
        return v;
    }

    public int config(@Nullable String id, @NotNull String key, int defaultValue) {
        if (id == null) {
            return config(key, defaultValue);
        } else {
            return fixedConfig(id, key, defaultValue);
        }
    }

    public int fixedConfig(@NotNull String id, @NotNull String key, int defaultValue) {
        String fkey = id + "." + key;
        if (config.containsKey(fkey)) {
            return config.get(fkey);
        }

        int v = Networks.getConfigManager().getInt("items." + fkey, defaultValue);
        config.put(fkey, v);
        return v;
    }

    public boolean isTransfer() {
        return name().endsWith("TRANSFER");
    }

    public boolean isGrabber() {
        return isTransfer() || name().endsWith("_GRABBER");
    }

    public boolean isPusher() {
        return isTransfer() || name().endsWith("_PUSHER");
    }
}
