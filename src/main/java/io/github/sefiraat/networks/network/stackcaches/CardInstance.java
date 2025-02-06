package io.github.sefiraat.networks.network.stackcaches;

import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.utils.Theme;
import lombok.Getter;
import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Getter
public class CardInstance extends ItemStackCache {

    private final int limit;
    private int amount;

    public CardInstance(@Nullable ItemStack itemStack, int amount, int limit) {
        super(itemStack);
        this.amount = amount;
        this.limit = limit;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Nullable
    public ItemStack withdrawItem(int amount) {
        if (this.getItemStack() == null) {
            return null;
        }
        final ItemStack clone = this.getItemStack().clone();
        clone.setAmount(Math.min(this.amount, amount));
        reduceAmount(clone.getAmount());
        return clone;
    }

    @Nullable
    public ItemStack withdrawItem() {
        if (this.getItemStack() == null) {
            return null;
        }
        return withdrawItem(this.getItemStack().getMaxStackSize());
    }

    public void increaseAmount(int amount) {
        long total = (long) this.amount + (long) amount;
        if (total > this.limit) {
            this.amount = this.limit;
        } else {
            this.amount = this.amount + amount;
        }
    }

    public void reduceAmount(int amount) {
        this.amount = this.amount - amount;
    }

    public void updateLore(@Nonnull ItemMeta itemMeta) {
        List<String> lore = itemMeta.getLore();
        lore.set(10, getLoreLine());
        itemMeta.setLore(lore);
    }

    public String getLoreLine() {
        if (this.getItemStack() == null) {
            return Networks.getLocalizationService().getString("messages.normal-operation.memory_card.empty");
        }
        String name = ChatColor.stripColor(ItemStackHelper.getDisplayName(getItemStack()));
        return Theme.CLICK_INFO + name + ": " + Theme.PASSIVE + this.amount;
    }
}
