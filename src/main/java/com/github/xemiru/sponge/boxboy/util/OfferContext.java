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
package com.github.xemiru.sponge.boxboy.util;

import com.github.xemiru.sponge.boxboy.Menu;
import com.github.xemiru.sponge.boxboy.button.Button;
import com.google.common.base.Preconditions;
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
        Preconditions.checkNotNull(sourceMenu);
        Preconditions.checkNotNull(clicker);

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
