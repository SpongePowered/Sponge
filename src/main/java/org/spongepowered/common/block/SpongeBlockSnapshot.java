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
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public class SpongeBlockSnapshot implements BlockSnapshot {

    private final BlockState blockState;
    @Nullable private final Location<World> location;
    private final ImmutableList<ImmutableDataManipulator<?, ?>> extraData;
    private final ImmutableSet<ImmutableValue<?>> values;
    private final ImmutableSet<Key<?>> keys;

    public SpongeBlockSnapshot(BlockState blockState, @Nullable Location<World> location, ImmutableList<ImmutableDataManipulator<?, ?>> extraData) {
        this.blockState = checkNotNull(blockState);
        this.location = location;
        this.extraData = extraData;
        ImmutableSet.Builder<ImmutableValue<?>> builder = ImmutableSet.builder();
        ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();
        builder.addAll(this.blockState.getValues());
        keyBuilder.addAll(this.blockState.getKeys());
        for (ImmutableDataManipulator<?, ?> manipulator : this.extraData) {
            builder.addAll(manipulator.getValues());
            keyBuilder.addAll(manipulator.getKeys());
        }
        this.values = builder.build();
        this.keys = keyBuilder.build();
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
    public ImmutableCollection<ImmutableDataManipulator<?, ?>> getManipulators() {
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
        return Optional.absent();
    }

    @Override
    public Optional<BlockSnapshot> with(Iterable<ImmutableDataManipulator<?, ?>> valueContainers) {
        return Optional.absent();
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
    public ImmutableCollection<ImmutableDataManipulator<?, ?>> getContainers() {
        return getManipulators();
    }

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        for (ImmutableValue<?> value : this.values) {
            if (value.getKey().equals(key)) {
                return Optional.of((E) value.get());
            }
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
        for (ImmutableValue<?> value : this.values) {
            if (value.getKey().equals(key)) {
                return Optional.of(((V) value.asMutable()));
            }
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(Key<?> key) {
        return this.keys.contains(key);
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
    public ImmutableSet<Key<?>> getKeys() {
        return this.keys;
    }

    @Override
    public ImmutableSet<ImmutableValue<?>> getValues() {
        return this.values;
    }
}
