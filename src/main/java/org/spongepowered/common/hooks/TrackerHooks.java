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
package org.spongepowered.common.hooks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public interface TrackerHooks {

    default void run(
        final Runnable process, final String name,
        final int chunkX, final int chunkZ
    ) {
        process.run();
    }

    default void incrementUnabsorbedTransaction(final Supplier<String> toGenericString) {

    }

    default void incrementIllegalThreadAccess(final Thread currentThread, final Thread sidedThread) {
    }

    default void incrementBlocksRestored(final ServerLevel world, final BlockPos pos, final BlockState replaced) {

    }

    default void updateChunkGauge(final ServerLevel level) {
    }

    default void updateServerTickTime(final long tickTime) {

    }

    default void updateWorldTps(final ServerLevel serverLevel) {

    }

    default void updateFakePlayerCount(final IntSupplier count) {

    }

    default void updatePooledSnapshotBuilder(final IntSupplier count) {

    }
}
