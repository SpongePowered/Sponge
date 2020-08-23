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

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.transaction.EffectTransactor;
import org.spongepowered.common.event.tracking.context.transaction.ResultingTransactionBySideEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.EffectResult;
import org.spongepowered.common.event.tracking.context.transaction.effect.PipelineCursor;
import org.spongepowered.common.event.tracking.context.transaction.effect.ProcessingSideEffect;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class WorldPipeline implements BlockPipeline {

    private final Supplier<Chunk> chunkSupplier;
    private final Supplier<ServerWorld> serverWorld;
    private final Supplier<ChunkSection> sectionSupplier;
    private final boolean wasEmpty;
    private final List<ResultingTransactionBySideEffect> worldEffects;
    private final ChunkPipeline chunkPipeline;

    WorldPipeline(final Builder builder) {
        this.chunkSupplier = builder.chunkSupplier;
        this.worldEffects = builder.effects;
        this.serverWorld = builder.serverWorld;
        this.sectionSupplier = builder.sectionSupplier;
        this.wasEmpty = Objects.requireNonNull(builder.sectionSupplier).get().isEmpty();
        this.chunkPipeline = builder.chunkPipeline;
    }

    public ServerWorld getServerWorld() {
        return Objects.requireNonNull(this.serverWorld, "ServerWorld Supplier is null in ChunkPipeline").get();
    }

    @Override
    public Chunk getAffectedChunk() {
        return Objects.requireNonNull(this.chunkSupplier, "Chunk Supplier is null in ChunkPipeline").get();
    }

    @Override
    public ChunkSection getAffectedSection() {
        return Objects.requireNonNull(this.sectionSupplier, "ChunkSection Supplier is null in ChunkPipeline").get();
    }

    public boolean processEffects(final PhaseContext<?> context, final BlockState currentState,
        final BlockState newProposedState, final BlockPos pos, final SpongeBlockChangeFlag flag
    ) {
        if (this.worldEffects.isEmpty()) {
            return false;
        }
        final ServerWorld serverWorld = Objects.requireNonNull(this.serverWorld).get();
        // We have to get the "old state" from
        final BlockState oldState = this.chunkPipeline.processChange(context, currentState, newProposedState, pos);
        if (oldState == null) {
            return false;
        }
        final int oldOpacity = oldState.getOpacity(serverWorld, pos);
        final @Nullable TileEntity existing = this.chunkSupplier.get().getTileEntity(pos, Chunk.CreateEntityType.CHECK);
        final PipelineCursor formerState = new PipelineCursor(oldState, oldOpacity, pos, existing);

        for (final ResultingTransactionBySideEffect effect : this.worldEffects) {
            try (final EffectTransactor ignored = context.getTransactor().pushEffect(effect)) {
                final EffectResult result = effect.effect.processSideEffect(
                    this,
                    formerState,
                    newProposedState,
                    flag
                );
                if (result.hasResult) {
                    return result.resultingState != null;
                }
            }
        }
        // if we've gotten here, means something is wrong, we didn't build our effects right.
        return false;
    }

    public static Builder builder(final ChunkPipeline pipeline) {
        return new Builder(Objects.requireNonNull(pipeline, "ChunkPipeline cannot be null!"));
    }

    public boolean wasEmpty() {
        return this.wasEmpty;
    }

    public static final class Builder {

        final Supplier<ServerWorld> serverWorld;
        final Supplier<Chunk> chunkSupplier;
        final Supplier<ChunkSection> sectionSupplier;
        List<ResultingTransactionBySideEffect> effects;
        final ChunkPipeline chunkPipeline;

        Builder(final ChunkPipeline chunkPipeline) {
            this.serverWorld = chunkPipeline::getServerWorld;
            this.chunkSupplier = chunkPipeline::getAffectedChunk;
            this.sectionSupplier = chunkPipeline::getAffectedSection;
            this.chunkPipeline = chunkPipeline;
        }

        public Builder addEffect(final ProcessingSideEffect effect) {
            if (this.effects == null) {
                this.effects = new LinkedList<>();
            }
            this.effects.add(new ResultingTransactionBySideEffect(Objects.requireNonNull(effect, "Effect is null")));
            return this;
        }

        public WorldPipeline build() {
            if (this.effects == null) {
                this.effects = Collections.emptyList();
            }
            return new WorldPipeline(this);
        }

    }
}
