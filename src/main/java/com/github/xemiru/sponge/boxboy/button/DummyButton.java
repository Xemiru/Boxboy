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

import com.github.xemiru.sponge.boxboy.util.Animation;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@link Button} implementation that does absolutely nothing and thus should be used purely for aesthetical purposes.
 */
public class DummyButton implements Button {

    private Animation<ItemStack> animation;
    private ItemStack representative;

    private DummyButton() {
        this.animation = null;
        this.representative = null;
    }

    /**
     * Creates a new {@link DummyButton} with a representative {@link ItemStack}.
     *
     * @param representative the representative ItemStack
     * @return the DummyButton
     */
    public static DummyButton of(ItemStack representative) {
        Objects.requireNonNull(representative);

        DummyButton btn = new DummyButton();
        btn.representative = representative;

        return btn;
    }

    /**
     * Creates a new {@link DummyButton} with a representative {@link Animation}.
     *
     * @param animation the representative Animation
     * @return the DummyButton
     */
    public static DummyButton of(Animation<ItemStack> animation) {
        Objects.requireNonNull(animation);

        DummyButton btn = new DummyButton();
        btn.animation = animation;

        return btn;
    }

    @Override
    public ItemStack getRepresentative() {
        return this.representative;
    }

    @Override
    public Optional<Animation<ItemStack>> getAnimatedRepresentative() {
        return Optional.ofNullable(this.animation);
    }
}
