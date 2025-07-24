package com.balugaq.netex.api.data;

import io.github.sefiraat.networks.network.stackcaches.ItemStackCache;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.utils.itemstack.ItemStackWrapper;
import lombok.Getter;
import lombok.Setter;
import me.matl114.matlib.nmsUtils.ItemUtils;
import me.matl114.matlib.utils.reflect.ReflectUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.lang.invoke.VarHandle;

@Getter
public class ItemContainer extends ItemStackCache  {
    private final int id;

    private final ItemStackWrapper wrapper;
    private final int storageUnitId;

    static final VarHandle ATOMIC_AMOUNT_HANDLE = ReflectUtils.getVarHandlePrivate(ItemContainer.class, "amount").withInvokeExactBehavior();
    @Setter
    volatile int amount;

    public ItemContainer(int id, @Nonnull ItemStack item, int amount, int storageId) {
        super(ItemUtils.cleanStack(StackUtils.getAsQuantity(item,1)));
        this.id = id;
        //this.sample = getItemStack();
        this.wrapper = ItemStackWrapper.wrap(getItemStack());
        this.amount = amount;
        this.storageUnitId =storageId;

    }


    public ItemStack getSample() {
        return getItemStack().clone();
    }

    public @NotNull ItemStack getSampleDirectly() {
        return getItemStack();
    }

    public boolean isSimilar(ItemStack other) {
        return StackUtils.itemsMatch(this, other);
    }
    public boolean isSimilar(ItemStackCache other) {
        return StackUtils.itemsMatch(this, other);
    }

    public void addAmount(int amount) {
        int oldValue, newValue;
        do{
            oldValue = this.amount;
            newValue = oldValue + amount;
        }while (!ATOMIC_AMOUNT_HANDLE.compareAndSet((ItemContainer)this,(int)oldValue, (int)newValue));
    }

    /**
     * Remove specific amount from container
     *
     * @param amount: amount will be removed
     * @return amount that actual removed
     */
    public int removeAmount(int amount) {
        int oldValue, newValue;int ret;
        do{
            oldValue = this.amount;
            if(oldValue > amount){
                newValue = oldValue - amount;
                ret = amount;
            }else {
                ret = oldValue;
                newValue = 0;
            }
        }while (!ATOMIC_AMOUNT_HANDLE.compareAndSet((ItemContainer)this, (int)oldValue, (int)newValue));
        return ret;
    }

    public @NotNull String toString() {
        return "ItemContainer{" + "id="
                + id + ", sample="
                + getItemStack() + ", wrapper="
                + wrapper + ", amount="
                + amount + '}';
    }
}
