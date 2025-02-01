package com.balugaq.netex.api.data;

import io.github.sefiraat.networks.network.barrel.OptionalSfItemCache;
import io.github.sefiraat.networks.network.stackcaches.ItemStackCache;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.utils.itemstack.ItemStackWrapper;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.atomic.AtomicBoolean;


public class ItemContainer extends ItemStackCache implements OptionalSfItemCache {
    @Getter
    private final int id;
    //private final ItemStack sample;
    //@Getter
    //private final ItemStackWrapper wrapper;
    @Getter
    private int amount;

    public ItemContainer(int id, ItemStack item, int amount) {
        super(StackUtils.getAsQuantity(item,1));
        this.id = id;
        //this.sample = getItemStack();
        //this.wrapper = ItemStackWrapper.wrap(sample);
        this.amount = amount;
    }

    public ItemStack getSample() {
        return getItemStack().clone();
    }

    public boolean isSimilar(ItemStack other) {
        return StackUtils.itemsMatch(this, other);
    }
    public boolean isSimilar(ItemStackCache other) {
        return StackUtils.itemsMatch(this, other);
    }

    public void addAmount(int amount) {
        this.amount += amount;
    }

    /**
     * Remove specific amount from container
     *
     * @param amount: amount will be removed
     * @return amount that actual removed
     */
    public int removeAmount(int amount) {
        if (this.amount > amount) {
            this.amount -= amount;
            return amount;
        } else {
            int re = this.amount;
            this.amount = 0;
            return re;
        }
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String toString() {
        return "ItemContainer{" +
                "id=" + id +
                ", sample=" + this.getItemStack() +
                ", wrapper=(ItemStackCache)this" +
                ", amount=" + amount +
                '}';
    }

    private String cacheId;
    private final AtomicBoolean initializedId=new AtomicBoolean(false);
    public final String getOptionalId(){
        if(initializedId.compareAndSet(false,true)){
            ItemMeta meta = getItemMeta();
            cacheId= meta==null?null: Slimefun.getItemDataService().getItemData(meta).orElse(null);
        }
        return cacheId;
    }
}
