package com.ytdd9527.networksexpansion.implementation.machines.networks.advanced;

import com.balugaq.netex.api.helpers.Icon;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.errorprone.annotations.Immutable;
import com.ytdd9527.networksexpansion.core.services.LocalizationService;
import com.ytdd9527.networksexpansion.implementation.ExpansionItems;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import com.ytdd9527.networksexpansion.implementation.tools.ItemMover;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.stackcaches.ItemStackCache;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.sefiraat.networks.utils.datatypes.DataTypeMethods;
import io.github.sefiraat.networks.utils.datatypes.PersistentQuantumStorageType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

public class NetworkResetter extends NetworkObject {
    private final int[] INPUT_SLOTS = new int[]{
            19
    };
    private final int[] OUTPUT_SLOTS = new int[]{
            25
    };
    private final int[] BORDER_SLOTS = new int[]{
            0,1,2,3,4,5,6,7,8,36,37,38,39,40,41,42,43,44,
            12,13,14,21,22,23,30,31,32
    };
    private final int[] BORDER_INPUT_SLOTS = new int[]{
            9,10,11,18,20,27,28,29
    };
    private final int[] BORDER_OUTPUT_SLOTS=new int[]{
            15, 16, 17,
            24,  26,
            33, 34, 35,
    };
    private final int RESET_SIMPLE_SLOT=13;
    private final ItemStack RESET_SIMPLE_ITEM = Icon.RESET_SIMPLE;
    private final int RESET_STORAGE_SLOT=31;
    private final ItemStack RESET_STORAGE_ITEM = Icon.RESET_STORAGE;
    public NetworkResetter(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe, NodeType.CUSTOM);
        Arrays.stream(INPUT_SLOTS).forEach(getSlotsToDrop()::add);
        Arrays.stream(OUTPUT_SLOTS).forEach(getSlotsToDrop()::add);
    }


    @Override
    public void postRegister() {
        new BlockMenuPreset(this.getId(), this.getItemName()) {

            @Override
            public void init() {
                setSize(45);
                for(int i:BORDER_SLOTS){
                    this.addItem(i, ChestMenuUtils.getBackground(),ChestMenuUtils.getEmptyClickHandler());
                }
                for(int i:BORDER_INPUT_SLOTS){
                    this.addItem(i, ChestMenuUtils.getInputSlotTexture(),ChestMenuUtils.getEmptyClickHandler());
                }
                for (int i:BORDER_OUTPUT_SLOTS){
                    this.addItem(i,ChestMenuUtils.getOutputSlotTexture(),ChestMenuUtils.getEmptyClickHandler());
                }
                this.addItem(RESET_SIMPLE_SLOT,RESET_SIMPLE_ITEM);
                this.addItem(RESET_STORAGE_SLOT,RESET_STORAGE_ITEM);
            }
            @Override
            public void newInstance(@NotNull BlockMenu menu, @NotNull Block b) {
                NetworkResetter.this.newMenuInstance(menu,b);
            }
            @Override
            public boolean canOpen(@Nonnull Block block, @Nonnull Player player) {
                return player.hasPermission("slimefun.inventory.bypass") || (NetworkResetter.this.canUse(player, false)
                        && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK));
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                if (flow == ItemTransportFlow.INSERT) {
                    return INPUT_SLOTS;
                }else {
                    return OUTPUT_SLOTS;
                }
            }
        };
    }

    private final int energyConsumption=1000;
    public void newMenuInstance(BlockMenu menu,Block b) {
        menu.addMenuClickHandler(RESET_SIMPLE_SLOT,((player, i, itemStack, clickAction) -> {
            final NodeDefinition definition = NetworkStorage.getNode(menu.getLocation());

            if (definition.getNode() == null||definition.getNode().getRoot()==null) {
                return false;
            }
            NetworkRoot root=definition.getNode().getRoot();
            if(root.getRootPower()>=energyConsumption){
                ItemStack item=menu.getItemInSlot(INPUT_SLOTS[0]);
                if(item==null||item.getType()==Material.AIR){
                    player.sendMessage(Networks.getLocalizationService().getString("messages.unsupported-operation.resetter.null_input"));
                    return false;
                }
                ItemStack output=menu.getItemInSlot(OUTPUT_SLOTS[0]);
                if(output!=null&&!output.getType().isAir()){
                    player.sendMessage(Networks.getLocalizationService().getString("messages.unsupported-operation.resetter.output_slot_full"));
                    return false;
                }
                SlimefunItem sfitem=SlimefunItem.getByItem(item);
                if(sfitem==null){
                    menu.replaceExistingItem(OUTPUT_SLOTS[0], item);
                    item.setAmount(0);
                }else {
                    if(sfitem instanceof NetworksDrawer ){
                        player.sendMessage(Networks.getLocalizationService().getString("messages.unsupported-operation.resetter.input_not_allowed"));
                        return false;
                    }
                    ItemStack stack=getResetItem(item,sfitem);
                    menu.replaceExistingItem(OUTPUT_SLOTS[0], stack);
                    item.setAmount(0);
                    root.removeRootPower(energyConsumption);
                }
            }else {
                player.sendMessage(Networks.getLocalizationService().getString("messages.unsupported-operation.resetter.no_power"));
            }
            return false;
        }));
        menu.addMenuClickHandler(RESET_STORAGE_SLOT,((player, i, itemStack, clickAction) -> {
            final NodeDefinition definition = NetworkStorage.getNode(menu.getLocation());

            if (definition.getNode() == null||definition.getNode().getRoot()==null) {
                return false;
            }
            NetworkRoot root=definition.getNode().getRoot();
            if(root.getRootPower()>=energyConsumption){
                ItemStack item=menu.getItemInSlot(INPUT_SLOTS[0]);
                if(item==null||item.getType()==Material.AIR){
                    player.sendMessage(Networks.getLocalizationService().getString("messages.unsupported-operation.resetter.null_input"));
                    return false;
                }
                ItemStack output=menu.getItemInSlot(OUTPUT_SLOTS[0]);
                if(output!=null&&!output.getType().isAir()){
                    player.sendMessage(Networks.getLocalizationService().getString("messages.unsupported-operation.resetter.output_slot_full"));
                    return false;
                }
                SlimefunItem sfitem=SlimefunItem.getByItem(item);
                if(sfitem instanceof NetworkQuantumStorage qs){
                    final ItemMeta itemMeta = item.getItemMeta();
                    try{
                        final QuantumCache cache=DataTypeMethods.getCustom(itemMeta, Keys.QUANTUM_STORAGE_INSTANCE, PersistentQuantumStorageType.TYPE);
                        if(cache!=null){
                            cache.setItemStack(getResetItem(cache.getItemStack(),SlimefunItem.getByItem(cache.getItemStack())));
                            DataTypeMethods.setCustom(itemMeta,Keys.QUANTUM_STORAGE_INSTANCE,PersistentQuantumStorageType.TYPE,cache);
                            cache.updateMetaLore(itemMeta);
                            item.setItemMeta(itemMeta);
                            menu.replaceExistingItem(OUTPUT_SLOTS[0], item);
                            item.setAmount(0);
                            root.removeRootPower(energyConsumption);
                            return false;
                        }
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }else if(sfitem instanceof ItemMover im){
                    final ItemMeta itemMeta = item.getItemMeta();
                    try{
                        ItemStack stored=ItemMover.getStoredItemStack(itemMeta);
                        if(stored!=null){
                            ItemMover.setStoredItemStack(item,getResetItem(stored,SlimefunItem.getByItem(stored)));
                            ItemMover.updateLore(item);
                            menu.replaceExistingItem(OUTPUT_SLOTS[0],item);
                            item.setAmount(0);
                            root.removeRootPower(energyConsumption);
                            return false;
                        }
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }
                player.sendMessage(Networks.getLocalizationService().getString("messages.unsupported-operation.resetter.invalid_storage"));
                return false;

            }else {
                player.sendMessage(Networks.getLocalizationService().getString("messages.unsupported-operation.resetter.no_power"));
            }
            return false;
        }));
    }
    public ItemStack getResetItem(ItemStack origin, SlimefunItem sfitem){
        if(sfitem==null){
            return origin;
        }
        ItemStack stack= ItemStackUtil.getCleanItem(sfitem.getItem());
        stack.setAmount(origin.getAmount());
        ItemMeta meta=stack.getItemMeta();
        ItemMeta originMeta=origin.getItemMeta();
        if(meta!=null&&originMeta!=null){
            //restore ench
            if(meta.hasEnchants()){
                for(Enchantment enchantment:meta.getEnchants().keySet()){
                    meta.removeEnchant(enchantment);
                }
            }
            for(Map.Entry<Enchantment,Integer> entry:originMeta.getEnchants().entrySet()){
                meta.addEnchant(entry.getKey(),entry.getValue(),true);
            }
            //restore attr
            if(originMeta.hasAttributeModifiers()){
                meta.setAttributeModifiers(originMeta.getAttributeModifiers());
            }else{
                meta.setAttributeModifiers(ImmutableMultimap.of());
            }

            if(meta instanceof Damageable dm1&&originMeta instanceof Damageable dm2){
                dm1.setDamage(dm2.getDamage());
            }
        }
        stack.setItemMeta(meta);
        return stack;
    }

}
