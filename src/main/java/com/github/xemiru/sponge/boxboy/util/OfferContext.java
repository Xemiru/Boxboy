package com.github.xemiru.sponge.boxboy.util;

import com.github.xemiru.sponge.boxboy.Menu;
import com.github.xemiru.sponge.boxboy.button.Button;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;

/**
 * Provides information about an attempt to offer a new item to a {@link Button} in a {@link Menu}.
 */
public class OfferContext {

    private Menu sourceMenu;
    private Player clicker;
    private ItemStack item;

    public OfferContext(Menu sourceMenu, Player clicker, ItemStack item) {
        this.sourceMenu = sourceMenu;
        this.clicker = clicker;
        this.item = item;
    }

    /**
     * @return the {@link Menu} associated with the action
     */
    public Menu getSourceMenu() {
        return this.sourceMenu;
    }

    /**
     * @return the {@link Player} associated with the action
     */
    public Player getClicker() {
        return this.clicker;
    }

    /**
     * @return the {@link ItemStack} that was offered to the slot when the action was performed
     */
    public Optional<ItemStack> getItem() {
        return Optional.ofNullable(this.item);
    }

}
