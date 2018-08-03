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

import java.util.Objects;

/**
 * A {@link Button} implementation that switches between two states defined by two provided {@link ActionButton}s.
 */
public class SwitchButton extends ToggleButton {

    private SwitchButton() {
        super();
    }

    /**
     * Creates a new {@link SwitchButton} using the two provided state buttons with the default state set to true.
     *
     * <p>The state button is activated <b>when being switched from as the active state</b> -- that is, when first
     * clicked, {@code stateA}'s {@link Button#onClick(ClickContext)} will be activated.</p>
     *
     * @param stateA the true state of the SwitchButton
     * @param stateB the false state of the SwitchButton
     * @return the new SwitchButton
     */
    public static SwitchButton of(ActionButton stateA, ActionButton stateB) {
        return SwitchButton.of(stateA, stateB, true);
    }

    /**
     * Creates a new {@link SwitchButton} using the two provided state buttons.
     *
     * <p>The state button is activated <b>when being switched from as the active state</b> -- that is, when first
     * clicked, {@code stateA}'s {@link Button#onClick(ClickContext)} will be activated if the default state of the
     * SwitchButton is true.</p>
     *
     * @param stateA the true state of the SwitchButton
     * @param stateB the false state of the SwitchButton
     * @param defaultState the default state of the SwitchButton
     * @return the new SwitchButton
     */
    public static SwitchButton of(ActionButton stateA, ActionButton stateB, boolean defaultState) {
        Objects.requireNonNull(stateA);
        Objects.requireNonNull(stateB);

        SwitchButton button = new SwitchButton();
        button.state = defaultState;
        button.a = stateA;
        button.b = stateB;

        return button;
    }

    @Override
    public void onClick(ClickContext context) {
        // We activate the action of the current state before switching.

        this.getCurrentStateButton().onClick(context);
        this.state = !state;

        // Invalidate, as the state has changed and therefore the representative may have.
        context.getSourceMenu().invalidate();
    }
}
