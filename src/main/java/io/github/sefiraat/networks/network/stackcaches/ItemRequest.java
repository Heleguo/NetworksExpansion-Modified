package io.github.sefiraat.networks.network.stackcaches;

import io.github.sefiraat.networks.network.barrel.OptionalSfItemCache;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class ItemRequest extends ItemStackCache implements OptionalSfItemCache {

    private int amount;
    @Getter
    private int maxStackSize;
    private static ItemRequest instanceTemplate=new ItemRequest(new ItemStack(Material.STONE),0);
    public static ItemRequest of(ItemStack itemStack, int amount) {
        return instanceTemplate.clone().init(itemStack, amount);
    }
    protected ItemRequest init(ItemStack itemStack, int amount) {
        super.init(itemStack);
        this.amount = amount;
        this.maxStackSize = itemStack.getMaxStackSize();
        this.id=null;
        this.initializedId=new AtomicBoolean(false);
        return this;
    }
    public ItemRequest(@Nonnull ItemStack itemStack, int amount) {
        super(itemStack);
        this.amount = amount;
        this.maxStackSize = itemStack.getMaxStackSize();
        this.id=null;
        this.initializedId=new AtomicBoolean(false);
    }


    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void receiveAmount(int amount) {
        this.amount = this.amount - amount;
    }

    public String toString() {
        return "ItemRequest{" +
                "itemStack=" + getItemStack() +
                ", amount=" + amount +
                '}';
    }
    public ItemRequest clone() {
        return (ItemRequest) super.clone();
    }

    private String id;
    private AtomicBoolean initializedId;
    public final String getOptionalId(){
        if(initializedId.compareAndSet(false,true)){
            ItemMeta meta = getItemMeta();
            id= meta==null?null: Slimefun.getItemDataService().getItemData(meta).orElse(null);
        }
        return id;
    }

}
