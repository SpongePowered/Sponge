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
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.test.gametest.EventRecorder;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.common.test.gametest.SpongeGameTests;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Uses {@code sponge:piston/push_row}: on a bedrock floor along z=1,
 * lever (0,1,1), east-facing piston (1,1,1), stone (2,1,1), gold block (3,1,1), air (4,1,1).
 *
 * <p>The piston moves inside the PhaseTracker's block event phase, which pushes
 * the piston as a {@link LocatableBlock} cause. One push throws two
 * {@link ChangeBlockEvent.All}: the row moving, then the piston base extending,
 * because the block entity and neighbor transactions in between split the batch.</p>
 */
@GameTestGroup("piston")
public final class PistonGameTests {

    private static final String STRUCTURE = "sponge:piston/push_row";
    private static final BlockPos LEVER = new BlockPos(0, 1, 1);
    private static final BlockPos PISTON = new BlockPos(1, 1, 1);
    private static final BlockPos STONE = new BlockPos(2, 1, 1);
    private static final BlockPos GOLD = new BlockPos(3, 1, 1);
    private static final BlockPos END = new BlockPos(4, 1, 1);
    // sponge:piston/sticky_row: as push_row with a sticky piston and only the stone
    private static final String STICKY = "sponge:piston/sticky_row";
    // sponge:piston/push_break: as push_row with a pumpkin at (4,1,1)
    private static final String BREAK = "sponge:piston/push_break";
    // sponge:piston/push_limit: lever and piston as push_row, stone from (2,1,1) to (14,1,1)
    private static final String LIMIT = "sponge:piston/push_limit";
    private static final BlockPos LIMIT_LAST = new BlockPos(14, 1, 1);

    // Two steps: extend to push the stone to (3,1,1), then unpower so the sticky piston pulls it back
    @GameTest(structure = PistonGameTests.STICKY, maxTicks = 60)
    void stickyRetract(final GameTestHelper helper) {
        final List<String> pre = new ArrayList<>();
        final EventListener<ChangeBlockEvent.Pre> listener = event -> {
            if (PistonGameTests.isPistonPre(helper, event)) {
                pre.add(event.context().containsKey(EventContextKeys.PISTON_EXTEND) ? "extend" : "retract");
            }
        };
        SpongeGameTests.listen(ChangeBlockEvent.Pre.class, listener);

        helper.pullLever(PistonGameTests.LEVER);
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertBlockPresent(Blocks.STONE, PistonGameTests.GOLD))
            .thenExecute(() -> helper.pullLever(PistonGameTests.LEVER))
            .thenWaitUntil(() -> {
                helper.assertBlockProperty(PistonGameTests.PISTON, PistonBaseBlock.EXTENDED, false);
                helper.assertBlockPresent(Blocks.STONE, PistonGameTests.STONE);
                helper.assertBlockPresent(Blocks.AIR, PistonGameTests.GOLD);
            })
            .thenExecute(() -> {
                helper.assertValueEqual(List.of("extend", "retract"), pre, Component.literal("piston Pre contexts"));
                Sponge.eventManager().unregisterListeners(listener);
            })
            .thenSucceed();
    }

    // Cancelling the retract Pre leaves the piston extended, holding the stone out at (3,1,1), with the lever off
    @GameTest(structure = PistonGameTests.STICKY, maxTicks = 60, settle = true, skyAccess = true)
    void stickyRetractPreCancel(final GameTestHelper helper) {
        final AtomicBoolean cancelled = new AtomicBoolean();
        final EventListener<ChangeBlockEvent.Pre> listener = event -> {
            if (PistonGameTests.isPistonPre(helper, event) && event.context().containsKey(EventContextKeys.PISTON_RETRACT)) {
                event.setCancelled(true);
                cancelled.set(true);
            }
        };
        SpongeGameTests.listen(ChangeBlockEvent.Pre.class, listener);

        helper.pullLever(PistonGameTests.LEVER);
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertBlockPresent(Blocks.STONE, PistonGameTests.GOLD))
            .thenExecute(() -> helper.pullLever(PistonGameTests.LEVER))
            .thenWaitUntil(() -> helper.assertTrue(cancelled.get(), Component.literal("No ChangeBlockEvent.Pre was thrown for the piston retracting")))
            .thenExecuteFor(10, () -> PistonGameTests.assertHeldOut(helper))
            .thenExecute(() -> Sponge.eventManager().unregisterListeners(listener))
            .thenSucceed();
    }

    // With the retract cancelled the piston is still extended, so switching the lever back on asks nothing of it:
    // the only events from then on are the lever's own
    @GameTest(structure = PistonGameTests.STICKY, maxTicks = 60, settle = true)
    void stickyRetractPreCancelRepower(final GameTestHelper helper) {
        final AtomicBoolean cancelled = new AtomicBoolean();
        final EventListener<ChangeBlockEvent.Pre> listener = event -> {
            if (PistonGameTests.isPistonPre(helper, event) && event.context().containsKey(EventContextKeys.PISTON_RETRACT)) {
                event.setCancelled(true);
                cancelled.set(true);
            }
        };
        SpongeGameTests.listen(ChangeBlockEvent.Pre.class, listener);
        final EventRecorder recorder = EventRecorder.start(helper);
        final AtomicLong repowered = new AtomicLong();

        helper.pullLever(PistonGameTests.LEVER);
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertBlockPresent(Blocks.STONE, PistonGameTests.GOLD))
            .thenExecute(() -> helper.pullLever(PistonGameTests.LEVER))
            .thenWaitUntil(() -> helper.assertTrue(cancelled.get(), Component.literal("No ChangeBlockEvent.Pre was thrown for the piston retracting")))
            .thenIdle(1) // the sequence would otherwise switch it back on in the tick the retract was cancelled
            .thenExecute(() -> {
                repowered.set(helper.getTick());
                helper.pullLever(PistonGameTests.LEVER);
            })
            .thenExecuteFor(10, () -> PistonGameTests.assertHeldOut(helper))
            .thenExecute(() -> {
                // The lever changing and notifying its neighbors, the piston among them, which has nothing left to do
                final List<EventRecorder.Entry> after = recorder.entries().stream().filter(entry -> entry.tick() >= repowered.get()).toList();
                helper.assertValueEqual(List.of("All", "NotifyNeighbor", "NotifyNeighbor", "Post"), after.stream().map(EventRecorder.Entry::event).toList(),
                    Component.literal("events after switching the lever back on, recorded: " + after));
                helper.assertTrue(after.stream().filter(entry -> !entry.event().equals("NotifyNeighbor")).allMatch(entry -> entry.positions().equals(List.of(PistonGameTests.LEVER))),
                    Component.literal("Block changes other than the lever's after switching it back on, recorded: " + after));
                recorder.stop();
                Sponge.eventManager().unregisterListeners(listener);
            })
            .thenSucceed();
    }

    // sponge:piston/push_break has a pumpkin at (4,1,1), which the push destroys instead of moving
    @GameTest(structure = PistonGameTests.BREAK, maxTicks = 40)
    void pushBreak(final GameTestHelper helper) {
        final EventRecorder recorder = EventRecorder.start(helper);

        helper.pullLever(PistonGameTests.LEVER);

        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.STONE, PistonGameTests.GOLD);
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, PistonGameTests.END);
            helper.assertItemEntityPresent(Items.PUMPKIN, PistonGameTests.END, 2.0);
            final List<EventRecorder.Entry> entries = recorder.entries();
            helper.assertTrue(entries.stream().anyMatch(entry -> entry.event().equals("DropItemEvent.Destruct") && entry.cause().equals("piston(1,1,1)")),
                Component.literal("No item drop was caused by the piston, recorded: " + entries));
            recorder.stop();
        });
    }

    // Cancelling the push must restore the pumpkin without also dropping it
    @GameTest(structure = PistonGameTests.BREAK, maxTicks = 40, settle = true, skyAccess = true)
    void pushBreakCancel(final GameTestHelper helper) {
        final AtomicBoolean cancelled = new AtomicBoolean();
        final EventListener<ChangeBlockEvent.All> listener = event -> {
            if (PistonGameTests.causedByPiston(helper, event)) {
                event.setCancelled(true);
                cancelled.set(true);
            }
        };
        SpongeGameTests.listen(ChangeBlockEvent.All.class, listener);

        helper.pullLever(PistonGameTests.LEVER);

        PistonGameTests.holdCancelled(helper, () -> {
            helper.assertTrue(cancelled.get(), Component.literal("No ChangeBlockEvent.All was caused by the piston"));
            helper.assertBlockProperty(PistonGameTests.PISTON, PistonBaseBlock.EXTENDED, false);
            helper.assertBlockPresent(Blocks.STONE, PistonGameTests.STONE);
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, PistonGameTests.GOLD);
            helper.assertBlockPresent(Blocks.PUMPKIN, PistonGameTests.END);
            helper.assertItemEntityNotPresent(Items.PUMPKIN);
        }, listener);
    }

    // sponge:piston/push_limit has 13 stones from (2,1,1) to (14,1,1), one more than a piston can push, so
    // vanilla never queues the block event and Sponge throws nothing for the piston
    @GameTest(structure = PistonGameTests.LIMIT, maxTicks = 40)
    void pushLimit(final GameTestHelper helper) {
        final EventRecorder recorder = EventRecorder.start(helper);

        helper.pullLever(PistonGameTests.LEVER);

        helper.runAfterDelay(5, () -> {
            final List<EventRecorder.Entry> entries = recorder.entries();
            helper.assertTrue(entries.stream().noneMatch(entry -> entry.cause().equals("piston(1,1,1)")),
                Component.literal("The piston caused events while over the push limit, recorded: " + entries));
            helper.assertBlockProperty(PistonGameTests.PISTON, PistonBaseBlock.EXTENDED, false);
            helper.assertBlockPresent(Blocks.STONE, PistonGameTests.LIMIT_LAST);
            helper.assertBlockPresent(Blocks.AIR, PistonGameTests.LIMIT_LAST.east());
            recorder.stop();
            helper.succeed();
        });
    }

    // Removing the last stone leaves exactly 12, which the piston can push
    @GameTest(structure = PistonGameTests.LIMIT, maxTicks = 40)
    void pushMax(final GameTestHelper helper) {
        helper.setBlock(PistonGameTests.LIMIT_LAST, Blocks.AIR);
        helper.pullLever(PistonGameTests.LEVER);

        helper.succeedWhen(() -> {
            helper.assertBlockProperty(PistonGameTests.PISTON, PistonBaseBlock.EXTENDED, true);
            helper.assertBlockPresent(Blocks.PISTON_HEAD, PistonGameTests.STONE);
            helper.assertBlockPresent(Blocks.STONE, PistonGameTests.LIMIT_LAST);
        });
    }

    @GameTest(structure = PistonGameTests.STRUCTURE, maxTicks = 40)
    void pushRow(final GameTestHelper helper) {
        final EventRecorder recorder = EventRecorder.start(helper);

        helper.pullLever(PistonGameTests.LEVER);

        helper.succeedWhen(() -> {
            final List<EventRecorder.Entry> entries = recorder.entries();
            helper.assertTrue(entries.stream().anyMatch(entry -> entry.event().equals("All") && entry.cause().equals("piston(1,1,1)")),
                Component.literal("No ChangeBlockEvent.All was caused by the piston, recorded: " + entries));
            helper.assertBlockProperty(PistonGameTests.PISTON, PistonBaseBlock.EXTENDED, true);
            helper.assertBlockPresent(Blocks.PISTON_HEAD, PistonGameTests.STONE);
            helper.assertBlockPresent(Blocks.STONE, PistonGameTests.GOLD);
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, PistonGameTests.END);
            recorder.stop();
        });
    }

    // The order Sponge throws the push's events in, from the lever pull to the moved blocks settling two ticks later
    @GameTest(structure = PistonGameTests.STRUCTURE, maxTicks = 40)
    void pushSequence(final GameTestHelper helper) {
        final EventRecorder recorder = EventRecorder.start(helper);

        helper.pullLever(PistonGameTests.LEVER);

        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.PISTON_HEAD, PistonGameTests.STONE);
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, PistonGameTests.END);
            final List<EventRecorder.Entry> entries = recorder.entries();

            // One Pre even though the lever notifies the piston twice: the duplicate block event is dropped before
            // Pre is thrown. Then the row and the base are separate All events, split by the row's neighbor
            // notifications, and one Post covers the whole push.
            final List<EventRecorder.Entry> push = entries.stream().filter(entry -> entry.cause().equals("piston(1,1,1)")).toList();
            helper.assertValueEqual(List.of("Pre", "All", "NotifyNeighbor", "All", "NotifyNeighbor", "Post"),
                push.stream().map(EventRecorder.Entry::event).toList(), Component.literal("piston events, recorded: " + entries));
            helper.assertValueEqual(List.of(PistonGameTests.STONE, PistonGameTests.GOLD, PistonGameTests.END),
                PistonGameTests.sorted(push.get(1).positions()), Component.literal("row All positions"));
            helper.assertValueEqual(List.of(PistonGameTests.PISTON), push.get(3).positions(), Component.literal("base All positions"));
            helper.assertValueEqual(List.of(PistonGameTests.PISTON, PistonGameTests.STONE, PistonGameTests.GOLD, PistonGameTests.END),
                PistonGameTests.sorted(push.get(5).positions()), Component.literal("Post positions"));

            // Two ticks later each moving block settles in its own event, furthest first
            helper.assertValueEqual(List.of(List.of(PistonGameTests.END), List.of(PistonGameTests.GOLD), List.of(PistonGameTests.STONE)),
                entries.stream()
                    .filter(entry -> entry.event().equals("All") && entry.cause().equals("PistonMovingBlockEntity"))
                    .map(EventRecorder.Entry::positions)
                    .toList(),
                Component.literal("settled All positions"));
            recorder.stop();
        });
    }

    // Cancels both events of the push
    @GameTest(structure = PistonGameTests.STRUCTURE, maxTicks = 40, settle = true, skyAccess = true)
    void pushCancel(final GameTestHelper helper) {
        final AtomicBoolean cancelled = new AtomicBoolean();
        final EventListener<ChangeBlockEvent.All> listener = event -> {
            if (PistonGameTests.causedByPiston(helper, event)) {
                event.setCancelled(true);
                cancelled.set(true);
            }
        };
        SpongeGameTests.listen(ChangeBlockEvent.All.class, listener);

        helper.pullLever(PistonGameTests.LEVER);

        PistonGameTests.holdCancelled(helper, () -> {
            helper.assertTrue(cancelled.get(), Component.literal("No ChangeBlockEvent.All was caused by the piston"));
            PistonGameTests.assertUnmoved(helper);
        }, listener);
    }

    // Cancelling Pre stops the push before anything moves, so no All event is thrown for it at all
    @GameTest(structure = PistonGameTests.STRUCTURE, maxTicks = 40, settle = true, skyAccess = true)
    void preCancel(final GameTestHelper helper) {
        final AtomicBoolean cancelled = new AtomicBoolean();
        final AtomicBoolean moved = new AtomicBoolean();
        final EventListener<ChangeBlockEvent.Pre> pre = event -> {
            if (PistonGameTests.isPistonPre(helper, event) && event.context().containsKey(EventContextKeys.PISTON_EXTEND)) {
                event.setCancelled(true);
                cancelled.set(true);
            }
        };
        final EventListener<ChangeBlockEvent.All> all = event -> {
            if (PistonGameTests.causedByPiston(helper, event)) {
                moved.set(true);
            }
        };
        SpongeGameTests.listen(ChangeBlockEvent.Pre.class, pre);
        SpongeGameTests.listen(ChangeBlockEvent.All.class, all);

        helper.pullLever(PistonGameTests.LEVER);

        PistonGameTests.holdCancelled(helper, () -> {
            helper.assertTrue(cancelled.get(), Component.literal("No ChangeBlockEvent.Pre was thrown for the piston extending"));
            helper.assertFalse(moved.get(), Component.literal("ChangeBlockEvent.All was still thrown after cancelling Pre"));
            PistonGameTests.assertUnmoved(helper);
        }, pre, all);
    }

    // A plugin that only vetoes the blocks being pushed; expected to roll the whole push back
    // Known issue until decided otherwise: cancelling only the row event leaves the base extended with no head
    @GameTest(structure = PistonGameTests.STRUCTURE, maxTicks = 40, required = false)
    void cancelFirstOnly(final GameTestHelper helper) {
        final Vector3i piston = VecHelper.toVector3i(helper.absolutePos(PistonGameTests.PISTON));
        final AtomicBoolean cancelled = new AtomicBoolean();
        final EventListener<ChangeBlockEvent.All> listener = event -> {
            if (PistonGameTests.causedByPiston(helper, event)
                && event.transactions().stream().noneMatch(transaction -> transaction.original().position().equals(piston))) {
                event.setCancelled(true);
                cancelled.set(true);
            }
        };
        SpongeGameTests.listen(ChangeBlockEvent.All.class, listener);

        helper.pullLever(PistonGameTests.LEVER);

        helper.succeedWhen(() -> {
            helper.assertTrue(cancelled.get(), Component.literal("No ChangeBlockEvent.All was thrown for the pushed row"));
            PistonGameTests.assertRowUnmoved(helper);
            helper.assertBlockProperty(PistonGameTests.PISTON, PistonBaseBlock.EXTENDED, true);
            Sponge.eventManager().unregisterListeners(listener);
        });
    }

    // Checks the rollback holds for a while before letting the listeners go. The piston is left powered but unextended,
    // as the cancel left it, and only a later neighbor update would have vanilla push again; the tests using this have
    // sky access, so vanilla removing the barrier ceiling above the piston after the test doesn't make one.
    private static void holdCancelled(final GameTestHelper helper, final Runnable assertions, final EventListener<?>... listeners) {
        helper.startSequence()
            .thenWaitUntil(assertions)
            .thenExecuteFor(10, assertions)
            .thenExecute(() -> Arrays.stream(listeners).forEach(Sponge.eventManager()::unregisterListeners))
            .thenSucceed();
    }

    private static List<BlockPos> sorted(final List<BlockPos> positions) {
        return positions.stream().sorted(Comparator.comparingInt(BlockPos::getX)).toList();
    }

    private static void assertUnmoved(final GameTestHelper helper) {
        PistonGameTests.assertRowUnmoved(helper);
        helper.assertBlockProperty(PistonGameTests.PISTON, PistonBaseBlock.EXTENDED, false);
    }

    // sponge:piston/sticky_row after a cancelled retract: still extended, with the stone held out at (3,1,1)
    private static void assertHeldOut(final GameTestHelper helper) {
        helper.assertBlockProperty(PistonGameTests.PISTON, PistonBaseBlock.EXTENDED, true);
        helper.assertBlockPresent(Blocks.PISTON_HEAD, PistonGameTests.STONE);
        helper.assertBlockPresent(Blocks.STONE, PistonGameTests.GOLD);
    }

    private static void assertRowUnmoved(final GameTestHelper helper) {
        helper.assertBlockPresent(Blocks.STONE, PistonGameTests.STONE);
        helper.assertBlockPresent(Blocks.GOLD_BLOCK, PistonGameTests.GOLD);
        helper.assertBlockPresent(Blocks.AIR, PistonGameTests.END);
    }

    private static boolean isPistonPre(final GameTestHelper helper, final ChangeBlockEvent.Pre event) {
        final Vector3i piston = VecHelper.toVector3i(helper.absolutePos(PistonGameTests.PISTON));
        return event.locations().stream().anyMatch(location -> location.blockPosition().equals(piston));
    }

    private static boolean causedByPiston(final GameTestHelper helper, final ChangeBlockEvent.All event) {
        final Vector3i piston = VecHelper.toVector3i(helper.absolutePos(PistonGameTests.PISTON));
        return event.cause().allOf(LocatableBlock.class).stream().anyMatch(block -> block.blockPosition().equals(piston));
    }
}
