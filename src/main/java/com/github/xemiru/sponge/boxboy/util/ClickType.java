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
