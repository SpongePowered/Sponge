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

import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.SpongeBlockChangeFlag;
import org.spongepowered.math.vector.Vector3i;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

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
    ResourceKey worldKey;
    @Nullable UUID creatorUniqueId;
    @Nullable UUID notifierUniqueId;
    Vector3i coordinates;
    @Nullable List<DataManipulator.Immutable> manipulators;
    @Nullable CompoundNBT compound;
    SpongeBlockChangeFlag flag = (SpongeBlockChangeFlag) BlockChangeFlags.ALL;
    @Nullable WeakReference<ServerWorld> worldRef;
    private final boolean pooled;


    private SpongeBlockSnapshotBuilder(final boolean pooled) {
        super(BlockSnapshot.class, 1);
        this.pooled = pooled;
    }

    @Override
    public SpongeBlockSnapshotBuilder world(final WorldProperties worldProperties) {
        this.worldKey = Objects.requireNonNull(worldProperties).getKey();
        return this;
    }

    public SpongeBlockSnapshotBuilder world(final ResourceKey key) {
        this.worldKey = Objects.requireNonNull(key);
        return this;
    }
    
    public SpongeBlockSnapshotBuilder world(final ServerWorld world) {
        this.worldKey = ((org.spongepowered.api.world.server.ServerWorld) Objects.requireNonNull(world)).getKey();
        this.worldRef = new WeakReference<ServerWorld>(world);
        return this;
    }

    @Override
    public SpongeBlockSnapshotBuilder blockState(final BlockState blockState) {
        this.blockState = Objects.requireNonNull(blockState);
        return this;
    }

    public SpongeBlockSnapshotBuilder blockState(final net.minecraft.block.BlockState blockState) {
        this.blockState = Objects.requireNonNull((BlockState) blockState);
        return this;
    }


    @Override
    public SpongeBlockSnapshotBuilder position(final Vector3i position) {
        this.coordinates = Objects.requireNonNull(position);
        if (this.compound != null) {
            this.compound.putInt(Constants.Sponge.BlockSnapshot.TILE_ENTITY_POSITION_X, position.getX());
            this.compound.putInt(Constants.Sponge.BlockSnapshot.TILE_ENTITY_POSITION_Y, position.getY());
            this.compound.putInt(Constants.Sponge.BlockSnapshot.TILE_ENTITY_POSITION_Z, position.getZ());
        }
        return this;
    }

    @Override
    public BlockSnapshot.Builder from(final ServerLocation location) {
        throw new UnsupportedOperationException("Not implemented correctly.");
    }

    @Override
    public SpongeBlockSnapshotBuilder creator(final UUID uuid) {
        throw new UnsupportedOperationException("Not implemented correctly.");
    }

    @Override
    public SpongeBlockSnapshotBuilder notifier(final UUID uuid) {
        this.notifierUniqueId = uuid;
        return this;
    }

    @Override
    public BlockSnapshot empty() {
        return SpongeBlockSnapshotBuilder.pooled()
                .world(Constants.World.INVALID_WORLD_KEY)
                .position(new Vector3i(0, 0, 0))
                .blockState(Blocks.AIR.getDefaultState())
                .build();
    }

    public SpongeBlockSnapshotBuilder unsafeNbt(final CompoundNBT compound) {
        this.compound = compound.copy();
        return this;
    }


    public SpongeBlockSnapshotBuilder flag(final BlockChangeFlag flag) {
        this.flag = (SpongeBlockChangeFlag) flag;
        return this;
    }

    @Override
    public <V> BlockSnapshot.Builder add(final Key<? extends Value<V>> key, final V value) {
        return null;
    }

    @Override
    public SpongeBlockSnapshotBuilder from(final BlockSnapshot holder) {
        this.blockState = holder.getState();
        this.worldKey = holder.getWorld();
        if (holder.getCreator().isPresent()) {
            this.creatorUniqueId = holder.getCreator().get();
        }
        if (holder.getNotifier().isPresent()) {
            this.notifierUniqueId = holder.getNotifier().get();
        }
        this.coordinates = holder.getPosition();
        return this;
    }

    public SpongeBlockSnapshotBuilder from(final SpongeBlockSnapshot snapshot) {
        this.blockState = snapshot.getState();
        this.worldKey = snapshot.getWorld();
        this.worldRef = snapshot.world;
        this.compound = snapshot.compound;
        this.coordinates = snapshot.getPosition();
        this.flag = snapshot.getChangeFlag();
        return this;
    }

    public SpongeBlockSnapshotBuilder tileEntity(final TileEntity added) {
        this.compound = null;
        final CompoundNBT tag = new CompoundNBT();
        added.write(tag);
        this.compound = tag;
        return this;
    }

    @Override
    public SpongeBlockSnapshotBuilder reset() {
        this.blockState = (BlockState) Blocks.AIR.getDefaultState();
        this.worldKey = Constants.World.INVALID_WORLD_KEY;
        this.creatorUniqueId = null;
        this.notifierUniqueId = null;
        this.coordinates = null;
        this.manipulators = null;
        this.compound = null;
        this.flag = null;
        return this;
    }

    @Override
    public SpongeBlockSnapshot build() {
        Objects.requireNonNull(this.blockState, "BlockState cannot be null!");
        final SpongeBlockSnapshot spongeBlockSnapshot = new SpongeBlockSnapshot(this);
        this.reset();
        if (this.pooled) {
            pool.push(this);
        }
        return spongeBlockSnapshot;
    }

    @Override
    protected Optional<BlockSnapshot> buildContent(final DataView container) throws InvalidDataException {
        if (!container.contains(Constants.Block.BLOCK_STATE, Queries.WORLD_KEY, Constants.Sponge.SNAPSHOT_WORLD_POSITION)) {
            return Optional.empty();
        }
        DataUtil.checkDataExists(container, Constants.Block.BLOCK_STATE);
        DataUtil.checkDataExists(container, Queries.WORLD_KEY);
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        final ResourceKey worldKey = container.getKey(Queries.WORLD_KEY).get();
        final Vector3i coordinate = DataUtil.getPosition3i(container);
        final Optional<String> creatorUuid = container.getString(Queries.CREATOR_ID);
        final Optional<String> notifierUuid = container.getString(Queries.NOTIFIER_ID);

        // We now reconstruct the custom data and all extra data.
        final BlockState blockState = container.getSerializable(Constants.Block.BLOCK_STATE, BlockState.class).get();

        builder
            .blockState(blockState)
            .world(worldKey)
            .position(coordinate)
        ;

        creatorUuid.ifPresent(s -> builder.creator(UUID.fromString(s)));
        notifierUuid.ifPresent(s -> builder.notifier(UUID.fromString(s)));
        container.getView(Constants.Sponge.UNSAFE_NBT)
                .map(dataView -> NbtTranslator.getInstance().translateData(dataView))
                .ifPresent(builder::unsafeNbt);
        if (container.contains(Constants.Sponge.SNAPSHOT_TILE_DATA)) {
            final List<DataView> dataViews = container.getViewList(Constants.Sponge.SNAPSHOT_TILE_DATA).get();
//            DataUtil.deserializeImmutableManipulatorList(dataViews).forEach(builder::add);
        }
        return Optional.of(builder.build());
    }

}
