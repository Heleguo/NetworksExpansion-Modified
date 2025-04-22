package io.github.sefiraat.networks.network.stackcaches;

import lombok.ToString;
import me.matl114.matlib.nmsUtils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

@ToString
public class ItemStackCache implements Cloneable {

    private ItemStack itemStack;
//    @Nullable
//    private ItemMeta itemMeta = null;
//    private boolean metaCached = false;
    private static final ItemStackCache instanceTemplate=new ItemStackCache(new ItemStack(Material.STONE));
    protected ItemStackCache init(ItemStack itemStack) {
        this.itemStack = itemStack;
//        this.itemMeta = null;
//        this.metaCached = false;
        return this;
    }
//    public static ItemStackCache of(ItemStack itemStack) {
//        return instanceTemplate.clone().init(itemStack);
//    }
    public ItemStackCache(@Nullable ItemStack itemStack) {
        init(itemStack);
    }

    @Nullable
    public final ItemStack getItemStack() {
        //fixme should make sure all itemStacks are CraftItemStacks
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

//        // refresh meta here
//        this.metaCached = false;
//        this.itemMeta = null;
    }
    public final boolean isItemMaxStacked() {
        return this.itemStack.getAmount() >= this.itemStack.getMaxStackSize();
    }
    @Nullable
    @Deprecated(forRemoval = true)
    public final ItemMeta getItemMeta() {
        return this.itemStack.getItemMeta();
    }
//    @Deprecated(forRemoval = true)
//    protected void setItemMeta0(ItemMeta itemMeta) {
//        this.itemStack.setItemMeta(itemMeta);
//    }

    @Nonnull
    public final Material getItemType() {
        return this.itemStack==null?Material.AIR: this.itemStack.getType();
    }

    @Override
    public ItemStackCache clone() {
        try {
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return (ItemStackCache) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
