package com.github.xemiru.sponge.boxboy.button;

import com.github.xemiru.sponge.boxboy.util.Animation;
import org.spongepowered.api.item.inventory.ItemStack;

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
        DummyButton btn = new DummyButton();
        btn.animation = animation;

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

}
