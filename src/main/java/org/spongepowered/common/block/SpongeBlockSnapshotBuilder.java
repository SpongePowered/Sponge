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
import static org.spongepowered.common.data.util.DataUtil.checkDataExists;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class SpongeBlockSnapshotBuilder extends AbstractDataBuilder<BlockSnapshot> implements BlockSnapshot.Builder {

    private static final Deque<SpongeBlockSnapshotBuilder> pool = new ConcurrentLinkedDeque<>();

    public static SpongeBlockSnapshotBuilder unpooled() {
        return new SpongeBlockSnapshotBuilder(false);
    }

    public static SpongeBlockSnapshotBuilder pooled() {
        final SpongeBlockSnapshotBuilder builder = pool.pollFirst();
        if (builder != null) {
            return builder.reset();
        }
        return new SpongeBlockSnapshotBuilder(true);
    }

    BlockState blockState;
    @Nullable BlockState extendedState;
    UUID worldUuid;
    @Nullable UUID creatorUuid;
    @Nullable UUID notifierUuid;
    Vector3i coords;
    @Nullable List<ImmutableDataManipulator<?, ?>> manipulators;
    @Nullable NBTTagCompound compound;
    SpongeBlockChangeFlag flag = (SpongeBlockChangeFlag) BlockChangeFlags.ALL;
    private final boolean pooled;


    private SpongeBlockSnapshotBuilder(boolean pooled) {
        super(BlockSnapshot.class, 1);
        this.pooled = pooled;
    }

    @Override
    public SpongeBlockSnapshotBuilder world(final WorldProperties worldProperties) {
        this.worldUuid = checkNotNull(worldProperties).getUniqueId();
        return this;
    }

    public SpongeBlockSnapshotBuilder worldId(final UUID worldUuid) {
        this.worldUuid = checkNotNull(worldUuid);
        return this;
    }

    @Override
    public SpongeBlockSnapshotBuilder blockState(final BlockState blockState) {
        this.blockState = checkNotNull(blockState);
        return this;
    }

    public SpongeBlockSnapshotBuilder blockState(final IBlockState blockState) {
        this.blockState = checkNotNull((BlockState) blockState);
        return this;
    }

    public SpongeBlockSnapshotBuilder extendedState(final BlockState extendedState) {
        this.extendedState = checkNotNull(extendedState);
        return this;
    }


    public SpongeBlockSnapshotBuilder extendedState(final IBlockState extended) {
        this.extendedState = checkNotNull((BlockState) extended);
        return this;
    }

    @Override
    public SpongeBlockSnapshotBuilder position(final Vector3i position) {
        this.coords = checkNotNull(position);
        if (this.compound != null) {
            this.compound.func_74768_a(Constants.Sponge.BlockSnapshot.TILE_ENTITY_POSITION_X, position.getX());
            this.compound.func_74768_a(Constants.Sponge.BlockSnapshot.TILE_ENTITY_POSITION_Y, position.getY());
            this.compound.func_74768_a(Constants.Sponge.BlockSnapshot.TILE_ENTITY_POSITION_Z, position.getZ());
        }
        return this;
    }

    @Override
    public SpongeBlockSnapshotBuilder from(final Location<World> location) {
        this.blockState = location.getBlock();
        this.worldUuid = location.getExtent().getUniqueId();
        this.coords = location.getBlockPosition();
        if (this.blockState.getType() instanceof ITileEntityProvider) {
            if (location.hasTileEntity()) {
                this.compound = new NBTTagCompound();
                final org.spongepowered.api.block.tileentity.TileEntity te = location.getTileEntity().get();
                ((TileEntity) te).func_189515_b(this.compound);
                this.manipulators = ((CustomDataHolderBridge) te).bridge$getCustomManipulators().stream()
                        .map(DataManipulator::asImmutable)
                        .collect(Collectors.toList());
            }
        }
        return this;
    }

    @Override
    public SpongeBlockSnapshotBuilder creator(final UUID uuid) {
        this.creatorUuid = uuid;
        return this;
    }

    @Override
    public SpongeBlockSnapshotBuilder notifier(final UUID uuid) {
        this.notifierUuid = uuid;
        return this;
    }

    public SpongeBlockSnapshotBuilder unsafeNbt(final NBTTagCompound compound) {
        this.compound = compound.func_74737_b();
        return this;
    }

    @Override
    public SpongeBlockSnapshotBuilder add(final DataManipulator<?, ?> manipulator) {
        return add(checkNotNull(manipulator, "manipulator").asImmutable());
    }

    @Override
    public SpongeBlockSnapshotBuilder add(final ImmutableDataManipulator<?, ?> manipulator) {
        checkNotNull(manipulator, "manipulator");
        if (this.manipulators == null) {
            this.manipulators = Lists.newArrayList();
        }
        for (final Iterator<ImmutableDataManipulator<?, ?>> iterator = this.manipulators.iterator(); iterator.hasNext();) {
            final ImmutableDataManipulator<?, ?> existing = iterator.next();
            if (manipulator.getClass().isInstance(existing)) {
                iterator.remove();
            }
        }
        this.manipulators.add(manipulator);
        return this;
    }

    @Override
    public <V> SpongeBlockSnapshotBuilder add(final Key<? extends BaseValue<V>> key, final V value) {
        checkNotNull(key, "key");
        checkState(this.blockState != null);
        this.blockState = this.blockState.with(key, value).orElse(this.blockState);
        if(this.extendedState != null) {
            this.extendedState = this.extendedState.with(key, value).orElse(this.extendedState);
        }
        return this;
    }

    public SpongeBlockSnapshotBuilder flag(final BlockChangeFlag flag) {
        this.flag = (SpongeBlockChangeFlag) flag;
        return this;
    }

    @Override
    public SpongeBlockSnapshotBuilder from(final BlockSnapshot holder) {
        this.blockState = holder.getState();
        this.worldUuid = holder.getWorldUniqueId();
        if (holder.getCreator().isPresent()) {
            this.creatorUuid = holder.getCreator().get();
        }
        if (holder.getNotifier().isPresent()) {
            this.notifierUuid = holder.getNotifier().get();
        }
        this.coords = holder.getPosition();
        this.manipulators = Lists.newArrayList(holder.getManipulators());
        if (holder instanceof SpongeBlockSnapshot) {
            final NBTTagCompound compound = ((SpongeBlockSnapshot) holder).compound;
            if (compound != null) {
                this.compound = compound.func_74737_b();
            }
        }
        return this;
    }

    @Override
    public SpongeBlockSnapshotBuilder reset() {
        this.blockState = (BlockState) Blocks.field_150350_a.func_176223_P();
        this.extendedState = null;
        this.worldUuid = null;
        this.creatorUuid = null;
        this.notifierUuid = null;
        this.coords = null;
        this.manipulators = null;
        this.compound = null;
        this.flag = null;
        return this;
    }

    @Override
    public SpongeBlockSnapshot build() {
        checkState(this.blockState != null);
        if (this.extendedState == null) {
            this.extendedState = this.blockState;
        }
        final SpongeBlockSnapshot spongeBlockSnapshot = new SpongeBlockSnapshot(this);
        this.reset();
        if (this.pooled) {
            pool.push(this);
        }
        return spongeBlockSnapshot;
    }

    @Override
    protected Optional<BlockSnapshot> buildContent(final DataView container) throws InvalidDataException {
        if (!container.contains(Constants.Block.BLOCK_STATE, Queries.WORLD_ID, Constants.Sponge.SNAPSHOT_WORLD_POSITION)) {
            return Optional.empty();
        }
        checkDataExists(container, Constants.Block.BLOCK_STATE);
        checkDataExists(container, Queries.WORLD_ID);
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        final UUID worldUuid = UUID.fromString(container.getString(Queries.WORLD_ID).get());
        final Vector3i coordinate = DataUtil.getPosition3i(container);
        final Optional<String> creatorUuid = container.getString(Queries.CREATOR_ID);
        final Optional<String> notifierUuid = container.getString(Queries.NOTIFIER_ID);

        // We now reconstruct the custom data and all extra data.
        final BlockState blockState = container.getSerializable(Constants.Block.BLOCK_STATE, BlockState.class).get();
        BlockState extendedState = null;
        if (container.contains(Constants.Block.BLOCK_EXTENDED_STATE)) {
            extendedState = container.getSerializable(Constants.Block.BLOCK_EXTENDED_STATE, BlockState.class).get();
        } else {
            extendedState = blockState;
        }

        builder.blockState(blockState)
                .extendedState(extendedState)
                .position(coordinate)
                .worldId(worldUuid);
        if (creatorUuid.isPresent()) {
            builder.creator(UUID.fromString(creatorUuid.get()));
        }
        if (notifierUuid.isPresent()) {
            builder.notifier(UUID.fromString(notifierUuid.get()));
        }
        final Optional<DataView> unsafeCompound = container.getView(Constants.Sponge.UNSAFE_NBT);
        final NBTTagCompound compound = unsafeCompound.isPresent() ? NbtTranslator.getInstance().translateData(unsafeCompound.get()) : null;
        if (compound != null) {
            builder.unsafeNbt(compound);
        }
        if (container.contains(Constants.Sponge.SNAPSHOT_TILE_DATA)) {
            final List<DataView> dataViews = container.getViewList(Constants.Sponge.SNAPSHOT_TILE_DATA).get();
            DataUtil.deserializeImmutableManipulatorList(dataViews).stream().forEach(builder::add);
        }
        return Optional.of(builder.build());
    }

}
