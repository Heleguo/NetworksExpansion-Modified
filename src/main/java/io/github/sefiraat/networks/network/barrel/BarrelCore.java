package io.github.sefiraat.networks.network.barrel;

import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface BarrelCore {

    /**
     * this method should be multi-thread safe
     * this method is shit
     * @param itemRequest
     * @return
     */
    @Nullable
    @Deprecated
    ItemStack requestItem(@Nonnull ItemRequest itemRequest);

    /**
     * this method should return the requested ItemStack, item should already been consumed from barrel
     * @param itemRequest
     * @return
     */
    ItemStack requestItemExact(ItemRequest itemRequest);

    default void depositItemStack(ItemStack... itemToDeposit) {
        for (ItemStack itemStack : itemToDeposit) {
            depositItemStack(itemStack);
        }
    }
    /**
     * this method should be multi-thread safe
     * @return
     */
    void depositItemStack(ItemStack itemsToDeposit);

    int[] getInputSlot();

    int[] getOutputSlot();
}
