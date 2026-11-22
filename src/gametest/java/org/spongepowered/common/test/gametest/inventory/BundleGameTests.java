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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.GameType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestAssertions;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.common.test.gametest.SpongeGameTests;

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
    void inventoryKeyWritesContents(final GameTestHelper helper) {
        final ItemStack bundle = ItemStack.of(ItemTypes.BUNDLE);
        final Inventory contents = Inventory.builder().slots(2).completeStructure().plugin(Sponge.game().platform().container(org.spongepowered.api.Platform.Component.IMPLEMENTATION)).build();
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

    private static Player playerWithBundle(final GameTestHelper helper, final int size) {
        final Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().setItem(0, BundleGameTests.bundle(size));
        return player;
    }

    private static int selected(final Player player) {
        return BundleItem.getSelectedItem(player.getInventory().getItem(0));
    }

    /**
     * Selects {@code index} in the bundle in the player's first hotbar slot, and returns every
     * {@link ChangeInventoryEvent.ValueChange} thrown for the player's inventory menu meanwhile, after {@code handler}
     * has seen it.
     */
    private static List<ChangeInventoryEvent.ValueChange> select(final Player player, final int index, final Consumer<ChangeInventoryEvent.ValueChange> handler) {
        final List<ChangeInventoryEvent.ValueChange> events = new ArrayList<>();
        final EventListener<ChangeInventoryEvent.ValueChange> listener = event -> {
            if (event.inventory() == player.inventoryMenu) {
                handler.accept(event);
                events.add(event);
            }
        };
        SpongeGameTests.listen(ChangeInventoryEvent.ValueChange.class, listener);
        try {
            player.inventoryMenu.setSelectedBundleItemIndex(BundleGameTests.HOTBAR_0, index);
        } finally {
            Sponge.eventManager().unregisterListeners(listener);
        }
        return events;
    }
}
