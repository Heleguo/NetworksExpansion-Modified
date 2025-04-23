package io.github.sefiraat.networks.network.stackcaches;

import io.github.sefiraat.networks.utils.StackUtils;
import me.matl114.matlib.nmsUtils.ItemUtils;
import me.matl114.matlib.nmsUtils.inventory.ItemCacheHashMap;
import me.matl114.matlib.nmsUtils.inventory.ItemHashCache;

public class ItemStackCacheStrategy extends ItemCacheHashMap.StrategyItemNoLoreHash {
    public static final ItemStackCacheStrategy INSTANCE = new ItemStackCacheStrategy();
    public boolean equals(ItemHashCache itemStack, ItemHashCache k1) {
        return (itemStack == null || k1 == null)? itemStack == k1 : StackUtils.itemsMatchCore(itemStack.getCraftStack(), k1.getCraftStack(), false);
    }
}
