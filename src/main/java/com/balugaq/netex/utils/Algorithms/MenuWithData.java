package com.balugaq.netex.utils.Algorithms;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;

import javax.annotation.Nonnull;
import javax.xml.crypto.Data;

public interface MenuWithData {
    public DataContainer newDataContainer();
    public int getDataSlot();
    @Nonnull
    default DataContainer getDataContainer(ChestMenu inv) {
        if(inv.getMenuClickHandler(getDataSlot()) instanceof DataContainer dc){
            return dc;
        }else {
            DataContainer newDataContainer = newDataContainer();
            inv.addMenuClickHandler(getDataSlot(), newDataContainer);
            return newDataContainer;
        }
    }
}
