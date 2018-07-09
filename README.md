# Boxboy

Inventory library for SpongePowered plugins.

# Setup

The Boxboy dependency can be simply added to the dependencies block. Maven Central should be a repository.

```gradle
repositories {
    mavenCentral()
}

dependencies {
    compile('com.github.xemiru.sponge:boxboy:1.0-fix1')
}
```

# Usage

## Creating a Menu

In general, plugins providing an API are guaranteed to be accessible as soon as the `GamePostInitializationEvent` fires. When that event fires, the Boxboy instance can be accessed through a call to `Boxboy.get()`.

You have two types of menus you can create through the singleton Boxboy instance.

A `Menu` is a standard menu that only uses the top inventory and will let the player interact normally with their own inventory on the bottom of the inventory view.

An `ExtendedMenu` is a menu that uses both the top and bottom inventories. The viewing players' inventories are temporarily stored away while viewing the menu and will be returned when the menu is closed. It is still a `Menu`, and does not actually have any extra visible methods.

Both types can be created through the Boxboy instance. The top inventory can be configured to be of different sizes or different inventory types.

```java
Menu menu = Boxboy.get().createMenu(3, Text.of("title"));
Menu extended = Boxboy.get().createExtendedMenu(3, Text.of("title"));

Menu hopper = Boxboy.get().createMenu(InventoryArchetypes.HOPPER, Text.of("title"));
```

## Menu Patterns

While you can simply set the `Button`s of a `Menu` through calls to `Menu.setButton(int, Button)`, it is possible to use a `MenuPattern` instead to prepare them using templates.

```java
Menu myMenu = Boxboy.get().createMenu(3, Text.of("title"));

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

# Version History

|Version|Summary|
|:--|:--|
|1.0|Initial release.|
|1.0-fix1|Let's be a plugin, instead of a shaded library.|
