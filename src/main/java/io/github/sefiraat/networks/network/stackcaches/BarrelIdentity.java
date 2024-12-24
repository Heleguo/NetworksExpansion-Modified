package io.github.sefiraat.networks.network.stackcaches;

import io.github.sefiraat.networks.network.barrel.BarrelCore;
import io.github.sefiraat.networks.network.barrel.BarrelType;
import io.github.sefiraat.networks.network.barrel.OptionalSfItemCache;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
public abstract class BarrelIdentity extends ItemStackCache implements BarrelCore, OptionalSfItemCache {

    private Location location;
    private long amount;
    private BarrelType type;

    @ParametersAreNonnullByDefault
    protected BarrelIdentity(Location location, ItemStack itemStack, long amount, BarrelType type) {
        super(itemStack);
        this.location = location;
        this.amount = amount;
        this.type = type;
    }
    protected String id;
    protected final AtomicBoolean initializedId=new AtomicBoolean(false);
    public final String getOptionalId(){
        if(initializedId.compareAndSet(false,true)){
            ItemMeta meta = getItemMeta();
            id= meta==null?null: Slimefun.getItemDataService().getItemData(meta).orElse(null);
        }
        return id;
    }
}
