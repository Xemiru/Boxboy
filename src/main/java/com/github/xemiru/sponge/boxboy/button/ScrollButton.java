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
package com.github.xemiru.sponge.boxboy.button;

import com.github.xemiru.sponge.boxboy.util.ClickContext;
import com.github.xemiru.sponge.boxboy.util.ClickType;
import org.spongepowered.api.item.inventory.ItemStack;

/**
 * A {@link Button} implementation that switches between a varying number of states defined provided
 * {@link ActionButton}s.
 */
public class ScrollButton implements Button {

    private int state;
    private ActionButton[] buttons;

    private ScrollButton() {
    }

    /**
     * Creates a new {@link ScrollButton} with the default state of 0 and array of {@link ActionButton}s.
     *
     * @param buttons the buttons representing the states of the ScrollButton
     * @return the ScrollButton
     */
    public static ScrollButton of(ActionButton... buttons) {
        return ScrollButton.of(0, buttons);
    }

    /**
     * Creates a new {@link ScrollButton} with the given default state and array of {@link ActionButton}s.
     *
     * @param defaultState the default state of the ScrollButton
     * @param buttons the buttons representing the states of the ScrollButton
     * @return the ScrollButton
     */
    public static ScrollButton of(int defaultState, ActionButton... buttons) {
        ScrollButton btn = new ScrollButton();
        btn.state = defaultState;
        btn.buttons = buttons;

        if (btn.state >= buttons.length)
            throw new IllegalArgumentException("Scroll button state out of bounds of provided button array");
        return btn;
    }

    /**
     * Returns the current state of this {@link ScrollButton} as an integer.
     *
     * @return the state of this ScrollButton
     */
    public int getCurrentState() {
        return this.state;
    }

    /**
     * Returns the {@link ActionButton} assigned to the current state of this {@link ScrollButton}.
     *
     * @return the representative ActionButton of this ScrollButton's current state
     */
    public ActionButton getCurrentStateButton() {
        return this.buttons[this.state];
    }

    @Override
    public boolean isAnimated() {
        return this.getCurrentStateButton().isAnimated();
    }

    @Override
    public ItemStack getRepresentative() {
        return this.getCurrentStateButton().getRepresentative();
    }

    @Override
    public void onClick(ClickContext context) {
        ClickType type = context.getType();
        if (type.isPrimary() || type.isSecondary()) {
            if (type.isPrimary()) this.state++;
            if (type.isSecondary()) this.state--;

            if(this.state >= buttons.length) this.state = 0;
            if(this.state <= -1) this.state = buttons.length - 1;

            this.getCurrentStateButton().onClick(context);
            context.getSourceMenu().invalidate();
        }
    }
}
