package com.ytdd9527.networksexpansion.core.items.machines;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.Algorithms.DataContainer;
import com.balugaq.netex.utils.Algorithms.MenuWithData;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.NetworkAsyncUtil;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.managers.ExperimentalFeatureManager;
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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.xml.crypto.Data;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

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
    protected int craftlimit=1;
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
            sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
            return;
        }

        final NetworkRoot root = definition.getNode().getRoot();

        CompletableFuture<Void> future=CompletableFuture.runAsync(() -> {
            if (!this.withholding) {
                final ItemStack stored = blockMenu.getItemInSlot(OUTPUT_SLOT);
                if (stored != null && stored.getType() != Material.AIR) {
                    root.addItemStack(stored);
                }
            }
        }, NetworkAsyncUtil.getInstance().getParallelExecutor());

        final long networkCharge = root.getRootPower();
        DataContainer container=getDataContainer(blockMenu);
        if (networkCharge > this.chargePerCraft) {
            BlueprintInstance instance=(BlueprintInstance)container.getObject(0);
            if(blockMenu.hasViewer()||instance==null){
                updateMenu(blockMenu);
                //update后仍旧未找到instance 视为没有
                if((instance=(BlueprintInstance) container.getObject(0))==null){
                    return;
                }
            }
            int craftAmount=Math.min(container.getInt(0),craftlimit);
            if(craftAmount<=0){
                return;
            }
            final ItemStack output = blockMenu.getItemInSlot(OUTPUT_SLOT);
            if (output != null
                    && output.getType() != Material.AIR
                    && (output.getAmount() + instance.getItemStack().getAmount()*craftAmount > output.getMaxStackSize()
                   // || !StackUtils.itemsMatch(instance, output)
                    //we will match it when we got the last crafted result
            )) {
                sendDebugMessage(blockMenu.getLocation(), ()->"Output slot is full");
                return;
            }
            //ExperimentalFeatureManager.getInstance().setEnableGlobalDebugFlag(true);
            tryCraft(blockMenu, instance, root,craftAmount,future);
            //ExperimentalFeatureManager.getInstance().endGlobalProfiler(()->"returned, %s");
            //ExperimentalFeatureManager.getInstance().setEnableGlobalDebugFlag(false);
        }
    }
    public DataContainer newDataContainer(){
        return new DataContainer() {
            int blueprintAmount=0;
            Object blueprintInstance;
            ItemStack lastRecipeOut;
            ItemRequest[] lastRecipeStacked;
            public int getInt(int val){
                return this.blueprintAmount;
            }
            public void setInt(int val,int val2){
                this.blueprintAmount=val2;
            }
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
        dataContainer.setInt(0,0);
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
        dataContainer.setInt(0,blueprint.getAmount());
        ItemStack[] inputs=instance.getRecipeItems();
        //recipe should be fixed
        for (Map.Entry<ItemStack[], ItemStack> entry : getRecipeEntries()) {
            if (getRecipeTester(inputs, entry.getKey())) {
                dataContainer.setItemStack(0, ItemStackUtil.getCleanItem(entry.getValue()));
                break;
            }
        }
        dataContainer.setItemStack(0,null);
        sendDebugMessage(blockMenu.getLocation(), ()->"blueprint instance set in cache");
    }
    private void tryCraft(@Nonnull BlockMenu blockMenu, @Nonnull BlueprintInstance instance, @Nonnull NetworkRoot root,int craftAmount,CompletableFuture<Void> waitInput) {
        // Get the recipe input
        //ExperimentalFeatureManager.getInstance().startGlobalProfiler();
        DataContainer container=getDataContainer(blockMenu);
        /* Make sure the network has the required items
         * Needs to be revisited as matching is happening stacks 2x when I should
         * only need the one
         */
        ItemRequest[] request0=container.getItemRequests(0);
        if(request0==null){
            sendDebugMessage(blockMenu.getLocation(), ()->"cached request is null");
            HashMap<ItemStack, Integer> requiredItems = new HashMap<>();
            for (int i = 0; i < 9; i++) {
                ItemStack requested = instance.getRecipeItems()[i];
                if (requested != null) {
                    //requested can be amount>1
                    //force set this to 1
                    int amount=requested.getAmount();
                    requested=requested.clone();
                    requested.setAmount(1);
                    requiredItems.merge(requested,amount, Integer::sum);
                }
            }
            //here we add craftAmount to multiply the request
            request0=requiredItems.entrySet().stream().map(e->ItemRequest.of(e.getKey(),e.getValue()*craftAmount)).toArray(ItemRequest[]::new);
            container.setItemRequests(0,request0);
        }
        final ItemRequest[] request=request0;
        //ExperimentalFeatureManager.getInstance().endGlobalProfiler(()->"fetch request ,%s");
        //ExperimentalFeatureManager.getInstance().startGlobalProfiler();
        //this is shit
        for (ItemRequest itemRequest : request) {
            if (!root.contains(itemRequest)) {
                final ItemRequest[] requests=request;
                sendDebugMessage(blockMenu.getLocation(), ()->"Network does not have required items,%s".formatted(Arrays.toString(requests)));
                return ;
            }
        }
        //ExperimentalFeatureManager.getInstance().endGlobalProfiler(()->"contains ,%s");
        //ExperimentalFeatureManager.getInstance().startGlobalProfiler();
        final ItemStack[] fetch = new ItemStack[request.length];
        // Then fetch the actual items
        AtomicBoolean amountMatched=new AtomicBoolean(true);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i=0;i<request.length;i++) {
            final int index=i;
            final ItemRequest requested = request[index];
            futures.add(CompletableFuture.runAsync(()->{
                if (requested.getItemStack() != null&&amountMatched.get()) {
                    final ItemStack fetched = root.getItemStack(requested.clone());
                    fetch[index] = fetched;
                    if(fetched==null||fetched.getAmount()<requested.getAmount()){
                        amountMatched.set(false);
                    }
                } else {
                    fetch[index] = null;
                }
            },NetworkAsyncUtil.getInstance().getParallelExecutor()));
        }
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRunAsync(()->{
            if(!amountMatched.get()){
                final ItemRequest[] requestFinal=request;
                sendDebugMessage(blockMenu.getLocation(),
                        ()->"Network does not fetch required items",
                        ()->"expected-requests: " + Arrays.toString(requestFinal),
                        ()->"actually-fetched: " + Arrays.toString(fetch)
                );
                returnItems(root,fetch);
                return;
            }
            //ExperimentalFeatureManager.getInstance().endGlobalProfiler(()->"getItemStack ,%s");
            //ExperimentalFeatureManager.getInstance().startGlobalProfiler();
            //item fetched ,now we assumed that they can be perfectly filled into the inputPattern of the BLUEPRINT_INSTANCE
            //so we can use the inputPattern result cached

            // Go through each slimefun recipe, test and set the ItemStack if found
            //they are done in updateMenu and cached in container ,if crafted = null, try again
            ItemStack[] inputPattern=instance.getRecipeItems();
            ItemStack crafted =container.getItemStack(0);
            if(crafted==null){
                sendDebugMessage(blockMenu.getLocation(), ()->"BluePrint do not cache a slimefun recipe");
                for (Map.Entry<ItemStack[], ItemStack> entry : getRecipeEntries()) {
                    if (getRecipeTester(inputPattern, entry.getKey())) {
                        //get A copy of matched recipeOutput
                        crafted= ItemStackUtil.getCleanItem(entry.getValue());
                        container.setItemStack(0,crafted);
                        break;
                    }
                }
            }
            //else{
            //get A copy of cached itemOutput
            //sendDebugMessage(blockMenu.getLocation(), ()->"get Copy of cached crafted result");

            //}
            //this is cached in BluePrint Instance
            if (crafted == null && canTestVanillaRecipe()) {
                //sendDebugMessage(blockMenu.getLocation(), ()->"No slimefun recipe found, trying vanilla");
                // If no slimefun recipe found, try a vanilla one
                instance.generateVanillaRecipe(blockMenu.getLocation().getWorld());
                if (instance.getRecipe() == null) {
                    returnItems(root, fetch);
                    sendDebugMessage(blockMenu.getLocation(), ()->"No vanilla recipe found");
                    return;
                } else  {
                    //setCache(blockMenu, instance);
                    crafted = instance.getRecipe().getResult();
                }
            }

            // If no item crafted OR result doesn't fit, escape
            if (crafted == null || crafted.getType() == Material.AIR) {
                sendDebugMessage(blockMenu.getLocation(),
                        ()->"No valid recipe found",
                        ()->"expected-inputs: " + Arrays.toString(inputPattern),
                        ()->"actually-fetched: " + Arrays.toString(fetch)
                );
                returnItems(root, fetch);
                return;
            }
            //ExperimentalFeatureManager.getInstance().endGlobalProfiler(()->"matchRecipe ,%s");
            //ExperimentalFeatureManager.getInstance().startGlobalProfiler();
            // Push item
            //in this place ,we don't modify crafted,so nothing will change in cached crafted
            //wait input task end so we can safely modify outputSlot
            waitInput.join();
            ItemStack outputSlot=blockMenu.getItemInSlot(OUTPUT_SLOT);
            if(outputSlot==null||outputSlot.getType()==Material.AIR){
                blockMenu.replaceExistingItem(OUTPUT_SLOT,crafted,false);
                if(craftAmount>1){
                    ItemStack pushed=blockMenu.getItemInSlot(OUTPUT_SLOT);
                    pushed.setAmount(craftAmount*crafted.getAmount());
                }
            }else if(outputSlot.getAmount()<outputSlot.getMaxStackSize()&&StackUtils.itemsMatch(outputSlot,crafted)){
                outputSlot.setAmount(outputSlot.getAmount()+crafted.getAmount()*craftAmount);
            }else{
                sendDebugMessage(blockMenu.getLocation(), ()->"Output slot item no space or hot fit");
                returnItems(root, fetch);
            }
            if (root.isDisplayParticles()) {
                final Location location = blockMenu.getLocation().clone().add(0.5, 1.1, 0.5);
                location.getWorld().spawnParticle(Particle.WAX_OFF, location, 0, 0, 4, 0);
            }
            //blockMenu.pushItem(crafted, OUTPUT_SLOT);
            root.removeRootPower(this.chargePerCraft);
            return;
        });
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
