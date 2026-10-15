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
import net.minecraft.world.level.block.RedstoneLampBlock;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.transaction.NotificationTicket;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.common.test.gametest.SpongeGameTests;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Whether neighbor notifications reach their targets as {@link NotifyNeighborBlockEvent} describes. A redstone lamp is
 * the witness: it only checks its power when a neighbor updates it, so a lamp that changes was updated.
 *
 * <p>{@code sponge:notify/lever_lamp}: on a bedrock floor, a lever (0,1,1) with an unlit lamp (1,1,1) beside it.</p>
 *
 * <p>{@code sponge:piston/push_lamp}: lever (0,1,2), east-facing piston (1,1,2), stone (2,1,2) and gold block (3,1,2), with
 * a lit lamp and no power at (3,1,3) beside the gold block, so it goes out once anything updates it. Those tests have sky
 * access, so no barrier ceiling is placed above the lamp, or removed from above it, around the test.</p>
 */
@GameTestGroup("notify_neighbor")
public final class NeighborNotificationGameTests {

    private static final String LEVER_LAMP = "sponge:notify/lever_lamp";
    private static final BlockPos LEVER = new BlockPos(0, 1, 1);
    private static final BlockPos LAMP = new BlockPos(1, 1, 1);
    private static final String PUSH_LAMP = "sponge:piston/push_lamp";
    private static final BlockPos ROW_LEVER = new BlockPos(0, 1, 2);
    private static final BlockPos ROW_PISTON = new BlockPos(1, 1, 2);
    private static final BlockPos ROW_LAMP = new BlockPos(3, 1, 3);

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

    // An invalidated ticket is a notification that doesn't happen, so the lamp is never told to check its power
    // Known issue: the update is delivered as it happens and the event is only thrown afterwards, so invalidating does nothing
    @GameTest(structure = NeighborNotificationGameTests.LEVER_LAMP, required = false)
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

        helper.pullLever(NeighborNotificationGameTests.LEVER);

        helper.startSequence()
            .thenWaitUntil(() -> helper.assertTrue(invalidated.get(), "No NotifyNeighborBlockEvent had a ticket for the lamp"))
            .thenExecuteFor(10, () -> helper.assertBlockProperty(NeighborNotificationGameTests.LAMP, RedstoneLampBlock.LIT, false))
            .thenExecute(() -> Sponge.eventManager().unregisterListeners(listener))
            .thenSucceed();
    }

    // A ticket's notifier is the block whose change notified the target: the lever, for the lamp
    // Known issue: the notifier position is the target's position when the source of the multi-neighbor update is unknown
    @GameTest(structure = NeighborNotificationGameTests.LEVER_LAMP, required = false)
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

    // The push updates the gold block's neighbors, the lamp among them, so it goes out
    @GameTest(structure = NeighborNotificationGameTests.PUSH_LAMP, maxTicks = 40, skyAccess = true)
    void pushUpdatesLamp(final GameTestHelper helper) {
        helper.pullLever(NeighborNotificationGameTests.ROW_LEVER);

        helper.succeedWhen(() -> helper.assertBlockProperty(NeighborNotificationGameTests.ROW_LAMP, RedstoneLampBlock.LIT, false));
    }

    // A cancelled push is rolled back as if it never happened, so its neighbors shouldn't have been told it did
    // Known issue: the push's updates are delivered before its event can be cancelled, and the rollback doesn't undo them
    @GameTest(structure = NeighborNotificationGameTests.PUSH_LAMP, maxTicks = 40, required = false, skyAccess = true)
    void cancelledPushLeavesLampLit(final GameTestHelper helper) {
        final Vector3i piston = VecHelper.toVector3i(helper.absolutePos(NeighborNotificationGameTests.ROW_PISTON));
        final AtomicBoolean cancelled = new AtomicBoolean();
        final EventListener<ChangeBlockEvent.All> listener = event -> {
            if (event.cause().allOf(LocatableBlock.class).stream().anyMatch(block -> block.blockPosition().equals(piston))) {
                event.setCancelled(true);
                cancelled.set(true);
            }
        };
        SpongeGameTests.listen(ChangeBlockEvent.All.class, listener);

        helper.pullLever(NeighborNotificationGameTests.ROW_LEVER);

        helper.startSequence()
            .thenWaitUntil(() -> helper.assertTrue(cancelled.get(), "No ChangeBlockEvent.All was caused by the piston"))
            .thenExecuteFor(10, () -> helper.assertBlockProperty(NeighborNotificationGameTests.ROW_LAMP, RedstoneLampBlock.LIT, true))
            .thenExecute(() -> Sponge.eventManager().unregisterListeners(listener))
            .thenSucceed();
    }
}
