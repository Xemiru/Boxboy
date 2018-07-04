package com.github.xemiru.sponge.boxboy.util;

import com.github.xemiru.sponge.boxboy.Menu;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.property.AbstractInventoryProperty;

/**
 * An {@link InventoryProperty} allowing an
 * {@link Inventory} to associate with the {@link Menu} that owns it.
 */
public class MenuProperty extends AbstractInventoryProperty<String, Menu> {

    public MenuProperty(Menu menu) {
        super(menu);
    }

    public MenuProperty(Menu menu, Operator op) {
        super(menu, op);
    }

    @Override
    public int compareTo(Property<?, ?> o) {
        if (o.getValue() instanceof Menu) {
            if (this.getValue() == o.getValue()) return 0;
        }

        return 1;
    }

    public static MenuProperty of(Menu menu) {
        return new MenuProperty(menu, Operator.EQUAL);
    }

}
