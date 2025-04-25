package io.github.sefiraat.networks.network.stackcaches;

import lombok.Getter;
import lombok.Setter;
import me.matl114.matlib.nmsUtils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

@Getter
public class ItemRequest extends ItemStackCache  {

    @Setter
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
        return this;
    }
    public ItemRequest(@Nonnull ItemStack itemStack, int amount) {
        super(ItemUtils.cleanStack( itemStack));
        this.amount = amount;
        this.maxStackSize = itemStack.getMaxStackSize();
//        this.id=null;
//        this.initializedId= false;
    }


    public void receiveAmount(int amount) {
        this.amount = this.amount - amount;
    }
    public void receiveAll(){
        this.amount = 0;
    }

    public String toString() {
        return "ItemRequest{" +
                "itemStack=" + getItemStack() +
                ", amount=" + amount
               // ", cachedId = "+(initializedId?(id==null?"null":id):"")
         +"}";
    }
    public ItemRequest clone() {
        this.getHashCodeNoLore();
        return (ItemRequest) super.clone();
    }

//    private String id;
//    private static final VarHandle ATOMIC_IDCACHE_HANDLE = ReflectUtils.getVarHandlePrivate(ItemRequest.class, "initializedId").withInvokeExactBehavior();
//    private boolean initializedId= false;
//    public final String getOptionalId(){
//        if(ATOMIC_IDCACHE_HANDLE.compareAndSet((ItemRequest)this,(boolean)false,(boolean)true)){
//            id= StackUtils.getOptionalId(getItemStack());
//        }
//        return id;
//    }

}
