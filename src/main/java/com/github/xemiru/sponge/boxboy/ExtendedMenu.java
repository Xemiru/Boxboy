/*
 * MIT License
 *
 * Copyright (c) 2018 Tellerva, Marc Lawrence
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
        Task.builder()
            .execute(() -> Sponge.getServer().getOnlinePlayers().forEach(this::updatePlayer))
            .submit(this.getInventory().getPlugin().getInstance().get());
    }

    /**
     * Updates a {@link Player}'s inventory with the contents of this {@link ExtendedMenu}'s bottom inventory.
     *
     * <p>This method is a no-op if the target player does not actually have this ExtendedMenu open.</p>
     *
     * @param p the Player to update the inventory of
     */
    void updatePlayer(Player p) {
        if (this.isViewingMenu(p)) Menu.updateInventory(this.buttons.length - 36, this.buttons, p.getInventory());
    }

}
