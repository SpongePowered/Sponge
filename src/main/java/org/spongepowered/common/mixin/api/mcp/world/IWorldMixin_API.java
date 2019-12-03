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
import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scheduler.ScheduledUpdateList;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.HeightType;
import org.spongepowered.api.world.ProtoWorld;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.chunk.ProtoChunk;
import org.spongepowered.api.world.gen.TerrainGenerator;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.volume.biome.worker.MutableBiomeVolumeStream;
import org.spongepowered.api.world.volume.block.worker.MutableBlockVolumeStream;
import org.spongepowered.api.world.volume.entity.ImmutableEntityVolume;
import org.spongepowered.api.world.volume.entity.UnmodifiableEntityVolume;
import org.spongepowered.api.world.volume.entity.worker.MutableEntityStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

@Mixin(IWorld.class)
public interface IWorldMixin_API<T extends ProtoWorld<T>> extends IEntityReaderMixin_API, IWorldReaderMixin_API<T>, IWorldGenerationReaderMixin_API, ProtoWorld<T> {
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
    default boolean setBiome(int x, int y, int z, BiomeType biome) {
        final IChunk iChunk = this.shadow$getChunk(x >> 4, z >> 4, ChunkStatus.BIOMES, true);
        if (iChunk == null) {
            return false;
        }
        return ((ProtoChunk) iChunk).setBiome(x, y, z, biome);
    }

    @Override
    default MutableBiomeVolumeStream<T> toBiomeStream() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge API: " + this.getClass());
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
    default boolean containsBlock(int x, int y, int z) {
        return this.shadow$chunkExists(x >> 4, z >> 4);
    }

    @Override
    default boolean isAreaAvailable(int x, int y, int z) {
        return this.shadow$chunkExists(x >> 4, z >> 4);
    }

    @Override
    default T getView(Vector3i newMin, Vector3i newMax) {
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
    default Optional<Entity> getEntity(UUID uuid) {
        return Optional.empty();
    }

    @Override
    default Collection<? extends Player> getPlayers() {
        return IEntityReaderMixin_API.super.getPlayers();
    }
    @Override default Collection<? extends Entity> getEntities(AABB box, Predicate<? super Entity> filter) {
        return IEntityReaderMixin_API.super.getEntities(box, filter);
    }
    @Override
    default <E extends Entity> Collection<? extends E> getEntities(Class<? extends E> entityClass, AABB box,
                                                                   @Nullable Predicate<? super E> predicate) {
        return IEntityReaderMixin_API.super.getEntities(entityClass, box, predicate);
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
    default boolean setBlock(Vector3i position, BlockState block) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge API: " + this.getClass());
    }

    @Override
    default boolean setBlock(int x, int y, int z, BlockState block) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge API: " + this.getClass());
    }

    @Override
    default boolean setBlock(Vector3i position, BlockState state, BlockChangeFlag flag) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge API: " + this.getClass());
    }

    @Override
    default <V> Optional<V> getProperty(int x, int y, int z, Property<V> property) {
        return Optional.empty();
    }

    @Override
    default OptionalInt getIntProperty(int x, int y, int z, Property<Integer> property) {
        return OptionalInt.empty();
    }

    @Override
    default OptionalDouble getDoubleProperty(int x, int y, int z, Property<Double> property) {
        return OptionalDouble.empty();
    }

    @Override
    default <V> Optional<V> getProperty(int x, int y, int z, Direction direction, Property<V> property) {
        return Optional.empty();
    }

    @Override
    default OptionalInt getIntProperty(int x, int y, int z, Direction direction, Property<Integer> property) {
        return OptionalInt.empty();
    }

    @Override
    default OptionalDouble getDoubleProperty(int x, int y, int z, Direction direction, Property<Double> property) {
        return OptionalDouble.empty();
    }

    @Override
    default Map<Property<?>, ?> getProperties(int x, int y, int z) {
        return Collections.emptyMap();
    }

    @Override
    default Collection<Direction> getFacesWithProperty(int x, int y, int z, Property<?> property) {
        return Collections.emptyList();
    }

    // TODO - the rest of this implementation.

    @Override
    default void spawnParticles(ParticleEffect particleEffect, Vector3d position, int radius) {
    }

    @Override
    default void playSound(SoundType sound, SoundCategory category, Vector3d position, double volume, double pitch, double minVolume) {

    }

    @Override
    default void stopSounds() {

    }

    @Override
    default void stopSounds(SoundType sound) {

    }

    @Override
    default void stopSounds(SoundCategory category) {

    }

    @Override
    default void stopSounds(SoundType sound, SoundCategory category) {

    }

    @Override
    default void playMusicDisc(Vector3i position, MusicDisc musicDiscType) {

    }

    @Override
    default void stopMusicDisc(Vector3i position) {

    }

    @Override
    default void sendTitle(Title title) {

    }

    @Override
    default void sendBookView(BookView bookView) {

    }

    @Override
    default void sendBlockChange(int x, int y, int z, BlockState state) {

    }

    @Override
    default void resetBlockChange(int x, int y, int z) {

    }

    @Override
    default Entity createEntity(EntityType<?> type, Vector3d position) throws IllegalArgumentException, IllegalStateException {
        return null;
    }

    @Override
    default Entity createEntityNaturally(EntityType<?> type, Vector3d position) throws IllegalArgumentException, IllegalStateException {
        return null;
    }

    @Override
    default Optional<Entity> createEntity(DataContainer entityContainer) {
        return Optional.empty();
    }

    @Override
    default Optional<Entity> createEntity(DataContainer entityContainer, Vector3d position) {
        return Optional.empty();
    }

    @Override
    default Collection<Entity> spawnEntities(Iterable<? extends Entity> entities) {
        return null;
    }

    @Override
    default MutableEntityStream<T> toEntityStream() {
        return null;
    }

    @Override
    default MutableBlockVolumeStream<T> toBlockStream() {
        return null;
    }
    @Override default ProtoChunk<?> getChunk(int x, int y, int z) {
        return null;
    }
    @Override default int getHeight(HeightType type, int x, int z) {
        return 0;
    }

    @Override
    default boolean hitBlock(int x, int y, int z, Direction side, GameProfile profile) {
        return false;
    }

    @Override
    default boolean interactBlock(int x, int y, int z, Direction side, GameProfile profile) {
        return false;
    }

    @Override
    default boolean interactBlockWith(int x, int y, int z, ItemStack itemStack, Direction side, GameProfile profile) {
        return false;
    }

    @Override
    default boolean placeBlock(int x, int y, int z, BlockState block, Direction side, GameProfile profile) {
        return false;
    }

    @Override
    default boolean digBlock(int x, int y, int z, GameProfile profile) {
        return false;
    }

    @Override
    default boolean digBlockWith(int x, int y, int z, ItemStack itemStack, GameProfile profile) {
        return false;
    }

    @Override
    default Duration getBlockDigTimeWith(int x, int y, int z, ItemStack itemStack, GameProfile profile) {
        return null;
    }

    @Override
    default <E> Optional<E> get(int x, int y, int z, Key<? extends Value<E>> key) {
        return Optional.empty();
    }

    @Override
    default <E, V extends Value<E>> Optional<V> getValue(int x, int y, int z, Key<V> key) {
        return Optional.empty();
    }

    @Override
    default boolean supports(int x, int y, int z, Key<?> key) {
        return false;
    }

    @Override
    default Set<Key<?>> getKeys(int x, int y, int z) {
        return null;
    }

    @Override
    default Set<Value.Immutable<?>> getValues(int x, int y, int z) {
        return null;
    }

    @Override
    default <E> DataTransactionResult offer(int x, int y, int z, Key<? extends Value<E>> key, E value) {
        return null;
    }

    @Override
    default DataTransactionResult remove(int x, int y, int z, Key<?> key) {
        return null;
    }

    @Override
    default DataTransactionResult undo(int x, int y, int z, DataTransactionResult result) {
        return null;
    }

    @Override
    default DataTransactionResult copyFrom(int xTo, int yTo, int zTo, DataHolder from) {
        return null;
    }

    @Override
    default DataTransactionResult copyFrom(int xTo, int yTo, int zTo, DataHolder from, MergeFunction function) {
        return null;
    }

    @Override
    default DataTransactionResult copyFrom(int xTo, int yTo, int zTo, int xFrom, int yFrom, int zFrom, MergeFunction function) {
        return null;
    }

    @Override
    default boolean validateRawData(int x, int y, int z, DataView container) {
        return false;
    }

    @Override
    default void setRawData(int x, int y, int z, DataView container) throws InvalidDataException {

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
    default boolean setBlock(int x, int y, int z, BlockState state, BlockChangeFlag flag) {
        return false;
    }

    @Override
    default boolean spawnEntity(Entity entity) {
        return false;
    }

    @Override
    default boolean removeBlock(int x, int y, int z) {
        return false;
    }

    @Override
    default ProtoChunk<?> getChunk(Vector3i chunkPosition) {
        return null;
    }

    @Override
    default ProtoChunk<?> getChunkAtBlock(Vector3i blockPosition) {
        return null;
    }

    @Override
    default ProtoChunk<?> getChunkAtBlock(int bx, int by, int bz) {
        return null;
    }
}
