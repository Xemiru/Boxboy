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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * A collection of {@link Button}s presented as a user interface through the use of {@link ItemStack}s within a given
 * {@link Inventory}.
 */
public class Menu {

    static Set<Menu> menus;
    static Map<UUID, Menu> viewerMap;

    static {
        // Use a weak set to automagically drop unused menu instances.
        // Inventories have a reference to the menus that own them through properties, thus menus should never be
        // disposed as long as their inventory is still in use in some form (e.g. being viewed by a player).

        Menu.menus = Collections.newSetFromMap(new WeakHashMap<>());
        Menu.viewerMap = new HashMap<>();
    }

    static void updateInventory(int index, Button[] array, Inventory inv) {
        int i = index;
        int slotIndex = 0;
        for (Inventory slot : inv.slots()) {
            // ignore offhand slots for player inventories
            if (inv instanceof PlayerInventory && slotIndex == 36) continue;

            Button btn = array[i];
            if (btn == null) {
                if (slot.peek().filter(item -> item.getType() != ItemTypes.AIR).isPresent()) slot.clear();
            } else if (!slot.contains(btn.getRepresentative())) {
                slot.clear();
                slot.set(btn.getRepresentative());
            }

            slotIndex++;
            i++;
        }
    }

    private Set<UUID> viewers;
    private boolean invalidated;
    private Inventory inventory;
    Button[] buttons;

    Menu() {
        Menu.menus.add(this);
    }

    // region Internal methods

    /**
     * Internal method.
     *
     * <p>Initializes this {@link Menu} with the provided {@link Inventory}.</p>
     *
     * @param inv the Inventory to use
     */
    void initialize(Inventory inv) {
        this.viewers = new HashSet<>();
        this.invalidated = false;
        this.inventory = inv;
        this.buttons = new Button[inv.capacity()];
    }

    /**
     * Internal method.
     *
     * <p>Removes the provided {@link Player} from the list of this {@link Menu}'s viewers.</p>
     *
     * @param player the Player to remove
     */
    void removeViewer(Player player) {
        this.viewers.remove(player.getUniqueId());
    }

    // endregion

    /**
     * Returns whether or not this {@link Menu} is in an invalidated state -- i.e., the menu is waiting to have its
     * representative inventory updated in the next tick to reflect changes made in the current.
     *
     * <p>If this Menu contains any {@link Button}s that are animated ({@link Button#isAnimated()} returns true), this
     * method will also always return true.</p>
     *
     * @return if this Menu is in an invalidated state
     */
    public boolean isInvalidated() {
        for (Button button : this.buttons) {
            if (button == null) continue;
            if (button.isAnimated()) return true;
        }

        return this.invalidated;
    }

    /**
     * Invalidates this {@link Menu}.
     *
     * @see #isInvalidated()
     */
    public void invalidate() {
        this.invalidated = true;
    }

    /**
     * Returns the capacity of this {@link Menu}, i.e. how many {@link Button}s can be held by it.
     *
     * <p>This value is the upper exclusive limit of the {@link #getButton(int)} and {@link #setButton(int, Button)}
     * methods.</p>
     *
     * @return the capacity of this Menu
     */
    public int getCapacity() {
        return this.buttons.length;
    }

    /**
     * Returns the {@link Button} set at the given index?
     *
     * @param index the index to query
     * @return the Button at the given index?
     */
    public Optional<Button> getButton(int index) {
        return Optional.ofNullable(this.buttons[index]);
    }

    /**
     * Sets a {@link Button} in this menu, or clears the one at the given index if null.
     *
     * @param index the index to set the Button at
     * @param button the Button to set, or null to clear
     */
    public void setButton(int index, Button button) {
        this.buttons[index] = button;
        this.invalidated = true;
    }

    /**
     * Returns whether or not a given {@link Player} has this {@link Menu} open.
     *
     * @param player the Player to query
     * @return if the given Player is currently viewing this Menu
     */
    public boolean isViewingMenu(Player player) {
        return Menu.viewerMap.get(player.getUniqueId()) == this;
    }

    /**
     * Returns the set of all {@link UUID}s belonging to {@link Player}s currently viewing this {@link Menu}.
     *
     * @return a set of UUIDs belonging to Players currently viewing this Menu
     */
    public Set<UUID> getViewers() {
        return this.viewers;
    }

    /**
     * Shows this {@link Menu} to the given {@link Player}.
     *
     * @param player the Player to show this Menu to
     */
    public void open(Player player) {
        UUID uid = player.getUniqueId();
        Menu previous = Menu.viewerMap.get(uid);

        // pretend that they're already viewing it so as to validate any viewer checks in an inventory open event
        Menu.viewerMap.put(uid, this);
        this.viewers.add(uid);

        if (!player.openInventory(this.inventory).isPresent()) {
            // reverse the effects of the last two lines if the open actually failed
            if (previous == null) Menu.viewerMap.remove(uid);
            else Menu.viewerMap.put(uid, previous);
            this.viewers.remove(uid);
        }
    }

    /**
     * Returns the underlying inventory used by this {@link Menu}.
     *
     * @return the underlying inventory used by this Menu
     */
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * Updates the {@link Inventory} used by this {@link Menu} to display its buttons.
     *
     * <p>The invalidation state is reset upon calling this method.</p>
     */
    public void updateInventory() {
        Menu.updateInventory(0, this.buttons, this.inventory);
        this.invalidated = false;
    }

}
