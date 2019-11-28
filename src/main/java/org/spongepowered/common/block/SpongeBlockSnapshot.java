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
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.mixin.core.world.WorldAccessor;
import org.spongepowered.common.registry.type.block.TileEntityTypeRegistryModule;
import org.spongepowered.common.registry.type.world.BlockChangeFlagRegistryModule;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.lang.ref.WeakReference;
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
    private ImmutableMap<Key<?>, ImmutableValue<?>> keyValueMap;
    private ImmutableSet<ImmutableValue<?>> valueSet;
    private ImmutableList<ImmutableDataManipulator<?, ?>> blockData;
    private ImmutableMap<Key<?>, ImmutableValue<?>> blockKeyValueMap;
    private ImmutableSet<ImmutableValue<?>> blockValueSet;
    @Nullable final CompoundNBT compound;
    @Nullable final UUID creatorUniqueId;
    @Nullable final UUID notifierUniqueId;
    // Internal use only
    private final BlockPos blockPos;
    private SpongeBlockChangeFlag changeFlag;
    @Nullable private WeakReference<ServerWorld> world;
    public BlockChange blockChange; // used for post event

    SpongeBlockSnapshot(final SpongeBlockSnapshotBuilder builder) {
        this.blockState = checkNotNull(builder.blockState, "The block state was null!");
        this.extendedState = builder.extendedState;
        this.worldUniqueId = checkNotNull(builder.worldUuid, "The world UUID was null");
        this.creatorUniqueId = builder.creatorUuid;
        this.notifierUniqueId = builder.notifierUuid;
        this.pos = checkNotNull(builder.coords);
        this.blockPos = VecHelper.toBlockPos(this.pos);

        // This avoids cross contamination of block state based values versus tile entity values.
        // TODO - delegate this to NbtProcessors when schematics are merged.
        final ImmutableMap.Builder<Key<?>, ImmutableValue<?>> tileBuilder = ImmutableMap.builder();
        this.extraData = builder.manipulators == null ? ImmutableList.<ImmutableDataManipulator<?, ?>>of() : ImmutableList.copyOf(builder.manipulators);
        for (final ImmutableDataManipulator<?, ?> manipulator : this.extraData) {
            for (final ImmutableValue<?> value : manipulator.getValues()) {
                tileBuilder.put(value.getKey(), value);
            }
        }
        this.keyValueMap = tileBuilder.build();
        this.valueSet = this.keyValueMap.isEmpty() ? ImmutableSet.of() : ImmutableSet.copyOf(this.keyValueMap.values());
        this.compound = builder.compound;
        this.changeFlag = builder.flag;
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
    public BlockSnapshot withState(final BlockState blockState) {
        return createBuilder().blockState(blockState).build();
    }

    @Override
    public BlockSnapshot withLocation(final Location<World> location) {
        return createBuilder()
            .position(location.getBlockPosition())
            .worldId(location.getExtent().getUniqueId())
            .build();
    }

    @Override
    public BlockSnapshot withContainer(final DataContainer container) {
        return SpongeBlockSnapshotBuilder.pooled().build(container).get();
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
    public boolean restore(final boolean force, final BlockChangeFlag flag) {
        final Optional<World> optionalWorld = SpongeImpl.getGame().getServer().getWorld(this.worldUniqueId);
        if (!optionalWorld.isPresent()) {
            return false;
        }

        final ServerWorld world = (ServerWorld) optionalWorld.get();
        final WorldServerBridge mixinWorldServer = (WorldServerBridge) world;
        // We need to deterministically define the context as nullable if we don't need to enter.
        // this way we guarantee an exit.
        try (final PhaseContext<?> context = BlockPhase.State.RESTORING_BLOCKS.createPhaseContext()) {
            context.buildAndSwitch();
            final BlockPos pos = VecHelper.toBlockPos(this.pos);
            if (!((WorldAccessor) world).accessor$isValid(pos)) { // Invalid position. Inline this check
                return false;
            }
            final net.minecraft.block.BlockState current = world.func_180495_p(pos);
            final net.minecraft.block.BlockState replaced = (net.minecraft.block.BlockState) this.blockState;
            if (!force && (current.func_177230_c() != replaced.func_177230_c() || current != replaced)) {
                return false;
            }

            // Prevent Shulker Boxes from dropping when restoring BlockSnapshot
//            if (current.getBlock().getClass() == BlockShulkerBox.class) {
//                world.bridge$removeTileEntity(pos);
//            }
            world.func_175713_t(pos);
            PhaseTracker.getInstance().setBlockState(mixinWorldServer, pos, replaced, BlockChangeFlagRegistryModule.andNotifyClients(flag));
            if (this.compound != null) {
                TileEntity te = world.func_175625_s(pos);
                if (te != null) {
                    te.func_145839_a(this.compound);
                }
                if (te == null) {
                    // Because, some mods will "unintentionally" only obey some of the rules but not all.
                    // In cases like this, we need to directly just say "fuck it" and deserialize from the compound directly.
                    try {
                        te = TileEntity.func_190200_a(world, this.compound);
                        if (te != null) {
                            world.func_175726_f(pos).func_150813_a(te);
                        }
                    } catch (Exception e) {
                        // Seriously? The mod should be broken then.
                        final PrettyPrinter printer = new PrettyPrinter(60).add("Unable to restore").centre().hr()
                            .add("A mod is not correctly deserializing a TileEntity that is being restored. ")
                            .addWrapped(60, "Note that this is not the fault of Sponge. Sponge is understanding that "
                                            + "a block is supposed to have a TileEntity, but the mod is breaking the contract"
                                            + "on how to re-create the tile entity. Please open an issue with the offending mod.")
                            .add("Here's the provided compound:");
                        printer.add();
                        try {
                            printer.addWrapped(80, "%s : %s", "This compound", this.compound);
                        } catch (Throwable error) {
                            printer.addWrapped(80, "Unable to get the string of this compound. Printing out some of the entries to better assist");

                        }
                        printer.add()
                            .add("Desired World: " + this.worldUniqueId)
                            .add("Position: " + this.pos)
                            .add("Desired BlockState: " + this.blockState);
                        printer.add();
                        printer.log(SpongeImpl.getLogger(), Level.ERROR);
                        return true; // I mean, I guess. the block was set up, but not the tile entity.
                    }

                }

                if (te != null) {
                    te.func_70296_d();
                }

            }
            // Finally, mark the location as being updated.
            world.func_184164_w().func_180244_a(pos);
            return true;
        }
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
        final Optional<World> worldOptional = SpongeImpl.getGame().getServer().getWorld(this.worldUniqueId);
        if (worldOptional.isPresent()) {
            return Optional.of(new Location<>(worldOptional.get(), this.getPosition()));
        }
        return Optional.empty();
    }

    public Optional<ServerWorld> getWorldServer() {
        if (this.world == null) {
            this.world = new WeakReference<>((ServerWorld) SpongeImpl.getGame().getServer().getWorld(this.worldUniqueId).orElseThrow(() -> new IllegalStateException("WorldServer not found for UUID: " + this.worldUniqueId)));
        }
        return Optional.ofNullable(this.world.get());
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getManipulators() {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>builder().addAll(this.getBlockManipulators()).addAll(this.extraData).build();
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer container = DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, getContentVersion())
            .set(Queries.WORLD_ID, this.worldUniqueId.toString())
            .createView(Constants.Sponge.SNAPSHOT_WORLD_POSITION)
                .set(Queries.POSITION_X, this.pos.getX())
                .set(Queries.POSITION_Y, this.pos.getY())
                .set(Queries.POSITION_Z, this.pos.getZ())
            .getContainer()
            .set(Constants.Block.BLOCK_STATE, this.blockState);

        if (this.blockState != this.extendedState) {
            container.set(Constants.Block.BLOCK_EXTENDED_STATE, this.extendedState);
        }
        if (this.compound != null) {
            container.set(Constants.Sponge.UNSAFE_NBT, NbtTranslator.getInstance().translateFrom(this.compound));
        }
        final List<DataView> dataList = DataUtil.getSerializedImmutableManipulatorList(this.extraData);
        if (!dataList.isEmpty()) {
            container.set(Constants.Sponge.SNAPSHOT_TILE_DATA, dataList);
        }
        return container;
    }

    @Override
    public <T extends ImmutableDataManipulator<?, ?>> Optional<T> get(final Class<T> containerClass) {
        final Optional<T> optional = this.blockState.get(containerClass);
        if (optional.isPresent()) {
            return optional;
        }
        for (final ImmutableDataManipulator<?, ?> dataManipulator : this.extraData) {
            if (containerClass.isInstance(dataManipulator)) {
                return Optional.of(((T) dataManipulator));
            }
        }
        return Optional.empty();
    }

    @Override
    public <T extends ImmutableDataManipulator<?, ?>> Optional<T> getOrCreate(final Class<T> containerClass) {
        return get(containerClass);
    }

    @Override
    public boolean supports(final Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        return this.blockState.supports(containerClass);
    }

    @Override
    public <E> Optional<BlockSnapshot> transform(final Key<? extends BaseValue<E>> key, final Function<E, E> function) {
        return Optional.empty();
    }

    @Override
    public <E> Optional<BlockSnapshot> with(final Key<? extends BaseValue<E>> key, final E value) {
        final Optional<BlockState> optional = this.blockState.with(key, value);
        if (optional.isPresent()) {
            return Optional.of(withState(optional.get()));
        }
        return Optional.empty();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Optional<BlockSnapshot> with(final BaseValue<?> value) {
        return with((Key) value.getKey(), value.get());
    }

    @Override
    public Optional<BlockSnapshot> with(final ImmutableDataManipulator<?, ?> valueContainer) {
        if (((BlockBridge) this.blockState.getType()).bridge$supports((Class<ImmutableDataManipulator<?, ?>>) valueContainer.getClass())) {
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
            }
            final SpongeBlockSnapshotBuilder builder = createBuilder();
            builder.add(valueContainer);
            return Optional.of(builder.build());
        }
        return Optional.of(createBuilder().add(valueContainer).build());
    }

    @Override
    public Optional<BlockSnapshot> with(final Iterable<ImmutableDataManipulator<?, ?>> valueContainers) {
        BlockSnapshot snapshot = this;
        for (final ImmutableDataManipulator<?, ?> manipulator : valueContainers) {
            final Optional<BlockSnapshot> optional = snapshot.with(manipulator);
            if (!optional.isPresent()) {
                return Optional.empty();
            }
            snapshot = optional.get();
        }
        return Optional.of(snapshot);
    }

    @Override
    public Optional<BlockSnapshot> without(final Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        return Optional.empty();
    }

    @Override
    public BlockSnapshot merge(final BlockSnapshot that) {
        return merge(that, MergeFunction.FORCE_NOTHING);
    }

    @Override
    public BlockSnapshot merge(final BlockSnapshot that, final MergeFunction function) {
        BlockSnapshot merged = this;
        merged = merged.withState(function.merge(this.blockState, that.getState()));
        for (final ImmutableDataManipulator<?, ?> manipulator : that.getContainers()) {
            final Optional<BlockSnapshot> optional = merged.with(function.merge(this.get(manipulator.getClass()).orElse(null), manipulator));
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
    public <E> Optional<E> get(final Key<? extends BaseValue<E>> key) {
        if (this.keyValueMap.containsKey(key)) {
            return Optional.of((E) this.keyValueMap.get(key).get());
        } else if (getKeyValueMap().containsKey(key)) {
            return Optional.of((E) this.blockKeyValueMap.get(key).get());
        }
        return Optional.empty();
    }

    private ImmutableMap<Key<?>, ImmutableValue<?>> getKeyValueMap() {
        if (this.blockKeyValueMap == null) {
            final ImmutableMap.Builder<Key<?>, ImmutableValue<?>> mapBuilder = ImmutableMap.builder();
            for (final ImmutableValue<?> value : this.blockState.getValues()) {
                mapBuilder.put(value.getKey(), value);
            }
            this.blockKeyValueMap = mapBuilder.build();
        }
        return this.blockKeyValueMap;
    }

    private ImmutableSet<ImmutableValue<?>> getValueSet() {
        if (this.blockValueSet == null) {
            this.blockValueSet = ImmutableSet.copyOf(getKeyValueMap().values());
        }
        return this.blockValueSet;
    }

    private ImmutableSet<ImmutableValue<?>> getTileValueSet() {
        if (this.valueSet == null) {
            this.valueSet = ImmutableSet.copyOf(this.getTileMap().values());
        }
        return this.valueSet;
    }

    private ImmutableMap<Key<?>, ImmutableValue<?>> getTileMap() {
        if (this.keyValueMap == null) {
            final ImmutableMap.Builder<Key<?>, ImmutableValue<?>> tileBuilder = ImmutableMap.builder();
            for (final ImmutableDataManipulator<?, ?> manipulator : this.extraData) {
                for (final ImmutableValue<?> value : manipulator.getValues()) {
                    tileBuilder.put(value.getKey(), value);
                }
            }
            this.keyValueMap = tileBuilder.build();
        }
        return this.keyValueMap;
    }


    private ImmutableList<ImmutableDataManipulator<?, ?>> getBlockManipulators() {
        if (this.blockData == null) {
            this.blockData = ImmutableList.copyOf(this.blockState.getContainers());
        }
        return this.blockData;
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(final Key<V> key) {
        if (this.keyValueMap.containsKey(key)) {
            return Optional.of((V) this.keyValueMap.get(key).asMutable());
        } else if (getKeyValueMap().containsKey(key)) {
            return Optional.of((V) this.blockKeyValueMap.get(key).asMutable());
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(final Key<?> key) {
        checkNotNull(key, "Key");
        return this.keyValueMap.containsKey(key) || getKeyValueMap().containsKey(key);
    }

    @Override
    public BlockSnapshot copy() {
        return this;
    }

    private Set<Key<?>> keys;
    @Override
    public Set<Key<?>> getKeys() {
        if (this.keys == null) {
            this.keys = ImmutableSet.<Key<?>>builder().addAll(getKeyValueMap().keySet()).addAll(getKeyValueMap().keySet()).build();
        }
        return this.keys;
    }
    private ImmutableSet<ImmutableValue<?>> values;

    @Override
    public Set<ImmutableValue<?>> getValues() {
        if (this.values == null) {
            this.values = ImmutableSet.<ImmutableValue<?>>builder().addAll(getTileValueSet()).addAll(getValueSet()).build();
        }
        return this.values;
    }

    public Optional<CompoundNBT> getCompound() {
        return this.compound == null ? Optional.<CompoundNBT>empty() : Optional.of(this.compound.func_74737_b());
    }

    public SpongeBlockSnapshotBuilder createBuilder() {
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        builder.blockState(this.blockState)
            .extendedState(this.extendedState)
            .position(this.pos)
            .worldId(this.worldUniqueId);
        for (final ImmutableDataManipulator<?, ?> manipulator : this.extraData) {
            builder.add(manipulator);
        }
        if (this.compound != null) {
            builder.unsafeNbt(this.compound);
        }
        return builder;
    }

    // Used internally for restores

    public SpongeBlockChangeFlag getChangeFlag() {
        return this.changeFlag;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("worldUniqueId", this.worldUniqueId)
                .add("position", this.pos)
                .add("blockState", this.blockState)
                .add("extendedState", this.extendedState)
                .toString();
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(final Class<T> propertyClass) {
        return this.blockState.getProperty(propertyClass);
    }

    @Override
    public Collection<Property<?, ?>> getApplicableProperties() {
        return this.blockState.getApplicableProperties();
    }

    @Override
    public Optional<TileEntityArchetype> createArchetype() {
        final BlockType type = this.blockState.getType();
        if (!(type instanceof ITileEntityProvider)) {
            return Optional.empty();
        }
        if (this.compound == null) { // We can't retrieve the TileEntityType
            return Optional.empty();
        }
        final String tileId = this.compound.func_74779_i(Constants.Item.BLOCK_ENTITY_ID);
        final Class<? extends TileEntity> tileClass = (Class<? extends TileEntity>) TileEntityTypeRegistryModule.getInstance().getById(tileId)
            .map(TileEntityType::getTileEntityType)
            .orElse(null);
        if (tileClass == null) {
            return Optional.empty();
        }
        final TileEntityType tileType = TileEntityTypeRegistryModule.getInstance().getForClass(tileClass);

        final TileEntityArchetype archetype = TileEntityArchetype.builder()
                .tile(tileType)
                .state(this.blockState)
                .tileData(NbtTranslator.getInstance().translate(this.compound))
                .build();
        return Optional.of(archetype);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SpongeBlockSnapshot that = (SpongeBlockSnapshot) o;
        return this.changeFlag == that.changeFlag &&
               Objects.equal(this.extendedState, that.extendedState) &&
               Objects.equal(this.worldUniqueId, that.worldUniqueId) &&
               Objects.equal(this.pos, that.pos) &&
               Objects.equal(this.extraData, that.extraData) &&
               Objects.equal(this.compound, that.compound);
    }

    @Override
    public int hashCode() {
        return Objects
            .hashCode(this.extendedState,
                this.worldUniqueId,
                this.pos,
                this.extraData,
                this.changeFlag,
                this.compound);
    }
}
