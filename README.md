# Boxboy

Inventory library for SpongePowered plugins.

# Setup

This library is meant to be shaded into your plugin. This setup uses this [shadow jar plugin](http://imperceptiblethoughts.com/shadow/) to accomplish this.

```gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.github.jengelman.gradle.plugins:shadow:2.0.4'
    }
}

apply plugin: 'com.github.johnrengelman.shadow'
apply plugin: 'java'
```

You can also use the Gradle Plugin syntax instead.

```
plugins {
    // ..

    id 'com.github.johnrengelman.shadow' version '2.0.4'
}
```

The `shadowJar` task can then be configured. You can choose to relocate the packages to avoid version conflicts with other plugins.

```gradle
shadowJar {
    relocate 'com.github.xemiru.sponge.boxboy', 'my.plugin.package.boxboy'
}
```

The Boxboy dependency can be added. It must be marked non-transitive so as to disallow the plugin from pulling Boxboy's dependency on the SpongeAPI into your final jar.

```
dependencies {
    compile('com.github.xemiru.sponge:boxboy:1.0') { transitive = false }
}
```

# Usage
## Setup

Boxboy intends to be used as an addition to your plugin. You can create your own instance of Boxboy to access all of its functionality.

Upon instantiation, Boxboy will attempt to register its tasks and events under your plugin. An error will occur if you try to instantiate Boxboy before the `GamePreinitializationEvent`.

```java
// (in your main plugin class)

@Inject private Game game;

private Boxboy boxboy;

// ..

@Listener
public void onPreinit(GamePreinitializationEvent e) {
    this.boxboy = new Boxboy(this, game);

    // ..
}
```

## Creating a Menu

You have two types of menus you can create through the instantiated Boxboy instance.

A `Menu` is a standard menu that only uses the top inventory and will let the player interact normally with their own inventory on the bottom of the inventory view.

An `ExtendedMenu` is a menu that uses both the top and bottom inventories. The viewing players' inventories are temporarily stored away while viewing the menu and will be returned when the menu is closed. It is still a `Menu`, and does not actually have any extra visible methods.

Both types can be created through the Boxboy instance. The top inventory can be configured to be of different sizes or different inventory types.

```java
Menu menu = this.boxboy.createMenu(3, Text.of("title"));
Menu extended = this.boxboy.createExtendedMenu(3, Text.of("title"));

Menu hopper = this.boxboy.createMenu(InventoryArchetypes.HOPPER, Text.of("title"));
```

## Menu Patterns

While you can simply set the `Button`s of a `Menu` through calls to `Menu.setButton(int, Button)`, it is possible to use a `MenuPattern` instead to prepare them using templates.

```java
Menu myMenu = this.boxboy.createMenu(3, Text.of("title"));

new MenuPattern()
    .setButton('A', DummyButton.of(ItemStack.of(ItemTypes.STAINED_GLASS_PANE, 1))
    .setPattern("AAAAAAAAA",
                "A       A",
                "AAAAAAAAA")
    .apply(myMenu);
```

## Buttons

There're five stock `Button` classes that Boxboy provides, all of which generally cover most use cases.

|Button Type|Description|
|:--|:--|
|Dummy|Has no functionality and is purely to display an item.|
|Action|Performs a single action when clicked.|
|Toggle|Switches between two states. Internally uses `Action` buttons; the button representing the state being switched to is executed.|
|Scroll|Switches between multiple states. Internally uses `Action` buttons; the button representing the state being switched to is executed.|
|Slot|Pretends to be a functional inventory slot. Executes actions when the item in the slot is changed.|

Should none of these buttons cover a specific need, a custom `Button` implementation can be written. The methods listed below are methods to be implemented (italicized methods are optional).

|Method|Description|
|:--|:--|
|_isAnimated_|Return whether or not the Button is animated.|
|_offer_|Perform an action when an item is offered to the slot by a viewing Player.|
|_onClick_|Perform an action when the button is clicked.|
|getRepresentative|Returns the `ItemStack` representing the button in a `Menu`.|

A button should implement either `offer` or `onClick` to have functionality.
