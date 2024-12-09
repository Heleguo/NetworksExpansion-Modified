package io.github.sefiraat.networks.slimefun.network;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.NetworkSlimefunItems;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.settings.IntRangeSetting;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("deprecation")
public class NetworkImport extends NetworkObject {

    private static final int[] INPUT_SLOTS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};

    private final ItemSetting<Integer> tickRate;

    public NetworkImport(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe, NodeType.IMPORT);

        this.tickRate = new IntRangeSetting(this, "tick_rate", 1, 1, 10);
        addItemSetting(this.tickRate);

        for (int inputSlot : INPUT_SLOTS) {
            this.getSlotsToDrop().add(inputSlot);
        }

        addItemHandler(
                new BlockTicker() {

                    private int tick = 1;

                    @Override
                    public boolean isSynchronized() {
                        return false;
                    }

                    @Override
                    public void tick(Block block, SlimefunItem item, SlimefunBlockData data) {
                        if (tick <= 1) {
                            final BlockMenu blockMenu = data.getBlockMenu();
                            addToRegistry(block);
                            tryAddItem(blockMenu);
                        }
                    }

                    @Override
                    public void uniqueTick() {
                        tick = tick <= 1 ? tickRate.getValue() : tick - 1;
                    }
                }
        );
    }

    private void tryAddItem(@Nonnull BlockMenu blockMenu) {
        final NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());

        if (definition.getNode() == null) {
            return;
        }
        List<CompletableFuture<?>> threads=new ArrayList<>();
        final NetworkRoot root=definition.getNode().getRoot();
        for (int i=0;i<INPUT_SLOTS.length;i++) {
            int inputSlot = INPUT_SLOTS[i];
            final ItemStack itemStack = blockMenu.getItemInSlot(inputSlot);
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            threads.add( CompletableFuture.runAsync(() -> {
                root.addItemStack(itemStack);
            }));
        }
        if(!threads.isEmpty()){
            CompletableFuture.allOf(threads.toArray(CompletableFuture[]::new)).join();
        }
    }

    @Override
    public void postRegister() {
        new BlockMenuPreset(this.getId(), this.getItemName()) {

            @Override
            public void init() {
                setSize(9);
            }

            @Override
            public boolean canOpen(@Nonnull Block block, @Nonnull Player player) {
                return player.hasPermission("slimefun.inventory.bypass") || (NetworkSlimefunItems.NETWORK_IMPORT.canUse(player, false)
                        && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK));
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                if (flow == ItemTransportFlow.INSERT) {
                    return INPUT_SLOTS;
                }
                return new int[0];
            }
        };
    }
}
