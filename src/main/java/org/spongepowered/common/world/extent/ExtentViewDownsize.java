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
package org.spongepowered.common.world.extent;


import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.ScheduledBlockUpdate;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.common.util.VecHelper;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

@Implements(@Interface(iface = Extent.class, prefix = "extent$"))
public class ExtentViewDownsize {

    private final Extent extent;
    private final Vector3i blockMin;
    private final Vector3i blockMax;
    private final Vector3i blockSize;
    private final Vector2i biomeMin;
    private final Vector2i biomeMax;
    private final Vector2i biomeSize;

    public ExtentViewDownsize(Extent extent, Vector3i blockMin, Vector3i blockMax) {
        this.extent = extent;
        this.blockMin = blockMin;
        this.blockMax = blockMax;
        this.blockSize = this.blockMax.sub(this.blockMin).add(Vector3i.ONE);
        this.biomeMin = blockMin.toVector2(true);
        this.biomeMax = blockMax.toVector2(true);
        this.biomeSize = this.biomeMax.sub(this.biomeMin).add(Vector2i.ONE);
    }

    public UUID extent$getUniqueId() {
        return this.extent.getUniqueId();
    }

    public boolean extent$isLoaded() {
        return this.extent.isLoaded();
    }

    public Vector2i extent$getBiomeMin() {
        return this.biomeMin;
    }

    public Vector2i extent$getBiomeMax() {
        return this.biomeMax;
    }

    public Vector2i extent$getBiomeSize() {
        return this.biomeSize;
    }

    public boolean extent$containsBiome(int x, int z) {
        return VecHelper.inBounds(x, z, this.biomeMin, this.biomeMax);
    }

    private void checkRange(int x, int z) {
        if (!VecHelper.inBounds(x, z, this.biomeMin, this.biomeMax)) {
            throw new PositionOutOfBoundsException(new Vector2i(x, z), this.biomeMin, this.biomeMax);
        }
    }

    public BiomeType extent$getBiome(int x, int z) {
        checkRange(x, z);
        return this.extent.getBiome(x, z);
    }

    public void extent$setBiome(int x, int z, BiomeType biome) {
        checkRange(x, z);
        this.extent.setBiome(x, z, biome);
    }

    public Vector3i extent$getBlockMax() {
        return this.blockMax;
    }

    public Vector3i extent$getBlockMin() {
        return this.blockMin;
    }

    public Vector3i extent$getBlockSize() {
        return this.blockSize;
    }

    public boolean extent$containsBlock(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, this.blockMin, this.blockMax);
    }

    private void checkRange(double x, double y, double z) {
        if (!VecHelper.inBounds(x, y, z, this.blockMin, this.blockMax)) {
            throw new PositionOutOfBoundsException(new Vector3d(x, y, z), this.blockMin.toDouble(), this.blockMax.toDouble());
        }
    }

    private void checkRange(int x, int y, int z) {
        if (!VecHelper.inBounds(x, y, z, this.blockMin, this.blockMax)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), this.blockMin, this.blockMax);
        }
    }

    public BlockType extent$getBlockType(int x, int y, int z) {
        return extent$getBlock(x, y, z).getType();
    }

    public BlockState extent$getBlock(int x, int y, int z) {
        checkRange(x, y, z);
        return this.extent.getBlock(x, y, z);
    }

    public void extent$setBlock(int x, int y, int z, BlockState block) {
        checkRange(x, y, z);
        this.extent.setBlock(x, y, z, block);
    }

    public BlockSnapshot extent$getBlockSnapshot(int x, int y, int z) {
        checkRange(x, y, z);
        return this.extent.getBlockSnapshot(x, y, z);
    }

    public void extent$setBlockSnapshot(int x, int y, int z, BlockSnapshot snapshot) {
        checkRange(x, y, z);
        this.extent.setBlockSnapshot(x, y, z, snapshot);
    }

    public void extent$interactBlock(int x, int y, int z, Direction side) {
        checkRange(x, y, z);
        this.extent.interactBlock(x, y, z, side);
    }

    public void extent$interactBlockWith(int x, int y, int z, ItemStack itemStack, Direction side) {
        checkRange(x, y, z);
        this.extent.interactBlockWith(x, y, z, itemStack, side);
    }

    public boolean extent$digBlock(int x, int y, int z) {
        checkRange(x, y, z);
        return this.extent.digBlock(x, y, z);
    }

    public boolean extent$digBlockWith(int x, int y, int z, ItemStack itemStack) {
        checkRange(x, y, z);
        return this.extent.digBlockWith(x, y, z, itemStack);
    }

    public int extent$getBlockDigTimeWith(int x, int y, int z, ItemStack itemStack) {
        checkRange(x, y, z);
        return this.extent.getBlockDigTimeWith(x, y, z, itemStack);
    }

    public boolean extent$isBlockFacePowered(int x, int y, int z, Direction direction) {
        checkRange(x, y, z);
        return this.extent.isBlockFacePowered(x, y, z, direction);
    }

    public boolean extent$isBlockFaceIndirectlyPowered(int x, int y, int z, Direction direction) {
        checkRange(x, y, z);
        return this.extent.isBlockFaceIndirectlyPowered(x, y, z, direction);
    }

    public Collection<Direction> extent$getPoweredBlockFaces(int x, int y, int z) {
        checkRange(x, y, z);
        return this.extent.getPoweredBlockFaces(x, y, z);
    }

    public Collection<Direction> extent$getIndirectlyPoweredBlockFaces(int x, int y, int z) {
        checkRange(x, y, z);
        return this.extent.getIndirectlyPoweredBlockFaces(x, y, z);
    }

    public boolean extent$isBlockFlammable(int x, int y, int z, Direction faceDirection) {
        checkRange(x, y, z);
        return this.extent.isBlockFlammable(x, y, z, faceDirection);
    }

    public Collection<ScheduledBlockUpdate> extent$getScheduledUpdates(int x, int y, int z) {
        checkRange(x, y, z);
        return this.extent.getScheduledUpdates(x, y, z);
    }

    public ScheduledBlockUpdate extent$addScheduledUpdate(int x, int y, int z, int priority, int ticks) {
        checkRange(x, y, z);
        return this.extent.addScheduledUpdate(x, y, z, priority, ticks);
    }

    public void extent$removeScheduledUpdate(int x, int y, int z, ScheduledBlockUpdate update) {
        checkRange(x, y, z);
        this.extent.removeScheduledUpdate(x, y, z, update);
    }

    public <T extends Property<?, ?>> Optional<T> extent$getProperty(int x, int y, int z, Class<T> propertyClass) {
        checkRange(x, y, z);
        return this.extent.getProperty(x, y, z, propertyClass);
    }

    public Collection<Property<?, ?>> extent$getProperties(int x, int y, int z) {
        checkRange(x, y, z);
        return this.extent.getProperties(x, y, z);
    }

    public <E> Optional<E> extent$get(int x, int y, int z, Key<? extends BaseValue<E>> key) {
        checkRange(x, y, z);
        return this.extent.get(x, y, z, key);
    }

    public <T extends DataManipulator<?, ?>> Optional<T> extent$get(int x, int y, int z, Class<T> manipulatorClass) {
        checkRange(x, y, z);
        return this.extent.get(x, y, z, manipulatorClass);
    }

    public ImmutableSet<ImmutableValue<?>> extent$getValues(int x, int y, int z) {
        checkRange(x, y, z);
        return this.extent.getValues(x, y, z);
    }

    public <T extends DataManipulator<?, ?>> Optional<T> extent$getOrCreate(int x, int y, int z, Class<T> manipulatorClass) {
        checkRange(x, y, z);
        return this.extent.getOrCreate(x, y, z, manipulatorClass);
    }

    public <E> E extent$getOrNull(int x, int y, int z, Key<? extends BaseValue<E>> key) {
        checkRange(x, y, z);
        return this.extent.getOrNull(x, y, z, key);
    }

    public <E> E extent$getOrElse(int x, int y, int z, Key<? extends BaseValue<E>> key, E defaultValue) {
        checkRange(x, y, z);
        return this.extent.getOrElse(x, y, z, key, defaultValue);
    }

    public <E, V extends BaseValue<E>> Optional<V> extent$getValue(int x, int y, int z, Key<V> key) {
        checkRange(x, y, z);
        return this.extent.getValue(x, y, z, key);
    }

    public boolean extent$supports(int x, int y, int z, Key<?> key) {
        checkRange(x, y, z);
        return this.extent.supports(x, y, z, key);
    }

    public boolean extent$supports(int x, int y, int z, BaseValue<?> value) {
        checkRange(x, y, z);
        return this.extent.supports(x, y, z, value);
    }

    public boolean extent$supports(int x, int y, int z, Class<? extends DataManipulator<?, ?>> manipulatorClass) {
        checkRange(x, y, z);
        return this.extent.supports(x, y, z, manipulatorClass);
    }

    public boolean extent$supports(int x, int y, int z, DataManipulator<?, ?> manipulator) {
        checkRange(x, y, z);
        return this.extent.supports(x, y, z, manipulator);
    }

    public ImmutableSet<Key<?>> extent$getKeys(int x, int y, int z) {
        checkRange(x, y, z);
        return this.extent.getKeys(x, y, z);
    }

    public <E> DataTransactionResult extent$transform(int x, int y, int z, Key<? extends BaseValue<E>> key, Function<E, E> function) {
        checkRange(x, y, z);
        return this.extent.transform(x, y, z, key, function);
    }

    public DataTransactionResult extent$offer(int x, int y, int z, BaseValue<?> value) {
        checkRange(x, y, z);
        return this.extent.offer(x, y, z, value);
    }

    public <E> DataTransactionResult extent$offer(int x, int y, int z, Key<? extends BaseValue<E>> key, E value) {
        checkRange(x, y, z);
        return this.extent.offer(x, y, z, key, value);
    }

    public DataTransactionResult extent$offer(int x, int y, int z, DataManipulator<?, ?> manipulator) {
        checkRange(x, y, z);
        return this.extent.offer(x, y, z, manipulator);
    }

    public DataTransactionResult extent$offer(int x, int y, int z, DataManipulator<?, ?> manipulator, MergeFunction function) {
        checkRange(x, y, z);
        return this.extent.offer(x, y, z, manipulator, function);
    }

    public DataTransactionResult extent$offer(int x, int y, int z, Iterable<DataManipulator<?, ?>> manipulators) {
        checkRange(x, y, z);
        return this.extent.offer(x, y, z, manipulators);
    }

    public DataTransactionResult extent$offer(Vector3i blockPosition, Iterable<DataManipulator<?, ?>> values, MergeFunction function) {
        checkRange(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
        return this.extent.offer(blockPosition, values, function);
    }

    public DataTransactionResult extent$remove(int x, int y, int z, Key<?> key) {
        checkRange(x, y, z);
        return this.extent.remove(x, y, z, key);
    }

    public DataTransactionResult extent$remove(int x, int y, int z, Class<? extends DataManipulator<?, ?>> manipulatorClass) {
        checkRange(x, y, z);
        return this.extent.remove(x, y, z, manipulatorClass);
    }

    public DataTransactionResult extent$undo(int x, int y, int z, DataTransactionResult result) {
        checkRange(x, y, z);
        return this.extent.undo(x, y, z, result);
    }

    public Collection<DataManipulator<?, ?>> extent$getManipulators(int x, int y, int z) {
        checkRange(x, y, z);
        return this.extent.getManipulators(x, y, z);
    }

    public boolean extent$validateRawData(int x, int y, int z, DataView container) {
        checkRange(x, y, z);
        return this.extent.validateRawData(x, y, z, container);
    }

    public void extent$setRawData(int x, int y, int z, DataView container) throws InvalidDataException {
        checkRange(x, y, z);
        this.extent.setRawData(x, y, z, container);
    }

    public DataTransactionResult extent$copyFrom(int xTo, int yTo, int zTo, DataHolder from) {
        checkRange(xTo, yTo, zTo);
        return this.extent.copyFrom(xTo, yTo, zTo, from);
    }

    public DataTransactionResult extent$copyFrom(int xTo, int yTo, int zTo, int xFrom, int yFrom, int zFrom) {
        checkRange(xTo, yTo, zTo);
        checkRange(xFrom, yFrom, zFrom);
        return this.extent.copyFrom(xTo, yTo, zTo, xFrom, yFrom, zFrom);
    }

    public DataTransactionResult extent$copyFrom(int xTo, int yTo, int zTo, DataHolder from, MergeFunction function) {
        checkRange(xTo, yTo, zTo);
        return this.extent.copyFrom(xTo, yTo, zTo, from, function);
    }

    public DataTransactionResult extent$copyFrom(int xTo, int yTo, int zTo, int xFrom, int yFrom, int zFrom, MergeFunction function) {
        checkRange(xTo, yTo, zTo);
        checkRange(xFrom, yFrom, zFrom);
        return this.extent.copyFrom(xTo, yTo, zTo, xFrom, yFrom, zFrom, function);
    }

    public Collection<TileEntity> extent$getTileEntities() {
        final Collection<TileEntity> tileEntities = this.extent.getTileEntities();
        for (Iterator<TileEntity> iterator = tileEntities.iterator(); iterator.hasNext(); ) {
            final TileEntity tileEntity = iterator.next();
            final Location block = tileEntity.getLocation();
            if (!VecHelper.inBounds(block.getX(), block.getY(), block.getZ(), this.blockMin, this.blockMax)) {
                iterator.remove();
            }
        }
        return tileEntities;
    }

    public Collection<TileEntity> extent$getTileEntities(Predicate<TileEntity> filter) {
        return this.extent.getTileEntities(Predicates.and(filter, new Predicate<TileEntity>() {

            @Override
            public boolean apply(TileEntity input) {
                final Location block = input.getLocation();
                return VecHelper
                    .inBounds(block.getX(), block.getY(), block.getZ(), ExtentViewDownsize.this.blockMin, ExtentViewDownsize.this.blockMax);
            }

        }));
    }

    public Optional<TileEntity> extent$getTileEntity(int x, int y, int z) {
        checkRange(x, y, z);
        return this.extent.getTileEntity(x, y, z);
    }

    public Optional<Entity> extent$createEntity(EntityType type, Vector3i position) {
        checkRange(position.getX(), position.getY(), position.getZ());
        return this.extent.createEntity(type, position);
    }

    public boolean extent$spawnEntity(Entity entity) {
        final Location location = entity.getLocation();
        checkRange(location.getX(), location.getY(), location.getZ());
        return this.extent.spawnEntity(entity);
    }

    public Collection<Entity> extent$getEntities() {
        final Collection<Entity> entities = this.extent.getEntities();
        for (Iterator<Entity> iterator = entities.iterator(); iterator.hasNext(); ) {
            final Entity tileEntity = iterator.next();
            final Location block = tileEntity.getLocation();
            if (!VecHelper.inBounds(block.getX(), block.getY(), block.getZ(), this.blockMin, this.blockMax)) {
                iterator.remove();
            }
        }
        return entities;
    }

    public Collection<Entity> extent$getEntities(Predicate<Entity> filter) {
        return this.extent.getEntities(Predicates.and(filter, new Predicate<Entity>() {

            @Override
            public boolean apply(Entity input) {
                final Location block = input.getLocation();
                return VecHelper
                    .inBounds(block.getX(), block.getY(), block.getZ(), ExtentViewDownsize.this.blockMin, ExtentViewDownsize.this.blockMax);
            }

        }));
    }

    public Optional<Entity> extent$createEntity(EntityType type, Vector3d position) {
        checkRange(position.getX(), position.getY(), position.getZ());
        return this.extent.createEntity(type, position);
    }

    public Optional<Entity> extent$createEntity(DataContainer entityContainer) {
        // TODO once entity containers are implemented
        //checkRange(position.getX(), position.getY(), position.getZ());
        return Optional.absent();
    }

    public Optional<Entity> extent$createEntity(DataContainer entityContainer, Vector3d position) {
        checkRange(position.getX(), position.getY(), position.getZ());
        return this.extent.createEntity(entityContainer, position);
    }

    public Extent extent$getExtentView(Vector3i newMin, Vector3i newMax) {
        checkRange(newMin.getX(), newMin.getY(), newMin.getZ());
        checkRange(newMax.getX(), newMax.getY(), newMax.getZ());
        return null;
    }

    public Extent extent$getExtentView(DiscreteTransform3 transform) {
        return null;
    }

    public Extent extent$getRelativeExtentView() {
        return extent$getExtentView(DiscreteTransform3.fromTranslation(extent$getBlockMin().negate()));
    }

}
