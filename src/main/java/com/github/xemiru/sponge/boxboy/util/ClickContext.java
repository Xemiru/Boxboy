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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Objects;
import java.util.Optional;

/**
 * Provides information about an attempt to press a {@link Button} in a {@link Menu}.
 */
public class ClickContext {

    private ClickType type;
    private Menu sourceMenu;
    private Player clicker;
    private ItemStack cursor;
    private ItemStack newCursor;

    public ClickContext(ClickType type, Menu sourceMenu, Player clicker, ItemStack cursor) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(sourceMenu);
        Objects.requireNonNull(clicker);

        this.type = type;
        this.cursor = cursor;
        this.sourceMenu = sourceMenu;
        this.clicker = clicker;
        this.newCursor = null;
    }

    /**
     * @return the {@link ClickType} associated with the action
     */
    public ClickType getType() {
        return this.type;
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
     * @return the {@link ItemStack} that was on the cursor when the action was performed
     */
    public Optional<ItemStack> getCursor() {
        return Optional.ofNullable(this.cursor);
    }

    /**
     * @return the {@link ItemStack} to be set on the cursor
     */
    public Optional<ItemStack> getNewCursor() {
        return Optional.ofNullable(this.newCursor);
    }

    /**
     * Sets the {@link ItemStack} to be set on the cursor.
     *
     * <p>Setting this to null will not clear the item on the cursor. An ItemStack containing the AIR item type should
     * instead be provided for a similar effect.</p>
     *
     * @param item the item to set
     */
    public void setNewCursor(ItemStack item) {
        this.newCursor = item;
    }

}
