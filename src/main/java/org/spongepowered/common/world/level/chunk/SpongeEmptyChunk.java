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
package org.spongepowered.common.world.level.chunk;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.CollectionValue;
import org.spongepowered.api.data.value.MapValue;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.scheduler.ScheduledUpdateList;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.HeightType;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.chunk.ChunkState;
import org.spongepowered.api.world.chunk.ChunkStates;
import org.spongepowered.api.world.chunk.WorldChunk;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.storage.SpongeChunkLayout;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public final class SpongeEmptyChunk implements WorldChunk {

    private final Level level;
    private final ChunkAccess chunk;

    public SpongeEmptyChunk(Level level, ChunkAccess chunk) {
        this.level = level;
        this.chunk = chunk;
    }

    private @Nullable Vector3i blockMin;
    private @Nullable Vector3i blockMax;

    @Override
    public Palette<BlockState, BlockType> blockPalette() {
        return ((World<?, ?>) this.level).blockPalette();
    }

    @Override
    public World<?, ?> world() {
        return (World<?, ?>) this.level;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public ChunkState state() {
        return ChunkStates.EMPTY.get();
    }

    @Override
    public Vector3i min() {
        if (this.blockMin == null) {
            this.blockMin = SpongeChunkLayout.INSTANCE.forceToWorld(this.chunkPosition());
        }
        return this.blockMin;
    }

    @Override
    public Vector3i max() {
        if (this.blockMax == null) {
            this.blockMax = this.min().add(new Vector3i(16, 256, 16)).sub(1, 1, 1);
        }
        return this.blockMax;
    }

    @Override
    public Vector3i chunkPosition() {
        final ChunkPos pos = this.chunk.getPos();
        return new Vector3i(pos.x, 0, pos.z);
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, this.min(), this.max());
    }

    @Override public boolean isAreaAvailable(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, this.min(), this.max());
    }

    @Override
    public Ticks inhabitedTime() {
        return Ticks.zero();
    }

    @Override
    public double regionalDifficultyFactor() {
        return 0;
    }

    @Override
    public double regionalDifficultyPercentage() {
        return 0;
    }

    private IllegalStateException emptyChunkError() {
        return new IllegalStateException("Method not allowed on empty Chunk");
    }

    @Override
    public void addEntity(Entity entity) {
        throw this.emptyChunkError();
    }

    @Override
    public void setInhabitedTime(Ticks newInhabitedTime) {
        throw this.emptyChunkError();
    }

    @Override
    public Biome biome(int x, int y, int z) {
        throw this.emptyChunkError();
    }

    @Override
    public VolumeStream<WorldChunk, Biome> biomeStream(Vector3i min, Vector3i max, StreamOptions options) {
        throw this.emptyChunkError();
    }

    @Override
    public boolean setBiome(int x, int y, int z, Biome biome) {
        throw this.emptyChunkError();
    }

    @Override
    public BlockState block(int x, int y, int z) {
        throw this.emptyChunkError();
    }

    @Override
    public FluidState fluid(int x, int y, int z) {
        throw this.emptyChunkError();
    }

    @Override
    public int highestYAt(int x, int z) {
        throw this.emptyChunkError();
    }

    @Override
    public VolumeStream<WorldChunk, BlockState> blockStateStream(Vector3i min, Vector3i max, StreamOptions options) {
        throw this.emptyChunkError();
    }

    @Override
    public boolean removeBlock(int x, int y, int z) {
        throw this.emptyChunkError();
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockState blockState, BlockChangeFlag flag) {
        throw this.emptyChunkError();
    }

    @Override
    public Collection<? extends BlockEntity> blockEntities() {
        throw this.emptyChunkError();
    }

    @Override
    public Optional<? extends BlockEntity> blockEntity(int x, int y, int z) {
        throw this.emptyChunkError();
    }

    @Override
    public VolumeStream<WorldChunk, BlockEntity> blockEntityStream(Vector3i min, Vector3i max, StreamOptions options) {
        throw this.emptyChunkError();
    }

    @Override
    public void addBlockEntity(int x, int y, int z, BlockEntity blockEntity) {
        throw this.emptyChunkError();
    }

    @Override
    public void removeBlockEntity(int x, int y, int z) {
        throw this.emptyChunkError();
    }

    @Override
    public Collection<? extends Player> players() {
        return Collections.emptyList();
    }

    @Override
    public Optional<Entity> entity(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public Collection<? extends Entity> entities() {
        return Collections.emptyList();
    }

    @Override
    public <T extends Entity> Collection<? extends T> entities(Class<? extends T> entityClass, AABB box, @Nullable Predicate<? super T> predicate) {
        return Collections.emptyList();
    }

    @Override
    public Collection<? extends Entity> entities(AABB box, Predicate<? super Entity> filter) {
        return Collections.emptyList();
    }

    @Override
    public VolumeStream<WorldChunk, Entity> entityStream(Vector3i min, Vector3i max, StreamOptions options) {
        throw this.emptyChunkError();
    }

    @Override
    public <E extends Entity> E createEntity(EntityType<E> type, Vector3d position) throws IllegalArgumentException, IllegalStateException {
        throw this.emptyChunkError();
    }

    @Override
    public <E extends Entity> E createEntityNaturally(EntityType<E> type, Vector3d position) throws IllegalArgumentException, IllegalStateException {
        throw this.emptyChunkError();
    }

    @Override
    public Optional<Entity> createEntity(DataContainer entityContainer) {
        throw this.emptyChunkError();
    }

    @Override
    public Optional<Entity> createEntity(DataContainer entityContainer, Vector3d position) {
        throw this.emptyChunkError();
    }

    @Override
    public boolean spawnEntity(Entity entity) {
        throw this.emptyChunkError();
    }

    @Override
    public Collection<Entity> spawnEntities(Iterable<? extends Entity> entities) {
        throw this.emptyChunkError();
    }

    @Override
    public int height(HeightType type, int x, int z) {
        throw this.emptyChunkError();
    }

    @Override
    public <E> Optional<E> get(int x, int y, int z, Key<? extends Value<E>> key) {
        return Optional.empty();
    }

    @Override
    public <E, V extends Value<E>> Optional<V> getValue(int x, int y, int z, Key<V> key) {
        return Optional.empty();
    }

    @Override
    public boolean supports(int x, int y, int z, Key<?> key) {
        return false;
    }

    @Override
    public Set<Key<?>> keys(int x, int y, int z) {
        return Collections.emptySet();
    }

    @Override
    public Set<Value.Immutable<?>> getValues(int x, int y, int z) {
        return Collections.emptySet();
    }

    @Override
    public <E> DataTransactionResult offer(int x, int y, int z, Key<? extends Value<E>> key, E value) {
        throw this.emptyChunkError();
    }

    @Override
    public DataTransactionResult remove(int x, int y, int z, Key<?> key) {
        throw this.emptyChunkError();
    }

    @Override
    public DataTransactionResult undo(int x, int y, int z, DataTransactionResult result) {
        throw this.emptyChunkError();
    }

    @Override
    public DataTransactionResult copyFrom(int xTo, int yTo, int zTo, ValueContainer from) {
        throw this.emptyChunkError();
    }

    @Override
    public DataTransactionResult copyFrom(int xTo, int yTo, int zTo, ValueContainer from, MergeFunction function) {
        throw this.emptyChunkError();
    }

    @Override
    public DataTransactionResult copyFrom(int xTo, int yTo, int zTo, int xFrom, int yFrom, int zFrom, MergeFunction function) {
        throw this.emptyChunkError();
    }

    @Override
    public boolean validateRawData(int x, int y, int z, DataView container) {
        return false;
    }

    @Override
    public void setRawData(int x, int y, int z, DataView container) throws InvalidDataException {
        throw this.emptyChunkError();
    }

    @Override
    public ScheduledUpdateList<BlockType> scheduledBlockUpdates() {
        throw this.emptyChunkError();
    }

    @Override
    public ScheduledUpdateList<FluidType> scheduledFluidUpdates() {
        throw this.emptyChunkError();
    }

    @Override
    public <E> DataTransactionResult offer(Key<? extends Value<E>> key, E value) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult offer(Value<?> value) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public <E> DataTransactionResult offerSingle(Key<? extends CollectionValue<E, ?>> key, E element) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public <K, V> DataTransactionResult offerSingle(Key<? extends MapValue<K, V>> key, K valueKey, V value) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public <K, V> DataTransactionResult offerAll(Key<? extends MapValue<K, V>> key, Map<? extends K, ? extends V> map) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult offerAll(MapValue<?, ?> value) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult offerAll(CollectionValue<?, ?> value) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public <E> DataTransactionResult offerAll(Key<? extends CollectionValue<E, ?>> key, Collection<? extends E> elements) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public <E> DataTransactionResult removeSingle(Key<? extends CollectionValue<E, ?>> key, E element) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public <K> DataTransactionResult removeKey(Key<? extends MapValue<K, ?>> key, K mapKey) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult removeAll(CollectionValue<?, ?> value) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public <E> DataTransactionResult removeAll(Key<? extends CollectionValue<E, ?>> key, Collection<? extends E> elements) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult removeAll(MapValue<?, ?> value) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public <K, V> DataTransactionResult removeAll(Key<? extends MapValue<K, V>> key, Map<? extends K, ? extends V> map) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public <E> DataTransactionResult tryOffer(Key<? extends Value<E>> key, E value) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult remove(Key<?> key) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult undo(DataTransactionResult result) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult copyFrom(ValueContainer that, MergeFunction function) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public <E> Optional<E> get(Key<? extends Value<E>> key) {
        return Optional.empty();
    }

    @Override
    public <E, V extends Value<E>> Optional<V> getValue(Key<V> key) {
        return Optional.empty();
    }

    @Override
    public boolean supports(Key<?> key) {
        return false;
    }

    @Override
    public Set<Key<?>> getKeys() {
        return Collections.emptySet();
    }

    @Override
    public Set<Value.Immutable<?>> getValues() {
        return Collections.emptySet();
    }
}
