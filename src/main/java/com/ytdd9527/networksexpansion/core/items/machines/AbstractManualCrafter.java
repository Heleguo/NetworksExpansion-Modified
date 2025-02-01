package com.ytdd9527.networksexpansion.core.items.machines;

import com.balugaq.netex.api.data.SuperRecipe;
import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.network.stackcaches.ItemStackCache;
import io.github.sefiraat.networks.slimefun.network.AdminDebuggable;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "deprecation"})
public abstract class AbstractManualCrafter extends SpecialSlimefunItem implements AdminDebuggable, EnergyNetComponent {
    public AbstractManualCrafter(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item, @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void preRegister() {
        BlockPlaceHandler blockPlaceHandler = getMachineBlockPlaceHandler();
        if (blockPlaceHandler != null) {
            addItemHandler(blockPlaceHandler);
        }

        BlockBreakHandler blockBreakHandler = getMachineBlockBreakHandler();
        if (blockBreakHandler != null) {
            addItemHandler(blockBreakHandler);
        }

        BlockTicker blockTicker = getMachineBlockTicker();
        if (blockTicker != null) {
            addItemHandler(blockTicker);
        }
    }

    @Override
    public void postRegister() {
        new BlockMenuPreset(this.getId(), this.getItemName()) {
            @Override
            public void init() {
                for (Map.Entry<Integer, ItemStack> entry : getBackgrounds().entrySet()) {
                    addItem(entry.getKey(), entry.getValue(), ChestMenuUtils.getEmptyClickHandler());
                }
            }

            @Override
            public boolean canOpen(@Nonnull Block block, @Nonnull Player player) {
                return player.hasPermission("slimefun.inventory.bypass") || (this.getSlimefunItem().canUse(player, false)
                        && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK));
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                if (isTransportable()) {
                    if (flow == ItemTransportFlow.WITHDRAW) {
                        return getOutputSlots();
                    }
                    return getInputSlots();
                }
                return new int[0];
            }

            @Override
            public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block b) {
                menu.addMenuClickHandler(getHandleSlot(), (p, slot, item, action) -> {
                    craft(p, menu);
                    return false;
                });
                Map<Integer, ChestMenu.MenuClickHandler> menuClickHandlers = getMenuClickHandlers();
                if (menuClickHandlers != null) {
                    for (Map.Entry<Integer, ChestMenu.MenuClickHandler> entry : menuClickHandlers.entrySet()) {
                        menu.addMenuClickHandler(entry.getKey(), entry.getValue());
                    }
                }
            }
        };
    }

    public void craft(Player player, BlockMenu blockMenu) {
        boolean success = false;
        for (SuperRecipe recipe : getRecipes()) {
            if (getCapacity() < recipe.getConsumeEnergy()) {
                continue;
            }

            if (getCharge(blockMenu.getLocation()) >= recipe.getConsumeEnergy()) {
                if (recipe.getHandler() != null) {
                    success = recipe.getHandler().handle(player, blockMenu);
                } else {
                    if (recipe.isShaped()) {
                        success = shapedCraft(recipe, player, blockMenu);
                    } else {
                        success = shapelessCraft(recipe, player, blockMenu);
                    }
                }
            }
            if (success) {
                break;
            }
        }
    }

    public boolean shapedCraft(SuperRecipe recipe, Player player, BlockMenu blockMenu) {
        World world = blockMenu.getLocation().getWorld();
        if (world == null) {
            return false;
        }

        for (int i = 0; i < Math.min(recipe.getInput().length, getInputSlots().length); i++) {
            ItemStack wanted = recipe.getInput()[i];
            if (wanted == null || wanted.getType() == Material.AIR) {
                continue;
            }

            ItemStack itemInSlot = blockMenu.getItemInSlot(getInputSlots()[i]);
            if (itemInSlot == null || itemInSlot.getType() == Material.AIR) {
                return false;
            }

            if (!StackUtils.itemsMatch(itemInSlot, wanted, false, true)) {
                return false;
            }
        }

        if (!BlockMenuUtil.fits(blockMenu, recipe.getOutput(), getOutputSlots())) {
            sendFeedback(blockMenu.getLocation(), FeedbackType.NO_ENOUGH_SPACE);
            return false;
        }

        for (int i = 0; i < Math.min(recipe.getInput().length, getInputSlots().length); i++) {
            ItemStack wanted = recipe.getInput()[i];
            if (wanted == null || wanted.getType() == Material.AIR) {
                continue;
            }

            blockMenu.consumeItem(getInputSlots()[i], recipe.getInput()[i].getAmount());
        }

        for (ItemStack item : recipe.getOutput()) {
            if (item != null && item.getType() != Material.AIR) {
                SlimefunItem sfi = SlimefunItem.getByItem(item);
                if (sfi != null) {
                    if (sfi.isDisabled() || sfi.isDisabledIn(world)) {
                        player.sendMessage(ChatColor.RED + "This item is disabled in this world.");
                        sendFeedback(blockMenu.getLocation(), FeedbackType.DISABLED_OUTPUT);
                        continue;
                    }
                }
                ItemStack left = BlockMenuUtil.pushItem(blockMenu, ItemStackCache.of( ItemStackUtil.getCleanItem(item)),true, getOutputSlots());
                if (left != null&&left.getAmount()>0 && left.getType() != Material.AIR) {
                    player.sendMessage(ChatColor.RED + "No enough space in output slots.");
                    sendFeedback(blockMenu.getLocation(), FeedbackType.NO_ENOUGH_SPACE);
                    world.dropItem(blockMenu.getLocation(), left);
                }
            }
        }

        if (recipe.getConsumeEnergy() > 0) {
            removeCharge(blockMenu.getLocation(), recipe.getConsumeEnergy());
        }
        player.sendMessage(ChatColor.GREEN + "Successfully crafted.");
        sendFeedback(blockMenu.getLocation(), FeedbackType.SUCCESS);
        return true;
    }
    @Deprecated
    public boolean shapelessCraft(SuperRecipe recipe, Player player, BlockMenu blockMenu) {
        World world = blockMenu.getLocation().getWorld();
        if (world == null) {
            return false;
        }
        // ItemStack : ItemStack.getAmount()
        Map<ItemStack, Integer> wanted = new HashMap<>();
        for (int i = 0; i < Math.min(recipe.getInput().length, getInputSlots().length); i++) {
            ItemStack wantedItem = recipe.getInput()[i];
            if (wantedItem == null || wantedItem.getType() == Material.AIR) {
                continue;
            }

            if (wanted.containsKey(wantedItem)) {
                wanted.put(wantedItem, wanted.get(wantedItem) + wantedItem.getAmount());
            } else {
                wanted.put(wantedItem, wantedItem.getAmount());
            }
        }

        // ItemStack : ItemStack.getAmount()
        Map<ItemStack, Integer> have = new HashMap<>();
        for (int slot : getInputSlots()) {
            ItemStack itemInSlot = blockMenu.getItemInSlot(slot);
            if (itemInSlot == null || itemInSlot.getType() == Material.AIR) {
                continue;
            }

            if (have.containsKey(itemInSlot)) {
                have.put(itemInSlot, have.get(itemInSlot) + itemInSlot.getAmount());
            } else {
                have.put(itemInSlot, itemInSlot.getAmount());
            }
        }

        for (Map.Entry<ItemStack, Integer> entry : wanted.entrySet()) {
            if (!have.containsKey(entry.getKey()) || have.get(entry.getKey()) < entry.getValue()) {
                break;
            }
        }

        if (!BlockMenuUtil.fits(blockMenu, recipe.getOutput(), getOutputSlots())) {
            sendFeedback(blockMenu.getLocation(), FeedbackType.NO_ENOUGH_SPACE);
            return false;
        }

        // Consume items
        for (Map.Entry<ItemStack, Integer> entry : wanted.entrySet()) {
            for (int slot : getInputSlots()) {
                ItemStack itemInSlot = blockMenu.getItemInSlot(slot);
                if (itemInSlot == null || itemInSlot.getType() == Material.AIR) {
                    continue;
                }

                if (StackUtils.itemsMatch(itemInSlot, entry.getKey())) {
                    int amount = Math.min(entry.getValue(), itemInSlot.getAmount());
                    blockMenu.consumeItem(slot, amount);

                    wanted.put(entry.getKey(), wanted.get(entry.getKey()) - amount);
                }

                if (wanted.get(entry.getKey()) <= 0) {
                    break;
                }
            }
        }

        if (!wanted.isEmpty()) {
            return false;
        }

        for (ItemStack item : recipe.getOutput()) {
            if (item != null && item.getType() != Material.AIR) {
                SlimefunItem sfi = SlimefunItem.getByItem(item);
                if (sfi != null) {
                    if (sfi.isDisabled() || sfi.isDisabledIn(world)) {
                        player.sendMessage(ChatColor.RED + "This item is disabled in this world.");
                        sendFeedback(blockMenu.getLocation(), FeedbackType.DISABLED_OUTPUT);
                        continue;
                    }
                }
                ItemStack left = BlockMenuUtil.pushItem(blockMenu, ItemStackCache.of(ItemStackUtil.getCleanItem(item)),true, getOutputSlots());
                if (left != null&&left.getAmount()>0 && left.getType() != Material.AIR) {
                    player.sendMessage(ChatColor.RED + "Not enough space in output slots.");
                    world.dropItem(blockMenu.getLocation(), left);
                }
            }
        }

        if (recipe.getConsumeEnergy() > 0) {
            removeCharge(blockMenu.getLocation(), recipe.getConsumeEnergy());
        }
        player.sendMessage(ChatColor.GREEN + "Successfully crafted.");
        return true;
    }

    @Override
    @Nonnull
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    public abstract List<SuperRecipe> getRecipes();

    public abstract int[] getInputSlots();

    public abstract int[] getOutputSlots();

    public abstract int getHandleSlot();

    public abstract boolean isTransportable();

    public abstract Map<Integer, ItemStack> getBackgrounds();

    public abstract Map<Integer, ChestMenu.MenuClickHandler> getMenuClickHandlers();

    public abstract BlockPlaceHandler getMachineBlockPlaceHandler();

    public abstract BlockBreakHandler getMachineBlockBreakHandler();

    public abstract BlockTicker getMachineBlockTicker();
}
