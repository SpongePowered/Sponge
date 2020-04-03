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

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.ITickList;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.scheduler.ScheduledUpdateList;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.BoundedWorldView;
import org.spongepowered.api.world.HeightType;
import org.spongepowered.api.world.ProtoWorld;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.chunk.ProtoChunk;
import org.spongepowered.api.world.gen.TerrainGenerator;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.volume.entity.ImmutableEntityVolume;
import org.spongepowered.api.world.volume.entity.UnmodifiableEntityVolume;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(IWorld.class)
public interface IWorldMixin_API<T extends ProtoWorld<T>> extends IEntityReaderMixin_API, IWorldReaderMixin_API<BoundedWorldView<T>>, IWorldGenerationReaderMixin_API, ProtoWorld<T> {
    @Shadow long shadow$getSeed();
    @Shadow float shadow$getCurrentMoonPhaseFactor();
    @Shadow float shadow$getCelestialAngle(float p_72826_1_);
    @Shadow ITickList<Block> shadow$getPendingBlockTicks();
    @Shadow ITickList<Fluid> shadow$getPendingFluidTicks();
    @Shadow net.minecraft.world.World shadow$getWorld();
    @Shadow WorldInfo shadow$getWorldInfo();
    @Shadow DifficultyInstance shadow$getDifficultyForLocation(BlockPos p_175649_1_);
    @Shadow Difficulty shadow$getDifficulty();
    @Shadow AbstractChunkProvider shadow$getChunkProvider();
    @Shadow boolean shadow$chunkExists(int p_217354_1_, int p_217354_2_);
    @Shadow Random shadow$getRandom();
    @Shadow void shadow$notifyNeighbors(BlockPos p_195592_1_, Block p_195592_2_);
    @Shadow void shadow$playSound(@Nullable PlayerEntity p_184133_1_, BlockPos p_184133_2_, SoundEvent p_184133_3_, net.minecraft.util.SoundCategory p_184133_4_, float p_184133_5_, float p_184133_6_);
    @Shadow void shadow$addParticle(IParticleData p_195594_1_, double p_195594_2_, double p_195594_4_, double p_195594_6_, double p_195594_8_, double p_195594_10_, double p_195594_12_);
    @Shadow void shadow$playEvent(@Nullable PlayerEntity p_217378_1_, int p_217378_2_, BlockPos p_217378_3_, int p_217378_4_);
    @Shadow void shadow$playEvent(int p_217379_1_, BlockPos p_217379_2_, int p_217379_3_);
    @Shadow Stream<VoxelShape> shadow$getEmptyCollisionShapes(@Nullable net.minecraft.entity.Entity p_223439_1_, AxisAlignedBB p_223439_2_, Set<net.minecraft.entity.Entity> p_223439_3_) ;
    @Shadow boolean shadow$checkNoEntityCollision(@Nullable net.minecraft.entity.Entity p_195585_1_, VoxelShape p_195585_2_);


    @Override
    default boolean setBiome(final int x, final int y, final int z, final BiomeType biome) {
        final IChunk iChunk = this.shadow$getChunk(x >> 4, z >> 4, ChunkStatus.BIOMES, true);
        if (iChunk == null) {
            return false;
        }
        return ((ProtoChunk) iChunk).setBiome(x, y, z, biome);
    }

    @Override
    default Vector3i getBlockMin() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge API: " + this.getClass());
    }

    @Override
    default Vector3i getBlockMax() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge API: " + this.getClass());
    }

    @Override
    default Vector3i getBlockSize() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge API: " + this.getClass());
    }

    @Override
    default boolean containsBlock(final int x, final int y, final int z) {
        return this.shadow$chunkExists(x >> 4, z >> 4);
    }

    @Override
    default boolean isAreaAvailable(final int x, final int y, final int z) {
        return this.shadow$chunkExists(x >> 4, z >> 4);
    }

    @Override
    default BoundedWorldView<T> getView(final Vector3i newMin, final Vector3i newMax) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge API: " + this.getClass());
    }

    @Override
    default UnmodifiableEntityVolume<?> asUnmodifiableEntityVolume() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge API: " + this.getClass());
    }

    @Override
    default ImmutableEntityVolume asImmutableEntityVolume() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge API: " + this.getClass());
    }

    @Override
    default Optional<Entity> getEntity(final UUID uuid) {
        return Optional.empty();
    }

    @Override
    default Collection<? extends Player> getPlayers() {
        return IEntityReaderMixin_API.super.getPlayers();
    }
    @Override default Collection<? extends Entity> getEntities(final AABB box, final Predicate<? super Entity> filter) {
        return IEntityReaderMixin_API.super.getEntities(box, filter);
    }
    @Override
    default <E extends Entity> Collection<? extends E> getEntities(final Class<? extends E> entityClass, final AABB box,
                                                                   @Nullable final Predicate<? super E> predicate) {
        return IEntityReaderMixin_API.super.getEntities(entityClass, box, predicate);
    }

    @Override
    default ProtoChunk<?> getChunk(final int x, final int y, final int z) {
        return null;
    }


    @Override
    default long getSeed() {
        return this.shadow$getSeed();
    }

    @Override
    default TerrainGenerator<?> getTerrainGenerator() {
        return (TerrainGenerator<?>) this.shadow$getChunkProvider().getChunkGenerator();
    }

    @Override
    default WorldProperties getProperties() {
        return (WorldProperties) this.shadow$getWorldInfo();
    }

    @Override
    default boolean setBlock(final Vector3i position, final BlockState block) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge API: " + this.getClass());
    }

    @Override
    default boolean setBlock(final int x, final int y, final int z, final BlockState block) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge API: " + this.getClass());
    }

    @Override
    default boolean setBlock(final Vector3i position, final BlockState state, final BlockChangeFlag flag) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge API: " + this.getClass());
    }

    @Override
    default Entity createEntity(final EntityType<?> type, final Vector3d position) throws IllegalArgumentException, IllegalStateException {
        return null;
    }

    @Override
    default Entity createEntityNaturally(final EntityType<?> type, final Vector3d position) throws IllegalArgumentException, IllegalStateException {
        return null;
    }

    @Override
    default Optional<Entity> createEntity(final DataContainer entityContainer) {
        return Optional.empty();
    }

    @Override
    default Optional<Entity> createEntity(final DataContainer entityContainer, final Vector3d position) {
        return Optional.empty();
    }

    @Override
    default Collection<Entity> spawnEntities(final Iterable<? extends Entity> entities) {
        return null;
    }

    @Override default int getHeight(final HeightType type, final int x, final int z) {
        return 0;
    }

    @Override
    default <E> Optional<E> get(final int x, final int y, final int z, final Key<? extends Value<E>> key) {
        return Optional.empty();
    }

    @Override
    default <E, V extends Value<E>> Optional<V> getValue(final int x, final int y, final int z, final Key<V> key) {
        return Optional.empty();
    }

    @Override
    default boolean supports(final int x, final int y, final int z, final Key<?> key) {
        return false;
    }

    @Override
    default Set<Key<?>> getKeys(final int x, final int y, final int z) {
        return null;
    }

    @Override
    default Set<Value.Immutable<?>> getValues(final int x, final int y, final int z) {
        return null;
    }

    @Override
    default <E> DataTransactionResult offer(final int x, final int y, final int z, final Key<? extends Value<E>> key, final E value) {
        return null;
    }

    @Override
    default DataTransactionResult remove(final int x, final int y, final int z, final Key<?> key) {
        return null;
    }

    @Override
    default DataTransactionResult undo(final int x, final int y, final int z, final DataTransactionResult result) {
        return null;
    }

    @Override
    default DataTransactionResult copyFrom(final int xTo, final int yTo, final int zTo, final DataHolder from) {
        return null;
    }

    @Override
    default DataTransactionResult copyFrom(
        final int xTo, final int yTo, final int zTo, final DataHolder from, final MergeFunction function) {
        return null;
    }

    @Override
    default DataTransactionResult copyFrom(final int xTo, final int yTo, final int zTo, final int xFrom, final int yFrom, final int zFrom, final MergeFunction function) {
        return null;
    }

    @Override
    default boolean validateRawData(final int x, final int y, final int z, final DataView container) {
        return false;
    }

    @Override
    default void setRawData(final int x, final int y, final int z, final DataView container) throws InvalidDataException {

    }

    @Override
    default int getSeaLevel() {
        return 0;
    }

    @Override
    default ScheduledUpdateList<BlockType> getScheduledBlockUpdates() {
        return null;
    }

    @Override
    default ScheduledUpdateList<FluidType> getScheduledFluidUpdates() {
        return null;
    }

    @Override
    default boolean setBlock(final int x, final int y, final int z, final BlockState state, final BlockChangeFlag flag) {
        return false;
    }

    @Override
    default boolean spawnEntity(final Entity entity) {
        return false;
    }

    @Override
    default boolean removeBlock(final int x, final int y, final int z) {
        return false;
    }

    @Override
    default ProtoChunk<?> getChunk(final Vector3i chunkPosition) {
        return null;
    }

    @Override
    default ProtoChunk<?> getChunkAtBlock(final Vector3i blockPosition) {
        return null;
    }

    @Override
    default ProtoChunk<?> getChunkAtBlock(final int bx, final int by, final int bz) {
        return null;
    }
}
