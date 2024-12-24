package io.github.sefiraat.networks.network.stackcaches;

import lombok.ToString;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

@ToString
public class ItemStackCache implements Cloneable {

    private ItemStack itemStack;
    @Nullable
    private ItemMeta itemMeta = null;
    private boolean metaCached = false;
    private static ItemStackCache instanceTemplate=new ItemStackCache(new ItemStack(Material.STONE));
    protected ItemStackCache init(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemMeta = null;
        this.metaCached = false;
        return this;
    }
    public static ItemStackCache of(ItemStack itemStack) {
        return instanceTemplate.clone().init(itemStack);
    }
    public ItemStackCache(@Nullable ItemStack itemStack) {
        init(itemStack);
    }

    @Nullable
    public final ItemStack getItemStack() {
        return this.itemStack;
    }

    public final int getItemAmount(){
        return this.itemStack.getAmount();
    }
    public final void setItemAmount(int amount){
        this.itemStack.setAmount(amount);
    }

    public final void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;

        // refresh meta here
        this.metaCached = false;
        this.itemMeta = null;
    }
    public final boolean isItemMaxStacked() {
        return this.itemStack.getAmount() >= this.itemStack.getMaxStackSize();
    }
    @Nullable
    public final ItemMeta getItemMeta() {
        if (this.itemMeta == null && !this.metaCached) {
            this.itemMeta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : null;
            this.metaCached = !this.metaCached;
        }
        return this.itemMeta;
    }
    protected void setItemMeta0(ItemMeta itemMeta) {
        this.itemMeta = itemMeta;
        this.metaCached =  true;
    }

    @Nonnull
    public final Material getItemType() {
        return this.itemStack.getType();
    }

    @Override
    public ItemStackCache clone() {
        try {
            ItemStackCache clone = (ItemStackCache) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
