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
package org.spongepowered.common.event.tracking.context.transaction.pipeline;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.transaction.EffectTransactor;
import org.spongepowered.common.event.tracking.context.transaction.ResultingTransactionBySideEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.EffectResult;
import org.spongepowered.common.event.tracking.context.transaction.effect.PipelineCursor;
import org.spongepowered.common.event.tracking.context.transaction.effect.ProcessingSideEffect;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class TileEntityPipeline implements BlockPipeline {

    private final @Nullable Supplier<Chunk> chunkSupplier;
    private final @Nullable Supplier<ServerWorld> serverWorld;
    private final @Nullable Supplier<ChunkSection> sectionSupplier;
    private final List<ResultingTransactionBySideEffect> effects;

    private TileEntityPipeline(final Builder builder) {
        this.chunkSupplier = builder.chunkSupplier;
        this.serverWorld = builder.serverWorld;
        this.sectionSupplier = builder.sectionSupplier;
        this.effects = builder.effects;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder kickOff(final ServerWorld world, final BlockPos pos) {
        final WeakReference<ServerWorld> worldRef = new WeakReference<>(world);
        final Chunk chunk = world.getChunkAt(pos);
        final WeakReference<Chunk> chunkRef = new WeakReference<>(chunk);
        final WeakReference<ChunkSection> sectionRef = new WeakReference<>(chunk.getSections()[pos.getY() >> 4]);
        final Supplier<ServerWorld> worldSupplier = () -> Objects.requireNonNull(worldRef.get(), "ServerWorld de-referenced");
        final Supplier<Chunk> chunkSupplier = () -> Objects.requireNonNull(chunkRef.get(), "Chunk de-referenced");
        final Supplier<ChunkSection> chunkSectionSupplier = () -> Objects.requireNonNull(sectionRef.get(), "ChunkSection de-referenced");
        return builder()
            .chunk(chunkSupplier)
            .world(worldSupplier)
            .chunkSection(chunkSectionSupplier);
    }

    @Override
    public ServerWorld getServerWorld() {
        return Objects.requireNonNull(this.serverWorld, "ServerWorld Supplier is null in TileEntityPipeline").get();
    }

    @Override
    public Chunk getAffectedChunk() {
        return Objects.requireNonNull(this.chunkSupplier, "Chunk Supplier is null in TileEntityPipeline").get();
    }

    @Override
    public ChunkSection getAffectedSection() {
        return Objects.requireNonNull(this.sectionSupplier, "ChunkSection Supplier is null in TileEntityPipeline").get();
    }

    @Override
    public boolean wasEmpty() {
        return false;
    }

    public boolean processEffects(final PhaseContext<?> context, final PipelineCursor initialCursor) {
        PipelineCursor currentCursor = initialCursor;
        for (final ResultingTransactionBySideEffect effect : this.effects) {
            try (final EffectTransactor ignored = context.getTransactor().pushEffect(effect)) {
                final EffectResult result = effect.effect.processSideEffect(
                    this,
                    currentCursor,
                    currentCursor.state,
                    (SpongeBlockChangeFlag) BlockChangeFlags.NONE
                );
                if (result.resultingState != currentCursor.state) {
                    currentCursor = new PipelineCursor(
                        result.resultingState,
                        currentCursor.opacity,
                        currentCursor.pos,
                        currentCursor.tileEntity
                    );
                }
                if (result.hasResult) {
                    return result.resultingState != null;
                }
            }
        }
        return false;
    }

    public static final class Builder {

        @Nullable Supplier<ServerWorld> serverWorld;
        @Nullable Supplier<Chunk> chunkSupplier;
        @Nullable Supplier<ChunkSection> sectionSupplier;
        List<ResultingTransactionBySideEffect> effects;

        public Builder addEffect(final ProcessingSideEffect effect) {
            if (this.effects == null) {
                this.effects = new LinkedList<>();
            }
            this.effects.add(new ResultingTransactionBySideEffect(Objects.requireNonNull(effect, "Effect is null")));
            return this;
        }

        public Builder chunk(final Supplier<Chunk> chunk) {
            this.chunkSupplier = chunk;
            return this;
        }

        public Builder chunkSection(final Supplier<ChunkSection> section) {
            this.sectionSupplier = section;
            return this;
        }

        public Builder world(final Supplier<ServerWorld> world) {
            this.serverWorld = world;
            return this;
        }

        public TileEntityPipeline build() {
            if (this.effects == null) {
                this.effects = Collections.emptyList();
            }
            Objects.requireNonNull(this.serverWorld, "ServerWorld must have been recorded!");
            Objects.requireNonNull(this.chunkSupplier, "Chunk must have been recorded!");
            return new TileEntityPipeline(this);
        }

    }
}
