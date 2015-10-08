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
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.service.persistence.NbtTranslator;
import org.spongepowered.common.util.VecHelper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public class SpongeBlockSnapshot implements BlockSnapshot {

    private final BlockState blockState;
    private final UUID worldUniqueId;
    private final Vector3i pos;
    private final ImmutableList<ImmutableDataManipulator<?, ?>> extraData;
    private final ImmutableMap<Key<?>, ImmutableValue<?>> keyValueMap;
    private final ImmutableSet<ImmutableValue<?>> valueSet;
    private int updateFlag; // internal use
    @Nullable final NBTTagCompound compound;

    // Internal use for restores
    public SpongeBlockSnapshot(SpongeBlockSnapshotBuilder builder, int flag) {
        this(builder);
        this.updateFlag = flag;
    }

    public SpongeBlockSnapshot(SpongeBlockSnapshotBuilder builder) {
        this.blockState = checkNotNull(builder.blockState, "The block state was null!");
        this.worldUniqueId = checkNotNull(builder.worldUuid);
        this.pos = checkNotNull(builder.coords);
        this.extraData = builder.manipulators == null ? ImmutableList.<ImmutableDataManipulator<?, ?>>of() : ImmutableList.copyOf(builder.manipulators);
        ImmutableMap.Builder<Key<?>, ImmutableValue<?>> mapBuilder = ImmutableMap.builder();
        for (ImmutableValue<?> value : this.blockState.getValues()) {
            mapBuilder.put(value.getKey(), value);
        }
        for (ImmutableDataManipulator<?, ?> manipulator : this.extraData) {
            for (ImmutableValue<?> value : manipulator.getValues()) {
                mapBuilder.put(value.getKey(), value);
            }
        }
        this.keyValueMap = mapBuilder.build();
        this.valueSet = ImmutableSet.copyOf(this.keyValueMap.values());
        this.compound = builder.compound == null ? null : (NBTTagCompound) builder.compound.copy();

    }

    @Override
    public BlockState getState() {
        return this.blockState;
    }

    @Override
    public BlockSnapshot withState(BlockState blockState) {
        return createBuilder().blockState(blockState).build();
    }

    @Override
    public BlockSnapshot withLocation(Location<World> location) {
        return createBuilder()
            .position(location.getBlockPosition())
            .worldId(location.getExtent().getUniqueId())
            .build();
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
        if (!force && (current.getBlock() != replaced.getBlock()
            || current.getBlock().getMetaFromState(current) != replaced.getBlock().getMetaFromState(replaced))) {
            return false;
        }

        world.setBlockState(pos, replaced, notifyNeighbors ? 3 : 2);
        world.markBlockForUpdate(pos);
        if (this.compound != null) {
            final TileEntity te = world.getTileEntity(pos);
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
            return Optional.of(new Location<>(worldOptional.get(), this.getPosition()));
        }
        return Optional.empty();
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
            .set(DataQueries.BLOCK_STATE, this.blockState);
        if (this.compound != null) {
            container.set(DataQueries.UNSAFE_NBT, NbtTranslator.getInstance().translateFrom(this.compound));
        }
        if (!dataList.isEmpty()) {
            container.set(DataQueries.DATA_MANIPULATORS, dataList);
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
        return Optional.empty();
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
        return Optional.empty();
    }

    @Override
    public <E> Optional<BlockSnapshot> with(Key<? extends BaseValue<E>> key, E value) {
        Optional<BlockState> optional = this.blockState.with(key, value);
        if (optional.isPresent()) {
            return Optional.of(withState(optional.get()));
        }
        return Optional.empty();
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
                return Optional.of(createBuilder().blockState(newState).build());
            } else {
                final SpongeBlockSnapshotBuilder builder = createBuilder();
                builder.add(valueContainer);
                return Optional.of(builder.build());
            }
        }
        return Optional.of(createBuilder().add(valueContainer).build());
    }

    @Override
    public Optional<BlockSnapshot> with(Iterable<ImmutableDataManipulator<?, ?>> valueContainers) {
        BlockSnapshot snapshot = this;
        for (ImmutableDataManipulator<?, ?> manipulator : valueContainers) {
            final Optional<BlockSnapshot> optional = snapshot.with(manipulator);
            if (!optional.isPresent()) {
                return Optional.empty();
            }
            snapshot = optional.get();
        }
        return Optional.of(snapshot);
    }

    @Override
    public Optional<BlockSnapshot> without(Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        return Optional.empty();
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
            Optional<BlockSnapshot> optional = merged.with(function.merge(this.get(manipulator.getClass()).orElse(null), manipulator));
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
        return Optional.empty();
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        if (this.keyValueMap.containsKey(key)) {
            return Optional.of((V) this.keyValueMap.get(key).asMutable());
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(Key<?> key) {
        return this.keyValueMap.containsKey(key);
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
        return this.compound == null ? Optional.<NBTTagCompound>empty() : Optional.of((NBTTagCompound) this.compound.copy());
    }


    public SpongeBlockSnapshotBuilder createBuilder() {
        final SpongeBlockSnapshotBuilder builder = new SpongeBlockSnapshotBuilder();
        builder.blockState((BlockState) this.blockState)
            .position(this.pos)
            .worldId(this.worldUniqueId);
        for (ImmutableDataManipulator<?, ?> manipulator : this.extraData) {
            builder.add((ImmutableDataManipulator) manipulator);
        }
        if (this.compound != null) {
            builder.unsafeNbt(this.compound);
        }
        return builder;
    }

    // Used internally for restores
    public int getUpdateFlag() {
        return this.updateFlag;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("worldUniqueId", this.worldUniqueId)
                .add("position", this.pos)
                .add("blockState", this.blockState)
                .toString();
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(Class<T> propertyClass) {
        return this.blockState.getProperty(propertyClass);
    }

    @Override
    public Collection<Property<?, ?>> getApplicableProperties() {
        return this.blockState.getApplicableProperties();
    }
}
