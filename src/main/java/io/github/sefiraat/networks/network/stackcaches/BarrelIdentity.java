package io.github.sefiraat.networks.network.stackcaches;

import io.github.sefiraat.networks.network.barrel.BarrelCore;
import io.github.sefiraat.networks.network.barrel.BarrelType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@Getter
@Setter
public abstract class BarrelIdentity extends ItemStackCache implements BarrelCore{

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
//    protected String id;
//    protected boolean initializedId = false;
//    private static final VarHandle ATOMIC_IDCACHE_HANDLE = ReflectUtils.getVarHandlePrivate(BarrelIdentity.class, "initializedId").withInvokeExactBehavior();
//    public final String getOptionalId(){
//        if(ATOMIC_IDCACHE_HANDLE.compareAndSet((BarrelIdentity)this,false,true)){
//            id = StackUtils.getOptionalId(getItemStack());
//        }
//        return id;
//    }
}
