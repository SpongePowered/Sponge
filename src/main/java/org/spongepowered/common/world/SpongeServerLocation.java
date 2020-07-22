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
package org.spongepowered.common.world;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.CollectionValue;
import org.spongepowered.api.data.value.MapValue;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.scheduler.ScheduledUpdate;
import org.spongepowered.api.scheduler.TaskPriority;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.storage.ChunkLayout;
import org.spongepowered.common.util.MissingImplementationException;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@DefaultQualifier(NonNull.class)
public final class SpongeServerLocation extends SpongeLocation<ServerWorld> implements ServerLocation {

    SpongeServerLocation(final ServerWorld world, final ChunkLayout chunkLayout, final Vector3d position) {
        super(world, chunkLayout, position);
    }

    SpongeServerLocation(final ServerWorld worldRef, final Vector3d position, final Vector3i chunkPosition, final Vector3i biomePosition) {
        super(worldRef, position, chunkPosition, biomePosition);
    }

    @Override
    public ServerLocation withWorld(final ServerWorld world) {
        return new SpongeServerLocation(world, this.getPosition(), this.getChunkPosition(), this.getBiomePosition());
    }

    @Override
    public ServerLocation withPosition(final Vector3d position) {
        final ChunkLayout chunkLayout = this.getWorld().getEngine().getChunkLayout();
        return new SpongeServerLocation(this.getWorld(), chunkLayout, position);
    }

    @Override
    public ServerLocation withBlockPosition(final Vector3i position) {
        final ChunkLayout chunkLayout = this.getWorld().getEngine().getChunkLayout();
        return new SpongeServerLocation(this.getWorld(), chunkLayout, position.toDouble());
    }

    @Override
    public ServerLocation sub(final Vector3d v) {
        return this.withPosition(this.getPosition().sub(v));
    }

    @Override
    public ServerLocation sub(final Vector3i v) {
        return this.withBlockPosition(this.getBlockPosition().sub(v));
    }

    @Override
    public ServerLocation sub(final double x, final double y, final double z) {
        return this.withPosition(this.getPosition().sub(x, y, z));
    }

    @Override
    public ServerLocation add(final Vector3d v) {
        return this.withPosition(this.getPosition().add(v));
    }

    @Override
    public ServerLocation add(final Vector3i v) {
        return this.withBlockPosition(this.getBlockPosition().add(v));
    }

    @Override
    public ServerLocation add(final double x, final double y, final double z) {
        return this.withPosition(this.getPosition().add(x, y, z));
    }

    @Override
    public ServerLocation relativeTo(final Direction direction) {
        return this.add(direction.asOffset());
    }

    @Override
    public ServerLocation relativeToBlock(final Direction direction) {
        return this.add(direction.asBlockOffset());
    }

    @Override
    public ResourceKey getWorldKey() {
        return this.getWorld().getKey();
    }

    @Override
    public LocatableBlock asLocatableBlock() {
        return new SpongeLocatableBlockBuilder()
                   .location(this)
                   .build();
    }

    @Override
    public <T> T map(final BiFunction<ServerWorld, Vector3d, T> mapper) {
        throw new MissingImplementationException("ServerLocation", "map");
    }

    @Override
    public <T> T mapBlock(final BiFunction<ServerWorld, Vector3i, T> mapper) {
        throw new MissingImplementationException("ServerLocation", "mapBlock");
    }

    @Override
    public <T> T mapChunk(final BiFunction<ServerWorld, Vector3i, T> mapper) {
        throw new MissingImplementationException("ServerLocation", "mapChunk");
    }

    @Override
    public <T> T mapBiome(final BiFunction<ServerWorld, Vector3i, T> mapper) {
        throw new MissingImplementationException("ServerLocation", "mapBiome");
    }

    @Override
    public boolean restoreSnapshot(final BlockSnapshot snapshot, final boolean force, final BlockChangeFlag flag) {
        return false;
    }

    @Override
    public boolean removeBlock() {
        return this.getWorld().removeBlock(this.getBlockPosition());
    }

    @Override
    public Entity createEntity(final EntityType<?> type) {
        return this.getWorld().createEntity(type, this.getPosition());

    }

    @Override
    public boolean spawnEntity(final Entity entity) {
        return this.getWorld().spawnEntity(entity);
    }

    @Override
    public Collection<Entity> spawnEntities(final Iterable<? extends Entity> entities) {
        return this.getWorld().spawnEntities(entities);

    }

    @Override
    public ServerLocation asHighestLocation() {
        return this.withBlockPosition(this.getWorld().getHighestPositionAt(this.getBlockPosition()));
    }

    @Override
    public BlockSnapshot createSnapshot() {
        return this.getWorld().createSnapshot(this.getBlockPosition());

    }

    @Override
    public Collection<? extends ScheduledUpdate<BlockType>> getScheduledBlockUpdates() {
        return this.getWorld().getScheduledBlockUpdates().getScheduledAt(this.getBlockPosition());
    }

    @Override
    public ScheduledUpdate<BlockType> scheduleBlockUpdate(final int delay, final TemporalUnit temporalUnit) {
        throw new MissingImplementationException("ServerLocation", "scheduleBlockUpdate");
    }

    @Override
    public ScheduledUpdate<BlockType> scheduleBlockUpdate(final int delay, final TemporalUnit temporalUnit,
        final TaskPriority priority
    ) {
        throw new MissingImplementationException("ServerLocation", "scheduleBlockUpdate");
    }

    @Override
    public ScheduledUpdate<BlockType> scheduleBlockUpdate(final Duration delay) {
        throw new MissingImplementationException("ServerLocation", "scheduleBlockUpdate");
    }

    @Override
    public ScheduledUpdate<BlockType> scheduleBlockUpdate(final Duration delay, final TaskPriority priority) {
        throw new MissingImplementationException("ServerLocation", "scheduleBlockUpdate");
    }

    @Override
    public Collection<? extends ScheduledUpdate<FluidType>> getScheduledFluidUpdates() {
        return this.getWorld().getScheduledFluidUpdates().getScheduledAt(this.getBlockPosition());
    }

    @Override
    public ScheduledUpdate<FluidType> scheduleFluidUpdate(final int delay, final TemporalUnit temporalUnit) {
        throw new MissingImplementationException("ServerLocation", "scheduleFluidUpdate");
    }

    @Override
    public ScheduledUpdate<FluidType> scheduleFluidUpdate(final int delay, final TemporalUnit temporalUnit,
        final TaskPriority priority) {
        throw new MissingImplementationException("ServerLocation", "scheduleFluidUpdate");
    }

    @Override
    public ScheduledUpdate<FluidType> scheduleFluidUpdate(final Duration delay) {
        throw new MissingImplementationException("ServerLocation", "scheduleFluidUpdate");
    }

    @Override
    public ScheduledUpdate<FluidType> scheduleFluidUpdate(final Duration delay, final TaskPriority priority) {
        throw new MissingImplementationException("ServerLocation", "scheduleFluidUpdate");
    }

    @Override
    public <E> DataTransactionResult transform(final Key<? extends Value<E>> key, final Function<E, E> function) {
        return this.getWorld().transform(this.getBlockPosition(), key, function);
    }

    @Override
    public <E> DataTransactionResult offer(final Key<? extends Value<E>> key, final E value) {
        return this.getWorld().offer(this.getBlockPosition(), key, value);
    }

    @Override
    public DataTransactionResult offer(final Value<?> value) {
        return this.getWorld().offer(this.getBlockPosition(), value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <E> DataTransactionResult offerSingle(final Key<? extends CollectionValue<E, ?>> key, final E element) {
        return this.getWorld().transform(this.getBlockPosition(), (Key) key, (Function<Collection<E>, Collection<E>>) es -> {
            es.add(element);
            return es;
        });
    }

    @Override
    public <K, V> DataTransactionResult offerSingle(final Key<? extends MapValue<K, V>> key, final K valueKey, final V value) {
        return this.getWorld().transform(this.getBlockPosition(), key, es -> {
            es.put(valueKey, value);
            return es;
        });
    }

    @Override
    public <K, V> DataTransactionResult offerAll(final Key<? extends MapValue<K, V>> key, final Map<? extends K, ? extends V> map) {
        return this.getWorld().transform(this.getBlockPosition(), key, es -> {
            es.putAll(map);
            return es;
        });
    }

    @Override
    public DataTransactionResult offerAll(final MapValue<?, ?> value) {
        return this.getWorld().offer(this.getBlockPosition(), value);
    }

    @Override
    public DataTransactionResult offerAll(final CollectionValue<?, ?> value) {
        return this.getWorld().offer(this.getBlockPosition(), value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <E> DataTransactionResult offerAll(final Key<? extends CollectionValue<E, ?>> key, final Collection<? extends E> elements) {
        return this.getWorld().offer(this.getBlockPosition(), (Key) key, elements);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <E> DataTransactionResult removeSingle(final Key<? extends CollectionValue<E, ?>> key, final E element) {
        return this.getWorld().transform(this.getBlockPosition(), (Key) key, (Function<Collection<E>, Collection<E>>) col -> {
            col.remove(element);
            return col;
        });
    }

    @Override
    public <K> DataTransactionResult removeKey(final Key<? extends MapValue<K, ?>> key, final K mapKey) {
        return this.getWorld().transform(this.getBlockPosition(), key, map -> {
            map.remove(mapKey);
            return map;
        });
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public DataTransactionResult removeAll(final CollectionValue<?, ?> value) {
        return this.getWorld().transform(this.getBlockPosition(), value.getKey(), col -> {
            col.removeAll(value.getAll());
            return col;
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <E> DataTransactionResult removeAll(final Key<? extends CollectionValue<E, ?>> key, final Collection<? extends E> elements) {
        return this.getWorld().transform(this.getBlockPosition(), (Key) key, (Function<Collection<E>, Collection<E>>) col -> {
            col.removeAll(elements);
            return col;
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public DataTransactionResult removeAll(final MapValue<?, ?> value) {
        return this.getWorld().transform(this.getBlockPosition(), (Key) value.getKey(), (Function<Map<?, ?>, Map<?, ?>>) map -> {
            value.keySet().forEach(map::remove);
            return map;
        });
    }

    @Override
    public <K, V> DataTransactionResult removeAll(final Key<? extends MapValue<K, V>> key, final Map<? extends K, ? extends V> map) {
        return this.getWorld().transform(this.getBlockPosition(), key, ex -> {
            map.keySet().forEach(ex::remove);
            return ex;
        });
    }

    @Override
    public <E> DataTransactionResult tryOffer(final Key<? extends Value<E>> key, final E value) {
        final DataTransactionResult result = this.offer(key, value);
        if (!result.isSuccessful()) {
            throw new IllegalArgumentException("Failed offer transaction!");
        }
        return result;
    }

    @Override
    public DataTransactionResult remove(final Key<?> key) {
        return this.getWorld().remove(this.getBlockPosition(), key);
    }

    @Override
    public DataTransactionResult undo(final DataTransactionResult result) {
        return this.getWorld().undo(this.getBlockPosition(), result);
    }

    @Override
    public DataTransactionResult copyFrom(final ValueContainer that, final MergeFunction function) {
        return this.getWorld().copyFrom(this.getBlockPosition(), that, function);
    }

    @Override
    public <E> Optional<E> get(final Direction direction, final Key<? extends Value<E>> key) {
        return this.getWorld().get(this.getBlockPosition(), key);
    }

    @Override
    public <E> Optional<E> get(final Key<? extends Value<E>> key) {
        return this.getWorld().get(this.getBlockPosition(), key);
    }

    @Override
    public <E, V extends Value<E>> Optional<V> getValue(final Key<V> key) {
        return this.getWorld().getValue(this.getBlockPosition(), key);
    }

    @Override
    public boolean supports(final Key<?> key) {
        return this.getWorld().supports(this.getBlockPosition(), key);
    }

    @Override
    public boolean supports(final Supplier<? extends Key<?>> key) {
        return this.getWorld().supports(this.getBlockPosition(), key);
    }

    @Override
    public Set<Key<?>> getKeys() {
        return this.getWorld().getKeys(this.getBlockPosition());
    }

    @Override
    public Set<Value.Immutable<?>> getValues() {
        return this.getWorld().getValues(this.getBlockPosition());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeLocation<?> that = (SpongeLocation<?>) o;
        return this.worldRef.equals(that.worldRef) &&
                   this.getPosition().equals(that.getPosition()) &&
                   this.getBlockPosition().equals(that.getBlockPosition());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.worldRef, this.getPosition(), this.getBlockPosition());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SpongeServerLocation.class.getSimpleName() + "[", "]")
                   .add("worldRef=" + this.getWorldKey())
                   .add("position=" + this.getPosition())
                   .toString();
    }

}
