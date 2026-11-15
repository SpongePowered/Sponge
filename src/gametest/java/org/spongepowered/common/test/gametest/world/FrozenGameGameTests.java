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
package org.spongepowered.common.test.gametest.world;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.common.test.gametest.FrozenGame;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.common.test.gametest.SpongeGameTests;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.util.concurrent.atomic.AtomicBoolean;

@GameTestGroup(value = "frozen_game", environment = FrozenGame.class)
public final class FrozenGameGameTests {

    /**
     * One test for everything, since a step moves the whole server and so every test of the batch with it.
     *
     * <p>Sand over air falls on a scheduled block tick. Frozen, that tick never runs, so the sand stays and no event
     * is thrown for it; ChangeBlockEventGameTests#scheduledTickFires is the same setup with the game running.</p>
     */
    @GameTest(maxTicks = 60)
    void gameOnlyMovesWhenStepped(final GameTestHelper helper) {
        final long start = helper.getLevel().getGameTime();
        final ArmorStand stand = helper.spawn(EntityTypes.ARMOR_STAND, BlockPos.ZERO);
        final int age = stand.tickCount;

        final BlockPos sand = new BlockPos(2, 3, 2);
        final Vector3i target = VecHelper.toVector3i(helper.absolutePos(sand));
        final AtomicBoolean fell = new AtomicBoolean();
        final EventListener<ChangeBlockEvent.All> listener = event -> {
            if (event.transactions().stream().anyMatch(transaction -> transaction.original().position().equals(target)
                && (Object) transaction.original().state() == Blocks.SAND.defaultBlockState())) {
                fell.set(true);
            }
        };
        SpongeGameTests.listen(ChangeBlockEvent.All.class, listener);
        helper.setBlock(sand.below(), Blocks.AIR);
        helper.setBlock(sand, Blocks.SAND);

        helper.startSequence()
            .thenIdle(5)
            .thenExecute(() -> {
                helper.assertValueEqual(start, helper.getLevel().getGameTime(), "game time while frozen");
                helper.assertValueEqual(age, stand.tickCount, "entity ticks while frozen");
                helper.assertBlockPresent(Blocks.SAND, sand);
                helper.assertFalse(fell.get(), "ChangeBlockEvent.All was thrown for the sand while frozen");
                FrozenGame.step(helper, 10);
            })
            .thenWaitUntil(() -> helper.assertValueEqual(start + 10, helper.getLevel().getGameTime(), "game time after ten steps"))
            .thenIdle(5)
            .thenExecute(() -> {
                helper.assertValueEqual(start + 10, helper.getLevel().getGameTime(), "game time once the steps ran out");
                helper.assertValueEqual(age + 10, stand.tickCount, "entity ticks after ten steps");
                helper.assertBlockPresent(Blocks.AIR, sand);
                helper.assertTrue(fell.get(), "ChangeBlockEvent.All was not thrown for the sand once stepped");
                Sponge.eventManager().unregisterListeners(listener);
            })
            .thenSucceed();
    }
}
