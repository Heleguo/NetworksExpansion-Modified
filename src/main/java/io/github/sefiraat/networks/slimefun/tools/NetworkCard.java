package io.github.sefiraat.networks.slimefun.tools;

import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.stackcaches.CardInstance;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.datatypes.DataTypeMethods;
import io.github.sefiraat.networks.utils.datatypes.PersistentCardInstanceType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.DistinctiveItem;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;

@Deprecated
public class NetworkCard extends SlimefunItem implements DistinctiveItem {

    private static final int[] SIZES = new int[]{
            4096,
            32768,
            262144,
            2097152,
            16777216,
            134217728,
            1073741824,
            Integer.MAX_VALUE
    };

    private static final String WIKI_PAGE = "Network-Memory-Card";

    private final int size;

    public NetworkCard(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, int size) {
        super(itemGroup, item, recipeType, recipe);
        this.size = size;
        addItemHandler((ItemUseHandler) e -> {
            final Player player = e.getPlayer();
            final ItemStack card = player.getInventory().getItemInMainHand();
            final ItemStack stackToSet = player.getInventory().getItemInOffHand().clone();

            e.cancel();
            if (card.getAmount() > 1) {
                player.sendMessage(Networks.getLocalizationService().getString("messages.unsupported-operation.memory_card.invalid_amount"));
                return;
            }

            if (isBlacklisted(stackToSet)) {
                player.sendMessage(Networks.getLocalizationService().getString("messages.unsupported-operation.memory_card.blacklisted_item"));
                return;
            }

            final SlimefunItem cardItem = SlimefunItem.getByItem(card);
            if (cardItem instanceof NetworkCard networkCard) {
                final ItemMeta cardMeta = card.getItemMeta();
                CardInstance cardInstance = DataTypeMethods.getCustom(
                        cardMeta,
                        Keys.CARD_INSTANCE,
                        PersistentCardInstanceType.TYPE
                );

                if (cardInstance == null) {
                    cardInstance = DataTypeMethods.getCustom(
                            cardMeta,
                            Keys.CARD_INSTANCE2,
                            PersistentCardInstanceType.TYPE
                    );
                }

                if (cardInstance == null) {
                    cardInstance = DataTypeMethods.getCustom(
                            cardMeta,
                            Keys.CARD_INSTANCE3,
                            PersistentCardInstanceType.TYPE,
                            new CardInstance(null, 0, networkCard.getSize())
                    );
                }

                if (cardInstance.getAmount() > 0) {
                    e.getPlayer().sendMessage(Networks.getLocalizationService().getString("messages.unsupported-operation.memory_card.not_empty"));
                    return;
                }

                cardInstance.setItemStack(stackToSet);
                DataTypeMethods.setCustom(cardMeta, Keys.CARD_INSTANCE, PersistentCardInstanceType.TYPE, cardInstance);
                cardInstance.updateLore(cardMeta);
                card.setItemMeta(cardMeta);
            }
        });
    }

    public static int[] getSizes() {
        return SIZES;
    }

    private boolean isBlacklisted(@Nonnull ItemStack itemStack) {
        return itemStack.getType() == Material.AIR
                || itemStack.getType().getMaxDurability() < 0
                || Tag.SHULKER_BOXES.isTagged(itemStack.getType())
                || itemStack.getType() == Material.BUNDLE;
    }

    public int getSize() {
        return this.size;
    }

    @Override
    public void postRegister() {
        addWikiPage(WIKI_PAGE);
    }

    @Override
    public boolean canStack(@Nonnull ItemMeta sfItemMeta, @Nonnull ItemMeta itemMeta) {
        return sfItemMeta.getPersistentDataContainer().equals(itemMeta.getPersistentDataContainer());
    }
}
