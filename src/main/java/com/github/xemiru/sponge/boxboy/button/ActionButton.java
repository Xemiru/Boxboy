package com.github.xemiru.sponge.boxboy.button;

import com.github.xemiru.sponge.boxboy.util.Animation;
import com.github.xemiru.sponge.boxboy.util.ClickContext;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * A {@link Button} implementation that performs a single action when clicked.
 */
public class ActionButton implements Button {

    private Animation<ItemStack> animation;
    private Consumer<ClickContext> consumer;
    private ItemStack representative;

    private ActionButton() {
        this.consumer = null;
        this.animation = null;
        this.representative = null;
    }

    /**
     * Creates a new {@link ActionButton} with a representative {@link ItemStack} and an associated action.
     *
     * @param representative the representative ItemStack
     * @param action the action to perform when clicked
     * @return the ActionButton
     */
    public static ActionButton of(ItemStack representative, Consumer<ClickContext> action) {
        ActionButton btn = new ActionButton();
        btn.representative = representative;
        btn.consumer = action;

        return btn;
    }

    /**
     * Creates a new {@link ActionButton} with a representative {@link Animation} and an associated action.
     *
     * @param animation the representative Animation
     * @param action the action to perform when clicked
     * @return the ActionButton
     */
    public static ActionButton of(Animation<ItemStack> animation, Consumer<ClickContext> action) {
        ActionButton btn = new ActionButton();
        btn.animation = animation;
        btn.consumer = action;

        return btn;
    }

    @Override
    public boolean isAnimated() {
        return this.animation != null;
    }

    @Override
    public ItemStack getRepresentative() {
        if (this.isAnimated()) return this.animation.getCurrentFrame();
        return this.representative;
    }

    @Override
    public void onClick(ClickContext context) {
        this.consumer.accept(context);
    }

}
