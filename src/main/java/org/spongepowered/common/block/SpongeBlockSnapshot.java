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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.BlockPhase;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;

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
    private final BlockState extendedState;
    private final UUID worldUniqueId;
    private final Vector3i pos;
    private final ImmutableList<ImmutableDataManipulator<?, ?>> extraData;
    private final ImmutableMap<Key<?>, ImmutableValue<?>> keyValueMap;
    private final ImmutableSet<ImmutableValue<?>> valueSet;
    private final ImmutableList<ImmutableDataManipulator<?, ?>> blockData;
    private final ImmutableMap<Key<?>, ImmutableValue<?>> blockKeyValueMap;
    private final ImmutableSet<ImmutableValue<?>> blockValueSet;
    @Nullable final NBTTagCompound compound;
    @Nullable final UUID creatorUniqueId;
    @Nullable final UUID notifierUniqueId;
    // Internal use only
    private final BlockPos blockPos;
    private int updateFlag;
    public BlockChange blockChange; // used for post event

    // Internal use for restores
    public SpongeBlockSnapshot(SpongeBlockSnapshotBuilder builder, int flag) {
        this(builder);
        this.updateFlag = flag;
    }

    public SpongeBlockSnapshot(SpongeBlockSnapshotBuilder builder) {
        this.blockState = checkNotNull(builder.blockState, "The block state was null!");
        this.extendedState = builder.extendedState;
        this.worldUniqueId = checkNotNull(builder.worldUuid);
        this.creatorUniqueId = builder.creatorUuid;
        this.notifierUniqueId = builder.notifierUuid;
        this.pos = checkNotNull(builder.coords);
        this.blockPos = VecHelper.toBlockPos(this.pos);
        final ImmutableMap.Builder<Key<?>, ImmutableValue<?>> mapBuilder = ImmutableMap.builder();
        for (ImmutableValue<?> value : this.blockState.getValues()) {
            mapBuilder.put(value.getKey(), value);
        }
        this.blockKeyValueMap = mapBuilder.build();
        this.blockValueSet = ImmutableSet.copyOf(this.blockKeyValueMap.values());
        this.blockData = ImmutableList.copyOf(this.blockState.getContainers());

        // This avoids cross contamination of block state based values versus tile entity values.
        final ImmutableMap.Builder<Key<?>, ImmutableValue<?>> tileBuilder = ImmutableMap.builder();
        this.extraData = builder.manipulators == null ? ImmutableList.<ImmutableDataManipulator<?, ?>>of() : ImmutableList.copyOf(builder.manipulators);
        for (ImmutableDataManipulator<?, ?> manipulator : this.extraData) {
            for (ImmutableValue<?> value : manipulator.getValues()) {
                tileBuilder.put(value.getKey(), value);
            }
        }
        this.keyValueMap = tileBuilder.build();
        this.valueSet = ImmutableSet.copyOf(this.keyValueMap.values());
        this.compound = builder.compound == null ? null : (NBTTagCompound) builder.compound.copy();

    }

    @Override
    public BlockState getState() {
        return this.blockState;
    }

    @Override
    public BlockState getExtendedState() {
        return this.extendedState;
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
        if (!SpongeImpl.getGame().getServer().getWorld(this.worldUniqueId).isPresent()) {
            return false;
        }

        net.minecraft.world.World world = (net.minecraft.world.World) SpongeImpl.getGame().getServer().getWorld(this.worldUniqueId).get();
        CauseTracker causeTracker = ((IMixinWorldServer) world).getCauseTracker();
        final IPhaseState currentState = causeTracker.getStack().peekState();
        if (currentState.tracksBlockRestores()) {
            causeTracker.switchToPhase(TrackingPhases.BLOCK, BlockPhase.State.RESTORING_BLOCKS,
                    PhaseContext.start()
                            .add(NamedCause.of(InternalNamedCauses.General.RESTORING_BLOCK, this))
                            .complete());
        }

        BlockPos pos = VecHelper.toBlockPos(this.pos);
        IBlockState current = world.getBlockState(pos);
        IBlockState replaced = (IBlockState) this.blockState;
        if (!force && (current.getBlock() != replaced.getBlock() || current.getBlock().getMetaFromState(current) != replaced.getBlock().getMetaFromState(replaced))) {
            if (currentState.tracksBlockRestores()) {
                causeTracker.completePhase();
            }
            return false;
        }

        world.setBlockState(pos, replaced, notifyNeighbors ? 3 : 2);
        ((WorldServer) world).getPlayerChunkMap().markBlockForUpdate(pos);
        if (this.compound != null) {
            final TileEntity te = world.getTileEntity(pos);
            if (te != null) {
                te.readFromNBT(this.compound);
                te.markDirty();
            }
        }
        if (currentState.tracksBlockRestores()) {
            causeTracker.completePhase();
        }
        return true;
    }

    @Override
    public Optional<UUID> getCreator() {
        return Optional.ofNullable(this.creatorUniqueId);
    }

    @Override
    public Optional<UUID> getNotifier() {
        return Optional.ofNullable(this.notifierUniqueId);
    }

    @Override
    public Optional<Location<World>> getLocation() {
        Optional<World> worldOptional = SpongeImpl.getGame().getServer().getWorld(this.worldUniqueId);
        if (worldOptional.isPresent()) {
            return Optional.of(new Location<>(worldOptional.get(), this.getPosition()));
        }
        return Optional.empty();
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getManipulators() {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>builder().addAll(this.blockData).addAll(this.extraData).build();
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer container = new MemoryDataContainer()
            .set(Queries.CONTENT_VERSION, getContentVersion())
            .set(Queries.WORLD_ID, this.worldUniqueId.toString())
            .createView(DataQueries.SNAPSHOT_WORLD_POSITION)
                .set(Queries.POSITION_X, this.pos.getX())
                .set(Queries.POSITION_Y, this.pos.getY())
                .set(Queries.POSITION_Z, this.pos.getZ())
            .getContainer()
            .set(DataQueries.BLOCK_STATE, this.blockState);

        if (this.blockState != this.extendedState) {
            container.set(DataQueries.BLOCK_EXTENDED_STATE, this.extendedState);
        }
        if (this.compound != null) {
            container.set(DataQueries.UNSAFE_NBT, NbtTranslator.getInstance().translateFrom(this.compound));
        }
        final List<DataView> dataList = DataUtil.getSerializedImmutableManipulatorList(this.extraData);
        if (!dataList.isEmpty()) {
            container.set(DataQueries.SNAPSHOT_TILE_DATA, dataList);
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

    @SuppressWarnings("rawtypes")
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
        } else if (this.blockKeyValueMap.containsKey(key)) {
            return Optional.of((E) this.blockKeyValueMap.get(key).get());
        }
        return Optional.empty();
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        if (this.keyValueMap.containsKey(key)) {
            return Optional.of((V) this.keyValueMap.get(key).asMutable());
        } else if (this.blockKeyValueMap.containsKey(key)) {
            return Optional.of((V) this.blockKeyValueMap.get(key).asMutable());
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(Key<?> key) {
        checkNotNull(key, "Key");
        return this.keyValueMap.containsKey(key) || this.blockKeyValueMap.containsKey(key);
    }

    @Override
    public BlockSnapshot copy() {
        return this;
    }

    @Override
    public Set<Key<?>> getKeys() {
        return ImmutableSet.<Key<?>>builder().addAll(this.keyValueMap.keySet()).addAll(this.blockKeyValueMap.keySet()).build();
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        return ImmutableSet.<ImmutableValue<?>>builder().addAll(this.valueSet).addAll(this.blockValueSet).build();
    }

    public Optional<NBTTagCompound> getCompound() {
        return this.compound == null ? Optional.<NBTTagCompound>empty() : Optional.of((NBTTagCompound) this.compound.copy());
    }

    @SuppressWarnings("rawtypes")
    public SpongeBlockSnapshotBuilder createBuilder() {
        final SpongeBlockSnapshotBuilder builder = new SpongeBlockSnapshotBuilder();
        builder.blockState(this.blockState)
            .extendedState(this.extendedState)
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

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("worldUniqueId", this.worldUniqueId)
                .add("position", this.pos)
                .add("blockState", this.blockState)
                .add("extendedState", this.extendedState)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SpongeBlockSnapshot that = (SpongeBlockSnapshot) o;
        return this.updateFlag == that.updateFlag &&
               Objects.equal(this.extendedState, that.extendedState) &&
               Objects.equal(this.worldUniqueId, that.worldUniqueId) &&
               Objects.equal(this.pos, that.pos) &&
               Objects.equal(this.extraData, that.extraData) &&
               Objects.equal(this.blockData, that.blockData) &&
               Objects.equal(this.compound, that.compound);
    }

    @Override
    public int hashCode() {
        return Objects
            .hashCode(this.extendedState,
                this.worldUniqueId,
                this.pos,
                this.extraData,
                this.blockData,
                this.updateFlag,
                this.compound);
    }
}
