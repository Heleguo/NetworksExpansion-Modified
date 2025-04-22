package com.balugaq.netex.utils.algorithms;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;

import javax.annotation.Nonnull;

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
