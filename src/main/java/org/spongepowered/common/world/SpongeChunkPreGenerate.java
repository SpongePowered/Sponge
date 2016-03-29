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
package org.spongepowered.common.world;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.GenericMath;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.common.scheduler.SpongeScheduler;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;

import javax.annotation.Nullable;

public class SpongeChunkPreGenerate implements WorldBorder.ChunkPreGenerate {

    private static final int DEFAULT_TICK_INTERVAL = 10;
    private static final float DEFAULT_TICK_PERCENT = 0.15f;
    private final World world;
    private final Vector3d center;
    private final double diameter;
    @Nullable private Object plugin = null;
    @Nullable private Logger logger = null;
    private int tickInterval = DEFAULT_TICK_INTERVAL;
    private int chunkCount = 0;
    private float tickPercent = DEFAULT_TICK_PERCENT;

    public SpongeChunkPreGenerate(World world, Vector3d center, double diameter) {
        this.world = world;
        this.center = center;
        this.diameter = diameter;
    }

    @Override
    public WorldBorder.ChunkPreGenerate owner(Object plugin) {
        checkNotNull(plugin, "plugin");
        this.plugin = plugin;
        return this;
    }

    @Override
    public WorldBorder.ChunkPreGenerate logger(@Nullable Logger logger) {
        this.logger = logger;
        return this;
    }

    @Override
    public WorldBorder.ChunkPreGenerate tickInterval(int tickInterval) {
        checkArgument(tickInterval > 0, "tickInterval must be greater than zero");
        this.tickInterval = tickInterval;
        return this;
    }

    @Override
    public WorldBorder.ChunkPreGenerate chunksPerTick(int chunkCount) {
        this.chunkCount = chunkCount;
        return this;
    }

    @Override
    public WorldBorder.ChunkPreGenerate tickPercentLimit(float tickPercent) {
        checkArgument(tickPercent <= 1, "tickPercent must be smaller or equal to 1");
        this.tickPercent = tickPercent;
        return this;
    }

    @Override
    public Task start() {
        checkNotNull(this.plugin, "owner not set");
        checkArgument(this.chunkCount > 0 || this.tickPercent > 0, "Must use at least one of \"chunks per tick\" or \"tick percent limit\"");
        return Task.builder().name(toString())
            .execute(new ChunkPreGenerator(this.world, this.center, this.diameter, this.chunkCount, this.tickPercent, this.logger))
            .intervalTicks(this.tickInterval).submit(this.plugin);
    }

    @Override
    public WorldBorder.ChunkPreGenerate from(Task value) {
        if (!(value instanceof SpongeChunkPreGenerate)) {
            throw new IllegalArgumentException("Not a chunk pre-gen task");
        }
        final SpongeChunkPreGenerate other = (SpongeChunkPreGenerate) value;
        // Bypass null check
        this.plugin = other.plugin;
        return logger(other.logger).tickInterval(other.tickInterval).chunksPerTick(other.chunkCount).tickPercentLimit(other.tickPercent);
    }

    @Override
    public WorldBorder.ChunkPreGenerate reset() {
        this.plugin = null;
        this.logger = null;
        this.tickInterval = 0;
        this.chunkCount = 0;
        this.tickPercent = DEFAULT_TICK_PERCENT;
        return this;
    }

    @Override
    public String toString() {
        return "SpongeChunkPreGen{" +
            "center=" + this.center +
            ", diameter=" + this.diameter +
            ", plugin=" + this.plugin +
            ", world=" + this.world +
            ", tickInterval=" + this.tickInterval +
            ", chunkCount=" + this.chunkCount +
            ", tickPercent=" + this.tickPercent +
            '}';
    }

    private static class ChunkPreGenerator implements Consumer<Task> {

        private static final Vector3i[] OFFSETS = {
            Vector3i.UNIT_X,
            Vector3i.UNIT_Z,
            Vector3i.UNIT_X.negate(),
            Vector3i.UNIT_Z.negate()
        };
        private static final String TIME_FORMAT = "s's 'S'ms'";
        private final World world;
        private final int chunkRadius;
        private final int chunkCount;
        private final float tickPercent;
        private final long tickTimeLimit;
        @Nullable private final Logger logger;
        private final Queue<Chunk> unloadQueue = new ArrayDeque<>();
        private final int unloadQueueThreshold;
        private Vector3i currentPosition;
        private int currentLayerIndex;
        private int currentLayerSize;
        private int currentIndexInLayer;
        private int totalCount;
        private long totalTime;

        ChunkPreGenerator(World world, Vector3d center, double diameter, int chunkCount, float tickPercent, @Nullable Logger logger) {
            this.world = world;
            this.chunkRadius = GenericMath.floor(diameter / 32);
            this.chunkCount = chunkCount;
            this.tickPercent = tickPercent;
            this.logger = logger;
            this.tickTimeLimit = Math.round(SpongeScheduler.getInstance().getPreferredTickInterval() * tickPercent);
            // Enough chunks to be for the last two layers to be full, so adjacent chunks always exist
            this.unloadQueueThreshold = 4 * this.chunkRadius - 2;
            final Optional<Vector3i> currentPosition = SpongeChunkLayout.instance.toChunk(center.toInt());
            if (currentPosition.isPresent()) {
                this.currentPosition = currentPosition.get();
            } else {
                throw new IllegalArgumentException("Center is not a valid chunk coordinate");
            }
            this.currentLayerIndex = 0;
            this.currentLayerSize = 0;
            this.currentIndexInLayer = 0;
            this.totalCount = 0;
            this.totalTime = 0;
        }

        @Override
        public void accept(Task task) {
            final long startTime = System.currentTimeMillis();
            int count = 0;
            do {
                this.world.loadChunk(nextChunkPosition(), true).ifPresent(this.unloadQueue::add);
                if (this.unloadQueue.size() > this.unloadQueueThreshold) {
                    this.unloadQueue.remove();
                }
            } while (hasNextChunkPosition() && checkChunkCount(++count) && checkTickTime(System.currentTimeMillis() - startTime));
            if (this.logger != null) {
                this.totalCount += count;
                final long deltaTime = System.currentTimeMillis() - startTime;
                this.totalTime += deltaTime;
                this.logger.info("Generated {} chunks in {}, {}% complete. Currently {} chunks are kept loaded", count,
                    DurationFormatUtils.formatDuration(deltaTime, TIME_FORMAT, false),
                    GenericMath.floor(this.totalCount / Math.pow(this.chunkRadius * 2 + 1, 2) * 100),
                    this.unloadQueue.size()
                );
            }
            if (!hasNextChunkPosition()) {
                if (this.logger != null) {
                    this.logger.info("Done! Generated a total of {} chunks in {}", this.totalCount,
                        DurationFormatUtils.formatDuration(this.totalTime, TIME_FORMAT, false));
                }
                this.unloadQueue.clear();
                task.cancel();
            }
        }

        private boolean hasNextChunkPosition() {
            return this.currentLayerIndex <= this.chunkRadius;
        }

        private Vector3i nextChunkPosition() {
            final Vector3i nextPosition = this.currentPosition;
            if (++this.currentIndexInLayer >= this.currentLayerSize * 4) {
                this.currentLayerIndex++;
                this.currentLayerSize += 2;
                this.currentIndexInLayer = 0;
                this.currentPosition = this.currentPosition.sub(Vector3i.UNIT_Z).sub(Vector3i.UNIT_X);
            }
            this.currentPosition = this.currentPosition.add(OFFSETS[this.currentIndexInLayer / this.currentLayerSize]);
            return nextPosition;
        }

        private boolean checkChunkCount(int count) {
            return this.chunkCount <= 0 || count < this.chunkCount;
        }

        private boolean checkTickTime(long tickTime) {
            return this.tickPercent <= 0 || tickTime < this.tickTimeLimit;
        }

    }

}
