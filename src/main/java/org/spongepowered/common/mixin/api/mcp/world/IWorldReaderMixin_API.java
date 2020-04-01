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
package org.spongepowered.common.mixin.api.mcp.world;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.HeightType;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.chunk.ProtoChunk;
import org.spongepowered.api.world.dimension.Dimension;
import org.spongepowered.api.world.volume.biome.ImmutableBiomeVolume;
import org.spongepowered.api.world.volume.biome.UnmodifiableBiomeVolume;
import org.spongepowered.api.world.volume.entity.ImmutableEntityVolume;
import org.spongepowered.api.world.volume.entity.UnmodifiableEntityVolume;
import org.spongepowered.api.world.volume.game.ReadableRegion;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(IWorldReader.class)
@Implements(@Interface(iface = ReadableRegion.class,
    prefix = "readable$"))
public interface IWorldReaderMixin_API<R extends ReadableRegion<R>> extends IEnvironmentBlockReaderMixin_API, ReadableRegion<R> {
    //@formatter:off

    @Shadow boolean shadow$isAirBlock(BlockPos p_175623_1_);
    @Shadow boolean shadow$canBlockSeeSky(BlockPos p_175710_1_);
    @Shadow int shadow$getLightSubtracted(BlockPos p_201669_1_, int p_201669_2_);
    @Nullable
    @Shadow IChunk shadow$getChunk(int p_217353_1_, int p_217353_2_, ChunkStatus p_217353_3_, boolean p_217353_4_);
    @Deprecated
    @Shadow boolean shadow$chunkExists(int p_217354_1_, int p_217354_2_);
    @Shadow BlockPos shadow$getHeight(Heightmap.Type p_205770_1_, BlockPos p_205770_2_);
    @Shadow int shadow$getHeight(Heightmap.Type p_201676_1_, int p_201676_2_, int p_201676_3_);
    @Shadow float shadow$getBrightness(BlockPos p_205052_1_);
    @Shadow int shadow$getSkylightSubtracted();
    @Shadow net.minecraft.world.border.WorldBorder shadow$getWorldBorder();
    @Shadow boolean shadow$checkNoEntityCollision(@javax.annotation.Nullable net.minecraft.entity.Entity p_195585_1_, VoxelShape p_195585_2_);
    @Shadow int shadow$getStrongPower(BlockPos p_175627_1_, Direction p_175627_2_);
    @Shadow boolean shadow$isRemote();
    @Shadow int shadow$getSeaLevel();
    @Shadow IChunk shadow$getChunk(BlockPos p_217349_1_);
    @Shadow IChunk shadow$getChunk(int p_212866_1_, int p_212866_2_);
    @Shadow IChunk shadow$getChunk(int p_217348_1_, int p_217348_2_, ChunkStatus p_217348_3_);
    @Shadow ChunkStatus shadow$getChunkStatus();
    @Shadow boolean shadow$func_217350_a(BlockState p_217350_1_, BlockPos p_217350_2_, ISelectionContext p_217350_3_);
    @Shadow boolean shadow$checkNoEntityCollision(net.minecraft.entity.Entity p_217346_1_);
    @Shadow boolean shadow$areCollisionShapesEmpty(AxisAlignedBB p_217351_1_);
    @Shadow boolean shadow$areCollisionShapesEmpty(net.minecraft.entity.Entity p_217345_1_);
    @Shadow boolean shadow$isCollisionBoxesEmpty(net.minecraft.entity.Entity p_195586_1_, AxisAlignedBB p_195586_2_);
    @Shadow boolean shadow$isCollisionBoxesEmpty(@javax.annotation.Nullable
                                                net.minecraft.entity.Entity p_211156_1_, AxisAlignedBB p_211156_2_, Set<net.minecraft.entity.Entity> p_211156_3_);
    @Shadow Stream<VoxelShape> shadow$getEmptyCollisionShapes(@javax.annotation.Nullable
                                                                   net.minecraft.entity.Entity p_223439_1_, AxisAlignedBB p_223439_2_, Set<net.minecraft.entity.Entity> p_223439_3_);
    @Shadow Stream<VoxelShape> shadow$getCollisionShapes(@javax.annotation.Nullable
                                                             net.minecraft.entity.Entity p_217352_1_, AxisAlignedBB p_217352_2_, Set<net.minecraft.entity.Entity> p_217352_3_);
    @Shadow Stream<VoxelShape> shadow$getCollisionShapes(@javax.annotation.Nullable final net.minecraft.entity.Entity p_223438_1_, AxisAlignedBB p_223438_2_);
    @Shadow boolean shadow$hasWater(BlockPos p_201671_1_);
    @Shadow boolean shadow$containsAnyLiquid(AxisAlignedBB p_72953_1_) ;
    @Shadow int shadow$getLight(BlockPos p_201696_1_);
    @Shadow int shadow$getNeighborAwareLightSubtracted(BlockPos p_205049_1_, int p_205049_2_);
    @Deprecated
    @Shadow boolean shadow$isBlockLoaded(BlockPos p_175667_1_);
    @Deprecated
    @Shadow boolean shadow$isAreaLoaded(BlockPos p_175707_1_, BlockPos p_175707_2_);
    @Deprecated
    @Shadow boolean shadow$isAreaLoaded(int p_217344_1_, int p_217344_2_, int p_217344_3_, int p_217344_4_, int p_217344_5_, int p_217344_6_);
    @Shadow net.minecraft.world.dimension.Dimension shadow$getDimension();

    //@formatter:on
    @Override
    default WorldBorder getBorder() {
        return (WorldBorder) this.shadow$getWorldBorder();
    }

    @Override
    default boolean isInBorder(final Entity entity) {
        return this.shadow$getWorldBorder().contains(((net.minecraft.entity.Entity) entity).getBoundingBox());
    }

    @Override
    default Dimension getDimension() {
        return (Dimension) this.shadow$getDimension();
    }

    @Override
    default boolean canSeeSky(final int x, final int y, final int z) {
        return this.shadow$isSkyLightMax(new BlockPos(x, y, z));
    }

    @Override
    default boolean hasWater(final int x, final int y, final int z) {
        return this.shadow$hasWater(new BlockPos(x, y, z));
    }

    @Override
    default int getSkylightSubtracted() {
        return this.shadow$getSkylightSubtracted();
    }

    @Intrinsic
    default int readable$getSeaLevel() {
        return this.shadow$getSeaLevel();
    }

    @Override
    default boolean isCollisionBoxesEmpty(@Nullable final Entity entity, final AABB aabb) {
        return this.shadow$isCollisionBoxesEmpty((net.minecraft.entity.Entity) entity, VecHelper.toMinecraftAABB(aabb));
    }

    @Override
    default boolean isAreaLoaded(final int xStart, final int yStart, final int zStart, final int xEnd, final int yEnd,
        final int zEnd, final boolean allowEmpty) {
        return this.shadow$isAreaLoaded(xStart, yStart, zStart, xEnd, yEnd, zEnd);
    }

    /**
     * Generates a random for usage, specific cases where randoms are being stored,
     * will override this appropriately.
     *
     * @return A generated Random
     */
    @Override
    default Random getRandom() {
        return new Random();
    }

    @Override
    default UnmodifiableBiomeVolume<?> asUnmodifiableBiomeVolume() {
        throw new UnsupportedOperationException(
            "Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }

    @Override
    default ImmutableBiomeVolume asImmutableBiomeVolume() {
        throw new UnsupportedOperationException(
            "Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }

    @Override
    default R getView(final Vector3i newMin, final Vector3i newMax) {
        throw new UnsupportedOperationException(
            "Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }

    @Override
    default UnmodifiableEntityVolume<?> asUnmodifiableEntityVolume() {
        throw new UnsupportedOperationException(
            "Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }

    @Override
    default ImmutableEntityVolume asImmutableEntityVolume() {
        throw new UnsupportedOperationException(
            "Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }

    @Override
    default Optional<Entity> getEntity(final UUID uuid) {
        throw new UnsupportedOperationException(
            "Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }

    @Override
    default Collection<? extends Player> getPlayers() {
        throw new UnsupportedOperationException(
            "Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }

    @Override
    default Collection<? extends Entity> getEntities(final AABB box, final Predicate<? super Entity> filter) {
        throw new UnsupportedOperationException(
            "Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }

    @Override
    default <T extends Entity> Collection<? extends T> getEntities(final Class<? extends T> entityClass, final AABB box,
        @javax.annotation.Nullable final Predicate<? super T> predicate) {
        throw new UnsupportedOperationException(
            "Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }

    @Override
    default ProtoChunk<?> getChunk(final int x, final int y, final int z) {
        return (ProtoChunk<?>) this.shadow$getChunk(x >> 4, z >> 4, ChunkStatus.EMPTY, true);
    }

    @Override
    default boolean isChunkLoaded(final int x, final int y, final int z, final boolean allowEmpty) {
        return this.shadow$chunkExists(x >> 4, z >> 4);
    }

    @Override
    default boolean hasChunk(final int x, final int y, final int z) {
        return this.shadow$chunkExists(x >> 4, z >> 4);
    }

    @Override
    default boolean hasChunk(final Vector3i position) {
        return this.shadow$chunkExists(position.getX() >> 4, position.getZ() >> 4);
    }

    @Override
    default int getHeight(final HeightType type, final int x, final int z) {
        return this.shadow$getHeight((Heightmap.Type) (Object) type, x, z);
    }
}
