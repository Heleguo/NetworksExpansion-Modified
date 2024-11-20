package io.github.sefiraat.networks.network.stackcaches;

import com.balugaq.netex.api.helpers.ItemStackHelper;
import io.github.sefiraat.networks.Networks;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class QuantumCache extends ItemStackCache {

    @Nullable
    private final ItemMeta storedItemMeta;
    private final boolean supportsCustomMaxAmount;
    @Getter
    private int limit;

    private long amount;
    private final byte[] lock=new byte[0];

    @Getter
    private boolean voidExcess;
    //lock for saving thread
    @Getter
    private final AtomicBoolean saving = new AtomicBoolean(false);

    public QuantumCache(@Nullable ItemStack storedItem, long amount, int limit, boolean voidExcess, boolean supportsCustomMaxAmount) {
        super(storedItem);
        this.storedItemMeta = storedItem == null ? null : storedItem.getItemMeta();
        this.amount = amount;
        this.limit = limit;
        this.voidExcess = voidExcess;
        this.supportsCustomMaxAmount = supportsCustomMaxAmount;
    }


    @Nullable
    public ItemMeta getStoredItemMeta() {
        return this.storedItemMeta;
    }
    public long getAmount(){
        synchronized (lock){
            return this.amount;
        }
    }
    public void setAmount(int amount) {
        synchronized (lock) {
            this.amount = amount;
        }
    }

    public boolean supportsCustomMaxAmount() {
        return this.supportsCustomMaxAmount;
    }

    public int increaseAmount(int amount) {
        synchronized (lock) {
            long total = this.amount + (long) amount;
            if (total > this.limit) {
                this.amount = this.limit;
                if (!this.voidExcess) {
                    return (int) (total - this.limit);
                }
            } else {
                this.amount = this.amount + amount;
            }
        }
        return 0;
    }

    public void reduceAmount(int amount) {
        synchronized (lock) {
            this.amount -= amount;
        }
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setVoidExcess(boolean voidExcess) {
        this.voidExcess = voidExcess;
    }


    @Nullable
    public ItemStack withdrawItem(int amount) {
        if (this.getItemStack() == null) {
            return null;
        }
        final ItemStack clone = this.getItemStack().clone();
        synchronized (lock) {
            clone.setAmount((int) Math.min(this.amount, amount));
            reduceAmount(clone.getAmount());
        }
        return clone;
    }

    @Nullable
    public ItemStack withdrawItem() {
        if (this.getItemStack() == null) {
            return null;
        }
        return withdrawItem(this.getItemStack().getMaxStackSize());
    }

    public void addMetaLore(ItemMeta itemMeta) {
        final List<String> lore = itemMeta.hasLore() ? new ArrayList<>(itemMeta.getLore()) : new ArrayList<>();
        String itemName = Networks.getLocalizationService().getString("messages.normal-operation.quantum_cache.empty");
        if (getItemStack() != null) {
            itemName = ItemStackHelper.getDisplayName(this.getItemStack());
        }
        lore.add("");
        lore.add(String.format(Networks.getLocalizationService().getString("messages.normal-operation.quantum_cache.stored_item"), itemName));
        lore.add(String.format(Networks.getLocalizationService().getString("messages.normal-operation.quantum_cache.stored_amount"), this.getAmount()));
        if (this.supportsCustomMaxAmount) {
            lore.add(String.format(Networks.getLocalizationService().getString("messages.normal-operation.quantum_cache.custom_max_limit"), this.getLimit()));
        }

        itemMeta.setLore(lore);
    }

    public void updateMetaLore(ItemMeta itemMeta) {
        final List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
        String itemName = Networks.getLocalizationService().getString("messages.normal-operation.quantum_cache.empty");
        if (getItemStack() != null) {
            itemName = ItemStackHelper.getDisplayName(this.getItemStack());
        }
        final int loreIndexModifier = this.supportsCustomMaxAmount ? 1 : 0;
        lore.set(lore.size() - 2 - loreIndexModifier, String.format(Networks.getLocalizationService().getString("messages.normal-operation.quantum_cache.stored_item"), itemName));
        lore.set(lore.size() - 1 - loreIndexModifier, String.format(Networks.getLocalizationService().getString("messages.normal-operation.quantum_cache.stored_amount"), this.getAmount()));
        if (this.supportsCustomMaxAmount) {
            lore.set(lore.size() - loreIndexModifier, String.format(Networks.getLocalizationService().getString("messages.normal-operation.quantum_cache.custom_max_limit"), this.getLimit()));
        }

        itemMeta.setLore(lore);
    }
}
