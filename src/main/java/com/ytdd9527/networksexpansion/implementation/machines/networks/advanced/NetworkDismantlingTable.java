package com.ytdd9527.networksexpansion.implementation.machines.networks.advanced;

import com.balugaq.netex.api.helpers.Icon;
import com.ytdd9527.networksexpansion.core.services.LocalizationService;
import com.ytdd9527.networksexpansion.implementation.ExpansionItems;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.stackcaches.ItemStackCache;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumWorkbench;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

public class NetworkDismantlingTable extends NetworkObject implements RecipeDisplayItem {
    private final int[] INPUT_SLOTS = new int[]{
            19
    };
    private final int[] OUTPUT_SLOTS = new int[]{
           15, 16, 17,
            24, 25, 26,
           33, 34, 35,
    };
    private final int[] BORDER_SLOTS = new int[]{
            0,1,2,3,4,5,6,7,8,36,37,38,39,40,41,42,43,44,
            12,13,14,21,22,23,30,31,32
    };
    private final int[] BORDER_INPUT_SLOTS = new int[]{
            9,10,11,18,20,27,28,29
    };
    private final int DISMANT_SLOT=22;
    private final ItemStack DISMANT_ITEM= Icon.DISMANT_BUTTON;

    public NetworkDismantlingTable(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
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
                this.addItem(DISMANT_SLOT,DISMANT_ITEM);
            }
            @Override
            public void newInstance(@NotNull BlockMenu menu, @NotNull Block b) {
                NetworkDismantlingTable.this.newMenuInstance(menu,b);
            }
            @Override
            public boolean canOpen(@Nonnull Block block, @Nonnull Player player) {
                return player.hasPermission("slimefun.inventory.bypass") || (NetworkDismantlingTable.this.canUse(player, false)
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
    List<MachineRecipe> machineRecipes=null;
    public List<MachineRecipe> getMachineRecipes(){
        if(machineRecipes==null||machineRecipes.isEmpty()){
            machineRecipes=new ArrayList<>();
            for(SlimefunItem item:Slimefun.getRegistry().getAllSlimefunItems()){
                if(item.getAddon()==Networks.getInstance()&&item.getRecipeType()!=RecipeType.NULL&& item.getRecipeType()!= NetworkQuantumWorkbench.TYPE &&item.getRecipe().length<=9){
                    machineRecipes.add(new MachineRecipe(0,new ItemStack[]{item.getRecipeOutput()},item.getRecipe()));
                }
            }
        }
        return machineRecipes;
    }
    List<ItemStack> displayedItems=null;
    @NotNull
    @Override
    public List<ItemStack> getDisplayRecipes() {
        if(displayedItems==null||displayedItems.isEmpty()){
            List<MachineRecipe> machineRecipes=getMachineRecipes();
            displayedItems=new ArrayList<>();
            for(MachineRecipe machineRecipe:machineRecipes){
                displayedItems.add(ChestMenuUtils.getBackground());
                displayedItems.add(machineRecipe.getInput()[0]);
            }
        }
        return displayedItems;
    }
    private final int energyConsumption=1000;
    public void newMenuInstance(BlockMenu menu,Block b) {
        menu.addMenuClickHandler(DISMANT_SLOT,((player, i, itemStack, clickAction) -> {
            final NodeDefinition definition = NetworkStorage.getNode(menu.getLocation());

            if (definition.getNode() == null||definition.getNode().getRoot()==null) {
                return false;
            }
            NetworkRoot root=definition.getNode().getRoot();
            if(root.getRootPower()>=energyConsumption){
                ItemStack item=menu.getItemInSlot(INPUT_SLOTS[0]);
                if(item==null||item.getType()==Material.AIR){
                    player.sendMessage(Networks.getLocalizationService().getString("messages.unsupported-operation.dismant-table.null_input"));
                    return false;
                }
                ItemStack cache=item;//ItemStackCache.of(item);
                for(MachineRecipe machineRecipe:getMachineRecipes()){
                    if(StackUtils.itemsMatch(cache,machineRecipe.getInput()[0])){
                        craft(root,item,machineRecipe,menu);
                        break;
                    }
                }
            }else {
                player.sendMessage(Networks.getLocalizationService().getString("messages.unsupported-operation.dismant-table.no_power"));
            }
            return false;
        }));
    }
    public void craft(NetworkRoot root,ItemStack input,MachineRecipe recipe,BlockMenu menu){
        int inputAmount=recipe.getInput()[0].getAmount();
        if(inputAmount<=0){
            return;
        }
        int maxCraft=input.getAmount()/inputAmount;
        if(root.getRootPower()<(long) maxCraft*energyConsumption){
            maxCraft=((int)root.getRootPower())/energyConsumption;
        }
        ItemStack[] output=recipe.getOutput();
        for(int i=0;i<output.length;i++){
            if(i>OUTPUT_SLOTS.length){
                return;
            }
            ItemStack item=output[i];
            if(item==null||item.getType()==Material.AIR){
                continue;
            }
            ItemStack tarItem=menu.getItemInSlot(OUTPUT_SLOTS[i]);
            if(tarItem==null){
                maxCraft=Math.min(maxCraft,item.getMaxStackSize()/item.getAmount());
            }else {
                if(StackUtils.itemsMatch(tarItem,item)){
                    maxCraft=Math.min(maxCraft,(item.getMaxStackSize()-tarItem.getAmount())/item.getAmount());
                }
                else {
                    return;
                }
            }
        }
        if(maxCraft>0){
            input.setAmount(input.getAmount()-maxCraft*inputAmount);
            for(int i=0;i<output.length;i++){
                ItemStack item=output[i];
                if(item==null||item.getType()==Material.AIR){
                    continue;
                }
                ItemStack tarItem=menu.getItemInSlot(OUTPUT_SLOTS[i]);
                if(tarItem==null){
                    menu.replaceExistingItem(OUTPUT_SLOTS[i],StackUtils.getAsQuantity(item,item.getAmount()*maxCraft));
                }else {
                    tarItem.setAmount(tarItem.getAmount()+maxCraft*item.getAmount());
                }
            }
            root.removeRootPower((long) maxCraft*energyConsumption);
        }
    }
}
