package org.spongepowered.common.world.extent;


import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
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
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;

import java.util.Collection;
import java.util.UUID;

@Implements(@Interface(iface = Extent.class, prefix = "extent$"))
public class ExtentViewDownsize {

    public BlockSnapshot extent$getBlockSnapshot(int x, int y, int z) {
        return null;
    }

    public void extent$setBlockSnapshot(int x, int y, int z, BlockSnapshot snapshot) {

    }

    public void extent$interactBlock(int x, int y, int z, Direction side) {

    }

    public void extent$interactBlockWith(int x, int y, int z, ItemStack itemStack, Direction side) {

    }

    public boolean extent$digBlock(int x, int y, int z) {
        return false;
    }

    public boolean extent$digBlockWith(int x, int y, int z, ItemStack itemStack) {
        return false;
    }

    public int extent$getBlockDigTimeWith(int x, int y, int z, ItemStack itemStack) {
        return 0;
    }

    public boolean extent$isBlockFacePowered(int x, int y, int z, Direction direction) {
        return false;
    }

    public boolean extent$isBlockFaceIndirectlyPowered(int x, int y, int z, Direction direction) {
        return false;
    }

    public Collection<Direction> extent$getPoweredBlockFaces(int x, int y, int z) {
        return null;
    }

    public Collection<Direction> extent$getIndirectlyPoweredBlockFaces(int x, int y, int z) {
        return null;
    }

    public boolean extent$isBlockFlammable(int x, int y, int z, Direction faceDirection) {
        return false;
    }

    public Collection<ScheduledBlockUpdate> extent$getScheduledUpdates(int x, int y, int z) {
        return null;
    }

    public ScheduledBlockUpdate extent$addScheduledUpdate(int x, int y, int z, int priority, int ticks) {
        return null;
    }

    public void extent$removeScheduledUpdate(int x, int y, int z, ScheduledBlockUpdate update) {

    }

    public boolean extent$isLoaded() {
        return false;
    }

    public Vector2i extent$getBiomeMin() {
        return null;
    }

    public Vector2i extent$getBiomeMax() {
        return null;
    }

    public Vector2i extent$getBiomeSize() {
        return null;
    }

    public boolean extent$containsBiome(int x, int z) {
        return false;
    }

    public BiomeType extent$getBiome(int x, int z) {
        return null;
    }

    public void extent$setBiome(int x, int z, BiomeType biome) {

    }

    public Vector3i extent$getBlockMax() {
        return null;
    }

    public Vector3i extent$getBlockMin() {
        return null;
    }

    public Vector3i extent$getBlockSize() {
        return null;
    }

    public boolean extent$containsBlock(int x, int y, int z) {
        return false;
    }

    public BlockType extent$getBlockType(int x, int y, int z) {
        return null;
    }

    public BlockState extent$getBlock(int x, int y, int z) {
        return null;
    }

    public void extent$setBlock(int x, int y, int z, BlockState block) {

    }

    public Collection<TileEntity> extent$getTileEntities() {
        return null;
    }

    public Collection<TileEntity> extent$getTileEntities(Predicate<TileEntity> filter) {
        return null;
    }

    public Optional<TileEntity> extent$getTileEntity(int x, int y, int z) {
        return null;
    }

    public Optional<Entity> extent$createEntity(EntityType type, Vector3i position) {
        return null;
    }

    public boolean extent$spawnEntity(Entity entity) {
        return false;
    }

    public Collection<Entity> extent$getEntities() {
        return null;
    }

    public Collection<Entity> extent$getEntities(Predicate<Entity> filter) {
        return null;
    }

    public Optional<Entity> extent$createEntity(EntityType type, Vector3d position) {
        return null;
    }

    public Optional<Entity> extent$createEntity(DataContainer entityContainer) {
        return null;
    }

    public Optional<Entity> extent$createEntity(DataContainer entityContainer, Vector3d position) {
        return null;
    }

    public UUID extent$getUniqueId() {
        return null;
    }

    public <T extends Property<?, ?>> Optional<T> extent$getProperty(int x, int y, int z, Class<T> propertyClass) {
        return null;
    }

    public <T extends DataManipulator<?, ?>> Optional<T> extent$get(int x, int y, int z, Class<T> manipulatorClass) {
        return null;
    }

    public <T extends DataManipulator<?, ?>> Optional<T> extent$getOrCreate(int x, int y, int z, Class<T> manipulatorClass) {
        return null;
    }

    public <E> E extent$getOrNull(int x, int y, int z, Key<? extends BaseValue<E>> key) {
        return null;
    }

    public <E> E extent$getOrElse(int x, int y, int z, Key<? extends BaseValue<E>> key, E defaultValue) {
        return null;
    }

    public <E, V extends BaseValue<E>> Optional<V> extent$getValue(int x, int y, int z, Key<V> key) {
        return null;
    }

    public boolean extent$supports(int x, int y, int z, BaseValue<?> value) {
        return false;
    }

    public boolean extent$supports(int x, int y, int z, Class<? extends DataManipulator<?, ?>> manipulatorClass) {
        return false;
    }

    public boolean extent$supports(int x, int y, int z, DataManipulator<?, ?> manipulator) {
        return false;
    }

    public ImmutableSet<Key<?>> extent$getKeys(int x, int y, int z) {
        return null;
    }

    public <E> DataTransactionResult extent$transform(int x, int y, int z, Key<? extends BaseValue<E>> key, Function<E, E> function) {
        return null;
    }

    public <E> DataTransactionResult extent$offer(int x, int y, int z, Key<? extends BaseValue<E>> key, E value) {
        return null;
    }

    public DataTransactionResult extent$offer(int x, int y, int z, DataManipulator<?, ?> manipulator) {
        return null;
    }

    public DataTransactionResult extent$offer(int x, int y, int z, DataManipulator<?, ?> manipulator, MergeFunction function) {
        return null;
    }

    public DataTransactionResult extent$offer(int x, int y, int z, Iterable<DataManipulator<?, ?>> manipulators) {
        return null;
    }

    public DataTransactionResult extent$offer(Vector3i blockPosition, Iterable<DataManipulator<?, ?>> values, MergeFunction function) {
        return null;
    }

    public DataTransactionResult extent$remove(int x, int y, int z, Class<? extends DataManipulator<?, ?>> manipulatorClass) {
        return null;
    }

    public DataTransactionResult extent$undo(int x, int y, int z, DataTransactionResult result) {
        return null;
    }

    public Collection<DataManipulator<?, ?>> extent$getManipulators(int x, int y, int z) {
        return null;
    }

    public void extent$setRawData(int x, int y, int z, DataView container) throws InvalidDataException {

    }

    public Collection<Property<?, ?>> extent$getProperties(int x, int y, int z) {
        return null;
    }

    public <E> Optional<E> extent$get(int x, int y, int z, Key<? extends BaseValue<E>> key) {
        return null;
    }

    public boolean extent$supports(int x, int y, int z, Key<?> key) {
        return false;
    }

    public ImmutableSet<ImmutableValue<?>> extent$getValues(int x, int y, int z) {
        return null;
    }

    public DataTransactionResult extent$offer(int x, int y, int z, BaseValue<?> value) {
        return null;
    }

    public DataTransactionResult extent$remove(int x, int y, int z, Key<?> key) {
        return null;
    }

    public DataTransactionResult extent$copyFrom(int xTo, int yTo, int zTo, DataHolder from) {
        return null;
    }

    public DataTransactionResult extent$copyFrom(int xTo, int yTo, int zTo, int xFrom, int yFrom, int zFrom) {
        return null;
    }

    public DataTransactionResult extent$copyFrom(int xTo, int yTo, int zTo, DataHolder from, MergeFunction function) {
        return null;
    }

    public DataTransactionResult extent$copyFrom(int xTo, int yTo, int zTo, int xFrom, int yFrom, int zFrom, MergeFunction function) {
        return null;
    }

    public boolean extent$validateRawData(int x, int y, int z, DataView container) {
        return false;
    }

}
