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

import io.leangen.geantyref.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.transaction.NotificationTicket;
import org.spongepowered.api.block.transaction.ScheduleUpdateTicket;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.block.ScheduleBlockUpdateEvent;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.test.gametest.EventRecorder;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.common.test.gametest.SpongeGameTests;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Whether neighbor notifications reach their targets as {@link NotifyNeighborBlockEvent} describes. A redstone lamp is
 * the witness: it only checks its power when a neighbor updates it, so a lamp that changes was updated.
 *
 * <p>{@code sponge:notify/lever_lamp}: on a bedrock floor, a lever (0,1,1) with an unlit lamp (1,1,1) beside it.</p>
 *
 * <p>{@code sponge:piston/push_lamp}: lever (0,1,2), east-facing piston (1,1,2), stone (2,1,2) and gold block (3,1,2), with
 * a lit lamp and no power at (3,1,3) beside the gold block, so it goes out once anything updates it. Those tests have sky
 * access, so no barrier ceiling is placed above the lamp, or removed from above it, around the test.</p>
 *
 * <p>{@code sponge:notify/water_spread}: a bedrock floor ringed by obsidian, empty inside; the test places a water source
 * at (3,1,3).</p>
 *
 * <p>{@code sponge:notify/tnt_water}: stone on each side of (3,1,3), where the test spawns primed tnt, and a water source
 * at (5,1,3) beside the east stone, walled in by obsidian on its other sides and open above.</p>
 */
@GameTestGroup("notify_neighbor")
public final class NeighborNotificationGameTests {

    private static final String LEVER_LAMP = "sponge:notify/lever_lamp";
    private static final BlockPos LEVER = new BlockPos(0, 1, 1);
    private static final BlockPos LAMP = new BlockPos(1, 1, 1);
    private static final String PUSH_LAMP = "sponge:piston/push_lamp";
    private static final BlockPos ROW_LEVER = new BlockPos(0, 1, 2);
    private static final BlockPos ROW_PISTON = new BlockPos(1, 1, 2);
    private static final BlockPos ROW_STONE = new BlockPos(2, 1, 2);
    private static final BlockPos ROW_GOLD = new BlockPos(3, 1, 2);
    private static final BlockPos ROW_LAMP = new BlockPos(3, 1, 3);
    private static final String WATER_SPREAD = "sponge:notify/water_spread";
    private static final BlockPos WATER = new BlockPos(3, 1, 3);
    private static final String TNT_WATER = "sponge:notify/tnt_water";
    private static final BlockPos TNT = new BlockPos(3, 1, 3);
    private static final BlockPos WATER_STONE = new BlockPos(4, 1, 3);
    private static final List<BlockPos> TNT_STONES = List.of(new BlockPos(2, 1, 3), NeighborNotificationGameTests.WATER_STONE, new BlockPos(3, 1, 2), new BlockPos(3, 1, 4));
    private static final BlockPos TNT_WATER_SOURCE = new BlockPos(5, 1, 3);

    // The lever's notification is what lights the lamp
    @GameTest(structure = NeighborNotificationGameTests.LEVER_LAMP)
    void leverLightsLamp(final GameTestHelper helper) {
        final Vector3i lamp = VecHelper.toVector3i(helper.absolutePos(NeighborNotificationGameTests.LAMP));
        final AtomicBoolean ticketed = new AtomicBoolean();
        final EventListener<NotifyNeighborBlockEvent> listener = event -> {
            if (event.tickets().stream().anyMatch(ticket -> ticket.targetPosition().equals(lamp))) {
                ticketed.set(true);
            }
        };
        SpongeGameTests.listen(NotifyNeighborBlockEvent.class, listener);

        helper.pullLever(NeighborNotificationGameTests.LEVER);

        helper.succeedWhen(() -> {
            helper.assertTrue(ticketed.get(), "No NotifyNeighborBlockEvent had a ticket for the lamp");
            helper.assertBlockProperty(NeighborNotificationGameTests.LAMP, RedstoneLampBlock.LIT, true);
            Sponge.eventManager().unregisterListeners(listener);
        });
    }

    // An invalidated ticket is a notification that doesn't happen. It has already been delivered by the time its event is
    // thrown, so what the lamp did in response, lighting up, is rolled back with it and never shown to listeners
    @GameTest(structure = NeighborNotificationGameTests.LEVER_LAMP)
    void invalidTicketKeepsLampUnlit(final GameTestHelper helper) {
        final Vector3i lamp = VecHelper.toVector3i(helper.absolutePos(NeighborNotificationGameTests.LAMP));
        final AtomicBoolean invalidated = new AtomicBoolean();
        final EventListener<NotifyNeighborBlockEvent> listener = event -> event.tickets().forEach(ticket -> {
            if (ticket.targetPosition().equals(lamp)) {
                ticket.invalidate();
                invalidated.set(true);
            }
        });
        SpongeGameTests.listen(NotifyNeighborBlockEvent.class, listener);
        final AtomicBoolean lampChangeShown = new AtomicBoolean();
        final EventListener<ChangeBlockEvent.All> changes = event -> {
            if (event.transactions().stream().anyMatch(transaction -> transaction.original().position().equals(lamp))) {
                lampChangeShown.set(true);
            }
        };
        SpongeGameTests.listen(ChangeBlockEvent.All.class, changes);

        helper.pullLever(NeighborNotificationGameTests.LEVER);

        helper.startSequence()
            .thenWaitUntil(() -> helper.assertTrue(invalidated.get(), "No NotifyNeighborBlockEvent had a ticket for the lamp"))
            .thenExecute(() -> helper.assertFalse(lampChangeShown.get(), "A ChangeBlockEvent.All showed the lamp lighting up"))
            .thenExecuteFor(10, () -> helper.assertBlockProperty(NeighborNotificationGameTests.LAMP, RedstoneLampBlock.LIT, false))
            .thenExecute(() -> {
                Sponge.eventManager().unregisterListeners(listener);
                Sponge.eventManager().unregisterListeners(changes);
            })
            .thenSucceed();
    }

    // A ticket's notifier is the block whose change notified the target: the lever, for the lamp
    @GameTest(structure = NeighborNotificationGameTests.LEVER_LAMP)
    void notifierIsTheLever(final GameTestHelper helper) {
        final Vector3i lamp = VecHelper.toVector3i(helper.absolutePos(NeighborNotificationGameTests.LAMP));
        final Vector3i lever = VecHelper.toVector3i(helper.absolutePos(NeighborNotificationGameTests.LEVER));
        final Set<Vector3i> notifiers = ConcurrentHashMap.newKeySet();
        final EventListener<NotifyNeighborBlockEvent> listener = event -> event.tickets().stream()
            .filter(ticket -> ticket.targetPosition().equals(lamp))
            .map(NotificationTicket::notifierPosition)
            .forEach(notifiers::add);
        SpongeGameTests.listen(NotifyNeighborBlockEvent.class, listener);

        helper.pullLever(NeighborNotificationGameTests.LEVER);

        helper.succeedWhen(() -> {
            helper.assertFalse(notifiers.isEmpty(), "No NotifyNeighborBlockEvent had a ticket for the lamp");
            helper.assertValueEqual(Set.of(lever), notifiers, "notifiers of the lamp's tickets");
            Sponge.eventManager().unregisterListeners(listener);
        });
    }

    // Control for the piston tests: nothing updates the lamp when nothing happens
    @GameTest(structure = NeighborNotificationGameTests.PUSH_LAMP, maxTicks = 40, skyAccess = true)
    void lampStaysLitUntouched(final GameTestHelper helper) {
        helper.startSequence()
            .thenExecuteFor(20, () -> helper.assertBlockProperty(NeighborNotificationGameTests.ROW_LAMP, RedstoneLampBlock.LIT, true))
            .thenSucceed();
    }

    // The push updates the gold block's neighbors, the lamp among them, so it goes out. Sponge's events follow the order
    // things happened in: the push's block changes, its notification of the lamp from where the gold block was, then the
    // lamp's scheduled tick, caused by that notification
    @GameTest(structure = NeighborNotificationGameTests.PUSH_LAMP, maxTicks = 40, skyAccess = true)
    void pushUpdatesLamp(final GameTestHelper helper) {
        final Vector3i gold = VecHelper.toVector3i(helper.absolutePos(NeighborNotificationGameTests.ROW_GOLD));
        final PistonEvents seen = PistonEvents.listen(helper);

        helper.pullLever(NeighborNotificationGameTests.ROW_LEVER);

        helper.succeedWhen(() -> {
            helper.assertBlockProperty(NeighborNotificationGameTests.ROW_LAMP, RedstoneLampBlock.LIT, false);
            final List<Seen> notifications = seen.lampTickets(NotifyNeighborBlockEvent.class);
            final List<Seen> ticks = seen.lampTickets(ScheduleBlockUpdateEvent.class);
            helper.assertValueEqual(1, notifications.size(), "piston NotifyNeighborBlockEvents with a ticket for the lamp, in " + seen);
            helper.assertValueEqual(1, ticks.size(), "piston ScheduleBlockUpdateEvents with a ticket for the lamp, in " + seen);
            final Seen notification = notifications.getFirst();
            final Seen tick = ticks.getFirst();
            helper.assertValueEqual(List.of(true), notification.lampTicketsValid(), "lamp tickets' validity when notified");
            helper.assertValueEqual(List.of(true), tick.lampTicketsValid(), "lamp tickets' validity when its tick was scheduled");
            helper.assertValueEqual(Set.of(gold), ((NotifyNeighborBlockEvent) notification.event()).tickets().stream()
                .filter(ticket -> ticket.targetPosition().equals(seen.lamp))
                .map(NotificationTicket::notifierPosition)
                .collect(Collectors.toSet()), "notifiers of the lamp's tickets");
            final int push = seen.indexOf(ChangeBlockEvent.All.class);
            helper.assertTrue(push >= 0, "No ChangeBlockEvent.All from the piston in " + seen);
            helper.assertTrue(push < seen.events.indexOf(notification) && seen.events.indexOf(notification) < seen.events.indexOf(tick),
                "Expected the push, then the lamp's notification, then its tick, got " + seen);
            helper.assertTrue(tick.event().cause().contains(notification.event()),
                "The lamp's tick wasn't caused by its notification: " + tick.event().cause());
            seen.unregister();
        });
    }

    // A cancelled push is rolled back as if it never happened: its notifications are cancelled with it, and so is what
    // they caused, the lamp's scheduled tick. No listener is shown a notification or tick that's been rolled back.
    @GameTest(structure = NeighborNotificationGameTests.PUSH_LAMP, maxTicks = 40, skyAccess = true)
    void cancelledPushLeavesLampLit(final GameTestHelper helper) {
        final AtomicBoolean cancelled = new AtomicBoolean();
        final PistonEvents seen = PistonEvents.listen(helper);
        final EventListener<ChangeBlockEvent.All> listener = event -> {
            if (seen.causedByPiston(event)) {
                event.setCancelled(true);
                cancelled.set(true);
            }
        };
        SpongeGameTests.listen(ChangeBlockEvent.All.class, listener);
        final BlockPos lamp = helper.absolutePos(NeighborNotificationGameTests.ROW_LAMP);

        helper.pullLever(NeighborNotificationGameTests.ROW_LEVER);

        helper.startSequence()
            .thenWaitUntil(() -> helper.assertTrue(cancelled.get(), "No ChangeBlockEvent.All was caused by the piston"))
            .thenExecute(() -> {
                helper.assertBlockProperty(NeighborNotificationGameTests.ROW_PISTON, PistonBaseBlock.EXTENDED, false);
                helper.assertBlockPresent(Blocks.STONE, NeighborNotificationGameTests.ROW_STONE);
                helper.assertBlockPresent(Blocks.GOLD_BLOCK, NeighborNotificationGameTests.ROW_GOLD);
                helper.assertBlockPresent(Blocks.AIR, NeighborNotificationGameTests.ROW_GOLD.east());
                helper.assertFalse(helper.getLevel().getBlockTicks().hasScheduledTick(lamp, Blocks.REDSTONE_LAMP),
                    "The lamp's tick is still scheduled");
                for (final Seen event : seen.events) {
                    helper.assertFalse(event.lampTicketsValid().contains(true), "A listener was shown a rolled back lamp ticket: " + seen);
                }
            })
            .thenExecuteFor(10, () -> helper.assertBlockProperty(NeighborNotificationGameTests.ROW_LAMP, RedstoneLampBlock.LIT, true))
            .thenExecute(() -> {
                Sponge.eventManager().unregisterListeners(listener);
                seen.unregister();
            })
            .thenSucceed();
    }

    // The source's first fluid tick spreads to all four sides in one capture, so its changes are one ChangeBlockEvent.All.
    // Each new flowing block notifies the source, which schedules itself a tick in response; that reaction is the
    // notification's child and shouldn't split the batch.
    @GameTest(structure = NeighborNotificationGameTests.WATER_SPREAD, maxTicks = 40, skyAccess = true)
    void waterSpreadIsOneChange(final GameTestHelper helper) {
        final List<BlockPos> sides = Direction.Plane.HORIZONTAL.stream().map(NeighborNotificationGameTests.WATER::relative).toList();
        final EventRecorder recorder = EventRecorder.start(helper);

        helper.setBlock(NeighborNotificationGameTests.WATER, Blocks.WATER);

        helper.succeedWhen(() -> {
            sides.forEach(side -> helper.assertBlockPresent(Blocks.WATER, side));
            final List<EventRecorder.Entry> spread = recorder.entries().stream()
                .filter(entry -> entry.event().equals("All") && entry.positions().stream().anyMatch(sides::contains))
                .toList();
            helper.assertValueEqual(1, spread.size(), "ChangeBlockEvent.Alls spreading to the source's sides, in " + recorder.entries());
            helper.assertValueEqual(Set.copyOf(sides), Set.copyOf(spread.getFirst().positions()), "positions the spread changed");
            recorder.stop();
        });
    }

    // An explosion breaks its blocks in one capture, so they're one ChangeBlockEvent.All. Breaking the stone beside the
    // water notifies it, and the water schedules itself a tick in response; that shouldn't split the batch.
    @GameTest(structure = NeighborNotificationGameTests.TNT_WATER, maxTicks = 40)
    void explosionIsOneChange(final GameTestHelper helper) {
        final EventRecorder recorder = EventRecorder.start(helper);

        helper.spawn(EntityTypes.TNT, NeighborNotificationGameTests.TNT).setFuse(1);

        helper.succeedWhen(() -> {
            NeighborNotificationGameTests.TNT_STONES.forEach(stone -> helper.assertBlockNotPresent(Blocks.STONE, stone));
            final List<EventRecorder.Entry> explosion = recorder.entries().stream()
                .filter(entry -> entry.event().equals("All") && entry.positions().stream().anyMatch(NeighborNotificationGameTests.TNT_STONES::contains))
                .toList();
            helper.assertValueEqual(1, explosion.size(), "ChangeBlockEvent.Alls breaking the stones, in " + recorder.entries());
            helper.assertTrue(explosion.getFirst().positions().containsAll(NeighborNotificationGameTests.TNT_STONES),
                "The explosion's ChangeBlockEvent.All didn't break every stone: " + explosion.getFirst());
            recorder.stop();
        });
    }

    // Keeping the stone beside the water out of an explosion keeps everything its breaking would have caused: the water is
    // never notified, so it never schedules the tick that would make it flow
    @GameTest(structure = NeighborNotificationGameTests.TNT_WATER, maxTicks = 40)
    void keptStoneKeepsWaterStill(final GameTestHelper helper) {
        final Vector3i kept = VecHelper.toVector3i(helper.absolutePos(NeighborNotificationGameTests.WATER_STONE));
        final AtomicBoolean invalidated = new AtomicBoolean();
        final EventListener<ChangeBlockEvent.All> listener = event -> event.transactions().stream()
            .filter(transaction -> transaction.original().position().equals(kept))
            .forEach(transaction -> {
                transaction.invalidate();
                invalidated.set(true);
            });
        SpongeGameTests.listen(ChangeBlockEvent.All.class, listener);
        final BlockPos water = helper.absolutePos(NeighborNotificationGameTests.TNT_WATER_SOURCE);

        // Placing the structure updates the water, which schedules itself a tick; how long the test waits after placing
        // differs between headless and in-game runs, so let that tick pass before the explosion
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertFalse(helper.getLevel().getFluidTicks().hasScheduledTick(water, Fluids.WATER),
                "The water still has the tick from placing the structure"))
            .thenExecute(() -> helper.spawn(EntityTypes.TNT, NeighborNotificationGameTests.TNT).setFuse(1))
            .thenWaitUntil(() -> helper.assertTrue(invalidated.get(), "No ChangeBlockEvent.All broke the stone beside the water"))
            .thenExecute(() -> {
                NeighborNotificationGameTests.TNT_STONES.stream()
                    .filter(stone -> !stone.equals(NeighborNotificationGameTests.WATER_STONE))
                    .forEach(stone -> helper.assertBlockNotPresent(Blocks.STONE, stone));
                helper.assertFalse(helper.getLevel().getFluidTicks().hasScheduledTick(water, Fluids.WATER), "The water has a tick scheduled");
            })
            .thenExecuteFor(10, () -> {
                helper.assertBlockPresent(Blocks.STONE, NeighborNotificationGameTests.WATER_STONE);
                helper.assertBlockState(NeighborNotificationGameTests.TNT_WATER_SOURCE, Blocks.WATER.defaultBlockState());
            })
            .thenExecute(() -> Sponge.eventManager().unregisterListeners(listener))
            .thenSucceed();
    }

    /**
     * A {@link ChangeBlockEvent.All}, {@link NotifyNeighborBlockEvent} or {@link ScheduleBlockUpdateEvent} the piston
     * caused, with whether each of its tickets for the lamp was valid when it was thrown.
     */
    private record Seen(Event event, List<Boolean> lampTicketsValid) {

        @Override
        public String toString() {
            return this.event.getClass().getSimpleName() + this.lampTicketsValid;
        }
    }

    // Every event the piston causes in push_lamp, in the order they're thrown, seen before any other listener
    private static final class PistonEvents implements EventListener<Event> {

        final List<Seen> events = new CopyOnWriteArrayList<>();
        final Vector3i piston;
        final Vector3i lamp;

        private PistonEvents(final GameTestHelper helper) {
            this.piston = VecHelper.toVector3i(helper.absolutePos(NeighborNotificationGameTests.ROW_PISTON));
            this.lamp = VecHelper.toVector3i(helper.absolutePos(NeighborNotificationGameTests.ROW_LAMP));
        }

        static PistonEvents listen(final GameTestHelper helper) {
            final PistonEvents seen = new PistonEvents(helper);
            SpongeGameTests.listen(TypeToken.get(ChangeBlockEvent.All.class), Order.PRE, seen);
            SpongeGameTests.listen(TypeToken.get(NotifyNeighborBlockEvent.class), Order.PRE, seen);
            SpongeGameTests.listen(new TypeToken<ScheduleBlockUpdateEvent<?>>() {}, Order.PRE, seen);
            return seen;
        }

        boolean causedByPiston(final Event event) {
            return event.cause().allOf(LocatableBlock.class).stream().anyMatch(block -> block.blockPosition().equals(this.piston));
        }

        @Override
        public void handle(final Event event) {
            if (!this.causedByPiston(event)) {
                return;
            }
            final List<Boolean> valid = switch (event) {
                case final NotifyNeighborBlockEvent notify -> notify.tickets().stream()
                    .filter(ticket -> ticket.targetPosition().equals(this.lamp))
                    .map(NotificationTicket::valid)
                    .toList();
                case final ScheduleBlockUpdateEvent<?> schedule -> schedule.tickets().stream()
                    .filter(ticket -> ticket.block().blockPosition().equals(this.lamp))
                    .map(ScheduleUpdateTicket::valid)
                    .toList();
                default -> List.of();
            };
            this.events.add(new Seen(event, valid));
        }

        List<Seen> lampTickets(final Class<? extends Event> type) {
            return this.events.stream().filter(seen -> type.isInstance(seen.event()) && !seen.lampTicketsValid().isEmpty()).toList();
        }

        int indexOf(final Class<? extends Event> type) {
            for (int i = 0; i < this.events.size(); i++) {
                if (type.isInstance(this.events.get(i).event())) {
                    return i;
                }
            }
            return -1;
        }

        void unregister() {
            Sponge.eventManager().unregisterListeners(this);
        }

        @Override
        public String toString() {
            return this.events.toString();
        }
    }
}
