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
package org.spongepowered.common.mixin.api.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.HeightType;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.border.WorldBorder;
import org.spongepowered.api.world.chunk.Chunk;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.volume.game.Region;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.level.border.WorldBorderBridge;
import org.spongepowered.common.world.schematic.PaletteWrapper;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Objects;

@SuppressWarnings({"RedundantTypeArguments", "RedundantCast"})
@Mixin(LevelReader.class)
public interface LevelReaderMixin_API<R extends Region<R>> extends Region<R> {

    // @formatter:off
    @Nullable @Shadow ChunkAccess shadow$getChunk(int p_217353_1_, int p_217353_2_, ChunkStatus p_217353_3_, boolean p_217353_4_);
    @Deprecated @Shadow boolean shadow$hasChunk(int p_217354_1_, int p_217354_2_);
    @Shadow int shadow$getHeight(Heightmap.Types p_201676_1_, int p_201676_2_, int p_201676_3_);
    @Shadow int shadow$getSkyDarken();
    @Shadow int shadow$getSeaLevel();
    @Shadow boolean shadow$isWaterAt(BlockPos p_201671_1_);
    @Deprecated @Shadow boolean shadow$hasChunksAt(int p_217344_1_, int p_217344_2_, int p_217344_3_, int p_217344_4_, int p_217344_5_, int p_217344_6_);
    @Shadow net.minecraft.world.level.dimension.DimensionType shadow$dimensionType();
    @Shadow boolean shadow$containsAnyLiquid(net.minecraft.world.phys.AABB bb);
    @Shadow Holder<net.minecraft.world.level.biome.Biome> shadow$getBiome(BlockPos p_226691_1_);
    @Shadow RegistryAccess shadow$registryAccess();
    // @formatter:on

    // BlockVolume

    @SuppressWarnings("unchecked")
    @Override
    default Palette<BlockState, BlockType> blockPalette() {
        return PaletteWrapper.of(
            PaletteTypes.BLOCK_STATE_PALETTE.get(),
            Block.BLOCK_STATE_REGISTRY,
            (Registry<BlockType>) this.shadow$registryAccess().registryOrThrow(Registries.BLOCK)
        );
    }

    // Region

    @Override
    default WorldType worldType() {
        return (WorldType) (Object) this.shadow$dimensionType();
    }

    @Override
    default WorldBorder border() {
        return ((WorldBorderBridge) ((CollisionGetter) this).getWorldBorder()).bridge$asImmutable();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    default WorldBorder setBorder(final WorldBorder border) {
        final WorldBorder worldBorder = ((WorldBorderBridge) ((CollisionGetter) this).getWorldBorder()).bridge$applyFrom(border);
        if (worldBorder == null) {
            return (WorldBorder) net.minecraft.world.level.border.WorldBorder.DEFAULT_SETTINGS;
        }
        return worldBorder;
    }

    @Override
    default boolean isInBorder(final Entity entity) {
        return ((CollisionGetter) this).getWorldBorder().isWithinBounds(((net.minecraft.world.entity.Entity) Objects.requireNonNull(entity, "entity")).getBoundingBox());
    }

    @Override
    default boolean canSeeSky(final int x, final int y, final int z) {
        return ((BlockAndTintGetter) this).canSeeSky(new BlockPos(x, y, z));
    }

    @Override
    default boolean hasLiquid(final int x, final int y, final int z) {
        return this.shadow$isWaterAt(new BlockPos(x, y, z));
    }

    @Override
    default boolean containsAnyLiquids(final AABB aabb) {
        final Vector3d max = Objects.requireNonNull(aabb, "aabb").max();
        final Vector3d min = aabb.min();
        return this.shadow$containsAnyLiquid(new net.minecraft.world.phys.AABB(min.x(), min.y(), min.z(), max.x(), max.y(), max.z()));
    }

    @Override
    default int skylightSubtracted() {
        return this.shadow$getSkyDarken();
    }

    @Override
    default int seaLevel() {
        return this.shadow$getSeaLevel();
    }

    @Override
    default boolean isAreaLoaded(final int xStart, final int yStart, final int zStart, final int xEnd, final int yEnd,
        final int zEnd, final boolean allowEmpty) {
        return this.shadow$hasChunksAt(xStart, yStart, zStart, xEnd, yEnd, zEnd);
    }

    // RandomProvider

    @Override
    default Source random() {
        return (Source) net.minecraft.util.RandomSource.create();
    }

    // ChunkVolume

    @SuppressWarnings("ConstantConditions")
    @Override
    default Chunk<@NonNull ?> chunk(final int x, final int y, final int z) {
        return (Chunk<@NonNull ?>) this.shadow$getChunk(x, z, ChunkStatus.EMPTY, true);
    }

    @Override
    default boolean isChunkLoaded(final int cx, final int cy, final int cz, final boolean allowEmpty) {
        return this.shadow$hasChunk(cx, cz);
    }

    @Override
    default boolean hasChunk(final int cx, final int cy, final int cz) {
        return this.shadow$hasChunk(cx, cz);
    }

    // HeightAwareVolume

    @Override
    default int height(final HeightType type, final int x, final int z) {
        return this.shadow$getHeight((Heightmap.Types) (Object) Objects.requireNonNull(type, "type"), x, z);
    }

    @Override
    default Biome biome(final int x, final int y, final int z) {
        return (Biome) (Object) this.shadow$getBiome(new BlockPos(x, y, z)).value();
    }

    @Override
    default VolumeStream<R, Biome> biomeStream(final Vector3i min, final Vector3i max, final StreamOptions options) {
        return VolumeStreamUtils.<R>getBiomeStream((LevelReader) (Object) this, min, max, options);
    }

    @Override
    default VolumeStream<R, BlockState> blockStateStream(final Vector3i min, final Vector3i max, final StreamOptions options) {
        return VolumeStreamUtils.<R>generateBlockStream((LevelReader) (Object) this, min, max, options);
    }

    @Override
    default VolumeStream<R, BlockEntity> blockEntityStream(final Vector3i min, final Vector3i max, final StreamOptions options) {
        return VolumeStreamUtils.<R>getBlockEntityStream((LevelReader) (Object) this, min, max, options);
    }

}
