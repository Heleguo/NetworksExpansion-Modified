package com.balugaq.netex.utils;

import io.github.sefiraat.networks.network.stackcaches.ItemStackCache;
import io.github.sefiraat.networks.utils.StackUtils;
import com.balugaq.netex.utils.Algorithms.DynamicArray;
import lombok.experimental.UtilityClass;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@UtilityClass
public class BlockMenuUtil {
    public static void pushItem(@Nonnull BlockMenu blockMenu, @Nullable ItemStack stack,int... slots) {
        pushItem(blockMenu,ItemStackCache.of(stack),false,slots);
    }
    public static void pushItemAlreadyMatched(@Nonnull BlockMenu inv, @Nullable ItemStack stack, List<Integer> slots)
    {
        int maxSize=stack.getMaxStackSize();
        int amount=stack.getAmount();
        if(amount<=0)return;
        ItemStackCache cachedStack=ItemStackCache.of(stack);
        for (int slot : slots) {
            ItemStack slotItem=inv.getItemInSlot(slot);

            if(slotItem==null||slotItem.getType()==Material.AIR) {
                int transfered=Math.min(amount,maxSize);
                inv.replaceExistingItem(slot,stack,false);
                slotItem=inv.getItemInSlot(slot);
                slotItem.setAmount(transfered);
                amount-=transfered;
            }else if(slotItem.getAmount()>=slotItem.getMaxStackSize()) {
                continue;
            }
            //for Async safety,compare again
            else if(StackUtils.itemsMatch(slotItem,cachedStack)){
                int slotAmount=slotItem.getAmount();
                int transfered=Math.min(amount,maxSize-slotAmount);
                slotItem.setAmount(slotAmount+transfered);
                amount-=transfered;
            }
            if(amount<=0){
                break;
            }
        }
        stack.setAmount(amount);
    }
    @Nullable
    public static ItemStack pushItem(@Nonnull BlockMenu blockMenu, @Nonnull ItemStackCache item,boolean needReturn, int... slots) {
        if (item == null || item.getItemType() == Material.AIR) {
            throw new IllegalArgumentException("Cannot push null or AIR");
        }
        item=ofSnapShot(blockMenu).pushItem(item,slots);
        if (needReturn&&item.getItemAmount() > 0) {
            return StackUtils.getAsQuantity(item, item.getItemAmount());
        } else {
            return null;
        }
    }
    //todo: this method should be optimized
    @Nonnull
    public static List<ItemStack> pushItem(@Nonnull BlockMenu blockMenu, @Nonnull ItemStack[] items, int... slots) {
        if (items == null || items.length == 0) {
            throw new IllegalArgumentException("Cannot push null or empty array");
        }
        List<ItemStack> listItems = Arrays.stream(items).filter(item->item != null && item.getType() != Material.AIR).toList();
        return pushItem(blockMenu, listItems, slots);
    }
    public BlockMenuSnapShot ofSnapShot(@Nonnull BlockMenu blockMenu) {
        return BlockMenuSnapShot.of(blockMenu);
    }
    public class BlockMenuSnapShot implements Cloneable {
        private static BlockMenuSnapShot instance = new BlockMenuSnapShot();
        private BlockMenu blockMenu;
        private DynamicArray<ItemStackCache> items;
        private BlockMenuSnapShot init(BlockMenu blockMenu){
            this.blockMenu=blockMenu;
            this.items=new DynamicArray<>(ItemStackCache[]::new,54,(i)->ItemStackCache.of(this.blockMenu.getItemInSlot(i)));
            return this;
        }
        public static BlockMenuSnapShot of(BlockMenu blockMenu){
            return instance.clone().init(blockMenu);
        }
        @Nonnull
        public ItemStackCache getItemInSlot(int slot){
            return items.get(slot);
        }
        public void replaceItemInSlot(int slot,@Nullable ItemStack stack,int amount){
            blockMenu.replaceExistingItem(slot, stack);
            ItemStack existing = blockMenu.getItemInSlot(slot);
            if(stack!=null){
                existing.setAmount(amount);
            }
            //todo 同步更新invCache
            getItemInSlot(slot).setItemStack(existing);
        }
        @Override
        protected BlockMenuSnapShot clone() {
            try {
                BlockMenuSnapShot clone = (BlockMenuSnapShot) super.clone();
                // TODO: copy mutable state here, so the clone can't change the internals of the original
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
        public ItemStackCache pushItem(@Nonnull ItemStack item,int... slots){
            return pushItem(ItemStackCache.of(item),slots);
        }
        @Nonnull
        public ItemStackCache pushItem(@Nonnull ItemStackCache cache,int... slots){
            Material material = cache.getItemType();
            int maxSize=material.getMaxStackSize();
            int leftAmount = cache.getItemAmount();
            for (int slot : slots) {
                if (leftAmount <= 0) {
                    break;
                }
                ItemStackCache invCache=getItemInSlot(slot);
                ItemStack existing = invCache.getItemStack();

                if (existing == null || existing.getType() == Material.AIR) {
                    int received = Math.min(leftAmount, maxSize);
                    blockMenu.replaceExistingItem(slot, cache.getItemStack());
                    existing = blockMenu.getItemInSlot(slot);
                    existing.setAmount(received);
                    //todo 同步更新invCache
                    invCache.setItemStack(existing);
                    leftAmount -= received;
                    // item.setItemAmount();
                } else {
                    int existingAmount = invCache.getItemAmount();
                    if (existingAmount >= maxSize) {
                        continue;
                    }
                    //使用缓存的槽位物品cache进行比较
                    if (!StackUtils.itemsMatch(cache, invCache)) {
                        continue;
                    }
                    int received = Math.max(0, Math.min(maxSize - existingAmount, leftAmount));
                    leftAmount -= received;
                    //todo 数量更新到invCache
                    invCache.setItemAmount(existingAmount + received);
                }
            }
            cache.setItemAmount(leftAmount);
            //left over
            return cache;
        }
        public void pushItemAlreadyMatched(@Nullable ItemStack stack, List<Integer> slots)
        {
            int maxSize=stack.getMaxStackSize();
            int amount=stack.getAmount();
            if(amount<=0)return;
            ItemStackCache cachedStack=ItemStackCache.of(stack);
            for (int slot : slots) {
                ItemStackCache slotItem=getItemInSlot(slot);

                if(slotItem.getItemStack()==null|| slotItem.getItemType()==Material.AIR) {
                    int transfered=Math.min(amount,maxSize);
                    replaceItemInSlot(slot,stack,transfered);
                    amount-=transfered;
                }else if(slotItem.isItemMaxStacked()) {
                    continue;
                }
                //for Async safety,compare again
                else if(StackUtils.itemsMatch(cachedStack,slotItem)){
                    int slotAmount=slotItem.getItemAmount();
                    int transfered=Math.min(amount,maxSize-slotAmount);
                    slotItem.setItemAmount(slotAmount+transfered);
                    amount-=transfered;
                }
                if(amount<=0){
                    break;
                }
            }
            stack.setAmount(amount);
        }
    }
    @Nonnull
    public static List<ItemStack> pushItem(@Nonnull BlockMenu blockMenu, @Nullable List<ItemStack> items, int... slots) {
        if (items ==null || items.isEmpty()) {
            throw new IllegalArgumentException("Cannot push null or empty list");
        }
        List<ItemStack> itemMap = new ArrayList<>(items.size());
        BlockMenuSnapShot snapShot=ofSnapShot(blockMenu);
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
                //ItemStack leftOver =
                ItemStackCache cache= snapShot.pushItem(item,slots);
                //left over
                if (cache.getItemStack()!=null&&cache.getItemAmount() >0) {
                    itemMap.add(cache.getItemStack());
                }
            }
        }
        return itemMap;
    }


    public static boolean fits(@Nonnull BlockMenu blockMenu, @Nonnull ItemStack item, int... slots) {
        if (item == null || item.getType() == Material.AIR) {
            return true;
        }

        int incoming = item.getAmount();
        for (int slot : slots) {
            ItemStack stack = blockMenu.getItemInSlot(slot);

            if (stack == null || stack.getType() == Material.AIR) {
                incoming -= item.getMaxStackSize();
            } else if (stack.getMaxStackSize() > stack.getAmount() && StackUtils.itemsMatch(item, stack)) {
                incoming -= stack.getMaxStackSize() - stack.getAmount();
            }

            if (incoming <= 0) {
                return true;
            }
        }

        return false;
    }

    public static boolean fits(@Nonnull BlockMenu blockMenu, @Nonnull ItemStack[] items, int... slots) {
        if (items == null || items.length == 0) {
            return false;
        }

        List<ItemStack> listItems = new ArrayList<>();
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
                listItems.add(item.clone());
            }
        }

        return fits(blockMenu, listItems, slots);
    }

    public static boolean fits(@Nonnull BlockMenu blockMenu, @Nonnull List<ItemStack> items, int... slots) {
        if (items == null || items.isEmpty()) {
            return false;
        }

        List<ItemStack> cloneMenu = new ArrayList<>();
        for (int i = 0; i < 54; i++) {
            cloneMenu.add(null);
        }

        for (int slot : slots) {
            ItemStack stack = blockMenu.getItemInSlot(slot);
            if (stack != null && stack.getType() != Material.AIR) {
                cloneMenu.set(slot, stack.clone());
            } else {
                cloneMenu.set(slot, null);
            }
        }

        for (ItemStack rawItem : items) {
            ItemStack item = rawItem.clone();
            int leftAmount = item.getAmount();
            for (int slot : slots) {
                if (leftAmount <= 0) {
                    break;
                }

                ItemStack existing = cloneMenu.get(slot);

                if (existing == null || existing.getType() == Material.AIR) {
                    int received = Math.min(leftAmount, item.getMaxStackSize());
                    cloneMenu.set(slot, StackUtils.getAsQuantity(item, leftAmount));
                    leftAmount -= received;
                    item.setAmount(Math.max(0, leftAmount));
                } else {
                    int existingAmount = existing.getAmount();
                    if (existingAmount >= item.getMaxStackSize()) {
                        continue;
                    }

                    if (!StackUtils.itemsMatch(item, existing)) {
                        continue;
                    }

                    int received = Math.max(0, Math.min(item.getMaxStackSize() - existingAmount, leftAmount));
                    leftAmount -= received;
                    existing.setAmount(existingAmount + received);
                    item.setAmount(leftAmount);
                }
            }

            if (leftAmount > 0) {
                return false;
            }
        }

        return true;
    }
}
