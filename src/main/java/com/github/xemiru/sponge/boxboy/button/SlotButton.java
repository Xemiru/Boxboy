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
import com.github.xemiru.sponge.boxboy.util.OfferContext;
import com.google.common.base.Preconditions;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * A {@link Button} implementation that pretends to be a normal inventory slot.
 */
public class SlotButton implements Button {

    private static final Consumer<Optional<ItemStack>> DO_NOTHING = item -> {};

    private ItemStack held;
    private Consumer<Optional<ItemStack>> processor;

    private SlotButton() {
    }

    /**
     * Creates a new {@link SlotButton} that simply pretends to be an item slot.
     *
     * @return a new SlotButton
     */
    public static SlotButton of() {
        return SlotButton.of(DO_NOTHING);
    }

    /**
     * Creates a new {@link SlotButton} that processes the items provided to it.
     *
     * <p>If the new item is equivalent to air, the optional provided to the processor is empty. The processor function
     * is not called if the item did not change during a click.</p>
     *
     * @param processor the processing function
     * @return a new SlotButton
     */
    public static SlotButton of(Consumer<Optional<ItemStack>> processor) {
        Preconditions.checkNotNull(processor);
        SlotButton btn = new SlotButton();

        btn.held = null;
        btn.processor = processor;
        return btn;
    }

    /**
     * Returns the {@link ItemStack} being held by this {@link SlotButton}.
     *
     * @return the ItemStack held by this SlotButton
     */
    public Optional<ItemStack> getHeldItem() {
        return Optional.ofNullable(this.held);
    }

    @Override
    public boolean offer(OfferContext context) {
        ItemStack original = this.held;
        this.held = context.getItem().orElse(null);

        if (this.held != null && this.held.getType() == ItemTypes.AIR) this.held = null;
        boolean same = original == this.held || (original != null && this.held != null && ItemStackComparators.TYPE_SIZE.compare(original, this.held) == 0);
        if (!same) this.processor.accept(Optional.ofNullable(this.held));
        return true;
    }

    @Override
    public boolean isAnimated() {
        return false;
    }

    @Override
    public ItemStack getRepresentative() {
        return this.held;
    }

    @Override
    public void onClick(ClickContext context) {
    }
}
