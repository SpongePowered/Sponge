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
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Optional;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.ScheduledBlockUpdate;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.DiscreteTransform2;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.world.Location;
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
import org.spongepowered.common.data.SpongeBlockProcessor;
import org.spongepowered.common.data.SpongeManipulatorRegistry;
import org.spongepowered.common.util.gen.ByteArrayImmutableBiomeBuffer;
import org.spongepowered.common.util.gen.ByteArrayMutableBiomeBuffer;
import org.spongepowered.common.util.gen.ShortArrayImmutableBlockBuffer;
import org.spongepowered.common.util.gen.ShortArrayMutableBlockBuffer;
import org.spongepowered.common.world.extent.ExtentBufferUtil;
import org.spongepowered.common.world.extent.MutableBiomeViewDownsize;
import org.spongepowered.common.world.extent.MutableBiomeViewTransform;
import org.spongepowered.common.world.extent.MutableBlockViewDownsize;
import org.spongepowered.common.world.extent.MutableBlockViewTransform;
import org.spongepowered.common.world.extent.UnmodifiableBiomeAreaWrapper;
import org.spongepowered.common.world.extent.UnmodifiableBlockVolumeWrapper;

import java.util.Collection;

@Mixin({World.class, Chunk.class})
public abstract class MixinExtent implements Extent {

    @Override
    public <T extends DataManipulator<T>> Optional<T> getData(Vector3i position, Class<T> dataClass) {
        return getData(position.getX(), position.getY(), position.getZ(), dataClass);
    }

    @Override
    public <T extends DataManipulator<T>> Optional<T> getOrCreate(Vector3i position, Class<T> manipulatorClass) {
        return getOrCreate(position.getX(), position.getY(), position.getZ(), manipulatorClass);
    }

    @Override
    public <T extends DataManipulator<T>> boolean remove(Vector3i position, Class<T> manipulatorClass) {
        return remove(position.getX(), position.getY(), position.getZ(), manipulatorClass);
    }

    @Override
    public <T extends DataManipulator<T>> boolean isCompatible(Vector3i position, Class<T> manipulatorClass) {
        return isCompatible(position.getX(), position.getY(), position.getZ(), manipulatorClass);
    }

    @Override
    public <T extends DataManipulator<T>> boolean isCompatible(int x, int y, int z, Class<T> manipulatorClass) {
        Optional<SpongeBlockProcessor<T>> blockUtilOptional = SpongeManipulatorRegistry.getInstance().getBlockUtil(manipulatorClass);
        // TODO for now, this is what we have to deal with...
        return blockUtilOptional.isPresent();
    }

    @Override
    public <T extends DataManipulator<T>> DataTransactionResult offer(Vector3i position, T manipulatorData) {
        return offer(position.getX(), position.getY(), position.getZ(), manipulatorData);
    }

    @Override
    public <T extends DataManipulator<T>> DataTransactionResult offer(int x, int y, int z, T manipulatorData) {
        return offer(x, y, z, manipulatorData, DataPriority.DATA_MANIPULATOR);
    }

    @Override
    public <T extends DataManipulator<T>> DataTransactionResult offer(Vector3i position, T manipulatorData, DataPriority priority) {
        return offer(position.getX(), position.getY(), position.getZ(), manipulatorData, priority);
    }

    @Override
    public Collection<DataManipulator<?>> getManipulators(Vector3i position) {
        return getManipulators(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(Vector3i position, Class<T> propertyClass) {
        return getProperty(position.getX(), position.getY(), position.getZ(), propertyClass);
    }

    @Override
    public Collection<Property<?, ?>> getProperties(Vector3i position) {
        return getProperties(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public boolean validateRawData(Vector3i position, DataContainer container) {
        return validateRawData(position.getX(), position.getY(), position.getZ(), container);
    }

    @Override
    public void setRawData(Vector3i position, DataContainer container) throws InvalidDataException {
        setRawData(position.getX(), position.getY(), position.getZ(), container);
    }

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
    public Location getLocation(int x, int y, int z) {
        return getLocation(new Vector3i(x, y, z));
    }

    @Override
    public Location getLocation(Vector3i position) {
        return new Location(this, position);
    }

    @Override
    public Location getLocation(double x, double y, double z) {
        return getLocation(new Vector3d(x, y, z));
    }

    @Override
    public Location getLocation(Vector3d position) {
        return new Location(this, position);
    }

    @Override
    public int getLuminance(Vector3i position) {
        return getLuminance(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public int getLuminance(int x, int y, int z) {
        return Math.max(getLuminanceFromGround(x, y, z), getLuminanceFromSky(x, y, z));
    }

    @Override
    public int getLuminanceFromSky(Vector3i position) {
        return getLuminanceFromSky(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public int getLuminanceFromGround(Vector3i position) {
        return getLuminanceFromGround(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public boolean isBlockPowered(Vector3i position) {
        return isBlockPowered(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public Optional<TileEntity> getTileEntity(Vector3i position) {
        return getTileEntity(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public Optional<TileEntity> getTileEntity(Location blockLoc) {
        return getTileEntity(blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ());
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
    public boolean digBlock(Vector3i position) {
        return digBlock(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public boolean digBlockWith(Vector3i position, ItemStack itemStack) {
        return digBlockWith(position.getX(), position.getY(), position.getZ(), itemStack);
    }

    @Override
    public int getBlockDigTime(Vector3i position) {
        return getBlockDigTime(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public int getBlockDigTimeWith(Vector3i position, ItemStack itemStack) {
        return getBlockDigTimeWith(position.getX(), position.getY(), position.getZ(), itemStack);
    }

    @Override
    public boolean isBlockFlammable(Vector3i position, Direction faceDirection) {
        return isBlockFlammable(position.getX(), position.getY(), position.getZ(), faceDirection);
    }

    @Override
    public boolean isBlockIndirectlyPowered(Vector3i position) {
        return isBlockIndirectlyPowered(position.getX(), position.getY(), position.getZ());
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
    public boolean isBlockPassable(Vector3i position) {
        return this.isBlockPassable(position.getX(), position.getY(), position.getZ());
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

}
