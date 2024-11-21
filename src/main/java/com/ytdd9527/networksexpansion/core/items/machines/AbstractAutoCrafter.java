package com.ytdd9527.networksexpansion.core.items.machines;

import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.Algorithms.DataContainer;
import com.balugaq.netex.utils.Algorithms.MenuWithData;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.stackcaches.BlueprintInstance;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.sefiraat.networks.utils.datatypes.DataTypeMethods;
import io.github.sefiraat.networks.utils.datatypes.PersistentCraftingBlueprintType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.xml.crypto.Data;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractAutoCrafter extends NetworkObject implements MenuWithData {
    private static final int[] BACKGROUND_SLOTS = new int[]{
            3, 4, 5, 12, 13, 14, 21, 22, 23
    };
    private static final int[] BLUEPRINT_BACKGROUND = new int[]{0, 1, 2, 9, 11, 18, 19, 20};
    private static final int[] OUTPUT_BACKGROUND = new int[]{6, 7, 8, 15, 17, 24, 25, 26};
    private static final int BLUEPRINT_SLOT = 10;
    private static final int OUTPUT_SLOT = 16;
    //private static final Map<Location, BlueprintInstance> INSTANCE_MAP = new HashMap<>();
    private final int chargePerCraft;
    private final boolean withholding;

    public AbstractAutoCrafter(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int chargePerCraft,
            boolean withholding
    ) {
        super(itemGroup, item, recipeType, recipe, NodeType.CRAFTER);

        this.chargePerCraft = chargePerCraft;
        this.withholding = withholding;

        this.getSlotsToDrop().add(BLUEPRINT_SLOT);
        this.getSlotsToDrop().add(OUTPUT_SLOT);

        addItemHandler(
                new BlockTicker() {
                    @Override
                    public boolean isSynchronized() {
                        return false;
                    }

                    @Override
                    public void tick(Block block, SlimefunItem slimefunItem, SlimefunBlockData data) {
                        BlockMenu blockMenu = data.getBlockMenu();
                        if (blockMenu != null) {
                            addToRegistry(block);
                            craftPreFlight(blockMenu);
                        }
                    }
                }
        );
    }

    protected void craftPreFlight(@Nonnull BlockMenu blockMenu) {
        final NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());

        if (definition == null || definition.getNode() == null) {
            sendDebugMessage(blockMenu.getLocation(), ()->"No network found");
            return;
        }
        final NetworkRoot root = definition.getNode().getRoot();
        if (!this.withholding) {
            final ItemStack stored = blockMenu.getItemInSlot(OUTPUT_SLOT);
            if (stored != null && stored.getType() != Material.AIR) {
                root.addItemStack(stored);
            }
        }
        final long networkCharge = root.getRootPower();

        if (networkCharge > this.chargePerCraft) {
            BlueprintInstance instance=(BlueprintInstance) getDataContainer(blockMenu).getObject(0);
            if(blockMenu.hasViewer()||instance==null){
                updateMenu(blockMenu);
                //update后仍旧未找到instance 视为没有
                if(getDataContainer(blockMenu).getObject(0)==null){
                    return;
                }
            }
            final ItemStack output = blockMenu.getItemInSlot(OUTPUT_SLOT);
            if (output != null
                    && output.getType() != Material.AIR
                    && (output.getAmount() + instance.getItemStack().getAmount() > output.getMaxStackSize() || !StackUtils.itemsMatch(instance, output))) {
                sendDebugMessage(blockMenu.getLocation(), ()->"Output slot is full");
                return;
            }
            if (tryCraft(blockMenu, instance, root)) {
                root.removeRootPower(this.chargePerCraft);
            }
        }
    }
    public DataContainer newDataContainer(){
        return new DataContainer() {
            Object blueprintInstance;
            ItemStack lastRecipeOut;
            ItemRequest[] lastRecipeStacked;
            public Object getObject(int lva){
                return blueprintInstance;
            }
            public void setObject(int lva, Object object){
                this.blueprintInstance = object;
            }
            public ItemStack getItemStack(int lva){
                return lastRecipeOut;
            }
            public void setItemStack(int lva, ItemStack item){
                lastRecipeOut = item;
            }
            public ItemRequest[] getItemRequests(int v){
                return lastRecipeStacked;
            }
            public void setItemRequests(int v, ItemRequest... items){
                lastRecipeStacked = items;
            }
        };
    }
    public int getDataSlot(){
        return 0;
    }
    protected void updateMenu(BlockMenu blockMenu){
        DataContainer dataContainer=getDataContainer(blockMenu);
        //reset blueprint cache
        dataContainer.setObject(0,null);
        dataContainer.setItemStack(0,null);
        dataContainer.setItemRequests(0,null);
        //try find new blueprint cache
        final ItemStack blueprint = blockMenu.getItemInSlot(BLUEPRINT_SLOT);

        if (blueprint == null || blueprint.getType() == Material.AIR) {
            sendDebugMessage(blockMenu.getLocation(), ()->"No blueprint found");
            return;
        }
        final SlimefunItem item = SlimefunItem.getByItem(blueprint);

        if (!isValidBlueprint(item)) {
            sendDebugMessage(blockMenu.getLocation(), ()->"Invalid blueprint");
            return;
        }

        //fetch blutprint instance
        final ItemMeta blueprintMeta = blueprint.getItemMeta();
        Optional<BlueprintInstance> optional;
        optional = DataTypeMethods.getOptionalCustom(blueprintMeta, Keys.BLUEPRINT_INSTANCE, PersistentCraftingBlueprintType.TYPE);
        if (optional.isEmpty()) {
            optional = DataTypeMethods.getOptionalCustom(blueprintMeta, Keys.BLUEPRINT_INSTANCE2, PersistentCraftingBlueprintType.TYPE);
        }

        if (optional.isEmpty()) {
            optional = DataTypeMethods.getOptionalCustom(blueprintMeta, Keys.BLUEPRINT_INSTANCE3, PersistentCraftingBlueprintType.TYPE);
        }

        if (optional.isEmpty()) {
            sendDebugMessage(blockMenu.getLocation(), ()->"No blueprint instance found");
            return;
        }

        BlueprintInstance instance = optional.get();
        dataContainer.setObject(0,instance);
        ItemStack[] inputs=instance.getRecipeItems();
        ItemStack crafted=null;
        //recipe should be fixed
        for (Map.Entry<ItemStack[], ItemStack> entry : getRecipeEntries()) {
            if (getRecipeTester(inputs, entry.getKey())) {
                crafted = entry.getValue().clone();
                dataContainer.setItemStack(0,crafted);
                break;
            }
        }
    }
    private boolean tryCraft(@Nonnull BlockMenu blockMenu, @Nonnull BlueprintInstance instance, @Nonnull NetworkRoot root) {
        // Get the recipe input

        DataContainer container=getDataContainer(blockMenu);
        /* Make sure the network has the required items
         * Needs to be revisited as matching is happening stacks 2x when I should
         * only need the one
         */
        ItemRequest[] request=container.getItemRequests(0);
        if(request==null){
            HashMap<ItemStack, Integer> requiredItems = new HashMap<>();
            for (int i = 0; i < 9; i++) {
                final ItemStack requested = instance.getRecipeItems()[i];
                if (requested != null) {
                    requiredItems.merge(requested, requested.getAmount(), Integer::sum);
                }
            }
            request=requiredItems.entrySet().stream().map(e->ItemRequest.of(e.getKey(),e.getValue())).toArray(ItemRequest[]::new);
            container.setItemRequests(0,request);
        }
        //this is shit
        for (ItemRequest itemRequest : request) {
            if (!root.contains(itemRequest)) {
                sendDebugMessage(blockMenu.getLocation(), ()->"Network does not have required items");
                return false;
            }
        }
        final ItemStack[] fetch = new ItemStack[request.length];
        // Then fetch the actual items
        boolean amountMatched=true;
        for (int i=0;i<request.length;i++) {
            final ItemRequest requested = request[i];
            if (requested.getItemStack() != null) {
                final ItemStack fetched = root.getItemStack(requested.clone());
                if(fetched!=null&&fetched.getAmount()==requested.getAmount()){
                    fetch[i] = fetched;
                }else {
                    amountMatched=false;
                    break;
                }
            } else {
                fetch[i] = null;
            }
        }
        if(!amountMatched){
            sendDebugMessage(blockMenu.getLocation(), ()->"Network does not fetch required items");
            final ItemRequest[] requestFinal=request;
            sendDebugMessage(blockMenu.getLocation(), ()->"expected-requests: " + Arrays.toString(requestFinal));
            sendDebugMessage(blockMenu.getLocation(), ()->"actually-fetched: " + Arrays.toString(fetch));
            returnItems(root,fetch);
            return false;
        }
        //item fetched ,now we assumed that they can be perfectly filled into the inputPattern of the BLUEPRINT_INSTANCE
        //so we can use the inputPattern result cached

        // Go through each slimefun recipe, test and set the ItemStack if found
        //they are done in updateMenu and cached in container ,if crafted = null, try again
        ItemStack[] inputPattern=instance.getRecipeItems();
        ItemStack crafted =container.getItemStack(0);
        if(crafted==null){
            for (Map.Entry<ItemStack[], ItemStack> entry : getRecipeEntries()) {
                if (getRecipeTester(inputPattern, entry.getKey())) {
                    //get A copy of matched recipeOutput
                    container.setItemStack(0,entry.getValue());
                    crafted = entry.getValue().clone();
                    break;
                }
            }
        }else{
            //get A copy of cached itemOutput
            crafted=crafted.clone();
        }
        //this is cached in BluePrint Instance
        if (crafted == null && canTestVanillaRecipe()) {
            sendDebugMessage(blockMenu.getLocation(), ()->"No slimefun recipe found, trying vanilla");
            // If no slimefun recipe found, try a vanilla one
            instance.generateVanillaRecipe(blockMenu.getLocation().getWorld());
            if (instance.getRecipe() == null) {
                returnItems(root, fetch);
                sendDebugMessage(blockMenu.getLocation(), ()->"No vanilla recipe found");
                return false;
            } else  {
                //setCache(blockMenu, instance);
                crafted = instance.getRecipe().getResult();
            }
        }

        // If no item crafted OR result doesn't fit, escape
        if (crafted == null || crafted.getType() == Material.AIR) {
            sendDebugMessage(blockMenu.getLocation(), ()->"No valid recipe found");
            sendDebugMessage(blockMenu.getLocation(), ()->"expected-inputs: " + Arrays.toString(inputPattern));
            sendDebugMessage(blockMenu.getLocation(), ()->"actually-fetched: " + Arrays.toString(fetch));
            returnItems(root, fetch);
            return false;
        }

        // Push item
        if (root.isDisplayParticles()) {
            final Location location = blockMenu.getLocation().clone().add(0.5, 1.1, 0.5);
            location.getWorld().spawnParticle(Particle.WAX_OFF, location, 0, 0, 4, 0);
        }
        blockMenu.pushItem(crafted, OUTPUT_SLOT);
        return true;
    }

    private void returnItems(@Nonnull NetworkRoot root, @Nonnull ItemStack[] inputs) {
        for (ItemStack input : inputs) {
            if (input != null) {
                root.addItemStack(input);
            }
        }
    }

//    public void releaseCache(@Nonnull BlockMenu blockMenu) {
//        if (blockMenu.hasViewer()) {
//            INSTANCE_MAP.remove(blockMenu.getLocation());
//        }
//    }
//
//    public void setCache(@Nonnull BlockMenu blockMenu, @Nonnull BlueprintInstance blueprintInstance) {
//        if (!blockMenu.hasViewer()) {
//            INSTANCE_MAP.putIfAbsent(blockMenu.getLocation().clone(), blueprintInstance);
//        }
//    }


    @Override
    public void postRegister() {
        new BlockMenuPreset(this.getId(), this.getItemName()) {

            @Override
            public void init() {
                drawBackground(BACKGROUND_SLOTS);
                drawBackground(Icon.BLUEPRINT_BACKGROUND_STACK, BLUEPRINT_BACKGROUND);
                drawBackground(Icon.OUTPUT_BACKGROUND_STACK, OUTPUT_BACKGROUND);
            }

            @Override
            public boolean canOpen(@Nonnull Block block, @Nonnull Player player) {
                return player.hasPermission("slimefun.inventory.bypass") || (this.getSlimefunItem().canUse(player, false)
                        && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK));
            }
            public void newInstance(BlockMenu inv,Block b){
                inv.addMenuOpeningHandler(player -> updateMenu(inv));
                inv.addMenuCloseHandler(player -> updateMenu(inv));
                updateMenu(inv);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                if (AbstractAutoCrafter.this.withholding && flow == ItemTransportFlow.WITHDRAW) {
                    return new int[]{OUTPUT_SLOT};
                }
                return new int[0];
            }
        };
    }

    public abstract boolean isValidBlueprint(SlimefunItem item);

    public abstract Set<Map.Entry<ItemStack[], ItemStack>> getRecipeEntries();

    public abstract boolean getRecipeTester(ItemStack[] inputs, ItemStack[] recipe);

    public boolean canTestVanillaRecipe() {
        return false;
    }
}
