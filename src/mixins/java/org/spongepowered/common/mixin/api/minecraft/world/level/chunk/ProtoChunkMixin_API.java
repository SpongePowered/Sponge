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
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.generation.GenerationChunk;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.holder.SpongeServerLocationBaseDataHolder;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.MissingImplementationException;
import org.spongepowered.common.util.SpongeTicks;
import org.spongepowered.common.world.storage.SpongeChunkLayout;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Mixin(ProtoChunk.class)
public abstract class ProtoChunkMixin_API extends ChunkAccess implements GenerationChunk, SpongeServerLocationBaseDataHolder {

    //@formatter:off
    @Shadow @Nullable private BelowZeroRetrogen belowZeroRetrogen;
    @Shadow public abstract Map<BlockPos, net.minecraft.world.level.block.entity.BlockEntity> shadow$getBlockEntities();
    @Shadow public abstract void shadow$setBlockEntity(final net.minecraft.world.level.block.entity.BlockEntity param1);
    @Shadow public abstract void shadow$removeBlockEntity(final BlockPos param0);
    @Shadow public abstract Holder<net.minecraft.world.level.biome.Biome> shadow$getNoiseBiome(int $$0, int $$1, int $$2);
    //@formatter:on


    private @Nullable Vector3i api$blockMin;
    private @Nullable Vector3i api$blockMax;

    public ProtoChunkMixin_API(
        final ChunkPos $$0, final UpgradeData $$1, final LevelHeightAccessor $$2, final Registry<net.minecraft.world.level.biome.Biome> $$3, final long $$4,
        final LevelChunkSection[] $$5, final BlendingData $$6
    ) {
        super($$0, $$1, $$2, $$3, $$4, $$5, $$6);
    }

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
            this.api$blockMax = this.min().add(SpongeChunkLayout.INSTANCE.chunkSize()).sub(1, 1, 1);
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
    public boolean setBiome(final int x, final int y, final int z, final Biome biome) {
        if (!this.getStatus().isOrAfter(ChunkStatus.BIOMES) && (this.belowZeroRetrogen == null || !this.belowZeroRetrogen.targetStatus().isOrAfter(ChunkStatus.BIOMES))) {
            throw new IllegalStateException("Asking for biomes before we have biomes");
        }
        return VolumeStreamUtils.setBiomeOnNativeChunk(x, y, z, biome, () -> this.getSection(this.getSectionIndex(y)), () -> {});
    }

    public Collection<? extends BlockEntity> blockEntities() {
        return (Collection) Collections.unmodifiableCollection(this.shadow$getBlockEntities().values());
    }

    @Override
    public Ticks inhabitedTime() {
        return new SpongeTicks(this.getInhabitedTime());
    }
    public void addBlockEntity(final int x, final int y, final int z, final BlockEntity blockEntity) {
        this.shadow$setBlockEntity((net.minecraft.world.level.block.entity.BlockEntity) blockEntity);
    }

    @Override
    public void setInhabitedTime(final Ticks newInhabitedTime) {
        Objects.requireNonNull(newInhabitedTime);
        if (newInhabitedTime.isInfinite()) {
            throw new IllegalArgumentException("Inhabited time cannot be infinite!");
        }
        this.setInhabitedTime(newInhabitedTime.ticks());
    }
    public void removeBlockEntity(final int x, final int y, final int z) {
        this.shadow$removeBlockEntity(new BlockPos(x, y, z));
    }

    @Override
    public Biome biome(final int x, final int y, final int z) {
        if (!this.contains(x, y, z)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), Constants.World.BLOCK_MIN, Constants.World.BLOCK_MAX);
        }
        return (Biome) (Object) this.shadow$getNoiseBiome(x, y, z).value();
    }

    @Override
    public ServerLocation impl$dataholder(final int x, final int y, final int z) {
        throw new MissingImplementationException("ProtoChunk", "impl$dataholder");
    }

}
