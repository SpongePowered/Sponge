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
package org.spongepowered.common.world.pregen;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.GenericMath;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.world.ChunkPreGenerationEvent;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.ChunkPreGenerate;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.chunk.ChunkProviderServerBridge;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

public class SpongeChunkPreGenerateTask implements ChunkPreGenerate, Consumer<Task> {

    private static final int DEFAULT_TICK_INTERVAL = 4;
    private static final float DEFAULT_TICK_PERCENT = 0.8f;

    private static final Vector3i[] OFFSETS = {
            Vector3i.UNIT_Z.negate().mul(2),
            Vector3i.UNIT_X.mul(2),
            Vector3i.UNIT_Z.mul(2),
            Vector3i.UNIT_X.negate().mul(2)
    };

    private final Scheduler scheduler;

    private final World world;
    private final Predicate<Vector3i> doesChunkExistCheck;
    private final int chunkRadius;
    private final int chunkCount;
    private final float tickPercent;
    private final long tickTimeLimit;
    private final Cause cause;
    private final int totalChunksToGenerate;
    private final Task spongeTask;
    private final int tickInterval;
    private final Object plugin;

    // If null, no listeners have been assigned, so they don't need to be registered or unregistered.
    @Nullable private final EventListener<ChunkPreGenerationEvent> eventListener;

    private Vector3i currentPosition;
    private int currentGenCount;
    private int currentLayer;
    private int currentIndex;
    private int nextJump;

    private int chunksSkipped = 0;
    private int chunksGenerated = 0;

    // Used for wall clock times.
    private long generationStartTime = 0;
    private long generationEndTime = 0;
    private boolean isCancelled = false;

    private SpongeChunkPreGenerateTask(Object plugin,
            World world, Vector3d center, double diameter, int chunkCount, float tickPercent, int tickInterval, Cause cause,
            List<Consumer<ChunkPreGenerationEvent>> eventListeners) {

        this.scheduler = Sponge.getScheduler();
        int preferredTickInterval = this.scheduler.getPreferredTickInterval();

        this.plugin = plugin;
        this.world = world;

        // In order to be able to check whether a chunk exists, we could use standard Sponge API methods. However,
        // because they set up an async method which we need to get sync anyway, we just bypass it.
        // This results in a extremely noticeable speed improvement.
        //
        // This also allows us to catch non Anvil file formats too.
        if (world.getWorldStorage() instanceof ChunkProviderServerBridge) {
            this.doesChunkExistCheck = this::checkChunkExistsAnvil;
        } else {
            this.doesChunkExistCheck = v -> false;
        }

        this.chunkRadius = GenericMath.floor(diameter / 32);
        this.chunkCount = chunkCount;
        this.tickPercent = tickPercent;
        this.tickTimeLimit = Math.round(preferredTickInterval * tickPercent);
        this.cause = cause;
        this.tickInterval = tickInterval;
        final Optional<Vector3i> currentPosition = SpongeChunkLayout.instance.toChunk(center.toInt());
        if (currentPosition.isPresent()) {
            this.currentPosition = currentPosition.get();
        } else {
            throw new IllegalArgumentException("Center is not a valid chunk coordinate");
        }
        this.currentGenCount = 4;
        this.currentLayer = 0;
        this.currentIndex = 0;
        this.nextJump = 0;

        this.totalChunksToGenerate = (int) Math.pow(this.chunkRadius * 2 + 1, 2);

        this.spongeTask = this.scheduler
                .createTaskBuilder()
                .intervalTicks(tickInterval)
                .execute(this)
                .submit(plugin);

        if (!eventListeners.isEmpty()) {
            this.eventListener = new SpongeChunkPreGenerateListener(this.spongeTask.getUniqueId(), eventListeners);
            Sponge.getEventManager().registerListener(plugin, ChunkPreGenerationEvent.class, this.eventListener);
        } else {
            this.eventListener = null;
        }
    }

    Task getSpongeTask() {
        return this.spongeTask;
    }

    @Override
    public WorldProperties getWorldProperties() {
        return this.world.getProperties();
    }

    @Override
    public int getTotalGeneratedChunks() {
        return this.chunksGenerated;
    }

    @Override
    public int getTotalSkippedChunks() {
        return this.chunksSkipped;
    }

    @Override
    public int getTargetTotalChunks() {
        return this.totalChunksToGenerate;
    }

    @Override
    public Duration getTotalTime() {
        return Duration.of((isCancelled() ? this.generationEndTime : System.currentTimeMillis()) - this.generationStartTime, ChronoUnit.MILLIS);
    }

    @Override
    public boolean isCancelled() {
        if (this.isCancelled) {
            return true;
        }

        // It's possible we haven't cancelled the task here, so we just make sure of it, and perform
        // some cleanup.
        if (!this.scheduler.getTaskById(this.spongeTask.getUniqueId()).isPresent()) {
            cancel();
        }

        return this.isCancelled;
    }

    @Override
    public void cancel() {
        if (!this.isCancelled) {
            if (this.eventListener != null) {
                Sponge.getEventManager().unregisterListeners(this.eventListener);
            }
            this.spongeTask.cancel();
            this.isCancelled = true;
        }
    }

    @Override
    public void accept(Task task) {
        final long stepStartTime = System.currentTimeMillis();
        if (this.generationStartTime == 0) {
            this.generationStartTime = stepStartTime;
        }

        // Create and fire event.
        ChunkPreGenerationEvent.Pre preEvent = SpongeEventFactory.createChunkPreGenerationEventPre(
                this.cause,
                this,
                this.world,
                false
        );

        if (Sponge.getEventManager().post(preEvent)) {
            // Cancelled event = cancelled task.
            cancelTask(task);
            return;
        }

        if (preEvent.getSkipStep()) {
            // Skip the step, but don't cancel the task.
            return;
        }

        // Count how many chunks are generated during the tick
        int count = 0;
        int skipped = 0;
        do {
            final Vector3i position = nextChunkPosition();
            final Vector3i pos1 = position.sub(Vector3i.UNIT_X);
            final Vector3i pos2 = position.sub(Vector3i.UNIT_Z);
            final Vector3i pos3 = pos2.sub(Vector3i.UNIT_X);

            // We can only skip generation if all chunks are loaded.
            if (!areAllChunksLoaded(position, pos1, pos2, pos3)) {

                // At least one chunk isn't generated, so to populate, we need to load them all.
                this.world.loadChunk(position, true);
                this.world.loadChunk(pos1, true);
                this.world.loadChunk(pos2, true);
                this.world.loadChunk(pos3, true);

                count += this.currentGenCount;
            } else {

                // Skipped them, log this.
                skipped += this.currentGenCount;
            }
        } while (hasNextChunkPosition() && checkChunkCount(count) && checkTickTime(System.currentTimeMillis() - stepStartTime));

        this.chunksGenerated += count;
        this.chunksSkipped += skipped;

        final long deltaTime = System.currentTimeMillis() - stepStartTime;
        this.generationEndTime = System.currentTimeMillis();

        // Create and fire event.
        if (Sponge.getEventManager().post(SpongeEventFactory.createChunkPreGenerationEventPost(
                this.cause,
                this,
                this.world,
                Duration.ofMillis(deltaTime),
                count,
                skipped
        ))) {
            cancelTask(task);
            return;
        }

        if (!hasNextChunkPosition()) {
            // Generation has completed.
            Sponge.getEventManager().post(SpongeEventFactory.createChunkPreGenerationEventComplete(
                    this.cause,
                    this,
                    this.world
            ));

            this.isCancelled = true;
            unregisterListener();
            task.cancel();
        }
    }

    private boolean areAllChunksLoaded(Vector3i chunk1, Vector3i chunk2, Vector3i chunk3, Vector3i chunk4) {
        return this.doesChunkExistCheck.test(chunk1) && this.doesChunkExistCheck.test(chunk2) &&
                this.doesChunkExistCheck.test(chunk3) && this.doesChunkExistCheck.test(chunk4);
    }

    private void unregisterListener() {
        if (this.eventListener != null) {
            Sponge.getEventManager().unregisterListeners(this.eventListener);
        }
    }

    private void cancelTask(Task task) {
        // Don't fire multiple instances.
        if (this.scheduler.getTaskById(task.getUniqueId()).isPresent()) {
            Sponge.getEventManager().post(SpongeEventFactory.createChunkPreGenerationEventCancelled(this.cause, this, this.world));
            task.cancel();
        }

        this.isCancelled = true;
        unregisterListener();
    }

    private boolean hasNextChunkPosition() {
        return this.currentLayer <= this.chunkRadius;
    }

    private Vector3i nextChunkPosition() {
        final Vector3i nextPosition = this.currentPosition;
        final int currentLayerIndex;
        if (this.currentIndex >= this.nextJump) {
            // Reached end of layer, jump to the next so we can keep spiralling
            this.currentPosition = this.currentPosition.sub(Vector3i.UNIT_X).sub(Vector3i.UNIT_Z);
            this.currentLayer++;
            // Each the jump increment increases by 4 at each new layer
            this.nextJump += this.currentLayer * 4;
            currentLayerIndex = 1;
        } else {
            // Get the current index since the last jump
            currentLayerIndex = this.currentIndex - (this.nextJump - this.currentLayer * 4);
            // Move to next position in layer, by following a square
            this.currentPosition = this.currentPosition.add(OFFSETS[currentLayerIndex / this.currentLayer]);
        }
        // If we're at the corner it's 3, else 2 for an edge
        this.currentGenCount = currentLayerIndex % this.currentLayer == 0 ? 3 : 2;
        this.currentIndex++;
        return nextPosition;
    }

    private boolean checkChunkCount(int count) {
        return this.chunkCount <= 0 || count < this.chunkCount;
    }

    private boolean checkTickTime(long tickTime) {
        return this.tickPercent <= 0 || tickTime < this.tickTimeLimit;
    }

    private boolean checkChunkExistsAnvil(Vector3i v) {
        CompletableFuture<Boolean> ret = ((ChunkProviderServerBridge) this.world.getWorldStorage()).bridge$doesChunkExistSync(v);
        try {
            return ret.get();
        } catch (InterruptedException | ExecutionException e) {
            SpongeImpl.getLogger().error(
                    "Could not determine chunk's existence on world {}: {} {}. Assuming false.",
                this.world.getName(), v.getX(), v.getZ());
            return false;
        }
    }

    public static class Builder implements ChunkPreGenerate.Builder {

        private static final String TIME_FORMAT = "s's 'S'ms'";

        private final World world;
        private final Vector3d center;
        private final double diameter;
        private final List<Consumer<ChunkPreGenerationEvent>> eventListeners = new ArrayList<>();

        @Nullable private Object plugin;
        private int tickInterval = DEFAULT_TICK_INTERVAL;
        private float tickPercent = DEFAULT_TICK_PERCENT;
        private int chunksPerTick = 0;

        public Builder(World world, Vector3d center, double diameter) {
            this.world = world;
            this.center = center;
            this.diameter = diameter;
        }

        public Builder(World world, WorldBorder worldBorder) {
            this(world, worldBorder.getCenter(), worldBorder.getNewDiameter());
        }

        @Override
        public ChunkPreGenerate.Builder owner(Object plugin) {
            checkNotNull(plugin, "plugin cannot be null");
            this.plugin = plugin;
            return this;
        }

        @Override
        public ChunkPreGenerate.Builder logger(@Nullable Logger logger) {
            if (logger != null) {
                this.addListener(event -> {
                    if (event instanceof ChunkPreGenerationEvent.Post) {
                        ChunkPreGenerationEvent.Post post = (ChunkPreGenerationEvent.Post) event;
                        logger.info("Generated {} chunks in {}, {}% complete", post.getChunksGeneratedThisStep(),
                            DurationFormatUtils.formatDuration(post.getTimeTakenForStep().toMillis(), TIME_FORMAT, false),
                            GenericMath.floor(
                                100 * (post.getChunkPreGenerate().getTotalGeneratedChunks() + post.getChunkPreGenerate().getTotalSkippedChunks())
                                    / post.getChunkPreGenerate().getTargetTotalChunks())
                        );
                    } else if (event instanceof ChunkPreGenerationEvent.Complete) {
                        logger.info("Done! Generated a total of {} chunks in {}", event.getChunkPreGenerate().getTargetTotalChunks(),
                            DurationFormatUtils.formatDuration(event.getChunkPreGenerate().getTotalTime().toMillis(), TIME_FORMAT, false));
                    }
                });
            }

            return this;
        }

        @Override
        public ChunkPreGenerate.Builder tickInterval(int tickInterval) {
            checkArgument(tickInterval > 0, "tickInterval must be greater than zero");
            this.tickInterval = tickInterval;
            return this;
        }

        @Override
        public ChunkPreGenerate.Builder chunksPerTick(int chunkCount) {
            this.chunksPerTick = chunkCount;
            return this;
        }

        @Override
        public ChunkPreGenerate.Builder tickPercentLimit(float tickPercent) {
            checkArgument(tickPercent <= 1, "tickPercent must be smaller or equal to 1");
            checkArgument(tickPercent > 0, "tickPercent must be greater than 0");
            this.tickPercent = tickPercent;
            return this;
        }

        @Override
        public ChunkPreGenerate.Builder addListener(Consumer<ChunkPreGenerationEvent> listener) {
            checkNotNull(listener, "listener cannot be null");
            this.eventListeners.add(listener);
            return this;
        }

        @Override
        public ChunkPreGenerate start() {
            checkNotNull(this.plugin, "owner cannot be null");
            checkArgument(this.chunksPerTick > 0 || this.tickPercent > 0, "Must use at least one of \"chunks per tick\" or \"tick percent limit\"");
            Sponge.getCauseStackManager().pushCause(this.plugin);
            Cause cause = Sponge.getCauseStackManager().getCurrentCause();
            Sponge.getCauseStackManager().popCause();
            return new SpongeChunkPreGenerateTask(this.plugin, this.world, this.center, this.diameter, this.chunksPerTick, this.tickPercent,
                    this.tickInterval, cause, this.eventListeners);
        }

        @Override
        public ChunkPreGenerate.Builder from(ChunkPreGenerate value) {
            if (!(value instanceof SpongeChunkPreGenerateTask)) {
                throw new IllegalArgumentException("Not a Sponge chunk pre-gen task");
            }

            final SpongeChunkPreGenerateTask other = (SpongeChunkPreGenerateTask) value;
            // Bypass null check
            this.plugin = other.plugin;
            return tickInterval(other.tickInterval)
                    .chunksPerTick(other.chunkCount)
                    .tickPercentLimit(other.tickPercent);
        }

        @Override
        public ChunkPreGenerate.Builder reset() {
            this.plugin = null;
            this.tickInterval = 0;
            this.chunksPerTick = 0;
            this.tickPercent = DEFAULT_TICK_PERCENT;
            this.eventListeners.clear();
            return this;
        }
    }

}
