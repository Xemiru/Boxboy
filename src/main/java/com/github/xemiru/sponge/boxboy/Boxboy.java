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

    private Object plugin;
    private Map<UUID, ItemStack[]> playerInvs;

    public Boxboy(Object plugin, Game game) {
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

    // endregion

    // region Event listeners

    @Listener
    public void onOpen(InteractInventoryEvent.Open e) {
        e.getCause().first(Player.class).ifPresent(viewer -> {
            for (Menu menu : Menu.menus) {
                if (menu.hasMenuOpen(viewer)) {
                    if (menu instanceof ExtendedMenu) {
                        this.storePlayer(viewer);
                        Task.builder().execute(() -> ((ExtendedMenu) menu).updatePlayer(viewer)).submit(this.plugin);
                    }

                    break;
                }
            }
        });
    }

    @Listener
    public void onClose(InteractInventoryEvent.Close e) {
        e.getCause().first(Player.class).ifPresent(viewer -> {
            if (this.hasStoredInventory(viewer))
                // Restore on the next possible tick.
                Task.builder().execute(() -> this.restorePlayer(viewer)).submit(this.plugin);
        });
    }

    @Listener
    public void onLeave(ClientConnectionEvent.Disconnect e) {
        e.getCause().first(Player.class).ifPresent(leaver -> {
            if (this.hasStoredInventory(leaver)) this.restorePlayer(leaver);
        });
    }

    @Listener
    public void onClick(ClickInventoryEvent e) {
        e.getCause().first(Player.class).ifPresent(clicker -> {
            for (Menu menu : Menu.menus) {
                if (menu.hasMenuOpen(clicker)) {
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

                    break;
                }
            }
        });
    }

    // endregion

}
