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
import static com.google.common.base.Preconditions.checkState;
import static org.spongepowered.api.data.DataQuery.of;
import static org.spongepowered.common.data.util.DataUtil.checkDataExists;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockSnapshotBuilder;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.service.persistence.SerializationService;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.service.persistence.NbtTranslator;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

public class SpongeBlockSnapshotBuilder implements BlockSnapshotBuilder {

    private BlockState blockState;
    private UUID worldUuid;
    private Vector3i coords;
    @Nullable private List<ImmutableDataManipulator<?, ?>> manipulators;
    @Nullable private NBTTagCompound compound;


    @Override
    public BlockSnapshotBuilder world(WorldProperties worldProperties) {
        this.worldUuid = checkNotNull(worldProperties).getUniqueId();
        return this;
    }

    @Override
    public BlockSnapshotBuilder blockState(BlockState blockState) {
        this.blockState = checkNotNull(blockState);
        return this;
    }

    @Override
    public BlockSnapshotBuilder position(Vector3i position) {
        this.coords = checkNotNull(position);
        return this;
    }

    @Override
    public BlockSnapshotBuilder from(Location<World> location) {
        this.blockState = location.getBlock();
        this.worldUuid = location.getExtent().getUniqueId();
        this.coords = location.getBlockPosition();
        if (location.hasTileEntity()) {
            this.compound = new NBTTagCompound();
            ((TileEntity) location.getTileEntity().get()).writeToNBT(this.compound);
            final List<ImmutableDataManipulator<?, ?>> list = Lists.newArrayList();
            for (DataManipulator<?, ?> manipulator : location.getContainers()) {
                list.add(manipulator.asImmutable());
            }
            this.manipulators = list;
        }
        return this;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <M extends DataManipulator<M, ?>> BlockSnapshotBuilder add(M manipulator) {
        return add((ImmutableDataManipulator) manipulator.asImmutable());
    }

    @Override
    public <I extends ImmutableDataManipulator<I, ?>> BlockSnapshotBuilder add(I manipulator) {
        return this;
    }

    @Override
    public BlockSnapshotBuilder from(BlockSnapshot holder) {
        this.blockState = holder.getState();
        this.worldUuid = holder.getWorldUniqueId();
        this.coords = holder.getPosition();
        this.manipulators = Lists.newArrayList(holder.getManipulators());
        if (holder instanceof SpongeBlockSnapshot) {
            if (((SpongeBlockSnapshot) holder).compound != null) {
                this.compound = (NBTTagCompound) ((SpongeBlockSnapshot) holder).compound.copy();
            }
        }
        return this;
    }

    @Override
    public BlockSnapshotBuilder reset() {
        this.blockState = BlockTypes.AIR.getDefaultState();
        this.worldUuid = null;
        this.coords = null;
        this.manipulators = null;
        this.compound = null;
        return this;
    }

    @Override
    public BlockSnapshot build() {
        checkState(this.blockState != null);
        if (this.manipulators == null) {
            if (this.compound == null) {
                return new SpongeBlockSnapshot(this.blockState, this.worldUuid, this.coords, ImmutableList.<ImmutableDataManipulator<?, ?>>of());
            } else {
                return new SpongeBlockSnapshot(this.blockState,
                                               this.worldUuid,
                                               this.coords,
                                               ImmutableList.<ImmutableDataManipulator<?, ?>>of(),
                                               this.compound);
            }
        } else {
            return new SpongeBlockSnapshot(this.blockState, this.worldUuid, this.coords, ImmutableList.copyOf(this.manipulators));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<BlockSnapshot> build(DataView container) throws InvalidDataException {
        checkDataExists(container, DataQueries.BLOCK_STATE);
        checkDataExists(container, DataQueries.BLOCK_SNAPSHOT_EXTRA_DATA);
        checkDataExists(container, DataQueries.SNAPSHOT_WORLD_UUID);
        final SerializationService serializationService = Sponge.getGame().getServiceManager().provide(SerializationService.class).get();
        // this is unused for now
        final UUID worldUuid = UUID.fromString(container.getString(DataQueries.SNAPSHOT_WORLD_UUID).get());
        final Vector3i coordinate = DataUtil.getPosition3i(container);
        // We now reconstruct the custom data and all extra data.
        final List<DataView> dataViews = container.getViewList(DataQueries.BLOCK_SNAPSHOT_EXTRA_DATA).get();
        final ImmutableList.Builder<ImmutableDataManipulator<?, ?>> extraDataBuilder = ImmutableList.builder();
        if (!dataViews.isEmpty()) {
            for (DataView dataView : dataViews) {
                try {
                    Class<ImmutableDataManipulator<?, ?>> clazz = (Class<ImmutableDataManipulator<?, ?>>) Class.
                        forName(dataView.getString(DataQueries.DATA_CLASS).get());
                    ImmutableDataManipulator<?, ?> manipulator = dataView
                        .getSerializable(DataQueries.INTERNAL_DATA, clazz, serializationService).get();
                    extraDataBuilder.add(manipulator);
                } catch (Exception e) {
                    throw new InvalidDataException("Could not deserialize a data manipulator!", e);
                }
            }
        }
        // And now to get the block state...
        final BlockState blockState = container.getSerializable(DataQueries.BLOCK_STATE, BlockState.class, serializationService).get();
        Optional<DataView> unsafeCompound = container.getView(DataQueries.BLOCK_SNAPSHOT_UNSAFE_DATA);
        final NBTTagCompound compound = unsafeCompound.isPresent() ? NbtTranslator.getInstance().translateData(unsafeCompound.get()) : null;
        // todo actually use the nbt compound
        return Optional.<BlockSnapshot>of(new SpongeBlockSnapshot(blockState, worldUuid, coordinate, extraDataBuilder.build(), compound));
    }
}
