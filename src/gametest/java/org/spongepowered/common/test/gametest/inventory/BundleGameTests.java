/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.test.gametest.inventory;

import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.GameType;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ContainerTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestAssertions;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.common.test.gametest.SpongeGameTests;
import org.spongepowered.plugin.PluginContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Selecting an item in a bundle, as a client does by scrolling over it, throws {@link ChangeInventoryEvent.ValueChange}
 * for {@link Keys#SLOT_INDEX}, and the bundle's contents are exposed through {@link Keys#INVENTORY},
 * {@link Keys#SLOT_INDEX} and {@link Keys#SLOT_ITEM_STACK_SNAPSHOT}.
 *
 * <p>The selection goes through {@code AbstractContainerMenu#setSelectedBundleItemIndex}, which is what the server
 * calls for a {@code ServerboundSelectBundleItemPacket}. The event is thrown right away rather than captured, so the
 * tests assert on the same tick. Listeners filter by the test's own mock player's menu, since tests run side by side.</p>
 */
@GameTestGroup("bundle")
public final class BundleGameTests {

    // The first hotbar slot in a player's InventoryMenu
    private static final int HOTBAR_0 = InventoryMenu.USE_ROW_SLOT_START;
    // Where the bundle is in the plugin menu tests
    private static final int MENU_SLOT = 4;

    @GameTest
    void selectingThrowsValueChange(final GameTestHelper helper) {
        final Player player = BundleGameTests.playerWithBundle(helper, 3);
        final List<ChangeInventoryEvent.ValueChange> events = BundleGameTests.select(player, 1, event -> {});

        GameTestAssertions.assertValueEqual(helper, events.size(), 1, "number of ValueChange events");
        final ChangeInventoryEvent.ValueChange event = events.getFirst();
        GameTestAssertions.assertTrue(helper, event.affectedKeys().contains(Keys.SLOT_INDEX), "SLOT_INDEX is not in the affected keys " + event.affectedKeys());
        GameTestAssertions.assertValueEqual(helper, event.transactions().size(), 1, "number of transactions");
        final SlotTransaction transaction = event.transactions().getFirst();
        GameTestAssertions.assertValueEqual(helper, transaction.original().get(Keys.SLOT_INDEX).orElse(-1), -1, "original SLOT_INDEX");
        GameTestAssertions.assertValueEqual(helper, transaction.finalReplacement().get(Keys.SLOT_INDEX).orElse(-1), 1, "final SLOT_INDEX");
        GameTestAssertions.assertValueEqual(helper, BundleGameTests.selected(player), 1, "selected index of the bundle");
        helper.succeed();
    }

    @GameTest
    void reselectingDeselects(final GameTestHelper helper) {
        final Player player = BundleGameTests.playerWithBundle(helper, 3);
        player.inventoryMenu.setSelectedBundleItemIndex(BundleGameTests.HOTBAR_0, 2);
        // Vanilla toggles: selecting the selected item again selects nothing
        final List<ChangeInventoryEvent.ValueChange> events = BundleGameTests.select(player, 2, event -> {});

        GameTestAssertions.assertValueEqual(helper, events.size(), 1, "number of ValueChange events");
        final SlotTransaction transaction = events.getFirst().transactions().getFirst();
        GameTestAssertions.assertValueEqual(helper, transaction.original().get(Keys.SLOT_INDEX).orElse(-1), 2, "original SLOT_INDEX");
        GameTestAssertions.assertFalse(helper, transaction.finalReplacement().get(Keys.SLOT_INDEX).isPresent(), "final SLOT_INDEX is present");
        GameTestAssertions.assertValueEqual(helper, BundleGameTests.selected(player), -1, "selected index of the bundle");
        helper.succeed();
    }

    @GameTest
    void cancellingDeselects(final GameTestHelper helper) {
        final Player player = BundleGameTests.playerWithBundle(helper, 3);
        final List<ChangeInventoryEvent.ValueChange> events = BundleGameTests.select(player, 1, event -> event.setCancelled(true));

        GameTestAssertions.assertValueEqual(helper, events.size(), 1, "number of ValueChange events");
        GameTestAssertions.assertValueEqual(helper, BundleGameTests.selected(player), -1, "selected index of the bundle");
        helper.succeed();
    }

    // The selected index is not part of the bundle's synced data, so the server cannot put the client back on an
    // earlier selection: cancelling clears the selection even when something was selected before
    @GameTest
    void cancellingClearsPreviousSelection(final GameTestHelper helper) {
        final Player player = BundleGameTests.playerWithBundle(helper, 3);
        player.inventoryMenu.setSelectedBundleItemIndex(BundleGameTests.HOTBAR_0, 0);
        BundleGameTests.select(player, 2, event -> event.setCancelled(true));

        GameTestAssertions.assertValueEqual(helper, BundleGameTests.selected(player), -1, "selected index of the bundle");
        helper.succeed();
    }

    @GameTest
    void customReplacementIsApplied(final GameTestHelper helper) {
        final Player player = BundleGameTests.playerWithBundle(helper, 3);
        final List<net.kyori.adventure.text.Component> lore = List.of(net.kyori.adventure.text.Component.text("scrolled"));
        BundleGameTests.select(player, 1, event -> event.transactions().forEach(transaction -> {
            final ItemStack custom = transaction.finalReplacement().asMutableCopy();
            custom.offer(Keys.LORE, lore);
            transaction.setCustom(custom.asImmutable());
        }));

        final ItemStack inSlot = ItemStackUtil.fromNative(player.getInventory().getItem(0));
        GameTestAssertions.assertValueEqual(helper, inSlot.get(Keys.LORE).orElse(List.of()), lore, "lore of the bundle");
        GameTestAssertions.assertValueEqual(helper, inSlot.type(), ItemTypes.BUNDLE.get(), "type of the stack in the slot");
        helper.succeed();
    }

    @GameTest
    void nonBundleThrowsNothing(final GameTestHelper helper) {
        final Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().setItem(0, new net.minecraft.world.item.ItemStack(Items.DIRT, 5));
        final List<ChangeInventoryEvent.ValueChange> events = BundleGameTests.select(player, 1, event -> {});

        GameTestAssertions.assertTrue(helper, events.isEmpty(), "ValueChange was thrown for a stack that is not a bundle");
        GameTestAssertions.assertValueEqual(helper, player.getInventory().getItem(0).getCount(), 5, "count of the dirt stack");
        helper.succeed();
    }

    @GameTest
    void inventoryKeyReadsContents(final GameTestHelper helper) {
        final net.minecraft.world.item.ItemStack nativeBundle = BundleGameTests.bundle(2);

        final Inventory inventory = ItemStackUtil.fromNative(nativeBundle).get(Keys.INVENTORY).orElse(null);
        GameTestAssertions.assertTrue(helper, inventory != null, "a bundle has no INVENTORY");
        GameTestAssertions.assertValueEqual(helper, inventory.capacity(), 2, "capacity of the bundle's INVENTORY");
        GameTestAssertions.assertValueEqual(helper, inventory.peekAt(0).map(ItemStack::type).orElse(null), ItemTypes.DIRT.get(), "type in slot 0");
        GameTestAssertions.assertValueEqual(helper, inventory.peekAt(1).map(ItemStack::type).orElse(null), ItemTypes.STONE.get(), "type in slot 1");

        // A copy: changing it leaves the bundle alone
        inventory.set(0, ItemStack.of(ItemTypes.DIAMOND));
        GameTestAssertions.assertValueEqual(helper, nativeBundle.get(DataComponents.BUNDLE_CONTENTS).getItemUnsafe(0).getItem(), Items.DIRT,
            "bundle's first item after changing the read INVENTORY");
        helper.succeed();
    }

    // An inventory cannot have no slots, so an empty bundle reads as one empty slot
    @GameTest
    void inventoryKeyReadsEmptyBundle(final GameTestHelper helper) {
        final Inventory inventory = ItemStack.of(ItemTypes.BUNDLE).get(Keys.INVENTORY).orElse(null);
        GameTestAssertions.assertTrue(helper, inventory != null, "an empty bundle has no INVENTORY");
        GameTestAssertions.assertValueEqual(helper, inventory.capacity(), 1, "capacity of an empty bundle's INVENTORY");
        GameTestAssertions.assertTrue(helper, inventory.peekAt(0).map(ItemStack::isEmpty).orElse(true), "slot 0 of an empty bundle is not empty");
        helper.succeed();
    }

    @GameTest
    void inventoryKeyRemoves(final GameTestHelper helper) {
        final net.minecraft.world.item.ItemStack nativeBundle = BundleGameTests.bundle(2);

        GameTestAssertions.assertTrue(helper, ItemStackUtil.fromNative(nativeBundle).remove(Keys.INVENTORY).isSuccessful(), "removing INVENTORY failed");
        GameTestAssertions.assertFalse(helper, nativeBundle.has(DataComponents.BUNDLE_CONTENTS), "the bundle still has contents");
        GameTestAssertions.assertFalse(helper, ItemStackUtil.fromNative(nativeBundle).supports(Keys.INVENTORY), "INVENTORY is supported without contents");
        helper.succeed();
    }

    @GameTest
    void inventoryKeyNotOnOtherItems(final GameTestHelper helper) {
        GameTestAssertions.assertFalse(helper, ItemStack.of(ItemTypes.DIRT).supports(Keys.INVENTORY), "dirt supports INVENTORY");
        helper.succeed();
    }

    /*
     * Vanilla only rejects contents whose weight cannot be counted. A stack weighs its count over its max stack size,
     * and slots cap the count, so the way to get there is a sum of fractions whose common denominator overflows an int:
     * single items whose max stack sizes are distinct primes.
     */
    @GameTest
    void inventoryKeyRejectsUncountableWeight(final GameTestHelper helper) {
        final ItemStack bundle = ItemStack.of(ItemTypes.BUNDLE);
        // 2 * 3 * ... * 29 is past Integer.MAX_VALUE
        final int[] primes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29};
        final Inventory contents = Inventory.builder().slots(primes.length).completeStructure().plugin(BundleGameTests.plugin()).build();
        for (int i = 0; i < primes.length; i++) {
            contents.set(i, ItemStackUtil.fromNative(BundleGameTests.stackingTo(primes[i])));
        }

        GameTestAssertions.assertFalse(helper, bundle.offer(Keys.INVENTORY, contents).isSuccessful(), "offering contents of uncountable weight succeeded");
        GameTestAssertions.assertTrue(helper, ItemStackUtil.toNative(bundle).get(DataComponents.BUNDLE_CONTENTS).isEmpty(), "the bundle changed");
        helper.succeed();
    }

    @GameTest
    void selectedSlotKeyRejectsUncountableWeight(final GameTestHelper helper) {
        // 3 * 5 * ... * 23 still fits an int; with the 3 swapped for a 97 it does not
        final List<net.minecraft.world.item.ItemStack> items = new ArrayList<>();
        for (final int prime : new int[] {3, 5, 7, 11, 13, 17, 19, 23}) {
            items.add(BundleGameTests.stackingTo(prime));
        }
        final net.minecraft.world.item.ItemStack nativeBundle = new net.minecraft.world.item.ItemStack(Items.BUNDLE);
        nativeBundle.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(items));
        BundleItem.toggleSelectedItem(nativeBundle, 0);

        GameTestAssertions.assertFalse(helper,
            ItemStackUtil.fromNative(nativeBundle).offer(Keys.SLOT_ITEM_STACK_SNAPSHOT, ItemStackUtil.snapshotOf(BundleGameTests.stackingTo(97))).isSuccessful(),
            "offering a selected item of uncountable weight succeeded");
        GameTestAssertions.assertValueEqual(helper, nativeBundle.get(DataComponents.BUNDLE_CONTENTS).getItemUnsafe(0).getMaxStackSize(), 3,
            "max stack size of the bundle's selected item");
        helper.succeed();
    }

    @GameTest
    void inventoryKeyWritesContents(final GameTestHelper helper) {
        final ItemStack bundle = ItemStack.of(ItemTypes.BUNDLE);
        final Inventory contents = Inventory.builder().slots(2).completeStructure().plugin(BundleGameTests.plugin()).build();
        contents.set(0, ItemStack.of(ItemTypes.EMERALD, 3));
        contents.set(1, ItemStack.of(ItemTypes.GOLD_INGOT, 4));

        GameTestAssertions.assertTrue(helper, bundle.offer(Keys.INVENTORY, contents).isSuccessful(), "offering INVENTORY to a bundle failed");

        final BundleContents written = ItemStackUtil.toNative(bundle).get(DataComponents.BUNDLE_CONTENTS);
        GameTestAssertions.assertValueEqual(helper, written.size(), 2, "number of stacks in the bundle");
        GameTestAssertions.assertValueEqual(helper, written.getItemUnsafe(0).getItem(), Items.EMERALD, "bundle's first item");
        GameTestAssertions.assertValueEqual(helper, written.getItemUnsafe(1).getCount(), 4, "count of the bundle's second item");
        helper.succeed();
    }

    @GameTest
    void selectedSlotKeys(final GameTestHelper helper) {
        final net.minecraft.world.item.ItemStack nativeBundle = BundleGameTests.bundle(2);
        final ItemStack bundle = ItemStackUtil.fromNative(nativeBundle);
        GameTestAssertions.assertFalse(helper, bundle.get(Keys.SLOT_INDEX).isPresent(), "SLOT_INDEX is present with nothing selected");
        GameTestAssertions.assertFalse(helper, bundle.get(Keys.SLOT_ITEM_STACK_SNAPSHOT).isPresent(), "SLOT_ITEM_STACK_SNAPSHOT is present with nothing selected");
        GameTestAssertions.assertFalse(helper, bundle.offer(Keys.SLOT_ITEM_STACK_SNAPSHOT, ItemStack.of(ItemTypes.DIAMOND).asImmutable()).isSuccessful(),
            "offering SLOT_ITEM_STACK_SNAPSHOT succeeded with nothing selected");

        BundleItem.toggleSelectedItem(nativeBundle, 1);
        GameTestAssertions.assertValueEqual(helper, bundle.get(Keys.SLOT_INDEX).orElse(-1), 1, "SLOT_INDEX");
        final ItemStackSnapshot selected = bundle.get(Keys.SLOT_ITEM_STACK_SNAPSHOT).orElse(ItemStackSnapshot.empty());
        GameTestAssertions.assertTrue(helper, selected.type() == ItemTypes.STONE.get(), "SLOT_ITEM_STACK_SNAPSHOT is " + selected.type() + ", not stone");

        GameTestAssertions.assertTrue(helper, bundle.offer(Keys.SLOT_ITEM_STACK_SNAPSHOT, ItemStack.of(ItemTypes.DIAMOND).asImmutable()).isSuccessful(),
            "offering SLOT_ITEM_STACK_SNAPSHOT failed");
        final BundleContents contents = nativeBundle.get(DataComponents.BUNDLE_CONTENTS);
        GameTestAssertions.assertValueEqual(helper, contents.getItemUnsafe(1).getItem(), Items.DIAMOND, "bundle's selected item after the offer");
        GameTestAssertions.assertValueEqual(helper, contents.getItemUnsafe(0).getItem(), Items.DIRT, "bundle's other item after the offer");
        helper.succeed();
    }

    @GameTest
    void menuChangeHandlerSeesSelection(final GameTestHelper helper) {
        BundleGameTests.withMenu(helper, (player, container, recorded) -> {
            final List<ChangeInventoryEvent.ValueChange> events = BundleGameTests.select(container, BundleGameTests.MENU_SLOT, 1, event -> {});

            GameTestAssertions.assertValueEqual(helper, recorded.size(), 1, "number of menu change callbacks");
            GameTestAssertions.assertValueEqual(helper, recorded.getFirst().get(Keys.SLOT_INDEX).orElse(-1), 1, "SLOT_INDEX the change handler saw");
            GameTestAssertions.assertValueEqual(helper, events.size(), 1, "number of ValueChange events");
            GameTestAssertions.assertValueEqual(helper, BundleItem.getSelectedItem(container.getSlot(BundleGameTests.MENU_SLOT).getItem()), 1, "selected index of the bundle");
        }, true);
        helper.succeed();
    }

    @GameTest
    void menuChangeHandlerCanReject(final GameTestHelper helper) {
        BundleGameTests.withMenu(helper, (player, container, recorded) -> {
            final List<ChangeInventoryEvent.ValueChange> events = BundleGameTests.select(container, BundleGameTests.MENU_SLOT, 1, event -> {});

            GameTestAssertions.assertValueEqual(helper, recorded.size(), 1, "number of menu change callbacks");
            GameTestAssertions.assertTrue(helper, events.isEmpty(), "ValueChange was thrown after the menu rejected the change");
            GameTestAssertions.assertValueEqual(helper, BundleItem.getSelectedItem(container.getSlot(BundleGameTests.MENU_SLOT).getItem()), -1, "selected index of the bundle");
        }, false);
        helper.succeed();
    }

    // A player with a connection has a synchronizer, which is told about the deselection
    @GameTest
    void cancellingInOpenMenuDeselects(final GameTestHelper helper) {
        BundleGameTests.withMenu(helper, (player, container, recorded) -> {
            final List<ChangeInventoryEvent.ValueChange> events = BundleGameTests.select(container, BundleGameTests.MENU_SLOT, 1, event -> event.setCancelled(true));

            GameTestAssertions.assertValueEqual(helper, events.size(), 1, "number of ValueChange events");
            GameTestAssertions.assertValueEqual(helper, BundleItem.getSelectedItem(container.getSlot(BundleGameTests.MENU_SLOT).getItem()), -1, "selected index of the bundle");
        }, true);
        helper.succeed();
    }

    /** A dirt, stone, ... bundle of {@code size} single items */
    private static net.minecraft.world.item.ItemStack bundle(final int size) {
        final List<net.minecraft.world.item.ItemStack> items = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            items.add(new net.minecraft.world.item.ItemStack(i % 2 == 0 ? Items.DIRT : Items.STONE));
        }
        final net.minecraft.world.item.ItemStack bundle = new net.minecraft.world.item.ItemStack(Items.BUNDLE);
        bundle.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(items));
        return bundle;
    }

    /** A single dirt that stacks to {@code size}, so it weighs 1/{@code size} in a bundle */
    private static net.minecraft.world.item.ItemStack stackingTo(final int size) {
        final net.minecraft.world.item.ItemStack dirt = new net.minecraft.world.item.ItemStack(Items.DIRT);
        dirt.set(DataComponents.MAX_STACK_SIZE, size);
        return dirt;
    }

    private static PluginContainer plugin() {
        return Sponge.game().platform().container(Platform.Component.IMPLEMENTATION);
    }

    private interface MenuTest {
        void run(ServerPlayer player, AbstractContainerMenu container, List<ItemStackSnapshot> recorded);
    }

    /**
     * Opens a plugin's single row menu with a bundle in {@link #MENU_SLOT} for a new player, whose change handler
     * records each new stack and returns {@code accept}, and closes it and removes the player afterwards.
     */
    @SuppressWarnings("removal")
    private static void withMenu(final GameTestHelper helper, final MenuTest test, final boolean accept) {
        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            final Inventory row = Inventory.builder().slots(9).completeStructure().plugin(BundleGameTests.plugin()).build();
            final ViewableInventory inventory = ViewableInventory.builder().type(ContainerTypes.GENERIC_9X1)
                .slots(row.slots(), 0)
                .completeStructure()
                .plugin(BundleGameTests.plugin())
                .build();
            inventory.set(BundleGameTests.MENU_SLOT, ItemStackUtil.fromNative(BundleGameTests.bundle(3)));
            final org.spongepowered.api.item.inventory.menu.InventoryMenu menu = inventory.asMenu();
            final List<ItemStackSnapshot> recorded = new ArrayList<>();
            menu.registerChange((cause, container, slot, slotIndex, oldStack, newStack) -> {
                if (slotIndex == BundleGameTests.MENU_SLOT) {
                    recorded.add(newStack);
                }
                return accept;
            });
            final Container opened = menu.open((org.spongepowered.api.entity.living.player.server.ServerPlayer) player).orElse(null);
            GameTestAssertions.assertTrue(helper, opened != null, "the menu did not open");

            test.run(player, (AbstractContainerMenu) opened, recorded);
        } finally {
            player.closeContainer();
            player.level().getServer().getPlayerList().remove(player);
        }
    }

    private static Player playerWithBundle(final GameTestHelper helper, final int size) {
        final Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().setItem(0, BundleGameTests.bundle(size));
        return player;
    }

    private static int selected(final Player player) {
        return BundleItem.getSelectedItem(player.getInventory().getItem(0));
    }

    private static List<ChangeInventoryEvent.ValueChange> select(final Player player, final int index, final Consumer<ChangeInventoryEvent.ValueChange> handler) {
        return BundleGameTests.select(player.inventoryMenu, BundleGameTests.HOTBAR_0, index, handler);
    }

    /**
     * Selects {@code index} in the bundle in the menu's {@code slot}, and returns every
     * {@link ChangeInventoryEvent.ValueChange} thrown for the menu meanwhile, after {@code handler} has seen it.
     */
    private static List<ChangeInventoryEvent.ValueChange> select(final AbstractContainerMenu menu, final int slot, final int index,
        final Consumer<ChangeInventoryEvent.ValueChange> handler) {
        final List<ChangeInventoryEvent.ValueChange> events = new ArrayList<>();
        final EventListener<ChangeInventoryEvent.ValueChange> listener = event -> {
            if (event.inventory() == menu) {
                handler.accept(event);
                events.add(event);
            }
        };
        SpongeGameTests.listen(ChangeInventoryEvent.ValueChange.class, listener);
        try {
            menu.setSelectedBundleItemIndex(slot, index);
        } finally {
            Sponge.eventManager().unregisterListeners(listener);
        }
        return events;
    }
}
