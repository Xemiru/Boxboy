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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A utility class to create templates for {@link Menu}s.
 */
public class MenuPattern {

    Map<Character, Button> mapping;
    private String pattern;

    public MenuPattern() {
        this.mapping = new HashMap<>();
    }

    /**
     * Copies the character mappings set on another {@link MenuPattern}.
     *
     * @param other the MenuPattern to copy from
     * @return this MenuPattern, for chaining
     */
    public MenuPattern copyMappingFrom(MenuPattern other) {
        this.mapping = new HashMap<>(other.mapping);
        return this;
    }

    /**
     * Sets a character to represent a {@link Button} for this {@link MenuPattern}.
     *
     * @param key the representative character
     * @param btn the button to represent
     * @return this MenuPattern, for chaining
     */
    public MenuPattern setButton(char key, Button btn) {
        this.mapping.put(key, btn);
        return this;
    }

    /**
     * Sets the pattern used to apply this {@link MenuPattern}'s {@link Button}s to a {@link Menu}.
     *
     * <p>The Buttons of the pattern must be set using {@link #setButton(char, Button)} first before calling this
     * method as this method verifies the validity of all characters within the provided String.</p>
     *
     * <p>Each string passed to this method will be concatenated together as one pattern string. Newline characters (\r,
     * \n) are ignored. Spaces will do nothing to the slot at the character's position. Underscore characters (_) will
     * forcibly remove the button in the slot at the character's position. All other characters are valid only if
     * registered through the setButton method.</p>
     *
     * @param pattern the pattern to set
     * @return this MenuPattern, for chaining
     */
    public MenuPattern setPattern(String... pattern) {
        StringBuilder sb = new StringBuilder();
        for (String pat : pattern) sb.append(pat);
        String finalPattern = sb.toString();

        // Verify the pattern.
        for (int i = 0; i < finalPattern.length(); i++) {
            char ch = finalPattern.charAt(i);
            if (Character.isWhitespace(ch) || ch == '_') continue;
            if (!mapping.containsKey(ch))
                throw new IllegalArgumentException("Pattern contains unrecognized mappings");
        }

        this.pattern = finalPattern.replace("\n", "").replace("\r", "");
        return this;
    }

    /**
     * Applies this {@link MenuPattern} to the given {@link Menu}.
     *
     * <p>Functionally equivalent to calling {@link #apply(Menu, boolean)} with {@code ignoreEmpty} set as true.</p>
     *
     * @param menu the Menu to apply to
     */
    public void apply(Menu menu) {
        this.apply(menu, true);
    }

    /**
     * Applies this {@link MenuPattern} to the given {@link Menu}.
     *
     * <p>The {@code ignoreEmpty} parameter determines the behavior when encountering a space character in the pattern.
     * If {@code ignoreEmpty} is true, the slot occupied by the space character will be left alone; it is otherwise
     * cleared of its current button.</p>
     *
     * @param menu the Menu to apply to
     * @param ignoreEmpty if slots occupied by spaces in the pattern should be left alone
     * @deprecated Underscores can now be used to represent slots that should forcibly be emptied. {@link #apply(Menu)}
     *     can be used after giving the pattern underscores instead.
     */
    public void apply(Menu menu, boolean ignoreEmpty) {
        Objects.requireNonNull(menu);

        if (this.pattern == null) throw new IllegalStateException("Pattern has not been set");
        if (this.pattern.length() > menu.getCapacity())
            throw new IllegalArgumentException("Menu too small to contain pattern");

        for (int i = 0; i < this.pattern.length(); i++) {
            char ch = this.pattern.charAt(i);
            if (ch == '_' || (ch == ' ' && !ignoreEmpty)) {
                menu.setButton(i, null);
            } else if (ch != ' ') {
                menu.setButton(i, this.mapping.get(ch));
            }
        }
    }

}
