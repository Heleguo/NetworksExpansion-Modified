package io.github.sefiraat.networks.network.stackcaches;

import io.github.sefiraat.networks.utils.StackUtils;
import me.matl114.matlib.nmsUtils.ItemUtils;
import me.matl114.matlib.utils.reflect.ReflectUtils;
import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.barrel.OptionalSfItemCache;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class QuantumCache extends ItemStackCache implements OptionalSfItemCache {
    private static final VarHandle ATOMIC_AMOUNT_HANDLE = ReflectUtils.getVarHandlePrivate(QuantumCache.class, "amount").withInvokeExactBehavior();
    private static final VarHandle ATOMIC_IDCACHE_HANDLE = ReflectUtils.getVarHandlePrivate(QuantumCache.class, "initializedId").withInvokeExactBehavior();
    @Nullable
    private final ItemMeta storedItemMeta;
    private final boolean supportsCustomMaxAmount;
    @Setter
    @Getter
    private int limit;
    @Getter
    private volatile long amount;
    @Setter
    @Getter
    private boolean voidExcess;
    //lock for saving thread
    @Getter
    private final AtomicBoolean saving = new AtomicBoolean(false);
    private String id;
    private boolean initializedId= false;
    public final String getOptionalId(){
        if(ATOMIC_IDCACHE_HANDLE.compareAndSet((QuantumCache)this,(boolean)false, (boolean)true)){
            id = StackUtils.getOptionalId(getItemStack());
        }
        return id;
    }
    /**
     * pendingMove must be set true when removed from CACHES
     */
    @Getter
    @Setter
    private boolean pendingMove=false;

    public QuantumCache(@Nullable ItemStack storedItem, long amount, int limit, boolean voidExcess, boolean supportsCustomMaxAmount) {
        super(storedItem == null? null : ItemUtils.cleanStack(storedItem));
        this.storedItemMeta = getItemStack() == null ? null : getItemStack().getItemMeta();
        this.amount = amount;
        this.limit = limit;
        this.voidExcess = voidExcess;
        this.supportsCustomMaxAmount = supportsCustomMaxAmount;
    }


    @Nullable
    public ItemMeta getStoredItemMeta() {
        return this.storedItemMeta;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean supportsCustomMaxAmount() {
        return this.supportsCustomMaxAmount;
    }

    public int increaseAmount(int amount) {
        long oldValue, newValue;int retValue = 0;
        do {
            oldValue = this.amount;
            long total = oldValue + (long) amount;
            if (total > this.limit) {
                newValue = this.limit;
                if (!this.voidExcess) {
                    retValue = (int) (total - this.limit);
                }
            } else {
                newValue = total;
            }
        }while (!ATOMIC_AMOUNT_HANDLE.compareAndSet((QuantumCache)this, (long)oldValue,(long)newValue));
        return retValue;
    }

    public void reduceAmount(int amount) {
        long oldValue, newValue;
        do{
            oldValue = this.amount;
            newValue = oldValue - amount;
        }while (!ATOMIC_AMOUNT_HANDLE.compareAndSet((QuantumCache)this,(long)oldValue,(long)newValue));
    }


    @Nullable
    public ItemStack withdrawItem(int amount) {
        if (this.getItemStack() == null) {
            return null;
        }
        final ItemStack clone = this.getItemStack().clone();
        long oldValue, newValue;int amountToWithDraw;
        do{
            oldValue = this.amount;
            if(oldValue <= 0){
                return null;
            }
            amountToWithDraw =(int) Math.min(this.amount, amount);
            newValue = oldValue - amountToWithDraw;
        }while (!ATOMIC_AMOUNT_HANDLE.compareAndSet((QuantumCache)this,(long)oldValue,(long)newValue));
        clone.setAmount(amountToWithDraw);
        return clone;
    }

    @Nullable
    public ItemStack withdrawItem() {
        if (this.getItemStack() == null || this.amount <= 0) {
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
