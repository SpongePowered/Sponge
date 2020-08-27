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

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ICollisionReader;
import net.minecraft.world.ILightReader;
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
import org.spongepowered.api.world.dimension.DimensionType;
import org.spongepowered.api.world.volume.game.ReadableRegion;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.dimension.DimensionTypeBridge;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

@Mixin(IWorldReader.class)
@Implements(@Interface(iface = ReadableRegion.class, prefix = "readable$"))
public interface IWorldReaderMixin_API<R extends ReadableRegion<R>> extends ReadableRegion<R> {

    //@formatter:off

    @Nullable @Shadow IChunk shadow$getChunk(int p_217353_1_, int p_217353_2_, ChunkStatus p_217353_3_, boolean p_217353_4_);
    @Deprecated @Shadow boolean shadow$chunkExists(int p_217354_1_, int p_217354_2_);
    @Shadow int shadow$getHeight(Heightmap.Type p_201676_1_, int p_201676_2_, int p_201676_3_);
    @Shadow int shadow$getSkylightSubtracted();
    @Shadow int shadow$getSeaLevel();
    @Shadow boolean shadow$hasWater(BlockPos p_201671_1_);
    @Deprecated @Shadow boolean shadow$isAreaLoaded(int p_217344_1_, int p_217344_2_, int p_217344_3_, int p_217344_4_, int p_217344_5_, int p_217344_6_);
    @Shadow net.minecraft.world.dimension.Dimension shadow$getDimension();
    @Shadow boolean shadow$containsAnyLiquid(AxisAlignedBB bb);

    //@formatter:on

    // ReadableRegion

    @Override
    default DimensionType getDimensionType() {
        return ((DimensionTypeBridge) this.shadow$getDimension().getType()).bridge$getSpongeDimensionType();
    }

    @Override
    default WorldBorder getBorder() {
        return (WorldBorder) ((ICollisionReader) this).getWorldBorder();
    }

    @Override
    default boolean isInBorder(final Entity entity) {
        return ((ICollisionReader) this).getWorldBorder().contains(((net.minecraft.entity.Entity) entity).getBoundingBox());
    }

    @Override
    default boolean canSeeSky(final int x, final int y, final int z) {
        return ((ILightReader) this).canSeeSky(new BlockPos(x, y, z));
    }

    @Override
    default boolean hasLiquid(final int x, final int y, final int z) {
        return this.shadow$hasWater(new BlockPos(x, y, z));
    }

    @Override
    default boolean containsAnyLiquids(AABB aabb) {
        final Vector3d max = aabb.getMax();
        final Vector3d min = aabb.getMin();
        return this.shadow$containsAnyLiquid(new AxisAlignedBB(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ()));
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
    default boolean isAreaLoaded(final int xStart, final int yStart, final int zStart, final int xEnd, final int yEnd,
        final int zEnd, final boolean allowEmpty) {
        return this.shadow$isAreaLoaded(xStart, yStart, zStart, xEnd, yEnd, zEnd);
    }

    // RandomProvider

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

    // ReadableEntityVolume

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
    default <T extends Entity> Collection<? extends T> getEntities(final Class<? extends T> entityClass, final AABB box,
        @javax.annotation.Nullable final Predicate<? super T> predicate) {
        throw new UnsupportedOperationException(
            "Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }

    // ChunkVolume

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

    // HeightAwareVolume

    @Override
    default int getHeight(final HeightType type, final int x, final int z) {
        return this.shadow$getHeight((Heightmap.Type) (Object) type, x, z);
    }

}
