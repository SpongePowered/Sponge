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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.common.test.gametest.SpongeGameTests;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Game test functions run inside the PhaseTracker's server tick context, so a
 * block change is captured and only thrown as {@link ChangeBlockEvent.All}
 * when that context unwinds at the end of the tick. Assertions therefore run
 * on a later tick through {@link GameTestHelper#succeedWhen(Runnable)}.
 */
@GameTestGroup("change_block")
public final class ChangeBlockEventGameTests {

    private static final BlockPos POS = new BlockPos(0, 1, 0);

    @GameTest
    void eventFires(final GameTestHelper helper) {
        final Vector3i target = VecHelper.toVector3i(helper.absolutePos(ChangeBlockEventGameTests.POS));
        final AtomicReference<BlockTransaction> seen = new AtomicReference<>();
        final EventListener<ChangeBlockEvent.All> listener = event -> ChangeBlockEventGameTests.find(event, target).ifPresent(seen::set);
        SpongeGameTests.listen(ChangeBlockEvent.All.class, listener);

        helper.setBlock(ChangeBlockEventGameTests.POS, Blocks.STONE);

        helper.succeedWhen(() -> {
            final BlockTransaction transaction = seen.get();
            helper.assertTrue(transaction != null, "ChangeBlockEvent.All was not thrown for " + target);
            helper.assertTrue((Object) transaction.finalReplacement().state() == Blocks.STONE.defaultBlockState(),
                "Expected stone as the final replacement but was " + transaction.finalReplacement().state());
            Sponge.eventManager().unregisterListeners(listener);
        });
    }

    @GameTest
    void cancelRestores(final GameTestHelper helper) {
        final Vector3i target = VecHelper.toVector3i(helper.absolutePos(ChangeBlockEventGameTests.POS));
        final AtomicBoolean cancelled = new AtomicBoolean();
        // Each test ticks in its own capture, so cancelling the whole event only rolls back this test's change
        final EventListener<ChangeBlockEvent.All> listener = event -> {
            if (ChangeBlockEventGameTests.find(event, target).isPresent()) {
                event.setCancelled(true);
                cancelled.set(true);
            }
        };
        SpongeGameTests.listen(ChangeBlockEvent.All.class, listener);

        final BlockState original = helper.getBlockState(ChangeBlockEventGameTests.POS);
        helper.setBlock(ChangeBlockEventGameTests.POS, Blocks.STONE);

        helper.succeedWhen(() -> {
            helper.assertTrue(cancelled.get(), "ChangeBlockEvent.All was not thrown for " + target);
            helper.assertBlockState(ChangeBlockEventGameTests.POS, state -> state == original, () -> "Expected " + original);
            Sponge.eventManager().unregisterListeners(listener);
        });
    }

    private static Optional<BlockTransaction> find(final ChangeBlockEvent.All event, final Vector3i target) {
        return event.transactions().stream()
            .filter(transaction -> transaction.original().position().equals(target))
            .findFirst();
    }
}
