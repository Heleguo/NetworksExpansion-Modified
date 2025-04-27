package io.github.sefiraat.networks.network.barrel;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import dev.sefiraat.netheopoiesis.implementation.Items;
import io.github.mooy1.infinityexpansion.items.storage.StorageCache;
import io.github.sefiraat.networks.NetworkAsyncUtil;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.utils.StackUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class InfinityBarrel extends BarrelIdentity {

    @Nonnull
    private final StorageCache cache;

    @ParametersAreNonnullByDefault
    public InfinityBarrel(Location location, ItemStack itemStack, long amount, StorageCache cache) {
        super(location, itemStack, amount, BarrelType.INFINITY);
        this.cache = cache;
    }

    @Nullable
    @Override
    public ItemStack requestItem(@Nonnull ItemRequest itemRequest) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(this.getLocation());
        return blockMenu == null ? null : blockMenu.getItemInSlot(this.getOutputSlot()[0]);
    }

    @Override
    public ItemStack requestItemExact(ItemRequest itemRequest) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(this.getLocation());
        if(blockMenu == null){
            return null;
        }else {
            ItemStack itemStack = blockMenu.getItemInSlot(this.getOutputSlot()[0]);
            if(itemStack == null){
                return null;
            }
            //leave at least one itemStack in the slot!
            //ensure async using Transportation LockFactory
            return NetworkAsyncUtil.getInstance().ensureRootLocation(this.getLocation(), ()->{
                int requestAmount = Math.min(itemRequest.getAmount(), itemStack.getAmount()-1);
                ItemStack itemStack1 = blockMenu.getItemInSlot(this.getOutputSlot()[0]);
                int amount = itemStack1.getAmount();
                ItemStack cloned = StackUtils.getAsQuantity(itemStack1,Math.min(requestAmount, amount-1));
                itemStack1.setAmount(amount - requestAmount);
                return cloned;
            });
        }
        //return blockMenu == null ? null : blockMenu.getItemInSlot(this.getOutputSlot()[0]);
    }

    @Override
    public void depositItemStack(ItemStack itemsToDeposit) {
        synchronized (this){
            cache.depositAll(new ItemStack[]{ itemsToDeposit}, true);
        }
    }
    public void depositItemStackExact(ItemStack matchedItemstack){
        depositItemStack(matchedItemstack);
    }

    @Override
    public int[] getInputSlot() {
        return new int[]{10};
    }

    @Override
    public int[] getOutputSlot() {
        return new int[]{16};
    }
}
