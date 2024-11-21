package io.github.sefiraat.networks.network.barrel;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NetworkStorage extends BarrelIdentity {
    final QuantumCache cacheReference;
    public NetworkStorage(Location location, ItemStack itemStack,QuantumCache storedCache, long amount) {
        super(location, itemStack, amount, BarrelType.NETWORKS);
        this.cacheReference = storedCache;
    }

    @Override
    @Nullable
    public ItemStack requestItem(@Nonnull ItemRequest itemRequest) {
//        final BlockMenu blockMenu = StorageCacheUtils.getMenu(this.getLocation());
//
//        if (blockMenu == null) {
//            return null;
//        }
        // we don't need to refresh this cacheReference
        //if this is removed in a normal way, pendingMove will be set true,
        //if QuantumStorage is replaced with air but Cache still exists,
        //as we have tested before,there's no dupe in this situation
        //as we all know,NTWStorage is just a temporary cache which refresh every sft
        //this check also prevent part of dupe13 somehow
        //final QuantumCache cache = NetworkQuantumStorage.getCaches().get(this.getLocation());
        if (cacheReference.isPendingMove()) {
            return null;
        }
        return NetworkQuantumStorage.getItemStack(cacheReference, this.getLocation(), itemRequest.getAmount());
    }

    @Override
    public void depositItemStack(ItemStack itemsToDeposit) {
        //final QuantumCache cache = NetworkQuantumStorage.getCaches().get(this.getLocation());
        // we don't need to refresh this cacheReference
        //if this is removed in a normal way, pendingMove will be set true,
        //if QuantumStorage is replaced with air but Cache still exists,
        //as we have tested before,there's no dupe in this situation
        //as we all know,NTWStorage is just a temporary cache which refresh every sft
        //this check also prevent part of dupe13 somehow
        if (!cacheReference.isPendingMove()) {
            NetworkQuantumStorage.tryInputItem(this.getLocation(), itemsToDeposit, cacheReference);
        }

    }


    @Override
    public int[] getInputSlot() {
        return new int[]{NetworkQuantumStorage.INPUT_SLOT};
    }

    @Override
    public int[] getOutputSlot() {
        return new int[]{NetworkQuantumStorage.OUTPUT_SLOT};
    }
}
