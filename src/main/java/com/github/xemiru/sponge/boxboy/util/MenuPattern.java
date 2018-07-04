package com.github.xemiru.sponge.boxboy.util;

import com.github.xemiru.sponge.boxboy.Menu;
import com.github.xemiru.sponge.boxboy.button.Button;

import java.util.HashMap;
import java.util.Map;

public class MenuPattern {

    private Map<Character, Button> mapping;
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
     * \n) are ignored. Spaces are considered empty slots. All other characters are valid only if registered through the
     * setButton method.</p>
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
            if (Character.isWhitespace(ch)) continue;
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
     * <p>The {@code forceEmpty} parameter determines the behavior when encountering a space character in the pattern.
     * If {@code forceEmpty} is true, the slot occupied by the space character will be cleared of its button; it is
     * otherwise left alone.</p>
     *
     * @param menu the Menu to apply to
     * @param forceEmpty if slots occupied by spaces in the pattern should be left alone
     */
    public void apply(Menu menu, boolean forceEmpty) {
        if (this.pattern.length() > menu.getCapacity())
            throw new IllegalArgumentException("Menu too small to contain pattern");

        for (int i = 0; i < this.pattern.length(); i++) {
            char ch = this.pattern.charAt(i);
            if (ch == ' ') {
                if (forceEmpty) menu.setButton(i, null);
            } else {
                menu.setButton(i, this.mapping.get(ch));
            }
        }
    }

}
