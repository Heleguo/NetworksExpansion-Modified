package com.balugaq.netex.utils.algorithms;

import io.github.sefiraat.networks.network.NetworkRoot;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;

import javax.annotation.Nonnull;

public interface MenuWithPrefetch extends MenuWithData {
    public int getPrefetchCount();
    default NetworkRoot.PusherPrefetcherInfo getPrefetcher(BlockMenu menu, int index){
        DataContainer container = getDataContainer(menu);
        return (NetworkRoot.PusherPrefetcherInfo) container.getObject(index);
    }
    default DataContainer newDataContainer(){
        return new DataContainer() {
            final Object[] value = new Object[MenuWithPrefetch.this.getPrefetchCount()];
            @Override
            public Object getObject(int val) {
                Object val1 = value[val];
                if(val1 != null){
                    return val1;
                }else {
                    val1 = new NetworkRoot.PusherPrefetcherInfo();
                    value[val] = val1;
                    return val1;
                }
            }
        };
    }


}
