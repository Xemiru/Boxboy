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
package com.github.xemiru.sponge.boxboy.util;

import com.github.xemiru.sponge.boxboy.Menu;
import com.github.xemiru.sponge.boxboy.button.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * A utility class to create templates for animating {@link Menu}s.
 */
public class AnimatedMenuPattern {

    // region statics

    private static Map<Menu, List<AnimationPattern>> patterns;

    static {
        AnimatedMenuPattern.patterns = new WeakHashMap<>();
    }

    /**
     * Clears the given {@link Menu} of any animations applied by instances of {@link AnimatedMenuPattern}.
     *
     * @param menu the Menu to clear animations of, if it has any
     */
    public static void clearAnimation(Menu menu) {
        Objects.requireNonNull(menu);
        AnimatedMenuPattern.patterns.remove(menu);
    }

    /**
     * Updates all {@link Menu}s with animations to their current frame.
     */
    public static void refreshAnimations() {
        AnimatedMenuPattern.patterns.forEach((menu, value) -> value.forEach(pattern -> {
            if (pattern.patternFrames.isNewFrame()) {
                String frame = pattern.patternFrames.getCurrentFrame();
                pattern.pattern.setPattern(frame);
                pattern.pattern.apply(menu);
            }
        }));
    }

    // endregion

    /**
     * Data bag class for menu animation data.
     */
    private class AnimationPattern {

        private MenuPattern pattern;
        private Animation<String> patternFrames;

    }

    private Map<Character, Button> mapping;
    private Animation<String> patternFrames;

    /**
     * Creates a new, empty {@link AnimatedMenuPattern}.
     */
    public AnimatedMenuPattern() {
        this.mapping = new HashMap<>();
        this.patternFrames = new Animation<>();
    }

    /**
     * Copies the character mappings set on another {@link AnimatedMenuPattern}.
     *
     * @param other the AnimatedMenuPattern to copy from
     * @return this AnimatedMenuPattern, for chaining
     */
    public AnimatedMenuPattern copyMappingFrom(AnimatedMenuPattern other) {
        this.mapping = new HashMap<>(other.mapping);
        return this;
    }

    /**
     * Copies the character mappings set on another {@link MenuPattern}.
     *
     * @param other the MenuPattern to copy from
     * @return this AnimatedMenuPattern, for chaining
     */
    public AnimatedMenuPattern copyMappingFrom(MenuPattern other) {
        this.mapping = new HashMap<>(other.mapping);
        return this;
    }

    /**
     * Sets a character to represent a {@link Button} for this {@link AnimatedMenuPattern}.
     *
     * @param key the representative character
     * @param btn the button to represent
     * @return this AnimatedMenuPattern, for chaining
     */
    public AnimatedMenuPattern setButton(char key, Button btn) {
        this.mapping.put(key, btn);
        return this;
    }

    /**
     * Adds a new frame used to apply this {@link AnimatedMenuPattern}'s {@link Button}s to a {@link Menu}.
     *
     * <p>The Buttons of the pattern must be set using {@link #setButton(char, Button)} first before calling this
     * method as this method verifies the validity of all characters within the provided String.</p>
     *
     * <p>Each string passed to this method will be concatenated together as one pattern string. Newline characters (\r,
     * \n) are ignored. Spaces will do nothing to the slot at the character's position. Underscore characters (_) will
     * forcibly remove the button in the slot at the character's position. All other characters are valid only if
     * registered through the setButton method.</p>
     *
     * @param millis how long the frame lasts, in milliseconds
     * @param pattern the pattern to set
     * @return this MenuPattern, for chaining
     */
    public AnimatedMenuPattern frame(long millis, String... pattern) {
        StringBuilder sb = new StringBuilder();
        for (String pat : pattern) sb.append(pat);
        String concat = sb.toString();

        // Verify the pattern.
        for (int i = 0; i < concat.length(); i++) {
            char ch = concat.charAt(i);
            if (Character.isWhitespace(ch) || ch == '_') continue;
            if (!mapping.containsKey(ch))
                throw new IllegalArgumentException("Pattern contains unrecognized mappings");
        }

        String finalPattern = concat.replace("\n", "").replace("\r", "");
        this.patternFrames.frame(finalPattern, millis);
        return this;
    }

    /**
     * Applies this {@link AnimatedMenuPattern} to the given {@link Menu}.
     *
     * <p>Animation patterns applied to Menus are not bound to the AnimatedMenuPattern instances that set them up;
     * <b>changes applied to this AnimatedMenuPattern instance after application to a Menu will not affect the
     * aforementioned Menu.</b></p>
     *
     * <p>There can be more than one application of an AnimatedMenuPattern to a Menu.</p>
     *
     * @param menu the Menu to apply to
     * @throws IllegalStateException if no frames have been provided to this AnimatedMenuPattern yet
     */
    public void apply(Menu menu) {
        if (this.patternFrames.isEmpty()) throw new IllegalStateException("No animation frames to apply");
        Objects.requireNonNull(menu);

        AnimationPattern pattern = new AnimationPattern();
        pattern.pattern = new MenuPattern();
        pattern.patternFrames = this.patternFrames.clone();
        pattern.pattern.mapping = new HashMap<>(this.mapping);

        AnimatedMenuPattern.patterns.putIfAbsent(menu, new ArrayList<>());
        AnimatedMenuPattern.patterns.get(menu).add(pattern);
    }

}
