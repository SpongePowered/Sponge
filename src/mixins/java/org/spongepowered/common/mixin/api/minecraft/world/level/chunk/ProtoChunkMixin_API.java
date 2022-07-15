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
package org.spongepowered.common.mixin.api.minecraft.world.level.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.generation.GenerationChunk;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.holder.SpongeLocationBaseDataHolder;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.MissingImplementationException;
import org.spongepowered.common.world.storage.SpongeChunkLayout;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Mixin(ProtoChunk.class)
public abstract class ProtoChunkMixin_API implements GenerationChunk, SpongeLocationBaseDataHolder {

    //@formatter:off
    @Shadow @javax.annotation.Nullable public abstract net.minecraft.world.level.block.state.BlockState shadow$setBlockState(final BlockPos param0,
            final net.minecraft.world.level.block.state.BlockState param1, final boolean param2);
    @Shadow public abstract Map<BlockPos, net.minecraft.world.level.block.entity.BlockEntity> shadow$getBlockEntities();
    @Shadow public abstract void shadow$setBlockEntity(final BlockPos param0, final net.minecraft.world.level.block.entity.BlockEntity param1);
    @Shadow public abstract void shadow$removeBlockEntity(final BlockPos param0);
    @Shadow @javax.annotation.Nullable private ChunkBiomeContainer biomes;
    //@formatter:on

    private @Nullable Vector3i api$blockMin;
    private @Nullable Vector3i api$blockMax;


    @Override
    public VolumeStream<GenerationChunk, Biome> biomeStream(final Vector3i min, final Vector3i max, final StreamOptions options) {
        throw new UnsupportedOperationException("Cannot stream biomes on ProtoChunk");
    }

    @Override
    public VolumeStream<GenerationChunk, BlockState> blockStateStream(final Vector3i min, final Vector3i max, final StreamOptions options) {
        throw new UnsupportedOperationException("Cannot stream block states on ProtoChunk");
    }

    @Override
    public VolumeStream<GenerationChunk, BlockEntity> blockEntityStream(final Vector3i min, final Vector3i max, final StreamOptions options) {
        throw new UnsupportedOperationException("Cannot stream block entities on ProtoChunk");
    }

    @Override
    public Vector3i min() {
        if (this.api$blockMin == null) {
            this.api$blockMin = SpongeChunkLayout.INSTANCE.forceToWorld(this.chunkPosition());
        }
        return this.api$blockMin;
    }

    @Override
    public Vector3i max() {
        if (this.api$blockMax == null) {
            this.api$blockMax = this.min().add(SpongeChunkLayout.CHUNK_SIZE).sub(1, 1, 1);
        }
        return this.api$blockMax;
    }

    @Override
    public boolean setBlock(final int x, final int y, final int z, final BlockState block) {
        return false;
    }

    @Override
    public boolean removeBlock(final int x, final int y, final int z) {
        return false;
    }

    @Override
    public Collection<? extends BlockEntity> blockEntities() {
        return (Collection) Collections.unmodifiableCollection(this.shadow$getBlockEntities().values());
    }

    @Override
    public void addBlockEntity(final int x, final int y, final int z, final BlockEntity blockEntity) {
        this.shadow$setBlockEntity(new BlockPos(x, y, z), (net.minecraft.world.level.block.entity.BlockEntity) blockEntity);
    }

    @Override
    public void removeBlockEntity(final int x, final int y, final int z) {
        this.shadow$removeBlockEntity(new BlockPos(x, y, z));
    }

    @Override
    public Biome biome(int x, int y, int z) {
        if (!this.contains(x, y, z)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), Constants.World.BLOCK_MIN, Constants.World.BLOCK_MAX);
        }
        if (this.biomes != null) {
            return (Biome) (Object) this.biomes.getNoiseBiome(x, y, z);
        }
        return null;
    }

    @Override
    public ServerLocation impl$dataholder(int x, int y, int z) {
        throw new MissingImplementationException("ProtoChunk", "impl$dataholder");
    }

}
