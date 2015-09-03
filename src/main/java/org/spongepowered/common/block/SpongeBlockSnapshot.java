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
package org.spongepowered.common.block;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.interfaces.block.IMixinBlock;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public class SpongeBlockSnapshot implements BlockSnapshot {

    private final BlockState blockState;
    @Nullable private final Location<World> location;
    private final ImmutableList<ImmutableDataManipulator<?, ?>> extraData;
    private final ImmutableMap<Key<?>, ImmutableValue<?>> keyValueMap;
    private final ImmutableSet<ImmutableValue<?>> valueSet;

    public SpongeBlockSnapshot(BlockState blockState) {
        this(blockState, null, ImmutableList.<ImmutableDataManipulator<?, ?>>of());
    }

    public SpongeBlockSnapshot(BlockState blockState, @Nullable Location<World> location) {
        this(blockState, location, ImmutableList.<ImmutableDataManipulator<?, ?>>of());
    }

    public SpongeBlockSnapshot(BlockState blockState, @Nullable Location<World> location, ImmutableList<ImmutableDataManipulator<?, ?>> extraData) {
        this.blockState = checkNotNull(blockState);
        this.location = location;
        this.extraData = extraData;
        ImmutableMap.Builder<Key<?>, ImmutableValue<?>> builder = ImmutableMap.builder();
        for (ImmutableValue<?> value : this.blockState.getValues()) {
            builder.put(value.getKey(), value);
        }
        for (ImmutableDataManipulator<?, ?> manipulator : this.extraData) {
            for (ImmutableValue<?> value : manipulator.getValues()) {
                builder.put(value.getKey(), value);
            }
        }
        this.keyValueMap = builder.build();
        this.valueSet = ImmutableSet.copyOf(this.keyValueMap.values());
    }

    public SpongeBlockSnapshot(BlockState blockState, TileEntity entity) {
        final ImmutableList.Builder<ImmutableDataManipulator<?, ?>> builder = ImmutableList.builder();
        for (DataManipulator<?, ?> manipulator : entity.getContainers()) {
            builder.add(manipulator.asImmutable());
        }
        this.blockState = checkNotNull(blockState);
        this.location = entity.getLocation();
        this.extraData = builder.build();
        ImmutableMap.Builder<Key<?>, ImmutableValue<?>> keyValueBuilder = ImmutableMap.builder();
        for (ImmutableValue<?> value : this.blockState.getValues()) {
            keyValueBuilder.put(value.getKey(), value);
        }
        for (ImmutableDataManipulator<?, ?> manipulator : this.extraData) {
            for (ImmutableValue<?> value : manipulator.getValues()) {
                keyValueBuilder.put(value.getKey(), value);
            }
        }
        this.keyValueMap = keyValueBuilder.build();
        this.valueSet = ImmutableSet.copyOf(this.keyValueMap.values());
    }

    @Override
    public BlockState getState() {
        return this.blockState;
    }

    @Override
    public BlockSnapshot withState(BlockState blockState) {
        return new SpongeBlockSnapshot(checkNotNull(blockState), this.location, ImmutableList.<ImmutableDataManipulator<?, ?>>of());
    }

    @Override
    public Optional<Location<World>> getLocation() {
        return Optional.fromNullable(this.location);
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getManipulators() {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>builder().addAll(this.blockState.getManipulators()).addAll(this.extraData).build();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
            .set(of("Location"), this.location == null ? "null" : this.location)
            .set(of("BlockState"), this.blockState)
            .set(of("ExtraData"), this.extraData);
    }

    @Override
    public <T extends ImmutableDataManipulator<?, ?>> Optional<T> get(Class<T> containerClass) {
        Optional<T> optional = this.blockState.get(containerClass);
        if (optional.isPresent()) {
            return optional;
        } else {
            for (ImmutableDataManipulator<?, ?> dataManipulator : this.extraData) {
                if (containerClass.isInstance(dataManipulator)) {
                    return Optional.of(((T) dataManipulator));
                }
            }
        }
        return Optional.absent();
    }

    @Override
    public <T extends ImmutableDataManipulator<?, ?>> Optional<T> getOrCreate(Class<T> containerClass) {
        return get(containerClass);
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        return this.blockState.supports(containerClass);
    }

    @Override
    public <E> Optional<BlockSnapshot> transform(Key<? extends BaseValue<E>> key, Function<E, E> function) {
        return Optional.absent();
    }

    @Override
    public <E> Optional<BlockSnapshot> with(Key<? extends BaseValue<E>> key, E value) {
        Optional<BlockState> optional = this.blockState.with(key, value);
        if (optional.isPresent()) {
            return Optional.of(withState(optional.get()));
        }
        return Optional.absent();
    }

    @Override
    public Optional<BlockSnapshot> with(BaseValue<?> value) {
        return with((Key) value.getKey(), value.get());
    }

    @Override
    public Optional<BlockSnapshot> with(ImmutableDataManipulator<?, ?> valueContainer) {
        if (((IMixinBlock) this.blockState.getType()).supports((Class<ImmutableDataManipulator<?, ?>>) valueContainer.getClass())) {
            final BlockState newState;
            boolean changeState = false;
            if (this.blockState.supports((Class<ImmutableDataManipulator<?, ?>>) valueContainer.getClass())) {
                newState = this.blockState.with(valueContainer).get();
                changeState = true;
            } else {
                newState = this.blockState;
            }
            if (changeState) {
                return Optional.<BlockSnapshot>of(new SpongeBlockSnapshot(newState, this.location, this.extraData));
            } else {
                final ImmutableList.Builder<ImmutableDataManipulator<?, ?>> builder = ImmutableList.builder();
                for (ImmutableDataManipulator<?, ?> manipulator : this.extraData) {
                    if (manipulator.getClass().isAssignableFrom(valueContainer.getClass())) {
                        builder.add(valueContainer);
                    } else {
                        builder.add(manipulator);
                    }
                }
                return Optional.<BlockSnapshot>of(new SpongeBlockSnapshot(newState, this.location, builder.build()));
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<BlockSnapshot> with(Iterable<ImmutableDataManipulator<?, ?>> valueContainers) {
        BlockSnapshot snapshot = this;
        for (ImmutableDataManipulator<?, ?> manipulator : valueContainers) {
            final Optional<BlockSnapshot> optional = snapshot.with(manipulator);
            if (!optional.isPresent()) {
                return Optional.absent();
            }
            snapshot = optional.get();
        }
        return Optional.of(snapshot);
    }

    @Override
    public Optional<BlockSnapshot> without(Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        return Optional.absent();
    }

    @Override
    public BlockSnapshot merge(BlockSnapshot that) {
        return merge(that, MergeFunction.FORCE_NOTHING);
    }

    @Override
    public BlockSnapshot merge(BlockSnapshot that, MergeFunction function) {
        BlockSnapshot merged = this;
        merged = merged.withState(function.merge(this.blockState, that.getState()));
        for (ImmutableDataManipulator<?, ?> manipulator : that.getContainers()) {
            Optional<BlockSnapshot> optional = merged.with(function.merge(this.get(manipulator.getClass()).orNull(), manipulator));
            if (optional.isPresent()) {
                merged = optional.get();
            }
        }
        return merged;
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getContainers() {
        return getManipulators();
    }

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        if (this.keyValueMap.containsKey(key)) {
            return Optional.of((E) this.keyValueMap.get(key).get());
        }
        return Optional.absent();
    }

    @Nullable
    @Override
    public <E> E getOrNull(Key<? extends BaseValue<E>> key) {
        return get(key).orNull();
    }

    @Override
    public <E> E getOrElse(Key<? extends BaseValue<E>> key, E defaultValue) {
        return get(key).or(defaultValue);
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        if (this.keyValueMap.containsKey(key)) {
            return Optional.of((V) this.keyValueMap.get(key).asMutable());
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(Key<?> key) {
        return this.keyValueMap.containsKey(key);
    }

    @Override
    public boolean supports(BaseValue<?> baseValue) {
        return supports(baseValue.getKey());
    }

    @Override
    public BlockSnapshot copy() {
        return this;
    }

    @Override
    public Set<Key<?>> getKeys() {
        return this.keyValueMap.keySet();
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        return this.valueSet;
    }
}
