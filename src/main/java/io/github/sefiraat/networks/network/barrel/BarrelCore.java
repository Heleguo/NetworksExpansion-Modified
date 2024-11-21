package io.github.sefiraat.networks.network.barrel;

import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface BarrelCore {

    /**
     * this method should be multi-thread safe
     * @param itemRequest
     * @return
     */
    @Nullable
    ItemStack requestItem(@Nonnull ItemRequest itemRequest);

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
