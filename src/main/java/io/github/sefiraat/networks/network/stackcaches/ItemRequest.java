package io.github.sefiraat.networks.network.stackcaches;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

@Getter
public class ItemRequest extends ItemStackCache {

    private int amount;
    private static ItemRequest instanceTemplate=new ItemRequest(new ItemStack(Material.STONE),0);
    public static ItemRequest of(ItemStack itemStack, int amount) {
        return instanceTemplate.clone().init(itemStack, amount);
    }
    protected ItemRequest init(ItemStack itemStack, int amount) {
        super.init(itemStack);
        this.amount = amount;
        return this;
    }
    public ItemRequest(@Nonnull ItemStack itemStack, int amount) {
        super(itemStack);
        this.amount = amount;
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
}
