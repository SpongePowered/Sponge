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
package org.spongepowered.common.mixin.core.world.level.chunk.storage;

import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.util.thread.StrictQueue;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.slf4j.Logger;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.world.chunk.ChunkEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.world.level.chunk.storage.IOWorker$PendingStoreAccessor;
import org.spongepowered.common.bridge.world.level.chunk.storage.IOWorkerBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.world.level.chunk.SpongeUnloadedChunkException;
import org.spongepowered.common.world.level.chunk.storage.SpongeIOWorkerType;
import org.spongepowered.math.vector.Vector3i;

import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Mixin(IOWorker.class)
public abstract class IOWorkerMixin implements IOWorkerBridge {

    // @formatter:off
    @Shadow @Final private static Logger LOGGER;

    @Shadow @Final private AtomicBoolean shutdownRequested;
    @Shadow @Final private ProcessorMailbox<StrictQueue.IntRunnable> mailbox;
    @Shadow @Final private RegionFileStorage storage;
    @Shadow @Final private Map<ChunkPos, IOWorker$PendingStoreAccessor> pendingWrites;

    @Shadow protected abstract boolean shadow$isOldChunk(CompoundTag $$0);
    @Shadow protected abstract void shadow$tellStorePending();
    // @formatter:on

    // We only set these for chunk and entity related IO workers
    @MonotonicNonNull private SpongeIOWorkerType impl$type;
    @MonotonicNonNull private ResourceKey<Level> impl$dimension;

    @Override
    public void bridge$setDimension(final SpongeIOWorkerType type, final ResourceKey<Level> dimension) {
        this.impl$type = type;
        this.impl$dimension = dimension;
    }

    @Inject(method = "runStore", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Ljava/util/concurrent/CompletableFuture;complete(Ljava/lang/Object;)Z"))
    private void impl$onSaved(final ChunkPos param0, final @Coerce Object param1, final CallbackInfo ci) {
        if (this.impl$type == SpongeIOWorkerType.CHUNK) {
            if (ShouldFire.CHUNK_EVENT_BLOCKS_SAVE_POST) {
                final Vector3i chunkPos = new Vector3i(param0.x, 0, param0.z);
                final ChunkEvent.Blocks.Save.Post postSave = SpongeEventFactory.createChunkEventBlocksSavePost(PhaseTracker.getInstance().currentCause(), chunkPos,
                        (org.spongepowered.api.ResourceKey) (Object) this.impl$dimension.location());
                SpongeCommon.post(postSave);
            }
        }
        else if (this.impl$type == SpongeIOWorkerType.ENTITY) {
            if (ShouldFire.CHUNK_EVENT_ENTITIES_SAVE_POST) {
                final Vector3i chunkPos = new Vector3i(param0.x, 0, param0.z);
                final ChunkEvent.Entities.Save.Post postSave = SpongeEventFactory.createChunkEventEntitiesSavePost(PhaseTracker.getInstance().currentCause(), chunkPos,
                        (org.spongepowered.api.ResourceKey) (Object) this.impl$dimension.location());
                SpongeCommon.post(postSave);
            }
        }
    }

    /**
     * @author aromaa - December 17th, 2023 - 1.19.4
     * @reason Fixes a deadlock when the world is unloading/unloaded.
     * This method is called from a non server threads and there is a chance
     * that the IOWorker is closed before it has the chance to schedule it.
     * After the IOWorker has been closed it no longer completes any further
     * tasks causing the join to wait indefinitely. Fixes this by submitting
     * the task directly instead of indirectly from the background executor
     * and not blocking inside of it.
     */
    @Overwrite
    private CompletableFuture<BitSet> createOldDataForRegion(final int x, final int z) {
        //The impl$submitTaskCancellable is related to another fix for a separate deadlock case.
        //The returned future is used inside the isOldChunkAround which tries to wait for the
        //completion but also ends up deadlocking when the IOWorker closes. This is fixed by
        //throwing a special exception when the IOWorker is no longer accepting new tasks.
        //See ChunkStatusMixin
        return this.impl$submitTaskCancellable(() -> { //Sponge: Use submitTask instead
            try {
                final ChunkPos min = ChunkPos.minFromRegion(x, z);
                final ChunkPos max = ChunkPos.maxFromRegion(x, z);
                final BitSet oldDataRegions = new BitSet();
                ChunkPos.rangeClosed(min, max).forEach(pos -> {
                    final CollectFields fields = new CollectFields(new FieldSelector(IntTag.TYPE, "DataVersion"), new FieldSelector(CompoundTag.TYPE, "blending_data"));

                    try {
                        //Sponge start: unwrapped the scanChunk content to not submit another task
                        final IOWorker$PendingStoreAccessor store = this.pendingWrites.get(pos);
                        if (store != null) {
                            if (store.accessor$data() != null) {
                                store.accessor$data().acceptAsRoot(fields);
                            }
                        } else {
                            this.storage.scanChunk(pos, fields);
                        }
                        //Sponge end
                    } catch (final Exception e) {
                        IOWorkerMixin.LOGGER.warn("Failed to scan chunk {}", pos, e);
                        return;
                    }

                    final Tag result = fields.getResult();
                    if (result instanceof final CompoundTag compoundTag && this.shadow$isOldChunk(compoundTag)) {
                        final int regionIndex = pos.getRegionLocalZ() * 32 + pos.getRegionLocalX();
                        oldDataRegions.set(regionIndex);
                    }
                });
                return Either.left(oldDataRegions);
            } catch (final Exception e) {
                return Either.right(e);
            }
        });
    }

    private <T> CompletableFuture<T> impl$submitTaskCancellable(final Supplier<Either<T, Exception>> supplier) {
        final CompletableFuture<T> future = this.mailbox.askEither(processor -> new StrictQueue.IntRunnable(0, () -> {
            if (!this.shutdownRequested.get()) {
                processor.tell(supplier.get());
            } else {
                processor.tell(Either.right(SpongeUnloadedChunkException.INSTANCE)); //Sponge: Complete exceptionally if shutdown was requested
            }

            this.shadow$tellStorePending();
        }));

        //Sponge start: Complete exceptionally if shutdown was requested
        if (this.shutdownRequested.get()) {
            future.completeExceptionally(SpongeUnloadedChunkException.INSTANCE);
        }
        //Sponge end

        return future;
    }
}
