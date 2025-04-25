package io.github.sefiraat.networks.network.barrel;

import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NetworkStorage extends BarrelIdentity {
    final QuantumCache cacheReference;
    final ItemStack quantumItemStackHolder;
    public NetworkStorage(Location location, ItemStack itemStack,QuantumCache storedCache, long amount) {
        super(location, itemStack, amount, BarrelType.NETWORKS);
        this.cacheReference = storedCache;
        this.quantumItemStackHolder = cacheReference.getItemStack();
//        this.id=storedCache.getOptionalId();
//        this.initializedId = true;
        //copy NoLore hashCode
        this.hashCodeNoLore = storedCache.getHashCodeNoLore();
        //this.hashCode = storedCache.getHashCode();
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

        //check here if the type of cacheReference is changed by player
        if (cacheReference.isPendingMove() || cacheReference.getItemStack() != quantumItemStackHolder) {
            return null;
        }
        return NetworkQuantumStorage.getItemStack(cacheReference, this.getLocation(), itemRequest.getAmount());
    }

    @Override
    public ItemStack requestItemExact(ItemRequest itemRequest) {
        //check here if the type of cacheReference is changed by player
        if (cacheReference.isPendingMove()|| cacheReference.getItemStack() != quantumItemStackHolder ) {
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

        //check here if the type of cacheReference is changed by player
        if (!cacheReference.isPendingMove() && cacheReference.getItemStack() == quantumItemStackHolder) {
            NetworkQuantumStorage.tryInputItem(this.getLocation(), itemsToDeposit, cacheReference);
        }

    }
    public void depositItemStackExact(ItemStack matchedItemstack){
        if (!cacheReference.isPendingMove() && cacheReference.getItemStack() == quantumItemStackHolder) {
            NetworkQuantumStorage.tryInputWithoutCheck(this.getLocation(), matchedItemstack, cacheReference);
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
