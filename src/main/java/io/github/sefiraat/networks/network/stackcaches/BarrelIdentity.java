package io.github.sefiraat.networks.network.stackcaches;

import com.balugaq.netex.api.data.ItemContainer;
import io.github.sefiraat.networks.network.barrel.BarrelCore;
import io.github.sefiraat.networks.network.barrel.BarrelType;
import io.github.sefiraat.networks.network.barrel.OptionalSfItemCache;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import lombok.Getter;
import lombok.Setter;
import me.matl114.matlib.utils.reflect.ReflectUtils;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.invoke.VarHandle;
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
    protected boolean initializedId = false;
    private static final VarHandle ATOMIC_IDCACHE_HANDLE = ReflectUtils.getVarHandlePrivate(BarrelIdentity.class, "initializedId").withInvokeExactBehavior();
    public final String getOptionalId(){
        if(ATOMIC_IDCACHE_HANDLE.compareAndSet((BarrelIdentity)this,false,true)){
            id = StackUtils.getOptionalId(getItemStack());
        }
        return id;
    }
}
