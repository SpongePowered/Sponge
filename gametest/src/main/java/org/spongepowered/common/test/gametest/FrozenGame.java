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
package org.spongepowered.common.test.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.api.world.server.ServerWorld;

/**
 * The game frozen as {@code /tick freeze} leaves it, for tests that decide when it moves: nothing but players ticks,
 * no block or fluid ticks run and time stands still, until a test {@link #step steps} it forward. The tests
 * themselves keep ticking, so their sequences and timeouts count server ticks as usual.
 *
 * <p>Freezing is server wide. The environment's tests have the server to themselves while they run, since each
 * environment's tests run as a batch of their own, but they run together: a step in one test moves the game for
 * all of them.</p>
 */
public final class FrozenGame implements GameTestEnvironment {

    @Override
    public void setup(final ServerWorld world) {
        ((ServerLevel) world).getServer().tickRateManager().setFrozen(true);
    }

    @Override
    public void teardown(final ServerWorld world) {
        final ServerTickRateManager ticks = ((ServerLevel) world).getServer().tickRateManager();
        ticks.setFrozenTicksToRun(0);
        ticks.setFrozen(false);
    }

    /** Runs the game for that many ticks, starting with the next server tick, and freezes it again */
    public static void step(final GameTestHelper helper, final int ticks) {
        if (!helper.getLevel().getServer().tickRateManager().stepGameIfPaused(ticks)) {
            throw new IllegalStateException("The game is not frozen: the test's group needs environment = FrozenGame.class");
        }
    }
}
