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
import com.github.xemiru.sponge.boxboy.util.ClickContext;
import com.github.xemiru.sponge.boxboy.util.ClickType;
import com.github.xemiru.sponge.boxboy.util.MenuProperty;
import com.github.xemiru.sponge.boxboy.util.OfferContext;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.AbstractInventoryProperty;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The entrypoint class for {@link Boxboy}-related tasks.
 */
public class Boxboy {

    private static Boxboy boxboy;

    /**
     * Returns the singleton instance of {@link Boxboy}.
     *
     * @return the singleton instance of Boxboy
     */
    public static Boxboy get() {
        return Boxboy.boxboy;
    }

    private Object plugin;
    private Map<UUID, ItemStack[]> playerInvs;

    Boxboy(Object plugin, Game game) {
        Boxboy.boxboy = this;

        this.plugin = plugin;
        this.playerInvs = new HashMap<>();

        if (game.getState().compareTo(GameState.PRE_INITIALIZATION) < 0)
            throw new IllegalStateException("Cannot instantiate Boxboy before pre-initialization");

        // Make sure it's actually a plugin.
        // Container variable isn't used.
        PluginContainer container = Sponge.getPluginManager().fromInstance(plugin).orElseThrow(
            () -> new IllegalArgumentException("Provided object was not a registered Sponge plugin"));

        Sponge.getEventManager().registerListeners(plugin, this);
        Sponge.getScheduler().createTaskBuilder()
            .name("Boxboy Menu Task (owned by " + plugin.getClass().getSimpleName() + ".class)")
            .intervalTicks(1)
            .execute(() -> Menu.menus.forEach(menu -> {
                if (menu.isInvalidated()) menu.updateInventory();
            }))
            .submit(this.plugin);
    }

    /**
     * Creates a new {@link Menu} using the Chest / Double Chest inventory archetype.
     *
     * <p>There may be at most 6 rows.</p>
     *
     * @param rows the row count to have for the inventory
     * @param title the title of the Menu
     * @return the Menu instance
     */
    public Menu createMenu(int rows, Text title) {
        if (rows < 1 || rows > 6)
            throw new IllegalArgumentException("Rows parameter must be between 1 and 6 (inclusive).");

        return this.createMenu(Inventory.builder()
            .of(InventoryArchetypes.CHEST)
            .property(InventoryTitle.of(title))
            .property(InventoryDimension.of(9, rows)));
    }

    /**
     * Creates a new {@link Menu} using the provided inventory archetype.
     *
     * @param arch the archetype to use
     * @param title the title of the Menu
     * @return the Menu instance
     */
    public Menu createMenu(InventoryArchetype arch, Text title) {
        return this.createMenu(Inventory.builder()
            .property(InventoryTitle.of(title))
            .of(arch));
    }

    /**
     * Creates a new {@link ExtendedMenu} using the Chest / Double Chest inventory archetype.
     *
     * <p>There may be at most 6 rows.</p>
     *
     * @param rows the row count to have for the inventory
     * @param title the title of the ExtendedMenu
     * @return the ExtendedMenu instance
     */
    public ExtendedMenu createExtendedMenu(int rows, Text title) {
        if (rows < 1 || rows > 6)
            throw new IllegalArgumentException("Rows parameter must be between 1 and 6 (inclusive).");

        return this.createPlayerMenu(Inventory.builder()
            .of(InventoryArchetypes.CHEST)
            .property(InventoryTitle.of(title))
            .property(InventoryDimension.of(9, rows)));
    }

    /**
     * Creates a new {@link ExtendedMenu} using the provided inventory archetype.
     *
     * @param arch the archetype to use
     * @param title the title of the ExtendedMenu
     * @return the ExtendedMenu instance
     */
    public ExtendedMenu createExtendedMenu(InventoryArchetype arch, Text title) {
        return this.createPlayerMenu(Inventory.builder()
            .property(InventoryTitle.of(title))
            .of(arch));
    }

    // region Internal methods

    /**
     * Internal method.
     *
     * <p>Creates a new {@link Menu} with the inventory set by the properties of the provided builder.</p>
     *
     * @param builder the builder to create from
     * @return the Menu instance
     */
    private Menu createMenu(Inventory.Builder builder) {
        Menu menu = new Menu();
        menu.initialize(builder.property(MenuProperty.of(menu)).build(plugin));

        return menu;
    }

    /**
     * Internal method.
     *
     * <p>Creates a new {@link ExtendedMenu} with the inventory set by the properties of the provided builder.</p>
     *
     * @param builder the builder to create from
     * @return the ExtendedMenu instance
     */
    private ExtendedMenu createPlayerMenu(Inventory.Builder builder) {
        ExtendedMenu menu = new ExtendedMenu();
        menu.initialize(builder.property(MenuProperty.of(menu)).build(plugin));

        return menu;
    }

    /**
     * Internal method.
     *
     * <p>Stores the inventory of the provided {@link Player}. This does not clear the Player's inventory.</p>
     *
     * @param player the Player to store the inventory of
     * @throws IllegalStateException if the Player already has a stored inventory
     */
    private void storePlayer(Player player) {
        UUID uid = player.getUniqueId();
        if (this.hasStoredInventory(player))
            throw new IllegalStateException("Player already has a stored inventory");

        Inventory inv = player.getInventory();
        ItemStack[] store = new ItemStack[inv.capacity()];

        int index = 0;
        for (Inventory slot : inv.slots()) {
            store[index] = slot.peek().orElse(ItemStack.empty());
            index++;
        }

        this.playerInvs.put(uid, store);
    }

    /**
     * Restores the stored inventory of the provided {@link Player}.
     *
     * @param player the Player to restore the inventory of
     * @throws IllegalStateException if the Player has no stored inventory
     */
    private void restorePlayer(Player player) {
        UUID uid = player.getUniqueId();
        if (!this.hasStoredInventory(player)) throw new IllegalStateException("Player has no stored inventory");

        Inventory inv = player.getInventory();
        ItemStack[] store = this.playerInvs.get(uid);

        int index = 0;
        for (Inventory slot : inv.slots()) {
            slot.set(store[index]);
            index++;
        }

        this.playerInvs.remove(uid);
    }

    /**
     * Returns whether or not the given {@link Player} has a stored inventory.
     *
     * @param player the Player to query
     * @return if the Player has a stored inventory
     */
    private boolean hasStoredInventory(Player player) {
        return this.playerInvs.containsKey(player.getUniqueId());
    }

    /**
     * Internal method.
     *
     * <p>Retrieve the {@link Menu} currently being viewed by a specific {@link Player}.</p>
     *
     * @param player the Player to query
     * @return the Menu being viewed?
     */
    private Optional<Menu> fromPlayer(Player player) {
        return Optional.ofNullable(Menu.viewerMap.get(player.getUniqueId()));
    }

    // endregion

    // region Event listeners

    @Listener
    public void onOpen(InteractInventoryEvent.Open e) {
        e.getCause().first(Player.class).ifPresent(viewer ->
            this.fromPlayer(viewer).ifPresent(menu -> {
                if (menu instanceof ExtendedMenu) {
                    this.storePlayer(viewer);
                    Task.builder().execute(() -> ((ExtendedMenu) menu).updatePlayer(viewer)).submit(this.plugin);
                }
            }));
    }

    @Listener
    public void onClose(InteractInventoryEvent.Close e) {
        e.getCause().first(Player.class).ifPresent(viewer -> {
            if (this.hasStoredInventory(viewer))
                Task.builder().execute(() ->
                    this.restorePlayer(viewer)).submit(this.plugin);

            this.fromPlayer(viewer).ifPresent(menu ->
                menu.removeViewer(viewer));

            Menu.viewerMap.remove(viewer.getUniqueId());
        });
    }

    @Listener
    public void onLeave(ClientConnectionEvent.Disconnect e) {
        e.getCause().first(Player.class).ifPresent(leaver -> {
            if (this.hasStoredInventory(leaver)) this.restorePlayer(leaver);
            leaver.getOpenInventory().ifPresent(container -> {
                Menu.viewerMap.remove(leaver.getUniqueId());
                this.fromPlayer(leaver).ifPresent(menu -> menu.removeViewer(leaver));
            });
        });
    }

    @Listener
    public void onClick(ClickInventoryEvent e) {
        e.getCause().first(Player.class).ifPresent(clicker ->
            this.fromPlayer(clicker).ifPresent(menu -> {
                ClickType type = ClickType.fromEvent(e);
                boolean first = true;
                boolean cancelled = false;
                SlotTransaction exempt = null;

                // Check for any tampering with the menu slots.
                for (SlotTransaction trans : e.getTransactions()) {
                    int slot = trans.getSlot().getInventoryProperty(SlotIndex.class)
                        .map(AbstractInventoryProperty::getValue).orElse(-1);
                    if (slot >= 0 && slot < menu.getCapacity()) {
                        // We only want the first transaction since it's the one directly involving the clicked slot.
                        if (first && type != ClickType.UNKNOWN) {
                            ItemStack cursorItem = trans.getFinal().createStack();
                            if (cursorItem.getType() == ItemTypes.AIR) cursorItem = null;

                            ClickContext context = new ClickContext(type, menu, clicker, cursorItem);
                            OfferContext oContext = new OfferContext(menu, clicker, cursorItem);
                            Optional<Button> button = menu.getButton(slot);

                            if (button.isPresent()) {
                                if (button.get().offer(oContext)) exempt = trans;
                                button.get().onClick(context);

                                context.getNewCursor().ifPresent(it -> e.getCursorTransaction().setCustom(it.createSnapshot()));
                            }
                        }

                        // If ANY menu slots are messed with, cancel all transactions.
                        cancelled = true;
                        break;
                    }

                    first = false;
                }

                if (cancelled) {
                    for (SlotTransaction it : e.getTransactions()) if (it != exempt) it.setValid(false);
                    if (exempt == null) e.getCursorTransaction().setValid(false);
                    if (e.getCursorTransaction().getCustom().isPresent()) e.getCursorTransaction().setValid(true);
                }
            }));
    }

    @Listener
    public void onPickup(ChangeInventoryEvent.Pickup.Pre e) {
        e.getCause().first(Player.class).ifPresent(player -> {
            // Cannot pick up items while viewing an extended menu.
            // Checking inventory store status instead of checking the currently viewed menu's type will ensure
            // restoration happens before pickups are allowed again.
            if (this.hasStoredInventory(player)) e.setCancelled(true);
        });
    }

    // endregion

}
