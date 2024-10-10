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
package org.spongepowered.common.mixin.api.minecraft.server.level;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStep;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.world.generation.GenerationChunk;
import org.spongepowered.api.world.generation.GenerationRegion;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.storage.SpongeChunkLayout;
import org.spongepowered.math.vector.Vector3i;

@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionMixin_API implements GenerationRegion {

    // @formatter:off
    @Shadow @Final private ServerLevel level;
    @Shadow @Final private ChunkAccess center;
    @Shadow @Final private ChunkStep generatingStep;
    @Shadow public abstract ChunkAccess shadow$getChunk(int param0, int param1);
    // @formatter:on


    private ResourceKey api$serverWorldKey;
    private @MonotonicNonNull Vector3i api$minChunk;
    private @MonotonicNonNull Vector3i api$maxChunk;
    private @MonotonicNonNull Vector3i api$minBlock;
    private @MonotonicNonNull Vector3i api$maxBlock;
    private @MonotonicNonNull Vector3i api$size;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void api$getWorldKeyOnConstruction(final ServerLevel $$0, final StaticCache2D $$1, final ChunkStep $$2, final ChunkAccess $$3,
            final CallbackInfo ci) {
        this.api$serverWorldKey = (ResourceKey) (Object) $$0.dimension().location();
    }

    @Override
    public @NonNull ResourceKey worldKey() {
        return this.api$serverWorldKey;
    }

    @Override
    public @NonNull Server engine() {
        return SpongeCommon.game().server();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public @NonNull GenerationChunk chunk(final int cx, final int cy, final int cz) {
        final ChunkAccess chunk;
        try {
            chunk = this.shadow$getChunk(cx, cz);
        } catch (final RuntimeException exception) {
            throw new IllegalArgumentException(exception.getMessage(), exception);
        }
        if (chunk == null) {
            // This indicates someone has asked outside of the region
            throw new IllegalArgumentException(String.format("Chunk coordinates (%d, %d, %d) is out of bounds.", cx, cy, cz));
        } else if (chunk instanceof LevelChunk) {
            // If this strange circumstance occurs, we just use Mojang's imposter and be on our way.
            return (GenerationChunk) new ImposterProtoChunk((LevelChunk) chunk, false);
        }
        return (GenerationChunk) chunk;
    }

    @Override
    public @NonNull Vector3i chunkMin() {
        if (this.api$minChunk == null) {
            final var center = this.center.getPos();
            final var radius = this.generatingStep.directDependencies().size();
            this.api$minChunk = VecHelper.toVector3i(center).sub(radius, 0, radius);
        }
        return this.api$minChunk;
    }

    @Override
    public @NonNull Vector3i chunkMax() {
        if (this.api$maxChunk == null) {
            final var center = this.center.getPos();
            final var radius = this.generatingStep.directDependencies().size();
            this.api$maxChunk = VecHelper.toVector3i(center).add(radius, 0, radius);
        }
        return this.api$maxChunk;
    }

    @Override
    public @NonNull Vector3i min() {
        if (this.api$minBlock == null) {
            this.api$minBlock = this.convertToBlock(this.chunkMin(), false);
        }
        return this.api$minBlock;
    }

    @Override
    public @NonNull Vector3i max() {
        if (this.api$maxBlock == null) {
            this.api$maxBlock = this.convertToBlock(this.chunkMax(), true);
        }
        return this.api$maxBlock;
    }

    @Override
    public @NonNull Vector3i size() {
        if (this.api$size == null) {
            this.api$size = this.max().sub(this.min()).add(Vector3i.ONE);
        }
        return this.api$size;
    }

    @Override
    public boolean isAreaAvailable(final int x, final int y, final int z) {
        return VecHelper.inBounds(x, y, z, this.min(), this.max());
    }

    private Vector3i convertToBlock(final Vector3i chunk, final boolean isMax) {
        final var layout = (SpongeChunkLayout) ((ServerWorld) this.level).chunkLayout();
        final Vector3i chunkMin = layout.forceToWorld(chunk);
        if (isMax) {
            return chunkMin.add(layout.getMask());
        }
        return chunkMin;
    }

}
