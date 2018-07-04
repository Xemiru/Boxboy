package com.github.xemiru.sponge.boxboy.button;

import com.github.xemiru.sponge.boxboy.util.ClickContext;
import com.github.xemiru.sponge.boxboy.util.OfferContext;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * A {@link Button} implementation that pretends to be a normal inventory slot.
 */
public class SlotButton implements Button {

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
        return SlotButton.of(null);
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
        if (this.processor != null && !same) this.processor.accept(Optional.ofNullable(this.held));
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
