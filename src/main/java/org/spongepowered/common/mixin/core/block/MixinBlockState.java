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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.util.Cycleable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.interfaces.block.IMixinBlockState;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(net.minecraft.block.state.BlockState.StateImplementation.class)
public abstract class MixinBlockState extends BlockStateBase implements BlockState, IMixinBlockState {

    @Shadow
    @SuppressWarnings("rawtypes")
    private ImmutableMap properties;
    @Shadow private Block block;

    private ImmutableSet<ImmutableValue<?>> values;
    private ImmutableSet<Key<?>> keys;
    private ImmutableList<ImmutableDataManipulator<?, ?>> manipulators;
    private ImmutableMap<Key<?>, Object> keyMap;

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
        final SpongeBlockSnapshotBuilder builder = new SpongeBlockSnapshotBuilder()
            .blockState(this)
            .position(location.getBlockPosition())
            .worldId(location.getExtent().getUniqueId());
        if (this.block.hasTileEntity() && location.getBlockType().equals(this.block)) {
            final TileEntity tileEntity = location.getTileEntity().get();
            for (DataManipulator<?, ?> manipulator : tileEntity.getContainers()) {
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
        } else {
            E current = this.get(key).get();
            final E newVal = checkNotNull(function.apply(current));
            return this.with(key, newVal);
        }
    }

    @Override
    public <E> Optional<BlockState> with(Key<? extends BaseValue<E>> key, E value) {
        if (!supports(key)) {
            return Optional.empty();
        }
        return ((IMixinBlock) this.block).getStateWithValue(this, key, value);
    }

    @Override
    public Optional<BlockState> with(BaseValue<?> value) {
        return with((Key<? extends BaseValue<Object>>) value.getKey(), value.get());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Optional<BlockState> with(ImmutableDataManipulator<?, ?> valueContainer) {
        if (supports((Class<ImmutableDataManipulator<?, ?>>) (Class) valueContainer.getClass())) {
            return ((IMixinBlock) this.block).getStateWithData(this, valueContainer);
        } else {
            return Optional.empty();
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
                @Nullable ImmutableDataManipulator old = temp.get(manipulator.getClass()).orElse(null);
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

    @Nullable
    @Override
    public <E> E getOrNull(Key<? extends BaseValue<E>> key) {
        return get(key).orElse(null);
    }

    @Override
    public <E> E getOrElse(Key<? extends BaseValue<E>> key, E defaultValue) {
        return get(key).orElse(checkNotNull(defaultValue));
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
    public DataContainer toContainer() {
        final DataContainer container = new MemoryDataContainer()
            .set(DataQueries.BLOCK_TYPE, this.getType().getId())
            .set(DataQueries.BLOCK_STATE_UNSAFE_META, this.getStateMeta());
        final List<DataView> manipulators = DataUtil.getSerializedImmutableManipulatorList(getContainers());
        if (!manipulators.isEmpty()) {
            container.set(DataQueries.DATA_MANIPULATORS, manipulators);
        }
        return container;
    }

    @Override
    public int getStateMeta() {
        return this.block.getMetaFromState(this);
    }

    @Override
    public Optional<BlockTrait<?>> getTrait(String blockTrait) {
        for (Object obj : this.properties.keySet()) {
            BlockTrait<?> trait = (BlockTrait<?>) obj;
            if (trait.getName().equalsIgnoreCase(blockTrait)) {
                return Optional.<BlockTrait<?>>of(trait);
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Comparable<T>> Optional<T> getTraitValue(BlockTrait<T> property) {
        if (!this.properties.containsKey(property)) {
            return Optional.empty();
        } else {
            return Optional.of((T) (Comparable<T>) property.getValueClass().cast(this.properties.get(property)));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmutableMap<BlockTrait<?>, ?> getTraitMap() {
        return getProperties();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<BlockTrait<?>> getTraits() {
        return getProperties().keySet();
    }

    @Override
    public Collection<?> getTraitValues() {
        return getProperties().values();
    }

}
