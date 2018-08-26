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
package org.spongepowered.common.mixin.core.world;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.EnumFacing;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.data.property.store.PropertyStore;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;

@SuppressWarnings({"unchecked", "rawtypes"})
@Mixin(net.minecraft.world.World.class)
public abstract class MixinWorld_Data implements World {

    @Override
    public Map<Property<?>, ?> getProperties(Vector3i coords) {
        return SpongeImpl.getPropertyRegistry().getPropertiesFor(new Location<>(this, coords));
    }

    @Override
    public Map<Property<?>, ?> getProperties(int x, int y, int z) {
        return SpongeImpl.getPropertyRegistry().getPropertiesFor(new Location<>(this, x, y, z));
    }

    @Override
    public <V> Optional<V> getProperty(Vector3i coords, Property<V> property) {
        return SpongeImpl.getPropertyRegistry().getStore(property).getFor(new Location<>(this, coords));
    }

    @Override
    public <V> Optional<V> getProperty(int x, int y, int z, Property<V> property) {
        return SpongeImpl.getPropertyRegistry().getStore(property).getFor(new Location<>(this, x, y, z));
    }

    @Override
    public <V> Optional<V> getProperty(Vector3i coords, Direction direction, Property<V> property) {
        return SpongeImpl.getPropertyRegistry().getStore(property).getFor(new Location<>(this, coords), direction);
    }

    @Override
    public <V> Optional<V> getProperty(int x, int y, int z, Direction direction, Property<V> property) {
        return SpongeImpl.getPropertyRegistry().getStore(property).getFor(new Location<>(this, x, y, z), direction);
    }

    @Override
    public OptionalDouble getDoubleProperty(Vector3i coords, Property<Double> property) {
        return SpongeImpl.getPropertyRegistry().getDoubleStore(property).getDoubleFor(new Location<>(this, coords));
    }

    @Override
    public OptionalDouble getDoubleProperty(int x, int y, int z, Property<Double> property) {
        return SpongeImpl.getPropertyRegistry().getDoubleStore(property).getDoubleFor(new Location<>(this, x, y, z));
    }

    @Override
    public OptionalInt getIntProperty(Vector3i coords, Property<Integer> property) {
        return SpongeImpl.getPropertyRegistry().getIntStore(property).getIntFor(new Location<>(this, coords));
    }

    @Override
    public OptionalInt getIntProperty(int x, int y, int z, Property<Integer> property) {
        return SpongeImpl.getPropertyRegistry().getIntStore(property).getIntFor(new Location<>(this, x, y, z));
    }

    @Override
    public Collection<Direction> getFacesWithProperty(Vector3i coords, Property<?> property) {
        final PropertyStore<?> store = Sponge.getPropertyRegistry().getStore(property);
        final Location<World> loc = new Location<>(this, coords);
        final ImmutableList.Builder<Direction> faces = ImmutableList.builder();
        for (EnumFacing facing : EnumFacing.values()) {
            final Direction direction = DirectionFacingProvider.getInstance().getKey(facing).get();
            if (store.getFor(loc, direction).isPresent()) {
                faces.add(direction);
            }
        }
        return faces.build();
    }

    @Override
    public Collection<Direction> getFacesWithProperty(int x, int y, int z, Property<?> property) {
        return getFacesWithProperty(new Vector3i(x, y, z), property);
    }

    @Override
    public <E> Optional<E> get(int x, int y, int z, Key<? extends BaseValue<E>> key) {
        final Optional<E> optional = getBlock(x, y, z).withExtendedProperties(new Location<>(this, x, y, z)).get(key);
        if (optional.isPresent()) {
            return optional;
        }
        final Optional<TileEntity> tileEntityOptional = getTileEntity(x, y, z);
        return tileEntityOptional.flatMap(tileEntity -> tileEntity.get(key));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> get(int x, int y, int z, Class<T> manipulatorClass) {
        final Collection<DataManipulator<?, ?>> manipulators = getManipulators(x, y, z);
        for (DataManipulator<?, ?> manipulator : manipulators) {
            if (manipulatorClass.isInstance(manipulator)) {
                return Optional.of((T) manipulator);
            }
        }
        return Optional.empty();
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> getOrCreate(int x, int y, int z, Class<T> manipulatorClass) {
        final Optional<T> optional = get(x, y, z, manipulatorClass);
        if (optional.isPresent()) {
            return optional;
        }
        final Optional<TileEntity> tileEntity = getTileEntity(x, y, z);
        return tileEntity.flatMap(tileEntity1 -> tileEntity1.getOrCreate(manipulatorClass));
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(int x, int y, int z, Key<V> key) {
        final BlockState blockState = getBlock(x, y, z).withExtendedProperties(new Location<>(this, x, y, z));
        if (blockState.supports(key)) {
            return blockState.getValue(key);
        }
        final Optional<TileEntity> tileEntity = getTileEntity(x, y, z);
        if (tileEntity.isPresent() && tileEntity.get().supports(key)) {
            return tileEntity.get().getValue(key);
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(int x, int y, int z, Key<?> key) {
        final BlockState blockState = getBlock(x, y, z);
        final boolean blockSupports = blockState.supports(key);
        final Optional<TileEntity> tileEntity = getTileEntity(x, y, z);
        final boolean tileEntitySupports = tileEntity.isPresent() && tileEntity.get().supports(key);
        return blockSupports || tileEntitySupports;
    }

    @Override
    public boolean supports(int x, int y, int z, Class<? extends DataManipulator<?, ?>> manipulatorClass) {
        final BlockState blockState = getBlock(x, y, z);
        final List<ImmutableDataManipulator<?, ?>> immutableDataManipulators = blockState.getManipulators();
        boolean blockSupports = false;
        for (ImmutableDataManipulator<?, ?> manipulator : immutableDataManipulators) {
            if (manipulator.asMutable().getClass().isAssignableFrom(manipulatorClass)) {
                blockSupports = true;
                break;
            }
        }
        if (!blockSupports) {
            final Optional<TileEntity> tileEntity = getTileEntity(x, y, z);
            final boolean tileEntitySupports;
            tileEntitySupports = tileEntity.isPresent() && tileEntity.get().supports(manipulatorClass);
            return tileEntitySupports;
        }
        return true;
    }

    @Override
    public Set<Key<?>> getKeys(int x, int y, int z) {
        final ImmutableSet.Builder<Key<?>> builder = ImmutableSet.builder();
        final BlockState blockState = getBlock(x, y, z).withExtendedProperties(new Location<>(this, x, y, z));
        builder.addAll(blockState.getKeys());
        final Optional<TileEntity> tileEntity = getTileEntity(x, y, z);
        tileEntity.ifPresent(tileEntity1 -> builder.addAll(tileEntity1.getKeys()));
        return builder.build();
    }

    @Override
    public Set<ImmutableValue<?>> getValues(int x, int y, int z) {
        final ImmutableSet.Builder<ImmutableValue<?>> builder = ImmutableSet.builder();
        final BlockState blockState = getBlock(x, y, z).withExtendedProperties(new Location<>(this, x, y, z));
        builder.addAll(blockState.getValues());
        final Optional<TileEntity> tileEntity = getTileEntity(x, y, z);
        tileEntity.ifPresent(tileEntity1 -> builder.addAll(tileEntity1.getValues()));
        return builder.build();
    }

    @Override
    public <E> DataTransactionResult offer(int x, int y, int z, Key<? extends BaseValue<E>> key, E value) {
        final BlockState blockState = getBlock(x, y, z).withExtendedProperties(new Location<>(this, x, y, z));
        if (blockState.supports(key)) {
            ImmutableValue<E> old = ((Value<E>) getValue(x, y, z, (Key) key).get()).asImmutable();
            setBlock(x, y, z, blockState.with(key, value).get());
            ImmutableValue<E> newVal = ((Value<E>) getValue(x, y, z, (Key) key).get()).asImmutable();
            return DataTransactionResult.successReplaceResult(newVal, old);
        }
        return getTileEntity(x, y, z)
                .map(tileEntity ->  tileEntity.offer(key, value))
                .orElseGet(DataTransactionResult::failNoData);
    }

    @Override
    public DataTransactionResult offer(int x, int y, int z, DataManipulator<?, ?> manipulator, MergeFunction function) {
        final BlockState blockState = getBlock(x, y, z).withExtendedProperties(new Location<>(this, x, y, z));
        final ImmutableDataManipulator<?, ?> immutableDataManipulator = manipulator.asImmutable();
        if (blockState.supports((Class) immutableDataManipulator.getClass())) {
            final List<ImmutableValue<?>> old = new ArrayList<>(blockState.getValues());
            final BlockState newState = blockState.with(immutableDataManipulator).get();
            old.removeAll(newState.getValues());
            setBlock(x, y, z, newState);
            return DataTransactionResult.successReplaceResult(old, manipulator.getValues());
        }
        return getTileEntity(x, y, z)
                .map(tileEntity -> tileEntity.offer(manipulator, function))
                .orElseGet(() -> DataTransactionResult.failResult(manipulator.getValues()));
    }

    @Override
    public DataTransactionResult remove(int x, int y, int z, Class<? extends DataManipulator<?, ?>> manipulatorClass) {
        final Optional<TileEntity> tileEntityOptional = getTileEntity(x, y, z);
        return tileEntityOptional
            .map(tileEntity -> tileEntity.remove(manipulatorClass))
            .orElseGet(DataTransactionResult::failNoData);
    }

    @Override
    public DataTransactionResult remove(int x, int y, int z, Key<?> key) {
        final Optional<TileEntity> tileEntityOptional = getTileEntity(x, y, z);
        return tileEntityOptional
            .map(tileEntity -> tileEntity.remove(key))
            .orElseGet(DataTransactionResult::failNoData);
    }

    @Override
    public DataTransactionResult undo(int x, int y, int z, DataTransactionResult result) {
        return DataTransactionResult.failNoData(); // todo
    }

    @Override
    public DataTransactionResult copyFrom(int xTo, int yTo, int zTo, DataHolder from) {
        return copyFrom(xTo, yTo, zTo, from, MergeFunction.IGNORE_ALL);
    }

    @Override
    public DataTransactionResult copyFrom(int xTo, int yTo, int zTo, DataHolder from, MergeFunction function) {
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        final Collection<DataManipulator<?, ?>> manipulators = from.getContainers();
        for (DataManipulator<?, ?> manipulator : manipulators) {
            builder.absorbResult(offer(xTo, yTo, zTo, manipulator, function));
        }
        return builder.build();
    }

    @Override
    public DataTransactionResult copyFrom(int xTo, int yTo, int zTo, int xFrom, int yFrom, int zFrom, MergeFunction function) {
        return copyFrom(xTo, yTo, zTo, new Location<World>(this, xFrom, yFrom, zFrom), function);
    }

    @Override
    public Collection<DataManipulator<?, ?>> getManipulators(int x, int y, int z) {
        final List<DataManipulator<?, ?>> list = new ArrayList<>();
        final Collection<ImmutableDataManipulator<?, ?>> manipulators = this.getBlock(x, y, z)
            .withExtendedProperties(new Location<>(this, x, y, z))
            .getManipulators();
        for (ImmutableDataManipulator<?, ?> immutableDataManipulator : manipulators) {
            list.add(immutableDataManipulator.asMutable());
        }
        final Optional<TileEntity> optional = getTileEntity(x, y, z);
        optional
            .ifPresent(tileEntity -> list.addAll(tileEntity.getContainers()));
        return list;
    }

    @Override
    public boolean validateRawData(int x, int y, int z, DataView container) {
        return false; // todo
    }

    @Override
    public void setRawData(int x, int y, int z, DataView container) throws InvalidDataException {
        // todo
    }


}
