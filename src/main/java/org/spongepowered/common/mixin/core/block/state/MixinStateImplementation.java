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
package org.spongepowered.common.mixin.core.block.state;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.util.Cycleable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataVersions;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.interfaces.block.IMixinBlockState;
import org.spongepowered.common.interfaces.data.IMixinCustomDataHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

/**
 * This shares implementation with {@link IMixinBlockState}, since this
 * all relies on Data API implementations.
 */
@Mixin(net.minecraft.block.state.BlockStateContainer.StateImplementation.class)
public abstract class MixinStateImplementation extends BlockStateBase implements BlockState, IMixinBlockState {

    @Shadow @Final private Block block;
    @Shadow @Final private ImmutableMap<IProperty<?>, Comparable<?>> properties;

    private ImmutableSet<ImmutableValue<?>> values;
    private ImmutableSet<Key<?>> keys;
    private ImmutableList<ImmutableDataManipulator<?, ?>> manipulators;
    private ImmutableMap<Key<?>, Object> keyMap;

    private String id;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public BlockState cycleValue(Key<? extends BaseValue<? extends Cycleable<?>>> key) {
        if (supports(key)) {
            final Cycleable value = (Cycleable) get((Key) key).get();
            final Cycleable next = value.cycleNext();
            return with((Key<? extends BaseValue<Object>>) (Key<?>) key, next).get();
        }
        throw new IllegalArgumentException("Used an invalid cyclable key! Check with supports in the future!");
    }

    @Override
    public BlockSnapshot snapshotFor(Location<World> location) {
        final SpongeBlockSnapshotBuilder builder = new SpongeBlockSnapshotBuilder()
            .blockState(this)
            .position(location.getBlockPosition())
            .worldId(location.getExtent().getUniqueId());
        if (this.block.hasTileEntity() && location.getBlockType().equals(this.block)) {
            final TileEntity tileEntity = location.getTileEntity().get();
            for (DataManipulator<?, ?> manipulator : ((IMixinCustomDataHolder) tileEntity).getCustomManipulators()) {
                builder.add(manipulator);
            }
            final NBTTagCompound compound = new NBTTagCompound();
            ((net.minecraft.tileentity.TileEntity) tileEntity).writeToNBT(compound);
            builder.unsafeNbt(compound);
        }
        return builder.build();
    }
    @Override
    public List<ImmutableDataManipulator<?, ?>> getManipulators() {
        if (this.manipulators == null) {
            this.manipulators = ImmutableList.copyOf(((IMixinBlock) this.block).getManipulators(this));
            populateKeyValues();
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
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ImmutableDataManipulator<?, ?>> Optional<T> getOrCreate(Class<T> containerClass) {
        for (ImmutableDataManipulator<?, ?> manipulator : this.getManipulators()) {
            if (containerClass.isInstance(manipulator)) {
                return Optional.of(((T) manipulator));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        return ((IMixinBlock) this.block).supports(containerClass);
    }

    @Override
    public <E> Optional<BlockState> transform(Key<? extends BaseValue<E>> key, Function<E, E> function) {
        if (!supports(checkNotNull(key))) {
            return Optional.empty();
        }
        E current = this.get(key).get();
        final E newVal = checkNotNull(function.apply(current));
        return this.with(key, newVal);
    }

    @Override
    public <E> Optional<BlockState> with(Key<? extends BaseValue<E>> key, E value) {
        if (!supports(key)) {
            return Optional.empty();
        }
        return ((IMixinBlock) this.block).getStateWithValue(this, key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<BlockState> with(BaseValue<?> value) {
        return with((Key<? extends BaseValue<Object>>) value.getKey(), value.get());
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public Optional<BlockState> with(ImmutableDataManipulator<?, ?> valueContainer) {
        if (supports((Class<ImmutableDataManipulator<?, ?>>) valueContainer.getClass())) {
            return ((IMixinBlock) this.block).getStateWithData(this, valueContainer);
        }
        return Optional.empty();
    }

    @Override
    public Optional<BlockState> with(Iterable<ImmutableDataManipulator<?, ?>> valueContainers) {
        BlockState state = this;
        for (ImmutableDataManipulator<?, ?> manipulator : valueContainers) {
            final Optional<BlockState> optional = state.with(manipulator);
            if (optional.isPresent()) {
                state = optional.get();
            } else {
                return Optional.empty();
            }
        }
        return Optional.of(state);
    }

    @Override
    public Optional<BlockState> without(Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        return Optional.empty(); // By default, all manipulators have to have the manipulator if it exists, we can't remove data.
    }

    @Override
    public BlockState merge(BlockState that) {
        if (!getType().equals(that.getType())) {
            return this;
        }
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

    @Override
    public BlockState merge(BlockState that, MergeFunction function) {
        if (!getType().equals(that.getType())) {
            return this;
        }
        BlockState temp = this;
        for (ImmutableDataManipulator<?, ?> manipulator : that.getManipulators()) {
            @Nullable ImmutableDataManipulator<?, ?> old = temp.get(manipulator.getClass()).orElse(null);
            Optional<BlockState> optional = temp.with(checkNotNull(function.merge(old, manipulator)));
            if (optional.isPresent()) {
                temp = optional.get();
            } else {
                return temp;
            }
        }
        return temp;
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getContainers() {
        return this.getManipulators();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        if(this.keyMap == null) {
            this.populateKeyValues();
        }
        if (this.keyMap.containsKey(checkNotNull(key))) {
            return Optional.of((E) this.keyMap.get(key));
        }
        return Optional.empty();
    }

    private void populateKeyValues() {
        ImmutableMap.Builder<Key<?>, Object> builder = ImmutableMap.builder();
        ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<ImmutableValue<?>> valueBuilder = ImmutableSet.builder();
        for (ImmutableDataManipulator<?, ?> manipulator : this.getManipulators()) {
            for (ImmutableValue<?> value : manipulator.getValues()) {
                builder.put(value.getKey(), value.get());
                valueBuilder.add(value);
                keyBuilder.add(value.getKey());
            }
        }
        this.values = valueBuilder.build();
        this.keys = keyBuilder.build();
        this.keyMap = builder.build();
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
        return Optional.empty();
    }

    @Override
    public boolean supports(Key<?> key) {
        return this.getKeys().contains(checkNotNull(key));
    }

    @Override
    public BlockState copy() {
        return this;
    }

    @Override
    public Set<Key<?>> getKeys() {
        if (this.keys == null) {
            populateKeyValues();
        }
        return this.keys;
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        if (this.values == null) {
            populateKeyValues();
        }
        return this.values;
    }

    @Override
    public int getContentVersion() {
        return DataVersions.BlockState.STATE_AS_CATALOG_ID;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, getContentVersion())
            .set(DataQueries.BLOCK_STATE, this.getId());
    }

    @Override
    public int getStateMeta() {
        return this.block.getMetaFromState(this);
    }

    @Override
    public void generateId(Block block) {
        StringBuilder builder = new StringBuilder();
        builder.append(((BlockType) block).getId());
        if (!this.properties.isEmpty()) {
            builder.append('[');
            Joiner joiner = Joiner.on(',');
            List<String> propertyValues = new ArrayList<>();
            for (Map.Entry<IProperty<?>, Comparable<?>> entry : this.properties.entrySet()) {
                propertyValues.add(entry.getKey().getName() + "=" + entry.getValue());
            }
            builder.append(joiner.join(propertyValues));
            builder.append(']');
        }
        this.id = builder.toString();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.id;
    }
}
