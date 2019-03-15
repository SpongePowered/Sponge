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
package org.spongepowered.common.mixin.core.world;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumLightType;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.HeightType;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.LightType;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.volume.EntityHit;
import org.spongepowered.api.world.volume.ReadableRegion;
import org.spongepowered.api.world.volume.biome.ImmutableBiomeVolume;
import org.spongepowered.api.world.volume.biome.UnmodifiableBiomeVolume;
import org.spongepowered.api.world.volume.biome.worker.BiomeVolumeStream;
import org.spongepowered.api.world.volume.block.ImmutableBlockVolume;
import org.spongepowered.api.world.volume.block.UnmodifiableBlockVolume;
import org.spongepowered.api.world.volume.block.worker.BlockVolumeStream;
import org.spongepowered.api.world.volume.entity.ImmutableEntityVolume;
import org.spongepowered.api.world.volume.entity.UnmodifiableEntityVolume;
import org.spongepowered.api.world.volume.entity.worker.EntityStream;
import org.spongepowered.api.world.volume.tileentity.worker.TileEntityStream;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.block.BlockUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.fluid.FluidUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.volume.entity.UnmodifiableDownsizedEntityVolume;
import org.spongepowered.common.world.volume.stream.SpongeBiomeStream;
import org.spongepowered.common.world.volume.wrapped.WrappedUnmodifiableBlockVolume;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

@SuppressWarnings("ConstantConditions")
@Mixin(IWorldReaderBase.class)
@Implements(@Interface(iface = ReadableRegion.class, prefix = "readableRegion$"))
public interface MixinIWorldReaderBase_API<R extends ReadableRegion<R>> extends ReadableRegion<R>, MixinIBlockReader_API {

    @Shadow net.minecraft.world.border.WorldBorder shadow$getWorldBorder();
    @Shadow boolean isInsideWorldBorder(net.minecraft.entity.Entity entityToCheck);
    @Shadow int shadow$getSkylightSubtracted();
    @Shadow boolean canSeeSky(BlockPos pos);
    @Shadow boolean hasWater(BlockPos pos);
    @Shadow int shadow$getSeaLevel();
    @Shadow boolean isCollisionBoxesEmpty(@Nullable net.minecraft.entity.Entity entityIn, AxisAlignedBB aabb);
    @Shadow boolean isChunkLoaded(int x, int z, boolean allowEmpty);
    @Shadow net.minecraft.world.dimension.Dimension shadow$getDimension();
    @Shadow int getHeight(Heightmap.Type heightmapType, int x, int z);
    @Shadow int getLightFor(EnumLightType type, BlockPos pos);
    @Shadow boolean isAreaLoaded(BlockPos center, int radius, boolean allowEmpty);
    @Shadow Biome getBiome(BlockPos pos);

    @Override
    default WorldBorder getBorder() {
        return (WorldBorder) shadow$getWorldBorder();
    }

    @Override
    default boolean isInBorder(Entity entity) {
        return isInsideWorldBorder(EntityUtil.toNative(entity));
    }

    @Override
    default boolean canSeeSky(Vector3i pos) {
        return canSeeSky(VecHelper.toBlockPos(pos));
    }

    @Override
    default boolean canSeeSky(int x, int y, int z) {
        return canSeeSky(new BlockPos(x, y, z));
    }

    @Override
    default boolean hasWater(int x, int y, int z) {
        return hasWater(new BlockPos(x, y, z));
    }

    @Override
    default int getSkylightSubtracted() {
        return shadow$getSkylightSubtracted();
    }

    @Override
    default int getSeaLevel() {
        return shadow$getSeaLevel();
    }

    @Override
    default boolean isCollisionBoxesEmpty(@Nullable Entity entity, AABB aabb) {
        return isCollisionBoxesEmpty(EntityUtil.toNullableNative(entity), VecHelper.toMinecraftAABB(aabb));
    }

    @Override
    default boolean isChunkLoaded(int x, int y, int z, boolean allowEmpty) {
        return isChunkLoaded(x, z, allowEmpty);
    }

    @Override
    default boolean isBlockLoaded(int x, int y, int z) {
        return isChunkLoaded(x >> 4, z >> 4, true);
    }
    @Override
    default boolean isBlockLoaded(int x, int y, int z, boolean allowEmpty) {
        return isChunkLoaded(x >> 4, z >> 4, allowEmpty);
    }

    @Override
    default boolean isBlockLoaded(Vector3i pos) {
        return isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4, true);
    }
    @Override
    default boolean isBlockLoaded(Vector3i pos, boolean allowEmpty) {
        return isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4, allowEmpty);
    }

    @Override
    default Dimension getDimension() {
        return (Dimension) shadow$getDimension();
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

    /**
     * Implements for less nesting.
     *
     * @param type The hight type
     * @param pos The position
     * @return The maximum height for the desired HeightType
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    default Vector3i getHeight(HeightType type, Vector3i pos) {
        return new Vector3i(pos.getX(), getHeight(type, pos.getX(), pos.getZ()), pos.getZ());
    }

    @Override
    default int getHeight(HeightType type, int x, int z) {
        return getHeight((Heightmap.Type) (Object) type, x, z);
    }

    @Override
    default int getLight(LightType type, int x, int y, int z) {
        return getLightFor((EnumLightType) (Object) type, new BlockPos(x, y, z));
    }

    @Override
    default BiomeType getBiome(Vector3i position) {
        return (BiomeType) getBiome(VecHelper.toBlockPos(position));
    }

    @Override
    default BiomeType getBiome(int x, int y, int z) {
        return (BiomeType) getBiome(new BlockPos(x, y, z));
    }

    @Override
    default UnmodifiableBiomeVolume<?> asUnmodifiableBiomeVolume() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }

    @Override
    default ImmutableBiomeVolume asImmutableBiomeVolume() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }

    @Override
    default BlockVolumeStream<R, ?> toBlockStream() {
        return null; // TODO - implement streams
    }

    @Override
    default EntityStream<R, ?> toEntityStream() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }
    @Override
    default ImmutableEntityVolume asImmutableEntityVolume() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }

    @Override
    default TileEntityStream<R, ?> toTileEntityStream() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }

    @SuppressWarnings("unchecked")
    @Override
    default BiomeVolumeStream<R, ?> toBiomeStream() {
        return new SpongeBiomeStream(this);
    }

    @Override
    default Collection<Entity> getNearbyEntities(Vector3d location, double distance) {
        return getEntities(e -> e.getPosition().distanceSquared(location) <= distance * distance);
    }

    @Override
    default Set<Entity> getIntersectingEntities(AABB box, Predicate<Entity> filter) {
        return Collections.emptySet(); // TODO - Implement AABB's, maybe need to get VoxelShape representations too
    }

    @Override
    default Set<EntityHit> getIntersectingEntities(Vector3d start, Vector3d end, Predicate<EntityHit> filter) {
        return Collections.emptySet(); // TODO - Implement AABB's, maybe need to get VoxelShape representations too
    }

    @Override
    default Set<EntityHit> getIntersectingEntities(Vector3d start, Vector3d direction, double distance, Predicate<EntityHit> filter) {
        return Collections.emptySet(); // TODO - Implement AABB's, maybe need to get VoxelShape representations too
    }

    @Override
    default Optional<AABB> getBlockSelectionBox(int x, int y, int z) {
        return Optional.empty(); // TODO - Implement AABB's, maybe need to get VoxelShape representations too
    }

    @Override
    default Set<AABB> getIntersectingBlockCollisionBoxes(AABB box) {
        return Collections.emptySet(); // TODO - Implement AABB's, maybe need to get VoxelShape representations too
    }

    @Override
    default Set<AABB> getIntersectingCollisionBoxes(Entity owner, AABB box) {
        return Collections.emptySet(); // TODO - Implement AABB's, maybe need to get VoxelShape representations too
    }


    @Override
    default Optional<Entity> getEntity(UUID uuid) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }

    @Override
    default Collection<Entity> getEntities() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }

    @Override
    default Collection<Entity> getEntities(Predicate<Entity> filter) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }
    @Override
    default UnmodifiableEntityVolume<?> asUnmodifiableEntityVolume() {
        // TODO - May need modifications for world readers that are not entity holders.
        return new UnmodifiableDownsizedEntityVolume(this, this.getBlockMin(), this.getBlockMax());
    }

    @Override
    default Collection<TileEntity> getTileEntities() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }

    @Override
    default Optional<TileEntity> getTileEntity(int x, int y, int z) {
        return Optional.ofNullable((TileEntity) getTileEntity(new BlockPos(x, y, z)));
    }

    @Override
    default Vector3i getBlockMin() {
        final net.minecraft.world.border.WorldBorder worldBorder = this.shadow$getWorldBorder();
        return new Vector3i(worldBorder.minX(), 0, worldBorder.minZ());
    }

    @Override
    default Vector3i getBlockMax() {
        final net.minecraft.world.border.WorldBorder border = this.shadow$getWorldBorder();
        return new Vector3i(border.maxX(), 256, border.maxZ());
    }

    @Override
    default Vector3i getBlockSize() {
        return getBlockMax().sub(getBlockMax());
    }

    @Override
    default boolean containsBlock(int x, int y, int z) {
        return shadow$getWorldBorder().contains(new BlockPos(x, y, z));
    }

    @Override
    default boolean isAreaAvailable(int x, int y, int z) {
        return isAreaLoaded(x, y, z, x, y, z, false);
    }
    @Override
    default R getView(Vector3i newMin, Vector3i newMax) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }

    @Override
    default BlockState getBlock(int x, int y, int z) {
        return BlockUtil.fromNative(getBlockState(new BlockPos(x, y, z)));
    }
    @Override
    default BlockState getBlock(Vector3i vector3i) {
        return BlockUtil.fromNative(getBlockState(VecHelper.toBlockPos(vector3i)));
    }

    @Override
    default FluidState getFluid(int x, int y, int z) {
        return FluidUtil.fromNative(getFluidState(new BlockPos(x, y, z)));
    }

    @Override
    default UnmodifiableBlockVolume<?> asUnmodifiableBlockVolume() {
        return new WrappedUnmodifiableBlockVolume<>(this);
    }
    @Override
    default ImmutableBlockVolume asImmutableBlockVolume() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorldReaderBase that isn't part of Sponge API");
    }

    @Override
    default int getHighestYAt(int x, int z) {
        return getHeight(Heightmap.Type.WORLD_SURFACE, x, z);
    }

}
