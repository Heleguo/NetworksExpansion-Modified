package io.github.sefiraat.networks.network.barrel;

import com.balugaq.netex.utils.BlockMenuUtil;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.NetworkAsyncUtil;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.utils.StackUtils;
import io.ncbpfluffybear.fluffymachines.items.Barrel;
import lombok.Getter;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Getter
public class FluffyBarrel extends BarrelIdentity {
    int limit;
    boolean voidExcess;

    public FluffyBarrel(Location location, ItemStack itemStack, int amount, int limit, boolean voidExcess) {
        super(location, itemStack, amount, BarrelType.FLUFFY);
        this.limit = limit;
        this.voidExcess = voidExcess;
        BlockMenu menu = StorageCacheUtils.getMenu(getLocation());
        Barrel barrel = (Barrel) StorageCacheUtils.getSfItem(getLocation());
        if (barrel != null) {
            barrel.updateMenu(getLocation().getBlock(), menu, true, getLimit());
        }
    }

    @Nullable
    @Override
    public ItemStack requestItem(@Nonnull ItemRequest itemRequest) {
        BlockMenu menu = StorageCacheUtils.getMenu(getLocation());
        if (menu == null) {
            return null;
        }

        int received = 0;
        ItemStack targetItem = itemRequest.getItemStack();
        for (int slot : getOutputSlot()) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            if (StackUtils.itemsMatch(item, targetItem)) {
                int max = Math.min(item.getAmount(), itemRequest.getAmount() - received);
                menu.consumeItem(slot, max);
                received += max;
            }
        }

        if (received <= 0) {
            return null;
        }

        return StackUtils.getAsQuantity(targetItem, received);
    }

    @Override
    public ItemStack requestItemExact(ItemRequest itemRequest) {
        BlockMenu menu = StorageCacheUtils.getMenu(getLocation());
        if (menu == null) {
            return null;
        }
        ItemStack targetItem = itemRequest.getItemStack();
        //ensure sync via transportation lock
        int received0 = NetworkAsyncUtil.getInstance().ensureLocation(getLocation(), ()->{
            int received = 0;

            for (int slot : getOutputSlot()) {
                ItemStack item = menu.getItemInSlot(slot);
                if (item == null || item.getType() == Material.AIR) {
                    continue;
                }
                if (StackUtils.itemsMatch(item, targetItem)) {
                    int max = Math.min(item.getAmount(), itemRequest.getAmount() - received);
                    item.setAmount(item.getAmount() - max);
                    //menu.consumeItem(slot, max);
                    received += max;
                }
            }
            return received;
        });
        if (received0 <= 0) {
            return null;
        }

        return StackUtils.getAsQuantity(targetItem, received0);
    }

    @Override
    public void depositItemStack(ItemStack itemsToDeposit) {
        BlockMenu menu = StorageCacheUtils.getMenu(getLocation());
        if (menu == null) {
            return;
        }
       // synchronized (this){
        NetworkAsyncUtil.getInstance().ensureLocation(getLocation(), ()->{
            BlockMenuUtil.pushItem(menu, itemsToDeposit, getInputSlot());
        });

        //}
    }
    public void depositItemStackExact(ItemStack matchedItemstack){
        depositItemStack(matchedItemstack);
    }

    @Override
    public int[] getInputSlot() {
        BlockMenu menu = StorageCacheUtils.getMenu(getLocation());
        if (menu == null) {
            return new int[0];
        }
        return menu.getPreset().getSlotsAccessedByItemTransport(menu, ItemTransportFlow.INSERT, null);
    }

    @Override
    public int[] getOutputSlot() {
        BlockMenu menu = StorageCacheUtils.getMenu(getLocation());
        if (menu == null) {
            return new int[0];
        }
        return menu.getPreset().getSlotsAccessedByItemTransport(menu, ItemTransportFlow.WITHDRAW, null);
    }
}
