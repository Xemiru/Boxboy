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
            .setPattern("AAAAAAAAA",
                "A  DBC  A",
                "AAA E AAA",
                "AAAAAAAAA",
                "AAAAAAAAA",
                "AAAACAAAA",
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
            .description(Text.of("aaa"))
            .executor((src, args) -> {
                Sponge.getServer().getOnlinePlayers().forEach(p ->
                    Sponge.getServer().getBroadcastChannel().send(Text.of(p.getName() + ": " + menu.hasMenuOpen(p))));
                return CommandResult.success();
            })
            .build(), "aaa");
    }

}
