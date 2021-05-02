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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
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
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.DataUtil;
import org.spongepowered.common.world.SpongeBlockChangeFlag;
import org.spongepowered.math.vector.Vector3i;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SpongeBlockSnapshotBuilder extends AbstractDataBuilder<@NonNull BlockSnapshot> implements BlockSnapshot.Builder {

    private static final Deque<SpongeBlockSnapshotBuilder> pool = new ConcurrentLinkedDeque<>();

    public static SpongeBlockSnapshotBuilder unpooled() {
        return new SpongeBlockSnapshotBuilder(false);
    }

    public static SpongeBlockSnapshotBuilder pooled() {
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pool.pollFirst();
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
    @Nullable CompoundTag compound;
    SpongeBlockChangeFlag flag = (SpongeBlockChangeFlag) BlockChangeFlags.ALL;
    @Nullable WeakReference<ServerLevel> worldRef;
    private final boolean pooled;


    private SpongeBlockSnapshotBuilder(final boolean pooled) {
        super(BlockSnapshot.class, 1);
        this.pooled = pooled;
    }

    @Override
    public @NonNull SpongeBlockSnapshotBuilder world(final @NonNull ServerWorldProperties worldProperties) {
        this.worldKey = Objects.requireNonNull(worldProperties).key();
        return this;
    }

    public SpongeBlockSnapshotBuilder world(final ResourceKey key) {
        this.worldKey = Objects.requireNonNull(key);
        return this;
    }
    
    public SpongeBlockSnapshotBuilder world(final ServerLevel world) {
        this.worldKey = ((org.spongepowered.api.world.server.ServerWorld) Objects.requireNonNull(world)).key();
        this.worldRef = new WeakReference<>(world);
        return this;
    }

    @Override
    public @NonNull SpongeBlockSnapshotBuilder blockState(final @NonNull BlockState blockState) {
        this.blockState = Objects.requireNonNull(blockState);
        return this;
    }

    public SpongeBlockSnapshotBuilder blockState(final net.minecraft.world.level.block.state.BlockState blockState) {
        this.blockState = Objects.requireNonNull((BlockState) blockState);
        return this;
    }


    @Override
    public @NonNull SpongeBlockSnapshotBuilder position(final @NonNull Vector3i position) {
        this.coordinates = Objects.requireNonNull(position);
        if (this.compound != null) {
            this.compound.putInt(Constants.Sponge.BlockSnapshot.TILE_ENTITY_POSITION_X, position.x());
            this.compound.putInt(Constants.Sponge.BlockSnapshot.TILE_ENTITY_POSITION_Y, position.y());
            this.compound.putInt(Constants.Sponge.BlockSnapshot.TILE_ENTITY_POSITION_Z, position.z());
        }
        return this;
    }

    @Override
    public BlockSnapshot.@NonNull Builder from(final @NonNull ServerLocation location) {
        return this.from(location.createSnapshot());
    }

    @Override
    public @NonNull SpongeBlockSnapshotBuilder creator(final UUID uuid) {
        this.creatorUniqueId = Objects.requireNonNull(uuid);
        return this;
    }

    @Override
    public @NonNull SpongeBlockSnapshotBuilder notifier(final UUID uuid) {
        this.notifierUniqueId = Objects.requireNonNull(uuid);
        return this;
    }

    @Override
    public @NonNull BlockSnapshot empty() {
        return SpongeBlockSnapshotBuilder.pooled()
                .world(Constants.World.INVALID_WORLD_KEY)
                .position(new Vector3i(0, 0, 0))
                .blockState(Blocks.AIR.defaultBlockState())
                .build();
    }

    @Override
    public <V> BlockSnapshot.@NonNull Builder add(final @NonNull Key<@NonNull ? extends Value<V>> key, final @NonNull V value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);

        this.blockState = this.blockState.with(key, value)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Key %s is not supported for block state %s",
                        key.key().asString(),
                        this.blockState.toString())));
        return this;
    }

    @Override
    public @NonNull SpongeBlockSnapshotBuilder from(final BlockSnapshot holder) {
        Objects.requireNonNull(holder);

        this.blockState = holder.state();
        this.worldKey = holder.world();
        if (holder.creator().isPresent()) {
            this.creatorUniqueId = holder.creator().get();
        }
        if (holder.notifier().isPresent()) {
            this.notifierUniqueId = holder.notifier().get();
        }
        this.coordinates = holder.position();
        return this;
    }

    public SpongeBlockSnapshotBuilder from(final SpongeBlockSnapshot snapshot) {
        Objects.requireNonNull(snapshot);

        this.blockState = snapshot.state();
        this.worldKey = snapshot.world();
        this.worldRef = snapshot.world;
        this.compound = snapshot.compound;
        this.coordinates = snapshot.position();
        this.flag = snapshot.getChangeFlag();
        return this;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public ResourceKey getWorldKey() {
        return this.worldKey;
    }

    public @Nullable UUID getCreatorUniqueId() {
        return this.creatorUniqueId;
    }

    public Vector3i getCoordinates() {
        return this.coordinates;
    }

    public @Nullable List<DataManipulator.Immutable> getManipulators() {
        return this.manipulators;
    }

    public @Nullable CompoundTag getCompound() {
        return this.compound;
    }

    public SpongeBlockChangeFlag getFlag() {
        return this.flag;
    }

    @Override
    public @NonNull SpongeBlockSnapshotBuilder reset() {
        this.blockState = (BlockState) Blocks.AIR.defaultBlockState();
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
    public @NonNull SpongeBlockSnapshot build() {
        Objects.requireNonNull(this.blockState, "BlockState cannot be null!");
        final SpongeBlockSnapshot spongeBlockSnapshot = new SpongeBlockSnapshot(this);
        this.reset();
        if (this.pooled) {
            SpongeBlockSnapshotBuilder.pool.push(this);
        }
        return spongeBlockSnapshot;
    }

    @Override
    protected @NonNull Optional<BlockSnapshot> buildContent(final DataView container) throws InvalidDataException {

        if (!container.contains(Constants.Block.BLOCK_STATE, Constants.Sponge.SNAPSHOT_WORLD_POSITION)) {
            return Optional.empty();
        }

        // if we have no world-key check if we can find by uuid
        if (!container.contains(Queries.WORLD_KEY)) {
            if (!container.contains(Constants.Sponge.BlockSnapshot.WORLD_UUID)) {
                return Optional.empty();
            }
            final UUID uuid = UUID.fromString(container.getString(Constants.Sponge.BlockSnapshot.WORLD_UUID).get());
            Sponge.server().worldManager().worldKey(uuid).ifPresent(worldKey -> container.set(Queries.WORLD_KEY, worldKey));
        }

        DataUtil.checkDataExists(container, Constants.Block.BLOCK_STATE);
        DataUtil.checkDataExists(container, Queries.WORLD_KEY);
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        final ResourceKey worldKey = container.getResourceKey(Queries.WORLD_KEY).get();
        final Vector3i coordinate = DataUtil.getPosition3i(container);
        final Optional<String> creatorUuid = container.getString(Queries.CREATOR_ID);
        final Optional<String> notifierUuid = container.getString(Queries.NOTIFIER_ID);

        final BlockState blockState = container.getSerializable(Constants.Block.BLOCK_STATE, BlockState.class).get();

        builder.blockState(blockState).world(worldKey).position(coordinate);

        creatorUuid.ifPresent(s -> builder.creator(UUID.fromString(s)));
        notifierUuid.ifPresent(s -> builder.notifier(UUID.fromString(s)));
        container.getView(Constants.Sponge.UNSAFE_NBT)
                .map(dataView -> NBTTranslator.INSTANCE.translate(dataView))
                .ifPresent(builder::addUnsafeCompound);
        return Optional.of(builder.build());
    }

    public SpongeBlockSnapshotBuilder addUnsafeCompound(final CompoundTag compound) {
        Objects.requireNonNull(compound);

        this.compound = compound.copy();
        return this;
    }

    public SpongeBlockSnapshotBuilder flag(final BlockChangeFlag flag) {
        this.flag = (SpongeBlockChangeFlag) flag;
        return this;
    }

    public SpongeBlockSnapshotBuilder tileEntity(final BlockEntity added) {
        this.compound = null;
        final CompoundTag tag = new CompoundTag();
        added.save(tag);
        this.compound = tag;
        return this;
    }
}
