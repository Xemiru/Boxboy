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
import com.github.xemiru.sponge.boxboy.util.ReflectUtil;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Container;
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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * The entrypoint class for {@link Boxboy}-related tasks.
 */
public class Boxboy {

    private static Boxboy boxboy;
    private static Method m_sendAllContents, m_getInventory;
    private static Field f_openContainer, f_inventoryContainer;
    private static boolean reflectionReady = true;

    static {
        try {
            Boxboy.m_sendAllContents = ReflectUtil.getDeclaredMethod("net.minecraft.entity.player.EntityPlayerMP",
                new String[]{"sendAllContents", "func_71110_a"},
                "net.minecraft.inventory.Container",
                "net.minecraft.util.NonNullList");

            Boxboy.m_getInventory = ReflectUtil.getDeclaredMethod("net.minecraft.inventory.Container",
                new String[]{"getInventory", "func_75138_a"});

            Boxboy.f_openContainer = ReflectUtil.getDeclaredField("net.minecraft.entity.player.EntityPlayer",
                "openContainer", "field_71070_bA");

            Boxboy.f_inventoryContainer = ReflectUtil.getDeclaredField("net.minecraft.entity.player.EntityPlayer",
                "inventoryContainer", "field_71069_bz");
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException ignored) {
            Boxboy.reflectionReady = false;
        }
    }

    /**
     * Returns the singleton instance of {@link Boxboy}.
     *
     * @return the singleton instance of Boxboy
     */
    public static Boxboy get() {
        return Boxboy.boxboy;
    }

    /**
     * Forces a {@link Player}'s client to refresh the inventories they're viewing.
     *
     * @param player the Player to refresh
     */
    private static void updatePlayerInventory(Player player) {
        if (!player.isOnline()) return;

        try {
            Object activeContainer = Boxboy.f_openContainer.get(player);
            if (activeContainer == null) activeContainer = Boxboy.f_inventoryContainer.get(player);
            Boxboy.m_sendAllContents.invoke(player, activeContainer, Boxboy.m_getInventory.invoke(activeContainer));
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }
    }

    private Object plugin;
    private Map<UUID, ItemStack[]> playerInvs;
    private Map<Container, UUID> containerTrack;

    Boxboy(Object plugin, Game game) {
        Boxboy.boxboy = this;

        this.plugin = plugin;
        this.playerInvs = new HashMap<>();
        this.containerTrack = new WeakHashMap<>();

        if (game.getState().compareTo(GameState.PRE_INITIALIZATION) < 0)
            throw new IllegalStateException("Cannot instantiate Boxboy before pre-initialization");

        if (!Boxboy.reflectionReady) {
            throw new IllegalStateException("Boxboy's reflection failed! It can't start..");
        }

        // Make sure it's actually a plugin.
        // Container variable isn't used.
        @SuppressWarnings("unused") PluginContainer container = Sponge.getPluginManager().fromInstance(plugin)
            .orElseThrow(() -> new IllegalArgumentException("Provided object was not a registered Sponge plugin"));

        Sponge.getEventManager().registerListeners(plugin, this);
        Task.builder()
            .name("Boxboy Menu Task (owned by " + plugin.getClass().getSimpleName() + ".class)")
            .intervalTicks(1)
            .execute(() -> Menu.menus.forEach(menu -> {
                if (!menu.getViewers().isEmpty() && menu.isInvalidated()) menu.updateInventory();
            })).submit(this.plugin);
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
     * Internal method.
     *
     * <p>Restores the stored inventory of the provided {@link Player}.</p>
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
     * Internal method.
     *
     * <p>Returns whether or not the given {@link Player} has a stored inventory.</p>
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

    /**
     * Internal method.
     *
     * <p>Retrieves the {@link Player} who is viewing or viewed the provided {@link Container}.</p>
     *
     * @param container the Container to query
     * @return the Player viewing?
     */
    private Optional<Player> fromContainer(Container container) {
        if (this.containerTrack.containsKey(container))
            return Sponge.getServer().getPlayer(this.containerTrack.get(container)).filter(Player::isOnline);

        for (Player player : Sponge.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory().orElse(null) == container) return Optional.of(player);
        }

        return Optional.empty();
    }

    // endregion

    // region Event listeners

    // region container-tracking events

    @Listener(order = Order.PRE)
    public void onOpenCT(InteractInventoryEvent.Open e) {
        this.fromContainer(e.getTargetInventory()).map(Player::getUniqueId)
            .ifPresent(uid -> this.containerTrack.put(e.getTargetInventory(), uid));
    }

    @Listener(order = Order.POST)
    public void onOpenCTPost(InteractInventoryEvent.Open e) {
        if (e.isCancelled()) this.containerTrack.remove(e.getTargetInventory());
    }

    @Listener(order = Order.POST)
    public void onCloseCT(InteractInventoryEvent.Close e) {
        this.containerTrack.remove(e.getTargetInventory());
    }

    // endregion

    @Listener
    public void onOpen(InteractInventoryEvent.Open e) {
        e.getTargetInventory().getInventoryProperty(MenuProperty.class).map(MenuProperty::getValue).ifPresent(menu ->
            this.fromContainer(e.getTargetInventory()).ifPresent(viewer -> {
                Menu.viewerMap.put(viewer.getUniqueId(), menu);
                menu.addViewer(viewer);

                if (menu instanceof ExtendedMenu) {
                    if (!this.hasStoredInventory(viewer)) this.storePlayer(viewer);
                    ((ExtendedMenu) menu).updatePlayer(viewer);
                } else if (this.hasStoredInventory(viewer)) this.restorePlayer(viewer);

                Task.builder().execute(() -> Boxboy.updatePlayerInventory(viewer)).submit(this.plugin);
            }));
    }

    @Listener
    public void onClose(InteractInventoryEvent.Close e) {
        e.getTargetInventory().getInventoryProperty(MenuProperty.class).map(MenuProperty::getValue).ifPresent(menu ->
            this.fromContainer(e.getTargetInventory()).ifPresent(viewer -> {
                if (this.hasStoredInventory(viewer)) this.restorePlayer(viewer);
                Task.builder().execute(() -> Boxboy.updatePlayerInventory(viewer)).submit(this.plugin);

                menu.removeViewer(viewer);
                Menu.viewerMap.remove(viewer.getUniqueId());
            }));
    }

    @Listener
    public void onLeave(ClientConnectionEvent.Disconnect e) {
        e.getCause().first(Player.class).ifPresent(leaver -> {
            if (this.hasStoredInventory(leaver)) this.restorePlayer(leaver);
            leaver.getOpenInventory().ifPresent(container -> {
                this.fromPlayer(leaver).ifPresent(menu -> menu.removeViewer(leaver));
                Menu.viewerMap.remove(leaver.getUniqueId());
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
