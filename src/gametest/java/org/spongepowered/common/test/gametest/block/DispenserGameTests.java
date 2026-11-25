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
package org.spongepowered.common.test.gametest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.common.test.gametest.SpongeGameTests;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The {@link DropItemEvent.Pre} a dispenser throws for the items it drops, and the {@link ChangeInventoryEvent} for the
 * slot it took the item from.
 *
 * <p>{@code sponge:dispenser/facing_up}: on a 3x3 bedrock floor, an empty upward-facing dispenser at (1,1,1) with air
 * above it, so a dropped item stays inside the test. Each test loads the dispenser and powers it with a redstone block
 * at (1,1,0).</p>
 */
@GameTestGroup("dispenser")
public final class DispenserGameTests {

    private static final String STRUCTURE = "sponge:dispenser/facing_up";
    private static final BlockPos DISPENSER = new BlockPos(1, 1, 1);
    private static final BlockPos POWER = new BlockPos(1, 1, 0);

    @GameTest(structure = DispenserGameTests.STRUCTURE, maxTicks = 40)
    void dropEventNamesDroppedItem(final GameTestHelper helper) {
        final DispenserBlockEntity dispenser = DispenserGameTests.load(helper, Items.DIAMOND);
        final List<ItemStackSnapshot> dropped = new CopyOnWriteArrayList<>();
        final AtomicBoolean beforeDispense = new AtomicBoolean();
        final EventListener<DropItemEvent.Pre> listener = event -> {
            if (event.cause().contains(dispenser)) {
                dropped.addAll(event.droppedItems());
                beforeDispense.set(!dispenser.getItem(0).isEmpty() && DispenserGameTests.drops(helper).isEmpty());
            }
        };
        SpongeGameTests.listen(DropItemEvent.Pre.class, listener);

        helper.setBlock(DispenserGameTests.POWER, Blocks.REDSTONE_BLOCK);

        helper.succeedWhen(() -> {
            helper.assertTrue(dropped.size() == 1, "Expected one dropped item in DropItemEvent.Pre but got " + dropped);
            helper.assertTrue(dropped.get(0).type() == ItemTypes.DIAMOND.get(), "DropItemEvent.Pre named " + dropped.get(0) + ", not the diamond");
            helper.assertTrue(beforeDispense.get(), "DropItemEvent.Pre was thrown after the diamond left the dispenser");
            helper.assertFalse(DispenserGameTests.drops(helper).isEmpty(), "The dispenser dropped no item");
            helper.assertContainerEmpty(DispenserGameTests.DISPENSER);
            Sponge.eventManager().unregisterListeners(listener);
        });
    }

    // Armor's behavior overrides execute, and with nobody in front to wear it, drops it through super.execute
    @GameTest(structure = DispenserGameTests.STRUCTURE, maxTicks = 40)
    void overriddenBehaviorFallbackThrowsDropEvent(final GameTestHelper helper) {
        final DispenserBlockEntity dispenser = DispenserGameTests.load(helper, Items.IRON_CHESTPLATE);
        final List<ItemStackSnapshot> dropped = new CopyOnWriteArrayList<>();
        final EventListener<DropItemEvent.Pre> listener = event -> {
            if (event.cause().contains(dispenser)) {
                dropped.addAll(event.droppedItems());
            }
        };
        SpongeGameTests.listen(DropItemEvent.Pre.class, listener);

        helper.setBlock(DispenserGameTests.POWER, Blocks.REDSTONE_BLOCK);

        helper.succeedWhen(() -> {
            helper.assertTrue(dropped.size() == 1, "Expected one dropped item in DropItemEvent.Pre but got " + dropped);
            helper.assertTrue(dropped.get(0).type() == ItemTypes.IRON_CHESTPLATE.get(), "DropItemEvent.Pre named " + dropped.get(0) + ", not the chestplate");
            helper.assertFalse(DispenserGameTests.drops(helper).isEmpty(), "The dispenser dropped no item");
            Sponge.eventManager().unregisterListeners(listener);
        });
    }

    // The event is thrown before the item leaves the dispenser, so cancelling it means no item entity is ever created
    @GameTest(structure = DispenserGameTests.STRUCTURE, maxTicks = 40)
    void cancelledDropKeepsItemInDispenser(final GameTestHelper helper) {
        final DispenserBlockEntity dispenser = DispenserGameTests.load(helper, Items.DIAMOND);
        final AtomicBoolean cancelled = new AtomicBoolean();
        final EventListener<DropItemEvent.Pre> listener = event -> {
            if (event.cause().contains(dispenser)) {
                event.setCancelled(true);
                cancelled.set(true);
            }
        };
        SpongeGameTests.listen(DropItemEvent.Pre.class, listener);

        helper.setBlock(DispenserGameTests.POWER, Blocks.REDSTONE_BLOCK);

        helper.startSequence()
            .thenWaitUntil(() -> helper.assertTrue(cancelled.get(), "No DropItemEvent.Pre from the dispenser"))
            .thenExecuteFor(10, () -> {
                helper.assertContainerContains(DispenserGameTests.DISPENSER, Items.DIAMOND);
                helper.assertTrue(DispenserGameTests.drops(helper).isEmpty(), "A cancelled drop is still in the world");
            })
            .thenExecute(() -> Sponge.eventManager().unregisterListeners(listener))
            .thenSucceed();
    }

    // Shooting an arrow drops no item, but the arrow still leaves its slot
    @GameTest(structure = DispenserGameTests.STRUCTURE, maxTicks = 40, skyAccess = true)
    void shotArrowThrowsInventoryEventAndNoDropEvent(final GameTestHelper helper) {
        final DispenserBlockEntity dispenser = DispenserGameTests.load(helper, Items.ARROW);
        final AtomicBoolean dropped = new AtomicBoolean();
        final List<SlotTransaction> changes = new CopyOnWriteArrayList<>();
        final EventListener<DropItemEvent.Pre> dropListener = event -> {
            if (event.cause().contains(dispenser)) {
                dropped.set(true);
            }
        };
        final EventListener<ChangeInventoryEvent> changeListener = event -> {
            if (event.cause().contains(dispenser)) {
                changes.addAll(event.transactions());
            }
        };
        SpongeGameTests.listen(DropItemEvent.Pre.class, dropListener);
        SpongeGameTests.listen(ChangeInventoryEvent.class, changeListener);

        helper.setBlock(DispenserGameTests.POWER, Blocks.REDSTONE_BLOCK);

        helper.startSequence()
            .thenWaitUntil(() -> helper.assertContainerEmpty(DispenserGameTests.DISPENSER))
            .thenExecute(() -> {
                helper.assertFalse(dropped.get(), "DropItemEvent.Pre thrown for a shot arrow");
                helper.assertTrue(changes.size() == 1, "Expected one slot change in ChangeInventoryEvent but got " + changes);
                helper.assertTrue(changes.get(0).original().type() == ItemTypes.ARROW.get(), "The slot change did not start from the arrow: " + changes.get(0));
                helper.assertTrue(changes.get(0).finalReplacement().isEmpty(), "The slot change did not end empty: " + changes.get(0));
                Sponge.eventManager().unregisterListeners(dropListener);
                Sponge.eventManager().unregisterListeners(changeListener);
            })
            .thenSucceed();
    }

    // The arrow is already in the world by the time the event is thrown, so cancelling puts the item back and discards it
    @GameTest(structure = DispenserGameTests.STRUCTURE, maxTicks = 40, skyAccess = true)
    void cancelledInventoryChangeUndoesShot(final GameTestHelper helper) {
        final DispenserBlockEntity dispenser = DispenserGameTests.load(helper, Items.ARROW);
        final AABB around = new AABB(helper.absolutePos(DispenserGameTests.DISPENSER)).inflate(3);
        final AtomicBoolean cancelled = new AtomicBoolean();
        final EventListener<ChangeInventoryEvent> listener = event -> {
            if (event.cause().contains(dispenser)) {
                event.setCancelled(true);
                cancelled.set(true);
            }
        };
        SpongeGameTests.listen(ChangeInventoryEvent.class, listener);

        helper.setBlock(DispenserGameTests.POWER, Blocks.REDSTONE_BLOCK);

        helper.startSequence()
            .thenWaitUntil(() -> helper.assertTrue(cancelled.get(), "No ChangeInventoryEvent from the dispenser"))
            .thenExecute(() -> {
                helper.assertContainerContains(DispenserGameTests.DISPENSER, Items.ARROW);
                helper.assertTrue(helper.getLevel().getEntitiesOfClass(AbstractArrow.class, around).isEmpty(), "A cancelled shot is still in the world");
                Sponge.eventManager().unregisterListeners(listener);
            })
            .thenSucceed();
    }

    private static List<ItemEntity> drops(final GameTestHelper helper) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(DispenserGameTests.DISPENSER)).inflate(3));
    }

    private static DispenserBlockEntity load(final GameTestHelper helper, final Item item) {
        final DispenserBlockEntity dispenser = helper.getBlockEntity(DispenserGameTests.DISPENSER);
        dispenser.setItem(0, new ItemStack(item));
        return dispenser;
    }
}
