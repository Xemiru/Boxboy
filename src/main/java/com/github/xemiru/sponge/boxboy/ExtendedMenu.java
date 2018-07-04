package com.github.xemiru.sponge.boxboy;

import com.github.xemiru.sponge.boxboy.button.Button;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.scheduler.Task;

/**
 * An extension of {@link Menu}s, allowing use of the bottom inventory (typically the viewing {@link Player}'s
 * inventory) as part of the menu.
 *
 * <p>The last 36 slots of this type of {@link Menu} always pertain to the 36 slots of the Player inventory.</p>
 */
public class ExtendedMenu extends Menu {

    @Override
    void initialize(Inventory inv) {
        super.initialize(inv);
        this.buttons = new Button[inv.capacity() + 36]; // 36 slots of the player inventory
    }

    @Override
    public void updateInventory() {
        super.updateInventory();
        Task.builder().execute(() -> Sponge.getServer().getOnlinePlayers().forEach(this::updatePlayer));
    }

    /**
     * Updates a {@link Player}'s inventory with the contents of this {@link ExtendedMenu}'s bottom inventory.
     *
     * <p>This method is a no-op if the target player does not actually have this ExtendedMenu open.</p>
     *
     * @param p the Player to update the inventory of
     */
    void updatePlayer(Player p) {
        if (this.hasMenuOpen(p)) Menu.updateInventory(this.buttons.length - 36, this.buttons, p.getInventory());
    }

}
