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

    /**
     * this method should be as same as depositItemStack, but we can make sure that input itemStack is similar to Barrel cached ItemStack and there is no need for comparison
     * @param matchedItemstack
     */
    void depositItemStackExact(ItemStack matchedItemstack);

    int[] getInputSlot();

    int[] getOutputSlot();
}
