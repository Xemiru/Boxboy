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
import com.google.common.base.Preconditions;
import org.spongepowered.api.item.inventory.ItemStack;

/**
 * A {@link Button} implementation that switches between two states defined by two provided {@link ActionButton}s.
 */
public class ToggleButton implements Button {

    private ActionButton a;
    private ActionButton b;
    private boolean state;

    private ToggleButton() {
        this.state = true;
        this.a = null;
        this.b = null;
    }

    /**
     * Creates a new {@link ToggleButton} using the two provided state buttons.
     *
     * <p>The state button is activated <b>when become the active state</b> -- {@code stateB}'s
     * {@link Button#onClick(ClickContext)} will be activated first.</p>
     *
     * @param stateA the true state of the ToggleButton
     * @param stateB the false state of the ToggleButton
     * @return the new ToggleButton
     */
    public static ToggleButton of(ActionButton stateA, ActionButton stateB) {
        return ToggleButton.of(stateA, stateB, true);
    }

    /**
     * Creates a new {@link ToggleButton} using the two provided state buttons.
     *
     * <p>The state button is activated <b>when becoming the active state</b> -- {@code stateB}'s
     * {@link Button#onClick(ClickContext)} will be activated first if the default state of the ToggleButton is
     * true.</p>
     *
     * @param stateA the true state of the ToggleButton
     * @param stateB the false state of the ToggleButton
     * @param defaultState the default state of the ToggleButton
     * @return the new ToggleButton
     */
    public static ToggleButton of(ActionButton stateA, ActionButton stateB, boolean defaultState) {
        Preconditions.checkNotNull(stateA);
        Preconditions.checkNotNull(stateB);

        ToggleButton button = new ToggleButton();
        button.state = defaultState;
        button.a = stateA;
        button.b = stateB;

        return button;
    }

    /**
     * Returns the current state of this {@link ToggleButton} as a boolean.
     *
     * @return the state of this ToggleButton
     */
    public boolean getCurrentState() {
        return this.state;
    }

    /**
     * Returns the {@link ActionButton} assigned to the current state of this {@link ToggleButton}.
     *
     * @return the representative ActionButton of this ToggleButton's current state
     */
    public ActionButton getCurrentStateButton() {
        if (this.state) return a;
        return b;
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
        // Swap the state.
        // We activate the action of the new state.

        this.state = !state;
        if (state) a.onClick(context);
        else b.onClick(context);

        // Invalidate, as the state has changed and therefore the representative may have.
        context.getSourceMenu().invalidate();
    }
}
