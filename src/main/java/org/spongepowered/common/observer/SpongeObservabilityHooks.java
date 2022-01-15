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
package org.spongepowered.common.observer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.common.hooks.TrackerHooks;
import org.spongepowered.observer.metrics.Meter;
import org.spongepowered.observer.metrics.meter.Counter;
import org.spongepowered.observer.metrics.meter.Gauge;
import org.spongepowered.observer.metrics.meter.Histogram;

import java.util.function.Supplier;

public class SpongeObservabilityHooks implements TrackerHooks {
    private static final Histogram PHASE_TICK_DURATION = Meter.newHistogram()
        .name("sponge", "phase_tracker", "tick_duration", "seconds")
        .help("Duration of tileentity tick calls?")
        .labelNames("phase", "chunk_x", "chunk_z")
        .exponentialBuckets(1E-9, 10, 10)
        .build();

    private static final Counter MISSED_TRANSACTION_CAPTURE = Meter.newCounter()
        .name("sponge", "phase_tracker", "missed_transaction_capture", "count")
        .help("Count of un-capturable transactions")
        .labelNames("transaction_name")
        .build();

    private static final Counter ASYNC_THREAD_ACCESS_COUNT = Meter.newCounter()
        .name("sponge", "phase_tracker", "async_thread_access", "count")
        .help("Number of async thread accesses to the PhaseTracker (should be 0)")
        .labelNames("requesting_thread", "target_thread")
        .build();

    private static final Counter BLOCKS_RESTORED = Meter.newCounter()
        .name("sponge", "events", "blocks_restored", "count")
        .help("Number of blocks restored")
        .labelNames("world")
        .build();

    private static Gauge CHUNKS_LOADED = Meter.newGauge()
        .name("minecraft", "chunks", "loaded")
        .help("Gauge of chunks loaded overall")
        .labelNames("world")
        .build();

    @Override
    public void run(
        final Runnable process, final String name,
        final int chunkX, final int chunkZ
    ) {
        SpongeObservabilityHooks.PHASE_TICK_DURATION.time(process, name, chunkX, chunkZ);
    }

    @Override
    public void incrementUnabsorbedTransaction(final Supplier<String> toGenericString) {
        SpongeObservabilityHooks.MISSED_TRANSACTION_CAPTURE.inc(toGenericString.get());
    }

    @Override
    public void incrementIllegalThreadAccess(final Thread currentThread, final Thread sidedThread) {
        SpongeObservabilityHooks.ASYNC_THREAD_ACCESS_COUNT.inc(currentThread.getName(), sidedThread.getName());
    }

    @Override
    public void incrementBlocksRestored(
        final ServerLevel world, final BlockPos pos, final BlockState replaced
    ) {
        SpongeObservabilityHooks.BLOCKS_RESTORED.inc(world.dimension().location().toString());
    }

    @Override
    public void updateChunkGauge(final ServerLevel level) {
        SpongeObservabilityHooks.CHUNKS_LOADED.set(level.getChunkSource().getLoadedChunksCount(), level.dimension().location().toString());
    }
}
