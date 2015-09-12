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
package org.spongepowered.common.mixin.core.block;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateBase;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.util.Cycleable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.data.BlockDataProcessor;
import org.spongepowered.common.data.BlockValueProcessor;
import org.spongepowered.common.data.SpongeDataRegistry;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.interfaces.block.IMixinBlockState;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(net.minecraft.block.state.BlockState.StateImplementation.class)
public abstract class MixinBlockState extends BlockStateBase implements BlockState, IMixinBlockState {

    @Shadow
    @SuppressWarnings("rawtypes")
    private ImmutableMap properties;
    @Shadow private Block block;

    @Nullable private ImmutableSet<ImmutableValue<?>> values;
    @Nullable private ImmutableSet<Key<?>> keys;
    @Nullable private ImmutableList<ImmutableDataManipulator<?, ?>> manipulators;
    @Nullable private ImmutableMap<Key<?>, Object> keyMap;

    @Override
    public BlockType getType() {
        return (BlockType) getBlock();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public BlockState cycleValue(Key<? extends BaseValue<? extends Cycleable<?>>> key) {
        if (supports(key)) {
            final Cycleable value = (Cycleable) get((Key) key).get();
            final Cycleable next = value.cycleNext();
            return with((Key<? extends BaseValue<Object>>) (Object) key, next).get();
        }
        throw new IllegalArgumentException("Used an invalid cyclable key! Check with supports in the future!");
    }

    @Override
    public BlockSnapshot snapshotFor(Location<World> location) {
        if (this.block.hasTileEntity() && location.getBlockType().equals(this.block)) {
            return new SpongeBlockSnapshot(this, location, location.getTileEntity().get());
        }
        return new SpongeBlockSnapshot(this, location);
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getManipulators() {
        if (this.manipulators == null) {
            this.manipulators = ImmutableList.copyOf(((IMixinBlock) this.block).getManipulators(this));
        }
        return this.manipulators;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ImmutableDataManipulator<?, ?>> Optional<T> get(Class<T> containerClass) {
        for (ImmutableDataManipulator<?, ?> manipulator : this.getManipulators()) {
            if (containerClass.isInstance(manipulator)) {
                return Optional.of((T) manipulator);
            }
        }
        return Optional.absent();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ImmutableDataManipulator<?, ?>> Optional<T> getOrCreate(Class<T> containerClass) {
        for (ImmutableDataManipulator<?, ?> manipulator : this.getManipulators()) {
            if (containerClass.isInstance(manipulator)) {
                return Optional.of(((T) manipulator));
            }
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        return ((IMixinBlock) this.block).supports(containerClass);
    }

    @Override
    public <E> Optional<BlockState> transform(Key<? extends BaseValue<E>> key, Function<E, E> function) {
        if (!supports(checkNotNull(key))) {
            return Optional.absent();
        } else {
            E current = this.get(key).get();
            final E newVal = checkNotNull(function.apply(current));
            Optional<BlockValueProcessor<E, ?>> optional = SpongeDataRegistry.getInstance().getBaseBlockValueProcessor(key);
            if (optional.isPresent()) {
                return optional.get().offerValue(this, newVal);
            }
        }
        return Optional.absent();
    }

    @Override
    public <E> Optional<BlockState> with(Key<? extends BaseValue<E>> key, E value) {
        if (!supports(key)) {
            return Optional.absent();
        }
        final Optional<BlockValueProcessor<E, ?>> optional = SpongeDataRegistry.getInstance().getBaseBlockValueProcessor(key);
        if (optional.isPresent()) {
            return optional.get().offerValue(this, value);
        }
        return Optional.absent();
    }

    @Override
    public Optional<BlockState> with(BaseValue<?> value) {
        return with((Key<? extends BaseValue<Object>>) value.getKey(), value.get());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Optional<BlockState> with(ImmutableDataManipulator<?, ?> valueContainer) {
        final Optional<BlockDataProcessor> optional = SpongeDataRegistry.getInstance().getWildBlockDataProcessor(valueContainer.getClass());
        if (!optional.isPresent()) {
            return Optional.absent();
        } else {
            return optional.get().withData(this, valueContainer);
        }
    }

    @Override
    public Optional<BlockState> with(Iterable<ImmutableDataManipulator<?, ?>> valueContainers) {
        BlockState state = this;
        for (ImmutableDataManipulator<?, ?> manipulator : valueContainers) {
            final Optional<BlockState> optional = state.with(manipulator);
            if (optional.isPresent()) {
                state = optional.get();
            } else {
                return Optional.absent();
            }
        }
        return Optional.of(state);
    }

    @Override
    public Optional<BlockState> without(Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        return Optional.absent();
    }

    @Override
    public BlockState merge(BlockState that) {
        if (!getType().equals(that.getType())) {
            return this;
        } else {
            BlockState temp = this;
            for (ImmutableDataManipulator<?, ?> manipulator : that.getManipulators()) {
                Optional<BlockState> optional = temp.with(manipulator);
                if (optional.isPresent()) {
                    temp = optional.get();
                } else {
                    return temp;
                }
            }
            return temp;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public BlockState merge(BlockState that, MergeFunction function) {
        if (!getType().equals(that.getType())) {
            return this;
        } else {
            BlockState temp = this;
            for (ImmutableDataManipulator<?, ?> manipulator : that.getManipulators()) {
                @Nullable ImmutableDataManipulator old = temp.get(manipulator.getClass()).orNull();
                Optional<BlockState> optional = temp.with(checkNotNull(function.merge(old, manipulator)));
                if (optional.isPresent()) {
                    temp = optional.get();
                } else {
                    return temp;
                }
            }
            return temp;
        }
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getContainers() {
        return this.getManipulators();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        if (this.keyMap == null) {
            generateKeyMap();
        }
        if (this.keyMap.containsKey(checkNotNull(key))) {
            return Optional.of((E) this.keyMap.get(key));
        }
        return Optional.absent();
    }

    private void generateKeyMap() {
        ImmutableMap.Builder<Key<?>, Object> builder = ImmutableMap.builder();
        for (ImmutableDataManipulator<?, ?> manipulator : this.getManipulators()) {
            for (ImmutableValue<?> value : manipulator.getValues()) {
                builder.put(value.getKey(), value.get());
            }
        }
        this.keyMap = builder.build();
    }

    @Nullable
    @Override
    public <E> E getOrNull(Key<? extends BaseValue<E>> key) {
        return get(key).orNull();
    }

    @Override
    public <E> E getOrElse(Key<? extends BaseValue<E>> key, E defaultValue) {
        return get(key).or(checkNotNull(defaultValue));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        checkNotNull(key);
        for (ImmutableValue<?> value : this.getValues()) {
            if (value.getKey().equals(key)) {
                return Optional.of((V) value.asMutable());
            }
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(Key<?> key) {
        return this.getKeys().contains(checkNotNull(key));
    }

    @Override
    public boolean supports(BaseValue<?> baseValue) {
        return supports(baseValue.getKey());
    }

    @Override
    public BlockState copy() {
        return this;
    }

    @Override
    public Set<Key<?>> getKeys() {
        if (this.keys == null) {
            this.keys = ImmutableSet.copyOf(((IMixinBlock) this.block).getApplicableKeys());
        }
        return this.keys;
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        if (this.values == null) {
            this.values = ImmutableSet.copyOf(((IMixinBlock) this.block).getValues(this));
        }
        return this.values;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
            .set(DataQueries.BLOCK_STATE_TYPE, this.getType().getId())
            .set(DataQueries.BLOCK_STATE_DATA, this.getManipulators())
            .set(DataQueries.BLOCK_STATE_UNSAFE_META, this.getStateMeta());
    }

    @Override
    public int getStateMeta() {
        return this.block.getMetaFromState(this);
    }
}
