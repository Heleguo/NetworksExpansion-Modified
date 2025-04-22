package com.ytdd9527.networksexpansion.core.items.machines;

import com.balugaq.netex.utils.algorithms.MenuWithData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractAdvancedAutoCrafter extends AbstractAutoCrafter implements MenuWithData {
//    private static final int[] BACKGROUND_SLOTS = new int[]{
//            3, 4, 5, 12, 13, 14, 21, 22, 23
//    };
//    private static final int[] BLUEPRINT_BACKGROUND = new int[]{0, 1, 2, 9, 11, 18, 19, 20};
//    private static final int[] OUTPUT_BACKGROUND = new int[]{6, 7, 8, 15, 17, 24, 25, 26};
//    private static final int BLUEPRINT_SLOT = 10;
//    private static final int OUTPUT_SLOT = 16;
//    //private static final Map<Location, BlueprintInstance> INSTANCE_MAP = new HashMap<>();
//    private final int chargePerCraft;
//    private final boolean withholding;

    public AbstractAdvancedAutoCrafter(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int chargePerCraft,
            boolean withholding
    ) {
        super(itemGroup, item, recipeType, recipe, chargePerCraft,withholding);
        this.craftlimit=64;
//        this.chargePerCraft = chargePerCraft;
//        this.withholding = withholding;
//
//        this.getSlotsToDrop().add(BLUEPRINT_SLOT);
//        this.getSlotsToDrop().add(OUTPUT_SLOT);
//
//        addItemHandler(
//                new BlockTicker() {
//                    @Override
//                    public boolean isSynchronized() {
//                        return false;
//                    }
//
//                    @Override
//                    public void tick(Block block, SlimefunItem slimefunItem, SlimefunBlockData data) {
//                        BlockMenu blockMenu = data.getBlockMenu();
//                        if (blockMenu != null) {
//                            addToRegistry(block);
//                            craftPreFlight(blockMenu);
//                        }
//                    }
//                }
//        );
    }

//    protected void craftPreFlight(@Nonnull BlockMenu blockMenu) {
//
//        releaseCache(blockMenu);
//
//        final NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());
//
//        if (definition == null || definition.getNode() == null) {
//            sendDebugMessage(blockMenu.getLocation(), ()->"No network found");
//            return;
//        }
//
//        final NetworkRoot root = definition.getNode().getRoot();
//
//        if (!this.withholding) {
//            final ItemStack stored = blockMenu.getItemInSlot(OUTPUT_SLOT);
//            if (stored != null && stored.getType() != Material.AIR) {
//                root.addItemStack(stored);
//            }
//        }
//
//        final ItemStack blueprint = blockMenu.getItemInSlot(BLUEPRINT_SLOT);
//
//        if (blueprint == null || blueprint.getType() == Material.AIR) {
//            sendDebugMessage(blockMenu.getLocation(), ()->"No blueprint found");
//            return;
//        }
//
//        final long networkCharge = root.getRootPower();
//
//        if (networkCharge > this.chargePerCraft) {
//            final SlimefunItem item = SlimefunItem.getByItem(blueprint);
//
//            if (!isValidBlueprint(item)) {
//                sendDebugMessage(blockMenu.getLocation(), ()->"Invalid blueprint");
//                return;
//            }
//
//            BlueprintInstance instance = INSTANCE_MAP.get(blockMenu.getLocation());
//
//            if (instance == null) {
//                final ItemMeta blueprintMeta = blueprint.getItemMeta();
//                Optional<BlueprintInstance> optional;
//                optional = DataTypeMethods.getOptionalCustom(blueprintMeta, Keys.BLUEPRINT_INSTANCE, PersistentCraftingBlueprintType.TYPE);
//
//                if (optional.isEmpty()) {
//                    optional = DataTypeMethods.getOptionalCustom(blueprintMeta, Keys.BLUEPRINT_INSTANCE2, PersistentCraftingBlueprintType.TYPE);
//                }
//
//                if (optional.isEmpty()) {
//                    optional = DataTypeMethods.getOptionalCustom(blueprintMeta, Keys.BLUEPRINT_INSTANCE3, PersistentCraftingBlueprintType.TYPE);
//                }
//
//                if (optional.isEmpty()) {
//                    sendDebugMessage(blockMenu.getLocation(), ()->"No blueprint instance found");
//                    return;
//                }
//
//                instance = optional.get();
//                setCache(blockMenu, instance);
//            }
//
//            final ItemStack output = blockMenu.getItemInSlot(OUTPUT_SLOT);
//            int blueprintAmount = blueprint.getAmount();
//
//            if (output != null
//                    && output.getType() != Material.AIR
//                    && (output.getAmount() + instance.getItemStack().getAmount() * blueprintAmount > output.getMaxStackSize() || !StackUtils.itemsMatch(instance, output))
//            ) {
//                sendDebugMessage(blockMenu.getLocation(), ()->"Output slot is full");
//                return;
//            }
//
//            if (tryCraft(blockMenu, instance, root, blueprintAmount)) {
//                root.removeRootPower(this.chargePerCraft);
//            }
//        }
//    }
//    public DataContainer newDataContainer(){
//        return new DataContainer() {
//            int craftAmount=0;
//            Object blueprintInstance;
//            ItemStack lastRecipeOut;
//            ItemRequest[] lastRecipeStacked;
//            public int getInt(int va){
//                return craftAmount;
//            }
//            public void setInt(int val){
//
//            }
//            public Object getObject(int lva){
//                return blueprintInstance;
//            }
//            public void setObject(int lva, Object object){
//                this.blueprintInstance = object;
//            }
//            public ItemStack getItemStack(int lva){
//                return lastRecipeOut;
//            }
//            public void setItemStack(int lva, ItemStack item){
//                lastRecipeOut = item;
//            }
//            public ItemRequest[] getItemRequests(int v){
//                return lastRecipeStacked;
//            }
//            public void setItemRequests(int v, ItemRequest... items){
//                lastRecipeStacked = items;
//            }
//        };
//    }
//    public int getDataSlot(){
//        return 0;
//    }
//    protected void updateMenu(BlockMenu blockMenu){
//        DataContainer dataContainer=getDataContainer(blockMenu);
//        //reset blueprint cache
//        dataContainer.setObject(0,null);
//        dataContainer.setItemStack(0,null);
//        dataContainer.setItemRequests(0,null);
//        //try find new blueprint cache
//        final ItemStack blueprint = blockMenu.getItemInSlot(BLUEPRINT_SLOT);
//
//        if (blueprint == null || blueprint.getType() == Material.AIR) {
//            sendDebugMessage(blockMenu.getLocation(), ()->"No blueprint found");
//            return;
//        }
//        final SlimefunItem item = SlimefunItem.getByItem(blueprint);
//
//        if (!isValidBlueprint(item)) {
//            sendDebugMessage(blockMenu.getLocation(), ()->"Invalid blueprint");
//            return;
//        }
//
//        //fetch blutprint instance
//        final ItemMeta blueprintMeta = blueprint.getItemMeta();
//        Optional<BlueprintInstance> optional;
//        optional = DataTypeMethods.getOptionalCustom(blueprintMeta, Keys.BLUEPRINT_INSTANCE, PersistentCraftingBlueprintType.TYPE);
//        if (optional.isEmpty()) {
//            optional = DataTypeMethods.getOptionalCustom(blueprintMeta, Keys.BLUEPRINT_INSTANCE2, PersistentCraftingBlueprintType.TYPE);
//        }
//
//        if (optional.isEmpty()) {
//            optional = DataTypeMethods.getOptionalCustom(blueprintMeta, Keys.BLUEPRINT_INSTANCE3, PersistentCraftingBlueprintType.TYPE);
//        }
//
//        if (optional.isEmpty()) {
//            sendDebugMessage(blockMenu.getLocation(), ()->"No blueprint instance found");
//            return;
//        }
//
//        BlueprintInstance instance = optional.get();
//        dataContainer.setObject(0,instance);
//        ItemStack[] inputs=instance.getRecipeItems();
//        //recipe should be fixed
//        for (Map.Entry<ItemStack[], ItemStack> entry : getRecipeEntries()) {
//            if (getRecipeTester(inputs, entry.getKey())) {
//                dataContainer.setItemStack(0,new ItemStack(entry.getValue()));
//                break;
//            }
//        }
//        dataContainer.setItemStack(0,null);
//    }
//    private boolean tryCraft(@Nonnull BlockMenu blockMenu, @Nonnull BlueprintInstance instance, @Nonnull NetworkRoot root,int craftAmount) {
//        // Get the recipe input
//        //ExperimentalFeatureManager.getInstance().startGlobalProfiler();
//        DataContainer container=getDataContainer(blockMenu);
//        /* Make sure the network has the required items
//         * Needs to be revisited as matching is happening stacks 2x when I should
//         * only need the one
//         */
//        ItemRequest[] request=container.getItemRequests(0);
//        if(request==null){
//            sendDebugMessage(blockMenu.getLocation(), ()->"cached request is null");
//            HashMap<ItemStack, Integer> requiredItems = new HashMap<>();
//            for (int i = 0; i < 9; i++) {
//                final ItemStack requested = instance.getRecipeItems()[i];
//                if (requested != null) {
//                    requiredItems.merge(requested, requested.getAmount(), Integer::sum);
//                }
//            }
//            request=requiredItems.entrySet().stream().map(e->ItemRequest.of(e.getKey(),e.getValue())).toArray(ItemRequest[]::new);
//            container.setItemRequests(0,request);
//        }
//        //ExperimentalFeatureManager.getInstance().endGlobalProfiler(()->"fetch request ,%s");
//        //ExperimentalFeatureManager.getInstance().startGlobalProfiler();
//        //this is shit
//        for (ItemRequest itemRequest : request) {
//            if (!root.contains(itemRequest)) {
//                final ItemRequest[] requests=request;
//                sendDebugMessage(blockMenu.getLocation(), ()->"Network does not have required items,%s".formatted(Arrays.toString(requests)));
//                return false;
//            }
//        }
//        //ExperimentalFeatureManager.getInstance().endGlobalProfiler(()->"contains ,%s");
//        //ExperimentalFeatureManager.getInstance().startGlobalProfiler();
//        final ItemStack[] fetch = new ItemStack[request.length];
//        // Then fetch the actual items
//        boolean amountMatched=true;
//        for (int i=0;i<request.length;i++) {
//            final ItemRequest requested = request[i];
//            if (requested.getItemStack() != null) {
//                final ItemStack fetched = root.getItemStack(requested.clone());
//                if(fetched!=null&&fetched.getAmount()==requested.getAmount()){
//                    fetch[i] = fetched;
//                }else {
//                    amountMatched=false;
//                    break;
//                }
//            } else {
//                fetch[i] = null;
//            }
//        }
//        if(!amountMatched){
//            final ItemRequest[] requestFinal=request;
//            sendDebugMessage(blockMenu.getLocation(),
//                    ()->"Network does not fetch required items",
//                    ()->"expected-requests: " + Arrays.toString(requestFinal),
//                    ()->"actually-fetched: " + Arrays.toString(fetch)
//            );
//            returnItems(root,fetch);
//            return false;
//        }
//        //ExperimentalFeatureManager.getInstance().endGlobalProfiler(()->"getItemStack ,%s");
//        //ExperimentalFeatureManager.getInstance().startGlobalProfiler();
//        //item fetched ,now we assumed that they can be perfectly filled into the inputPattern of the BLUEPRINT_INSTANCE
//        //so we can use the inputPattern result cached
//
//        // Go through each slimefun recipe, test and set the ItemStack if found
//        //they are done in updateMenu and cached in container ,if crafted = null, try again
//        ItemStack[] inputPattern=instance.getRecipeItems();
//        ItemStack crafted =container.getItemStack(0);
//        if(crafted==null){
//            sendDebugMessage(blockMenu.getLocation(), ()->"Network does not fetch required items");
//            for (Map.Entry<ItemStack[], ItemStack> entry : getRecipeEntries()) {
//                if (getRecipeTester(inputPattern, entry.getKey())) {
//                    //get A copy of matched recipeOutput
//                    crafted=new ItemStack( entry.getValue());
//                    container.setItemStack(0,crafted);
//                    break;
//                }
//            }
//        }
//        //else{
//        //get A copy of cached itemOutput
//        //sendDebugMessage(blockMenu.getLocation(), ()->"get Copy of cached crafted result");
//
//        //}
//        //this is cached in BluePrint Instance
//        if (crafted == null && canTestVanillaRecipe()) {
//            //sendDebugMessage(blockMenu.getLocation(), ()->"No slimefun recipe found, trying vanilla");
//            // If no slimefun recipe found, try a vanilla one
//            instance.generateVanillaRecipe(blockMenu.getLocation().getWorld());
//            if (instance.getRecipe() == null) {
//                returnItems(root, fetch);
//                sendDebugMessage(blockMenu.getLocation(), ()->"No vanilla recipe found");
//                return false;
//            } else  {
//                //setCache(blockMenu, instance);
//                crafted = instance.getRecipe().getResult();
//            }
//        }
//
//        // If no item crafted OR result doesn't fit, escape
//        if (crafted == null || crafted.getType() == Material.AIR) {
//            sendDebugMessage(blockMenu.getLocation(),
//                    ()->"No valid recipe found",
//                    ()->"expected-inputs: " + Arrays.toString(inputPattern),
//                    ()->"actually-fetched: " + Arrays.toString(fetch)
//            );
//            returnItems(root, fetch);
//            return false;
//        }
//        //ExperimentalFeatureManager.getInstance().endGlobalProfiler(()->"matchRecipe ,%s");
//        //ExperimentalFeatureManager.getInstance().startGlobalProfiler();
//        // Push item
//        //in this place ,we don't modify crafted,so nothing will change in cached crafted
//        ItemStack outputSlot=blockMenu.getItemInSlot(OUTPUT_SLOT);
//        if(outputSlot==null||outputSlot.getType()==Material.AIR){
//            blockMenu.replaceExistingItem(OUTPUT_SLOT,crafted,false);
//        }else if(outputSlot.getAmount()<outputSlot.getMaxStackSize()&&StackUtils.itemsMatch(outputSlot,crafted)){
//            outputSlot.setAmount(outputSlot.getAmount()+crafted.getAmount());
//        }else{
//            sendDebugMessage(blockMenu.getLocation(), ()->"Output slot item no space or hot fit");
//            returnItems(root, fetch);
//        }
//        if (root.isDisplayParticles()) {
//            final Location location = blockMenu.getLocation().clone().add(0.5, 1.1, 0.5);
//            location.getWorld().spawnParticle(Particle.WAX_OFF, location, 0, 0, 4, 0);
//        }
//        //blockMenu.pushItem(crafted, OUTPUT_SLOT);
//        return true;
//    }
////    private boolean tryCraft(@Nonnull BlockMenu blockMenu, @Nonnull BlueprintInstance instance, @Nonnull NetworkRoot root, @Nonnull int blueprintAmount) {
////        // Get the recipe input
////        final ItemStack[] inputs = new ItemStack[9];
////        final ItemStack[] acutalInputs = new ItemStack[9];
////
////        /* Make sure the network has the required items
////         * Needs to be revisited as matching is happening stacks 2x when I should
////         * only need the one
////         */
////
////        HashMap<ItemStack, Integer> requiredItems = new HashMap<>();
////        for (int i = 0; i < 9; i++) {
////            final ItemStack requested = instance.getRecipeItems()[i];
////            if (requested != null) {
////                requiredItems.merge(requested, requested.getAmount() * blueprintAmount, Integer::sum);
////            }
////        }
////
////        for (Map.Entry<ItemStack, Integer> entry : requiredItems.entrySet()) {
////            if (!root.contains(new ItemRequest(entry.getKey(), entry.getValue()))) {
////                sendDebugMessage(blockMenu.getLocation(), ()->"Not enough items in network");
////                return false;
////            }
////        }
////
////        // Then fetch the actual items
////        for (int i = 0; i < 9; i++) {
////            final ItemStack requested = instance.getRecipeItems()[i];
////            if (requested != null) {
////                final ItemStack fetched = root.getItemStack(new ItemRequest(requested, requested.getAmount() * blueprintAmount));
////                if (fetched != null) {
////                    acutalInputs[i] = fetched;
////                    ItemStack fetchedClone = fetched.clone();
////                    fetchedClone.setAmount((int) (fetched.getAmount() / blueprintAmount));
////                    inputs[i] = fetchedClone;
////                    if (fetchedClone.getAmount() != requested.getAmount()) {
////                        returnItems(root, acutalInputs);
////                    }
////                } else {
////                    acutalInputs[i] = null;
////                    inputs[i] = null;
////                }
////            } else {
////                inputs[i] = null;
////            }
////        }
////
////        ItemStack crafted = null;
////
////        // Go through each slimefun recipe, test and set the ItemStack if found
////        for (Map.Entry<ItemStack[], ItemStack> entry : getRecipeEntries()) {
////            if (getRecipeTester(inputs, entry.getKey())) {
////                crafted = entry.getValue().clone();
////                break;
////            }
////        }
////
////        if (crafted == null && canTestVanillaRecipe()) {
////            // If no slimefun recipe found, try a vanilla one
////            instance.generateVanillaRecipe(blockMenu.getLocation().getWorld());
////            if (instance.getRecipe() == null) {
////                returnItems(root, inputs);
////                sendDebugMessage(blockMenu.getLocation(), ()->"No vanilla recipe found");
////                return false;
////            } else if (Arrays.equals(instance.getRecipeItems(), inputs)) {
////                setCache(blockMenu, instance);
////                crafted = instance.getRecipe().getResult();
////            }
////        }
////
////        // If no item crafted OR result doesn't fit, escape
////        if (crafted == null || crafted.getType() == Material.AIR) {
////            sendDebugMessage(blockMenu.getLocation(), ()->"No valid recipe found");
////            sendDebugMessage(blockMenu.getLocation(), ()->"inputs: " + Arrays.toString(inputs));
////            returnItems(root, acutalInputs);
////            return false;
////        }
////
////        // Push item
////        final Location location = blockMenu.getLocation().clone().add(0.5, 1.1, 0.5);
////        if (root.isDisplayParticles()) {
////            location.getWorld().spawnParticle(Particle.WAX_OFF, location, 0, 0, 4, 0);
////        }
////
////        crafted.setAmount(crafted.getAmount() * blueprintAmount);
////
////        if (crafted.getAmount() > crafted.getMaxStackSize()) {
////            returnItems(root, acutalInputs);
////            sendDebugMessage(blockMenu.getLocation(), ()->"Result is too large");
////            return false;
////        }
////
////        blockMenu.pushItem(crafted, OUTPUT_SLOT);
////        return true;
////    }
//
//    private void returnItems(@Nonnull NetworkRoot root, @Nonnull ItemStack[] inputs) {
//        for (ItemStack input : inputs) {
//            if (input != null) {
//                root.addItemStack(input);
//            }
//        }
//    }
//
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
//
//
//    @Override
//    public void postRegister() {
//        new BlockMenuPreset(this.getId(), this.getItemName()) {
//
//            @Override
//            public void init() {
//                drawBackground(BACKGROUND_SLOTS);
//                drawBackground(Icon.BLUEPRINT_BACKGROUND_STACK, BLUEPRINT_BACKGROUND);
//                drawBackground(Icon.OUTPUT_BACKGROUND_STACK, OUTPUT_BACKGROUND);
//            }
//
//            @Override
//            public boolean canOpen(@Nonnull Block block, @Nonnull Player player) {
//                return player.hasPermission("slimefun.inventory.bypass") || (this.getSlimefunItem().canUse(player, false)
//                        && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK));
//            }
//
//            @Override
//            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
//                if (AbstractAdvancedAutoCrafter.this.withholding && flow == ItemTransportFlow.WITHDRAW) {
//                    return new int[]{OUTPUT_SLOT};
//                }
//                return new int[0];
//            }
//        };
//    }
//
//    public abstract boolean isValidBlueprint(SlimefunItem item);
//
//    public abstract Set<Map.Entry<ItemStack[], ItemStack>> getRecipeEntries();
//
//    public abstract boolean getRecipeTester(ItemStack[] inputs, ItemStack[] recipe);
//
//    public boolean canTestVanillaRecipe() {
//        return false;
//    }
}
