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
package org.spongepowered.common.mixin.core.world.extent;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.ScheduledBlockUpdate;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.DiscreteTransform2;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.ImmutableBlockVolume;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.extent.StorageType;
import org.spongepowered.api.world.extent.UnmodifiableBiomeArea;
import org.spongepowered.api.world.extent.UnmodifiableBlockVolume;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.util.gen.ByteArrayImmutableBiomeBuffer;
import org.spongepowered.common.util.gen.ByteArrayMutableBiomeBuffer;
import org.spongepowered.common.util.gen.ShortArrayImmutableBlockBuffer;
import org.spongepowered.common.util.gen.ShortArrayMutableBlockBuffer;
import org.spongepowered.common.world.extent.ExtentBufferUtil;
import org.spongepowered.common.world.extent.ExtentViewDownsize;
import org.spongepowered.common.world.extent.ExtentViewTransform;
import org.spongepowered.common.world.extent.MutableBiomeViewDownsize;
import org.spongepowered.common.world.extent.MutableBiomeViewTransform;
import org.spongepowered.common.world.extent.MutableBlockViewDownsize;
import org.spongepowered.common.world.extent.MutableBlockViewTransform;
import org.spongepowered.common.world.extent.UnmodifiableBiomeAreaWrapper;
import org.spongepowered.common.world.extent.UnmodifiableBlockVolumeWrapper;

import java.util.Collection;

@Mixin({World.class, Chunk.class, ExtentViewDownsize.class, ExtentViewTransform.class})
public abstract class MixinExtent implements Extent {

    @Override
    public BiomeType getBiome(Vector2i position) {
        return getBiome(position.getX(), position.getY());
    }

    @Override
    public void setBiome(Vector2i position, BiomeType biome) {
        setBiome(position.getX(), position.getY(), biome);
    }

    @Override
    public BlockState getBlock(Vector3i position) {
        return getBlock(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public void setBlock(Vector3i position, BlockState block) {
        setBlock(position.getX(), position.getY(), position.getZ(), block);
    }

    @Override
    public BlockType getBlockType(Vector3i position) {
        return getBlockType(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public void setBlockType(Vector3i position, BlockType type) {
        setBlockType(position.getX(), position.getY(), position.getZ(), type);
    }

    @Override
    public void setBlockType(int x, int y, int z, BlockType type) {
        setBlock(x, y, z, type.getDefaultState());
    }

    @Override
    public BlockSnapshot createSnapshot(Vector3i position) {
        return createSnapshot(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public void restoreSnapshot(Vector3i position, BlockSnapshot snapshot) {
        restoreSnapshot(position.getX(), position.getY(), position.getZ(), snapshot);
    }

    @Override
    public Optional<TileEntity> getTileEntity(Vector3i position) {
        return getTileEntity(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public boolean containsBiome(Vector2i position) {
        return containsBiome(position.getX(), position.getY());
    }

    @Override
    public boolean containsBlock(Vector3i position) {
        return containsBlock(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public void interactBlock(Vector3i position, Direction side) {
        interactBlock(position.getX(), position.getY(), position.getZ(), side);
    }

    @Override
    public void interactBlockWith(Vector3i position, ItemStack itemStack, Direction side) {
        interactBlockWith(position.getX(), position.getY(), position.getZ(), itemStack, side);
    }

    @Override
    public int getBlockDigTimeWith(Vector3i position, ItemStack itemStack) {
        return getBlockDigTimeWith(position.getX(), position.getY(), position.getZ(), itemStack);
    }

    @Override
    public boolean digBlock(Vector3i position) {
        return digBlock(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public boolean digBlockWith(Vector3i position, ItemStack itemStack) {
        return digBlockWith(position.getX(), position.getY(), position.getZ(), itemStack);
    }

    @Override
    public boolean isBlockFlammable(Vector3i position, Direction faceDirection) {
        return isBlockFlammable(position.getX(), position.getY(), position.getZ(), faceDirection);
    }

    @Override
    public boolean isBlockFacePowered(Vector3i position, Direction direction) {
        return isBlockFacePowered(position.getX(), position.getY(), position.getZ(), direction);
    }

    @Override
    public boolean isBlockFaceIndirectlyPowered(Vector3i position, Direction direction) {
        return isBlockFaceIndirectlyPowered(position.getX(), position.getY(), position.getZ(), direction);
    }

    @Override
    public Collection<Direction> getPoweredBlockFaces(Vector3i position) {
        return getPoweredBlockFaces(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public Collection<Direction> getIndirectlyPoweredBlockFaces(Vector3i position) {
        return getIndirectlyPoweredBlockFaces(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public Collection<ScheduledBlockUpdate> getScheduledUpdates(Vector3i position) {
        return getScheduledUpdates(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public ScheduledBlockUpdate addScheduledUpdate(Vector3i position, int priority, int ticks) {
        return addScheduledUpdate(position.getX(), position.getY(), position.getZ(), priority, ticks);
    }

    @Override
    public void removeScheduledUpdate(Vector3i position, ScheduledBlockUpdate update) {
        removeScheduledUpdate(position.getX(), position.getY(), position.getZ(), update);
    }

    @Override
    public MutableBiomeArea getBiomeView(Vector2i newMin, Vector2i newMax) {
        if (!containsBiome(newMin.getX(), newMin.getY())) {
            throw new PositionOutOfBoundsException(newMin, getBiomeMin(), getBiomeMax());
        }
        if (!containsBiome(newMax.getX(), newMax.getY())) {
            throw new PositionOutOfBoundsException(newMax, getBiomeMin(), getBiomeMax());
        }
        return new MutableBiomeViewDownsize(this, newMin, newMax);
    }

    @Override
    public MutableBiomeArea getBiomeView(DiscreteTransform2 transform) {
        return new MutableBiomeViewTransform(this, transform);
    }

    @Override
    public MutableBiomeArea getRelativeBiomeView() {
        return getBiomeView(DiscreteTransform2.fromTranslation(getBiomeMin().negate()));
    }

    @Override
    public UnmodifiableBiomeArea getUnmodifiableBiomeView() {
        return new UnmodifiableBiomeAreaWrapper(this);
    }

    @Override
    public MutableBiomeArea getBiomeCopy() {
        return getBiomeCopy(StorageType.STANDARD);
    }

    @Override
    public MutableBiomeArea getBiomeCopy(StorageType type) {
        switch (type) {
            case STANDARD:
                return new ByteArrayMutableBiomeBuffer(ExtentBufferUtil.copyToArray(this, getBiomeMin(), getBiomeMax(), getBiomeSize()),
                    getBiomeMin(), getBiomeSize());
            case THREAD_SAFE:
            default:
                throw new UnsupportedOperationException(type.name());
        }
    }

    @Override
    public ImmutableBiomeArea getImmutableBiomeCopy() {
        return ByteArrayImmutableBiomeBuffer.newWithoutArrayClone(ExtentBufferUtil.copyToArray(this, getBiomeMin(), getBiomeMax(), getBiomeSize()),
            getBiomeMin(), getBiomeSize());
    }

    @Override
    public MutableBlockVolume getBlockView(Vector3i newMin, Vector3i newMax) {
        if (!containsBlock(newMin.getX(), newMin.getY(), newMin.getZ())) {
            throw new PositionOutOfBoundsException(newMin, getBlockMin(), getBlockMax());
        }
        if (!containsBlock(newMax.getX(), newMax.getY(), newMax.getZ())) {
            throw new PositionOutOfBoundsException(newMax, getBlockMin(), getBlockMax());
        }
        return new MutableBlockViewDownsize(this, newMin, newMax);
    }

    @Override
    public MutableBlockVolume getBlockView(DiscreteTransform3 transform) {
        return new MutableBlockViewTransform(this, transform);
    }

    @Override
    public MutableBlockVolume getRelativeBlockView() {
        return getBlockView(DiscreteTransform3.fromTranslation(getBlockMin().negate()));
    }

    @Override
    public UnmodifiableBlockVolume getUnmodifiableBlockView() {
        return new UnmodifiableBlockVolumeWrapper(this);
    }

    @Override
    public MutableBlockVolume getBlockCopy() {
        return getBlockCopy(StorageType.STANDARD);
    }

    @Override
    public MutableBlockVolume getBlockCopy(StorageType type) {
        switch (type) {
            case STANDARD:
                return new ShortArrayMutableBlockBuffer(ExtentBufferUtil.copyToArray(this, getBlockMin(), getBlockMax(), getBlockSize()),
                    getBlockMin(), getBlockSize());
            case THREAD_SAFE:
            default:
                throw new UnsupportedOperationException(type.name());
        }
    }

    @Override
    public ImmutableBlockVolume getImmutableBlockCopy() {
        return ShortArrayImmutableBlockBuffer.newWithoutArrayClone(ExtentBufferUtil.copyToArray(this, getBlockMin(), getBlockMax(), getBlockSize()),
            getBlockMin(), getBlockSize());
    }

    @Override
    public void setRawData(Vector3i position, DataView container) throws InvalidDataException {
        setRawData(position.getX(), position.getY(), position.getZ(), container);
    }

    @Override
    public boolean validateRawData(Vector3i position, DataView container) {
        return validateRawData(position.getX(), position.getY(), position.getZ(), container);
    }

    @Override
    public Collection<DataManipulator<?, ?>> getManipulators(Vector3i coordinates) {
        return getManipulators(coordinates.getX(), coordinates.getY(), coordinates.getZ());
    }

    @Override
    public <E> Optional<E> get(Vector3i coordinates, Key<? extends BaseValue<E>> key) {
        return get(coordinates.getX(), coordinates.getY(), coordinates.getZ(), key);
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> get(Vector3i coordinates, Class<T> manipulatorClass) {
        return get(coordinates.getX(), coordinates.getY(), coordinates.getZ(), manipulatorClass);
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> getOrCreate(Vector3i coordinates, Class<T> manipulatorClass) {
        return getOrCreate(coordinates.getX(), coordinates.getY(), coordinates.getZ(), manipulatorClass);
    }

    @Override
    public <E> E getOrNull(Vector3i coordinates, Key<? extends BaseValue<E>> key) {
        return getOrNull(coordinates.getX(), coordinates.getY(), coordinates.getZ(), key);
    }

    @Override
    public <E> E getOrElse(Vector3i coordinates, Key<? extends BaseValue<E>> key, E defaultValue) {
        return getOrElse(coordinates.getX(), coordinates.getY(), coordinates.getZ(), key, defaultValue);
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Vector3i coordinates, Key<V> key) {
        return getValue(coordinates.getX(), coordinates.getY(), coordinates.getZ(), key);
    }

    @Override
    public boolean supports(Vector3i coordinates, Key<?> key) {
        return supports(coordinates.getX(), coordinates.getY(), coordinates.getZ(), key);
    }

    @Override
    public boolean supports(Vector3i coordinates, Class<? extends DataManipulator<?, ?>> manipulatorClass) {
        return supports(coordinates.getX(), coordinates.getY(), coordinates.getZ(), manipulatorClass);
    }

    @Override
    public boolean supports(Vector3i coordinates, DataManipulator<?, ?> manipulator) {
        return supports(coordinates.getX(), coordinates.getY(), coordinates.getZ(), manipulator);
    }

    @Override
    public ImmutableSet<Key<?>> getKeys(Vector3i coordinates) {
        return getKeys(coordinates.getX(), coordinates.getY(), coordinates.getZ());
    }

    @Override
    public ImmutableSet<ImmutableValue<?>> getValues(Vector3i coordinates) {
        return getValues(coordinates.getX(), coordinates.getY(), coordinates.getZ());
    }

    @Override
    public <E> DataTransactionResult offer(Vector3i coordinates, BaseValue<E> value) {
        return offer(coordinates.getX(), coordinates.getY(), coordinates.getZ(), value);
    }

    @Override
    public DataTransactionResult offer(Vector3i coordinates, DataManipulator<?, ?> manipulator) {
        return offer(coordinates.getX(), coordinates.getY(), coordinates.getZ(), manipulator);
    }

    @Override
    public DataTransactionResult offer(Vector3i coordinates, Iterable<DataManipulator<?, ?>> manipulators) {
        return offer(coordinates.getX(), coordinates.getY(), coordinates.getZ(), manipulators);
    }

    @Override
    public DataTransactionResult remove(Vector3i coordinates, Class<? extends DataManipulator<?, ?>> manipulatorClass) {
        return remove(coordinates.getX(), coordinates.getY(), coordinates.getZ(), manipulatorClass);
    }

    @Override
    public DataTransactionResult remove(Vector3i coordinates, Key<?> key) {
        return remove(coordinates.getX(), coordinates.getY(), coordinates.getZ(), key);
    }

    @Override
    public DataTransactionResult undo(Vector3i coordinates, DataTransactionResult result) {
        return undo(coordinates.getX(), coordinates.getY(), coordinates.getZ(), result);
    }

    @Override
    public DataTransactionResult copyFrom(Vector3i coordinatesTo, Vector3i coordinatesFrom) {
        return copyFrom(coordinatesTo.getX(), coordinatesTo.getY(), coordinatesTo.getZ(), coordinatesFrom.getX(), coordinatesFrom.getY(),
            coordinatesFrom.getZ());
    }

    @Override
    public DataTransactionResult copyFrom(Vector3i coordinatesTo, Vector3i coordinatesFrom, MergeFunction function) {
        return copyFrom(coordinatesTo.getX(), coordinatesTo.getY(), coordinatesTo.getZ(), coordinatesFrom.getX(), coordinatesFrom.getY(),
            coordinatesFrom.getZ(), function);
    }

    @Override
    public DataTransactionResult copyFrom(Vector3i to, DataHolder from, MergeFunction function) {
        return copyFrom(to.getX(), to.getY(), to.getZ(), from, function);
    }

    @Override
    public DataTransactionResult copyFrom(Vector3i to, DataHolder from) {
        return copyFrom(to.getX(), to.getY(), to.getZ(), from);
    }

    @Override
    public DataTransactionResult offer(Vector3i coordinates, DataManipulator<?, ?> manipulator, MergeFunction function) {
        return offer(coordinates.getX(), coordinates.getY(), coordinates.getZ(), manipulator, function);
    }

    @Override
    public <E> DataTransactionResult transform(Vector3i coordinates, Key<? extends BaseValue<E>> key, Function<E, E> function) {
        return transform(coordinates.getX(), coordinates.getY(), coordinates.getZ(), key, function);
    }

    @Override
    public <E> DataTransactionResult offer(Vector3i coordinates, Key<? extends BaseValue<E>> key, E value) {
        return offer(coordinates.getX(), coordinates.getY(), coordinates.getZ(), key, value);
    }

    @Override
    public boolean supports(Vector3i coordinates, BaseValue<?> value) {
        return supports(coordinates.getX(), coordinates.getY(), coordinates.getZ(), value);
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(Vector3i coordinates, Class<T> propertyClass) {
        return getProperty(coordinates.getX(), coordinates.getY(), coordinates.getZ(), propertyClass);
    }

    @Override
    public Collection<Property<?, ?>> getProperties(Vector3i coordinates) {
        return getProperties(coordinates.getX(), coordinates.getY(), coordinates.getZ());
    }

}
