package com.github.xemiru.sponge.boxboy.util;

import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;

public enum ClickType {
    DOUBLE,
    MIDDLE,
    PRIMARY,
    SECONDARY,
    SHIFT_PRIMARY,
    SHIFT_SECONDARY,
    UNKNOWN;

    public static ClickType fromEvent(ClickInventoryEvent e) {
        if (e instanceof ClickInventoryEvent.Double) return DOUBLE;
        else if (e instanceof ClickInventoryEvent.Middle) return MIDDLE;

            // shifts extend their non-shift versions and therefore should be checked first
        else if (e instanceof ClickInventoryEvent.Shift.Primary) return SHIFT_PRIMARY;
        else if (e instanceof ClickInventoryEvent.Shift.Secondary) return SHIFT_SECONDARY;
        else if (e instanceof ClickInventoryEvent.Primary) return PRIMARY;
        else if (e instanceof ClickInventoryEvent.Secondary) return SECONDARY;
        else return UNKNOWN;
    }

    public boolean isPrimary() {
        switch (this) {
            case DOUBLE:
            case PRIMARY:
            case SHIFT_PRIMARY:
                return true;
            default:
                return false;
        }
    }

    public boolean isSecondary() {
        switch(this) {
            case SECONDARY:
            case SHIFT_SECONDARY:
                return true;
            default:
                return false;
        }
    }

    public boolean isShift() {
        switch(this) {
            case SHIFT_PRIMARY:
            case SHIFT_SECONDARY:
                return true;
            default:
                return false;
        }
    }

}
