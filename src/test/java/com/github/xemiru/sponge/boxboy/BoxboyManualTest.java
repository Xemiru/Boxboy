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
package com.github.xemiru.sponge.boxboy;

import com.github.xemiru.sponge.boxboy.button.ActionButton;
import com.github.xemiru.sponge.boxboy.button.DummyButton;
import com.github.xemiru.sponge.boxboy.button.ScrollButton;
import com.github.xemiru.sponge.boxboy.button.SlotButton;
import com.github.xemiru.sponge.boxboy.button.ToggleButton;
import com.github.xemiru.sponge.boxboy.util.ClickContext;
import com.github.xemiru.sponge.boxboy.util.MenuPattern;
import com.google.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.util.function.Consumer;

@Plugin(id = "boxboy_test")
public class BoxboyManualTest {

    @Inject
    private Game game;
    private Boxboy boxboy;

    @Listener
    public void onServerStart(GamePreInitializationEvent e) {
        this.boxboy = new Boxboy(this, game);

        // We're testing ..
        // -- each of our stock buttons and their functionality
        // -- menu function
        // -- button animation and updating
        // -- menu pattern setup

        // Use the /hhh command in-game to launch a menu.

        Menu menu = boxboy.createExtendedMenu(3, Text.of("bleh"));
        Consumer<ClickContext> scrollContext = context ->
            Sponge.getServer().getBroadcastChannel().send(Text.of(context.getClicker().getName()));

        // set a button to be replaced by a space and an underscore character to see if spaces/underscores are correctly
        // being applied
        menu.setButton(10, DummyButton.of(ItemStack.of(ItemTypes.STICK, 1)));
        menu.setButton(16, DummyButton.of(ItemStack.of(ItemTypes.STICK, 1)));

        // second menu to be opened by a button
        Menu menu2 = boxboy.createExtendedMenu(3, Text.of("bleh2"));

        menu2.setButton(0, DummyButton.of(ItemStack.of(ItemTypes.ACACIA_BOAT, 1)));

        new MenuPattern()
            .setButton('A', DummyButton.of(ItemStack.of(ItemTypes.STAINED_GLASS_PANE, 1)))
            .setButton('B', ToggleButton.of(
                ActionButton.of(ItemStack.of(ItemTypes.IRON_SWORD, 1), context -> Sponge.getServer().getBroadcastChannel().send(Text.of("hi"))),
                ActionButton.of(ItemStack.of(ItemTypes.DIAMOND_SWORD, 1), context -> Sponge.getServer().getBroadcastChannel().send(Text.of("hello")))))
            .setButton('C', SlotButton.of(it -> System.out.println(it.map(item -> item.getType().getName()).orElse("nothing"))))
            .setButton('D', ActionButton.of(ItemStack.of(ItemTypes.WOODEN_SWORD, 1), context ->
                context.setNewCursor(ItemStack.of(ItemTypes.GOLDEN_SWORD, 1))))
            .setButton('E', ScrollButton.of(
                ActionButton.of(ItemStack.of(ItemTypes.COAL, 1), scrollContext),
                ActionButton.of(ItemStack.of(ItemTypes.IRON_INGOT, 1), scrollContext),
                ActionButton.of(ItemStack.of(ItemTypes.GOLD_INGOT, 1), scrollContext),
                ActionButton.of(ItemStack.of(ItemTypes.DIAMOND, 1), scrollContext),
                ActionButton.of(ItemStack.of(ItemTypes.EMERALD, 1), scrollContext)))
            // safe menu open in the event-based implementation
            .setButton('F', ActionButton.of(ItemStack.of(ItemTypes.ITEM_FRAME, 1), context -> Task.builder()
                .execute(() -> menu2.open(context.getClicker()))
                .submit(this)))
            // unsafe menu open in the event-based implementation
            .setButton('G', ActionButton.of(ItemStack.of(ItemTypes.SKULL, 1),
                context -> menu2.open(context.getClicker())))
            .setPattern("AAAAAAAAA",
                "A  DBC _A",
                "AAA E AAA",
                "AA F G AA",
                "AAA E AAA",
                "A  DBC  A",
                "AAAAAAAAA")
            .apply(menu);

        Sponge.getCommandManager().register(this, CommandSpec.builder()
            .description(Text.of("hhh"))
            .executor((src, args) -> {
                if (src instanceof Player) menu.open((Player) src);
                return CommandResult.success();
            })
            .build(), "hhh");

        Sponge.getCommandManager().register(this, CommandSpec.builder()
            .description(Text.of("create"))
            .executor((src, args) -> {
                if (src instanceof Player) boxboy.createMenu(3, Text.of("throwaway")).open((Player) src);
                return CommandResult.success();
            })
            .build(), "create");

        Sponge.getCommandManager().register(this, CommandSpec.builder()
            .description(Text.of("manualgc"))
            .executor((src, args) -> {
                src.sendMessage(Text.of(String.format("There are %s menus active.", Menu.menus.size())));
                System.gc();

                src.sendMessage(Text.of(String.format("GC complete. There are %s menus active.", Menu.menus.size())));
                return CommandResult.success();
            }).build(), "manualgc");

        Sponge.getCommandManager().register(this, CommandSpec.builder()
            .description(Text.of("aaa"))
            .executor((src, args) -> {
                Sponge.getServer().getOnlinePlayers().forEach(p ->
                    Sponge.getServer().getBroadcastChannel().send(Text.of(p.getName() + ": " + menu.isViewingMenu(p))));
                return CommandResult.success();
            })
            .build(), "aaa");

        Sponge.getCommandManager().register(this, CommandSpec.builder()
            .description(Text.of("forceclose"))
            .executor((src, args) -> {
                Sponge.getServer().getOnlinePlayers().forEach(Player::closeInventory);
                return CommandResult.success();
            })
            .build(), "forceclose");
    }

}
