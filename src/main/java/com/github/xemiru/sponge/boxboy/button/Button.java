package com.github.xemiru.sponge.boxboy.button;

import com.github.xemiru.sponge.boxboy.Menu;
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
     * @return if this Button is animated
     */
    default boolean isAnimated() {
        return false;
    }

    /**
     * Returns the {@link ItemStack} representing this button.
     *
     * @return the ItemStack currently representing this button
     */
    ItemStack getRepresentative();

    /**
     * Offers a change of the {@link ItemStack} in the slot this {@link Button} occupies.
     *
     * <p>This method is called before {@link #onClick(ClickContext)}.</p>
     *
     * @param context the context associated with the offe
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
    default void onClick(ClickContext context) {}

}
