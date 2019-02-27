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
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.state.AbstractStateHolder;
import net.minecraft.state.IProperty;
import org.spongepowered.api.CatalogKey;
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
import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.Cycleable;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
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
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

/**
 * This shares implementation with {@link IMixinBlockState}, since this
 * all relies on Data API implementations.
 */
@Implements(@Interface(iface = BlockState.class, prefix = "blockState$"))
@Mixin(net.minecraft.block.state.BlockState.class)
public abstract class MixinStateImplementation extends AbstractStateHolder implements IMixinBlockState {

    // All of these fields are lazily evaluated either at startup or the first time
    // they are accessed by a plugin, depending on how much of an impact the
    // implementation can pose during start up, or whether game state
    // can affect the various systems in place (i.e. we sometimes can't load certain
    // systems before other registries have finished registering their stuff)
    @Nullable private ImmutableSet<Value.Immutable<?>> values;
    @Nullable private ImmutableSet<Key<?>> keys;
    @Nullable private ImmutableList<ImmutableDataManipulator<?, ?>> manipulators;
    @Nullable private ImmutableMap<Key<?>, Object> keyMap;
    @Nullable private ImmutableMap<Property<?>, ?> dataProperties;
    @Nullable private CatalogKey id;

    protected MixinStateImplementation(Object p_i49008_1_, ImmutableMap p_i49008_2_) {
        super(p_i49008_1_, p_i49008_2_);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public  <T extends Cycleable<T>> Optional<BlockState> blockState$cycleValue(Key<? extends Value<T>> key) {
        final Optional<Cycleable<?>> optional = blockState$get((Key) key);
        return optional
            .map(Cycleable::cycleNext)
            .flatMap(newVal -> {
                return with((Key) key, newVal);
            });
    }

    public BlockSnapshot blockState$snapshotFor(Location location) {
        final SpongeBlockSnapshotBuilder builder = new SpongeBlockSnapshotBuilder()
            .blockState((BlockState) this)
            .position(location.getBlockPosition())
            .worldId(location.getWorld().getUniqueId());
        if (((Block) this.object).hasTileEntity() && location.getBlock().getType().equals(this.object)) {
            final TileEntity tileEntity = location.getTileEntity()
                .orElseThrow(() -> new IllegalStateException("Unable to retrieve a TileEntity for location: " + location));
            for (DataManipulator<?, ?> manipulator : ((IMixinCustomDataHolder) tileEntity).getCustomManipulators()) {
                builder.add(manipulator);
            }
            final NBTTagCompound compound = new NBTTagCompound();
            ((net.minecraft.tileentity.TileEntity) tileEntity).write(compound);
            builder.unsafeNbt(compound);
        }
        return builder.build();
    }

    public List<ImmutableDataManipulator<?, ?>> blockState$getManipulators() {
        return lazyLoadManipulatorsAndKeys();
    }

    @Nullable
    private ImmutableMap<Key<?>, Object> getKeyMap() {
        if (this.keyMap == null) {
            lazyLoadManipulatorsAndKeys();
        }
        return this.keyMap;
    }

    private ImmutableList<ImmutableDataManipulator<?, ?>> lazyLoadManipulatorsAndKeys() {
        if (this.manipulators == null) {
            this.manipulators = ImmutableList.copyOf(((IMixinBlock) this.object).getManipulators((IBlockState) this));
        }
        if (this.keyMap == null) {
            ImmutableMap.Builder<Key<?>, Object> builder = ImmutableMap.builder();
            ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();
            ImmutableSet.Builder<Value.Immutable<?>> valueBuilder = ImmutableSet.builder();
            for (ImmutableDataManipulator<?, ?> manipulator : this.manipulators) {
                for (Value.Immutable<?> value : manipulator.getValues()) {
                    builder.put(value.getKey(), value.get());
                    valueBuilder.add(value);
                    keyBuilder.add(value.getKey());
                }
            }
            this.values = valueBuilder.build();
            this.keys = keyBuilder.build();
            this.keyMap = builder.build();
        }
        return this.manipulators;
    }

    @SuppressWarnings("unchecked")
    public <T extends ImmutableDataManipulator<?, ?>> Optional<T> blockState$get(Class<T> containerClass) {
        for (ImmutableDataManipulator<?, ?> manipulator : this.blockState$getManipulators()) {
            if (containerClass.isInstance(manipulator)) {
                return Optional.of((T) manipulator);
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public <T extends ImmutableDataManipulator<?, ?>> Optional<T> blockState$getOrCreate(Class<T> containerClass) {
        for (ImmutableDataManipulator<?, ?> manipulator : this.blockState$getManipulators()) {
            if (containerClass.isInstance(manipulator)) {
                return Optional.of(((T) manipulator));
            }
        }
        return Optional.empty();
    }

    public boolean blockState$supports(Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        return ((IMixinBlock) this.object).supports(containerClass);
    }

    public <E> Optional<BlockState> blockState$transform(Key<? extends Value<E>> key, Function<E, E> function) {
        return this.blockState$get(checkNotNull(key, "Key cannot be null!")) // If we don't have a value for the key, we don't support it.
            .map(checkNotNull(function, "Function cannot be null!"))
            .map(newVal -> with(key, newVal).orElse((BlockState) this)); // We can either return this value or the updated value, but not an empty
    }

    public <E> Optional<BlockState> with(Key<? extends Value<E>> key, E value) {
        if (!blockState$supports(key)) {
            return Optional.empty();
        }
        return ((IMixinBlock) this.object).getStateWithValue((IBlockState) this, key, value);
    }

    @SuppressWarnings("unchecked")
    public Optional<BlockState> blockState$with(Value<?> value) {
        return with((Key<? extends Value<Object>>) value.getKey(), value.get());
    }

    @SuppressWarnings({"unchecked"})
    public Optional<BlockState> blockState$with(ImmutableDataManipulator<?, ?> valueContainer) {
        if (((BlockState) (this)).supports((Class<ImmutableDataManipulator<?, ?>>) valueContainer.getClass())) {
            return ((IMixinBlock) this.object).getStateWithData((IBlockState) this, valueContainer);
        }
        return Optional.empty();
    }

    public Optional<BlockState> blockState$with(Iterable<ImmutableDataManipulator<?, ?>> valueContainers) {
        BlockState state = (BlockState) this;
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

    public Optional<BlockState> blockState$without(Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        return Optional.empty(); // By default, all manipulators have to have the manipulator if it exists, we can't remove data.
    }

    public BlockState blockState$merge(BlockState that) {
        if (!((BlockState) (this)).getType().equals(that.getType())) {
            return (BlockState) this;
        }
        BlockState temp = (BlockState) this;
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

    public BlockState blockState$merge(BlockState that, MergeFunction function) {
        if (!((BlockState) (this)).getType().equals(that.getType())) {
            return (BlockState) this;
        }
        BlockState temp = (BlockState) this;
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

    public <V> Optional<V> blockState$getProperty(Property<V> property) {
        checkNotNull(property, "property");
        return Optional.ofNullable((V) getSpongeInternalProperties().get(property));
    }

    public <V> Optional<V> blockState$getProperty(Direction direction, Property<V> property) {
        return blockState$getProperty(property);
    }

    public OptionalInt blockState$getIntProperty(Property<Integer> property) {
        return blockState$getProperty(property).map(OptionalInt::of).orElse(OptionalInt.empty());
    }

    public OptionalInt blockState$getIntProperty(Direction direction, Property<Integer> property) {
        return blockState$getIntProperty(property);
    }

    public OptionalDouble getDoubleProperty(Property<Double> property) {
        return blockState$getProperty(property).map(OptionalDouble::of).orElse(OptionalDouble.empty());
    }

    public OptionalDouble getDoubleProperty(Direction direction, Property<Double> property) {
        return getDoubleProperty(property);
    }

    public Map<Property<?>, ?> blockState$getProperties() {
        return getSpongeInternalProperties();
    }

    private ImmutableMap<Property<?>, ?> getSpongeInternalProperties() {
        if (this.dataProperties == null) {
            this.dataProperties = ((IMixinBlock) this.object).getProperties((IBlockState) this);
        }
        return this.dataProperties;
    }

    public List<ImmutableDataManipulator<?, ?>> blockState$getContainers() {
        return this.blockState$getManipulators();
    }

    @SuppressWarnings("unchecked")
    public <E> Optional<E> blockState$get(Key<? extends Value<E>> key) {
        return Optional.ofNullable((E) this.getKeyMap().get(key));
    }

    @SuppressWarnings("unchecked")
    public <E, V extends Value<E>> Optional<V> blockState$getValue(Key<V> key) {
        checkNotNull(key);
        for (Value.Immutable<?> value : this.blockState$getValues()) {
            if (value.getKey().equals(key)) {
                return Optional.of((V) value.asMutable());
            }
        }
        return Optional.empty();
    }

    public boolean blockState$supports(Key<?> key) {
        return this.blockState$getKeys().contains(checkNotNull(key));
    }

    public BlockState blockState$copy() {
        return (BlockState) this;
    }

    public Set<Key<?>> blockState$getKeys() {
        if (this.keys == null) {
            lazyLoadManipulatorsAndKeys();
        }
        return this.keys;
    }

    public Set<Value.Immutable<?>> blockState$getValues() {
        if (this.values == null) {
            lazyLoadManipulatorsAndKeys();
        }
        return this.values;
    }

    public int blockState$getContentVersion() {
        return DataVersions.BlockState.STATE_AS_CATALOG_ID;
    }

    public DataContainer blockState$toContainer() {
        return DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, blockState$getContentVersion())
            .set(DataQueries.BLOCK_STATE, this.blockState$getKey());
    }

    @Override
    public void generateId(Block block) {
        final String nameSpace = ((BlockType) block).getKey().getNamespace();
        StringBuilder builder = new StringBuilder();
        builder.append(((BlockType) block).getKey().getValue());

        final ImmutableMap<IProperty<?>, Comparable<?>> properties = this.getValues();
        if (!properties.isEmpty()) {
            builder.append('[');
            Joiner joiner = Joiner.on(',');
            List<String> propertyValues = new ArrayList<>();
            for (Map.Entry<IProperty<?>, Comparable<?>> entry : properties.entrySet()) {
                propertyValues.add(entry.getKey().getName() + "=" + entry.getValue());
            }
            builder.append(joiner.join(propertyValues));
            builder.append(']');
        }
        this.id =  CatalogKey.of(nameSpace, builder.toString());
    }

    public CatalogKey blockState$getKey() {
        if (this.id == null) {
            generateId((Block) this.object);
        }
        return this.id;
    }

    public String blockState$getName() {
        if (this.id == null) {
            generateId((Block) this.object);
        }
        return this.id.getValue();
    }
}
