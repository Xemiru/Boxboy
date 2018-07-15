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

import com.github.xemiru.sponge.boxboy.Menu;
import com.github.xemiru.sponge.boxboy.util.Animation;
import com.github.xemiru.sponge.boxboy.util.ClickContext;
import com.github.xemiru.sponge.boxboy.util.OfferContext;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;

/**
 * A clickable button in a {@link Menu}, represented by an {@link ItemStack}.
 */
public interface Button {

    /**
     * Returns whether or not this {@link Button} is animated.
     *
     * <p>See {@link Menu#isInvalidated()} for how this value is used.</p>
     *
     * @return if this Button is animated
     * @see Menu#isInvalidated()
     * @deprecated While still usable for special implementations of non-static representatives through the use of
     *             {@link #getRepresentative()}, it is recommended to implement {@link #getAnimatedRepresentative()}
     *             instead.
     */
    @Deprecated
    default boolean isAnimated() {
        return this.getAnimatedRepresentative().isPresent();
    }

    /**
     * Returns the {@link ItemStack} representing this {@link Button}.
     *
     * @return the ItemStack currently representing this Button
     */
    default ItemStack getRepresentative() {
        return ItemStack.empty();
    }

    /**
     * Returns the animated representative of this {@link Button}.
     *
     * <p>This representative takes priority over {@link #getRepresentative()}. Should this return empty, menus will
     * instead make use of the previous method to get the final representative item.</p>
     *
     * @return the animated representative ItemStack of this Button
     */
    default Optional<Animation<ItemStack>> getAnimatedRepresentative() {
        return Optional.empty();
    }

    /**
     * Offers a change of the {@link ItemStack} in the slot this {@link Button} occupies.
     *
     * <p>This method is called before {@link #onClick(ClickContext)}.</p>
     *
     * @param context the context associated with the offer
     * @return if the transaction is allowed to succeed
     */
    default boolean offer(OfferContext context) {
        return false;
    }

    /**
     * Performs an action when clicked.
     *
     * <p>This method is called after {@link #offer(OfferContext)}.</p>
     *
     * @param context the context associated with the click
     */
    default void onClick(ClickContext context) {
    }

}
