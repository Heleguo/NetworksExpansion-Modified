package com.balugaq.netex.api.enums;

import com.ytdd9527.networksexpansion.utils.TextUtil;
import io.github.sefiraat.networks.Networks;

import java.util.List;

public enum TransportMode {
    NONE,
    NULL_ONLY,
    NONNULL_ONLY,
    FIRST_ONLY,
    LAST_ONLY,
    FIRST_STOP,
    GREEDY,
    LAZY;


    public String getName() {
        return TextUtil.colorRandomString(getRawName());
    }
    public List<String> getDescription(){
        return switch (this){
            case NONE -> Networks.getLocalizationService().getStringList("icons.transport_mode_description.none");
            case NULL_ONLY -> Networks.getLocalizationService().getStringList("icons.transport_mode_description.null_only");
            case NONNULL_ONLY -> Networks.getLocalizationService().getStringList("icons.transport_mode_description.nonnull_only");
            case FIRST_ONLY -> Networks.getLocalizationService().getStringList("icons.transport_mode_description.first_only");
            case LAST_ONLY -> Networks.getLocalizationService().getStringList("icons.transport_mode_description.last_only");
            case FIRST_STOP -> Networks.getLocalizationService().getStringList("icons.transport_mode_description.first_stop");
            case GREEDY -> Networks.getLocalizationService().getStringList("icons.transport_mode_description.greedy");
            case LAZY -> Networks.getLocalizationService().getStringList("icons.transport_mode_description.lazy");
        };
    }

    public String getRawName() {
        return switch (this) {
            case NONE -> Networks.getLocalizationService().getString("icons.transport_mode.none");
            case NULL_ONLY -> Networks.getLocalizationService().getString("icons.transport_mode.null_only");
            case NONNULL_ONLY -> Networks.getLocalizationService().getString("icons.transport_mode.nonnull_only");
            case FIRST_ONLY -> Networks.getLocalizationService().getString("icons.transport_mode.first_only");
            case LAST_ONLY -> Networks.getLocalizationService().getString("icons.transport_mode.last_only");
            case FIRST_STOP -> Networks.getLocalizationService().getString("icons.transport_mode.first_stop");
            case GREEDY -> Networks.getLocalizationService().getString("icons.transport_mode.greedy");
            case LAZY -> Networks.getLocalizationService().getString("icons.transport_mode.lazy");
        };
    }

    public TransportMode next() {
        int index = this.ordinal() + 1;
        if (index >= TransportMode.values().length) {
            index = 0;
        }
        return TransportMode.values()[index];
    }

    public TransportMode previous() {
        int index = this.ordinal() - 1;
        if (index < 0) {
            index = TransportMode.values().length - 1;
        }
        return TransportMode.values()[index];
    }
}
