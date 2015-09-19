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

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.service.persistence.NbtTranslator;
import org.spongepowered.common.util.VecHelper;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public class SpongeBlockSnapshot implements BlockSnapshot {

    private final BlockState blockState;
    private final UUID worldUniqueId;
    private final Vector3i pos;
    private final ImmutableList<ImmutableDataManipulator<?, ?>> extraData;
    private final ImmutableMap<Key<?>, ImmutableValue<?>> keyValueMap;
    private final ImmutableSet<ImmutableValue<?>> valueSet;
    @Nullable final NBTTagCompound compound;

    public SpongeBlockSnapshot(BlockState blockState, World world, Vector3i pos) {
        this(blockState, world.getUniqueId(), pos, ImmutableList.<ImmutableDataManipulator<?, ?>>of());
    }

    public SpongeBlockSnapshot(BlockState blockState, UUID worldUniqueId, Vector3i pos, ImmutableList<ImmutableDataManipulator<?, ?>> list) {
        this(blockState, worldUniqueId, pos, list, null);
    }

    public SpongeBlockSnapshot(BlockState blockState, UUID worldUniqueId, Vector3i pos, ImmutableList<ImmutableDataManipulator<?, ?>> extraData,
                               @Nullable NBTTagCompound compound) {
        this.blockState = checkNotNull(blockState);
        this.worldUniqueId = checkNotNull(worldUniqueId);
        this.pos = checkNotNull(pos);
        this.extraData = checkNotNull(extraData);
        ImmutableMap.Builder<Key<?>, ImmutableValue<?>> builder = ImmutableMap.builder();
        for (ImmutableValue<?> value : this.blockState.getValues()) {
            builder.put(value.getKey(), value);
        }
        for (ImmutableDataManipulator<?, ?> manipulator : this.extraData) {
            for (ImmutableValue<?> value : manipulator.getValues()) {
                builder.put(value.getKey(), value);
            }
        }
        this.keyValueMap = builder.build();
        this.valueSet = ImmutableSet.copyOf(this.keyValueMap.values());
        this.compound = compound == null ? null : (NBTTagCompound) compound.copy();
    }

    public SpongeBlockSnapshot(BlockState blockState, Location<World> location) {
        this(blockState, location.getExtent(), location.getBlockPosition());
    }

    public SpongeBlockSnapshot(BlockState blockState, Location<World> location, NBTTagCompound nbt) {
        this(blockState, location.getExtent().getUniqueId(), location.getBlockPosition(), ImmutableList.<ImmutableDataManipulator<?, ?>>of(), nbt);
    }

    public SpongeBlockSnapshot(BlockState blockState, Location<World> location, ImmutableList<ImmutableDataManipulator<?, ?>> extraData) {
        this(blockState, location.getExtent().getUniqueId(), location.getBlockPosition(), extraData);
    }

    public SpongeBlockSnapshot(BlockState blockState, Location<World> location, TileEntity entity) {
        final ImmutableList.Builder<ImmutableDataManipulator<?, ?>> builder = ImmutableList.builder();
        for (DataManipulator<?, ?> manipulator : entity.getContainers()) {
            builder.add(manipulator.asImmutable());
        }
        this.blockState = checkNotNull(blockState);
        this.pos = location.getBlockPosition();
        this.worldUniqueId = location.getExtent().getUniqueId();
        this.extraData = builder.build();
        ImmutableMap.Builder<Key<?>, ImmutableValue<?>> keyValueBuilder = ImmutableMap.builder();
        for (ImmutableValue<?> value : this.blockState.getValues()) {
            keyValueBuilder.put(value.getKey(), value);
        }
        for (ImmutableDataManipulator<?, ?> manipulator : this.extraData) {
            for (ImmutableValue<?> value : manipulator.getValues()) {
                keyValueBuilder.put(value.getKey(), value);
            }
        }
        this.keyValueMap = keyValueBuilder.build();
        this.valueSet = ImmutableSet.copyOf(this.keyValueMap.values());
        this.compound = new NBTTagCompound();
        ((net.minecraft.tileentity.TileEntity) entity).writeToNBT(this.compound);
    }

    @Override
    public BlockState getState() {
        return this.blockState;
    }

    @Override
    public BlockSnapshot withState(BlockState blockState) {
        return new SpongeBlockSnapshot(checkNotNull(blockState),
                                       this.worldUniqueId,
                                       this.pos,
                                       ImmutableList.<ImmutableDataManipulator<?, ?>>of());
    }

    @Override
    public BlockSnapshot withLocation(Location<World> location) {
        final NBTTagCompound cloned;
        if (this.compound != null) {
            cloned = (NBTTagCompound) this.compound.copy();
            cloned.setInteger(NbtDataUtil.TILE_ENTITY_POSITION_X, location.getBlockPosition().getX());
            cloned.setInteger(NbtDataUtil.TILE_ENTITY_POSITION_Y, location.getBlockPosition().getY());
            cloned.setInteger(NbtDataUtil.TILE_ENTITY_POSITION_Z, location.getBlockPosition().getZ());
        } else {
            cloned = null;
        }
        return new SpongeBlockSnapshot(this.blockState,
                                       location.getExtent().getUniqueId(),
                                       location.getBlockPosition(),
                                       this.extraData,
                                       cloned);
    }

    @Override
    public BlockSnapshot withContainer(DataContainer container) {
        return new SpongeBlockSnapshotBuilder().build(container).get();
    }

    @Override
    public UUID getWorldUniqueId() {
        return this.worldUniqueId;
    }

    @Override
    public Vector3i getPosition() {
        return this.pos;
    }

    @Override
    public boolean restore(boolean force, boolean notifyNeighbors) {
        if (!Sponge.getGame().getServer().getWorld(this.worldUniqueId).isPresent()) {
            return false;
        }

        net.minecraft.world.World world = (net.minecraft.world.World) Sponge.getGame().getServer().getWorld(this.worldUniqueId).get();
        BlockPos pos = VecHelper.toBlockPos(this.pos);
        IBlockState current = world.getBlockState(pos);
        IBlockState replaced = (IBlockState) this.blockState;
        if (current.getBlock() != replaced.getBlock()
            || current.getBlock().getMetaFromState(current) != replaced.getBlock().getMetaFromState(replaced)) {
            if (force) {
                world.setBlockState(pos, replaced, notifyNeighbors ? 3 : 2);
            } else {
                return false;
            }
        }

        world.setBlockState(pos, replaced, notifyNeighbors ? 3 : 2);
        world.markBlockForUpdate(pos);
        if (this.compound != null) {
            final net.minecraft.tileentity.TileEntity te = world.getTileEntity(pos);
            if (te != null) {
                te.readFromNBT(this.compound);
                te.markDirty();
            }
        }

        return true;
    }

    @Override
    public Optional<Location<World>> getLocation() {
        Optional<World> worldOptional = Sponge.getGame().getServer().getWorld(this.worldUniqueId);
        if (worldOptional.isPresent()) {
            return Optional.of(new Location<World>(worldOptional.get(), this.getPosition()));
        }
        return Optional.absent();
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getManipulators() {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>builder().addAll(this.blockState.getManipulators()).addAll(this.extraData).build();
    }

    @Override
    public DataContainer toContainer() {
        final List<DataView> dataList = DataUtil.getSerializedImmutableManipulatorList(this.extraData);
        final DataContainer container = new MemoryDataContainer()
            .set(Location.WORLD_ID, this.worldUniqueId.toString())
            .createView(DataQueries.SNAPSHOT_WORLD_POSITION)
                .set(Location.POSITION_X, this.pos.getX())
                .set(Location.POSITION_Y, this.pos.getY())
                .set(Location.POSITION_Z, this.pos.getZ())
            .getContainer()
            .set(DataQueries.BLOCK_STATE, this.blockState)
            .set(DataQueries.DATA_MANIPULATORS, dataList);
        if (this.compound != null) {
            container.set(DataQueries.UNSAFE_NBT, NbtTranslator.getInstance().translateFrom(this.compound));
        }
        return container;
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
        if (((IMixinBlock) this.blockState.getType()).supports((Class<ImmutableDataManipulator<?, ?>>) valueContainer.getClass())) {
            final BlockState newState;
            boolean changeState = false;
            if (this.blockState.supports((Class<ImmutableDataManipulator<?, ?>>) valueContainer.getClass())) {
                newState = this.blockState.with(valueContainer).get();
                changeState = true;
            } else {
                newState = this.blockState;
            }
            if (changeState) {
                return Optional.<BlockSnapshot>of(new SpongeBlockSnapshot(newState, this.worldUniqueId, this.pos, this.extraData));
            } else {
                final ImmutableList.Builder<ImmutableDataManipulator<?, ?>> builder = ImmutableList.builder();
                for (ImmutableDataManipulator<?, ?> manipulator : this.extraData) {
                    if (manipulator.getClass().isAssignableFrom(valueContainer.getClass())) {
                        builder.add(valueContainer);
                    } else {
                        builder.add(manipulator);
                    }
                }
                return Optional.<BlockSnapshot>of(new SpongeBlockSnapshot(newState, this.worldUniqueId, this.pos, builder.build()));
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<BlockSnapshot> with(Iterable<ImmutableDataManipulator<?, ?>> valueContainers) {
        BlockSnapshot snapshot = this;
        for (ImmutableDataManipulator<?, ?> manipulator : valueContainers) {
            final Optional<BlockSnapshot> optional = snapshot.with(manipulator);
            if (!optional.isPresent()) {
                return Optional.absent();
            }
            snapshot = optional.get();
        }
        return Optional.of(snapshot);
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
    public List<ImmutableDataManipulator<?, ?>> getContainers() {
        return getManipulators();
    }

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        if (this.keyValueMap.containsKey(key)) {
            return Optional.of((E) this.keyValueMap.get(key).get());
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
        if (this.keyValueMap.containsKey(key)) {
            return Optional.of((V) this.keyValueMap.get(key).asMutable());
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(Key<?> key) {
        return this.keyValueMap.containsKey(key);
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
    public Set<Key<?>> getKeys() {
        return this.keyValueMap.keySet();
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        return this.valueSet;
    }

    public Optional<NBTTagCompound> getCompound() {
        return this.compound == null ? Optional.<NBTTagCompound>absent() : Optional.of((NBTTagCompound) this.compound.copy());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("worldUniqueId", this.worldUniqueId)
                .add("position", this.pos)
                .add("blockState", this.blockState)
                .toString();
    }
}
