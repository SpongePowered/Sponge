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

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeBlockChangeFlag;
import org.spongepowered.math.vector.Vector3i;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Function;

@DefaultQualifier(NonNull.class)
public final class SpongeBlockSnapshot implements BlockSnapshot {

    private final BlockState blockState;
    private final ResourceKey worldKey;
    private final Vector3i pos;
    @Nullable final CompoundNBT compound;
    // Internal use only
    private final BlockPos blockPos;
    private final SpongeBlockChangeFlag changeFlag;
    @Nullable private WeakReference<ServerWorld> world;
    @MonotonicNonNull public BlockChange blockChange; // used for post event

    SpongeBlockSnapshot(final SpongeBlockSnapshotBuilder builder) {
        this.blockState = Objects.requireNonNull(builder.blockState);
        this.worldKey = Objects.requireNonNull(builder.worldKey);
        this.pos = Objects.requireNonNull(builder.coordinates);
        this.blockPos = VecHelper.toBlockPos(this.pos);
        this.compound = builder.compound;
        this.changeFlag = builder.flag;
        this.world = builder.worldRef;
        builder.worldRef = null;
    }

    @Override
    public BlockState getState() {
        return this.blockState;
    }

    @Override
    public BlockSnapshot withState(final BlockState blockState) {
        return this.createBuilder().blockState(blockState).build();
    }

    @Override
    public BlockSnapshot withContainer(final DataContainer container) {
        return SpongeBlockSnapshotBuilder.pooled().build(container).get();
    }

    @Override
    public ResourceKey getWorld() {
        return this.worldKey;
    }

    @Override
    public Vector3i getPosition() {
        return this.pos;
    }

    @Override
    public Optional<ServerLocation> getLocation() {
        return Optional.empty();
    }

    @Override
    public BlockSnapshot withLocation(final ServerLocation location) {
        return null;
    }

    @Override
    public boolean restore(final boolean force, final BlockChangeFlag flag) {
        // TODO - rewrite with the PhaseTracker being the hook or use SpongeImplHooks to do the restore.
        throw new UnsupportedOperationException("Not implemented yet, please fix when this is called");
    }

    @Override
    public Optional<UUID> getCreator() {
        return Optional.empty();
    }

    @Override
    public Optional<UUID> getNotifier() {
        return Optional.empty();
    }

    @Override
    public Optional<BlockEntityArchetype> createArchetype() {
        throw new UnsupportedOperationException("Not implemented yet, please fix when this is called");
    }

    @Override
    public BlockSnapshot withRawData(final DataView container) throws InvalidDataException {
        throw new UnsupportedOperationException("Not implemented yet, please fix when this is called");
    }

    @Override
    public boolean validateRawData(final DataView container) {
        throw new UnsupportedOperationException("Not implemented yet, please fix when this is called");
    }

    @Override
    public BlockSnapshot copy() {
        return this;
    }

    @Override
    public <E> Optional<BlockSnapshot> transform(final Key<? extends Value<E>> key, final Function<E, E> function) {
        throw new UnsupportedOperationException("Not implemented yet, please fix when this is called");
    }

    @Override
    public <E> Optional<BlockSnapshot> with(final Key<? extends Value<E>> key, final E value) {
        throw new UnsupportedOperationException("Not implemented yet, please fix when this is called");
    }

    @Override
    public Optional<BlockSnapshot> with(final Value<?> value) {
        throw new UnsupportedOperationException("Not implemented yet, please fix when this is called");
    }

    @Override
    public Optional<BlockSnapshot> without(final Key<?> key) {
        throw new UnsupportedOperationException("Not implemented yet, please fix when this is called");
    }

    @Override
    public BlockSnapshot mergeWith(final BlockSnapshot that, final MergeFunction function) {
        throw new UnsupportedOperationException("Not implemented yet, please fix when this is called");
    }

    @Override
    public int getContentVersion() {
        throw new UnsupportedOperationException("Not implemented yet, please fix when this is called");
    }

    @Override
    public DataContainer toContainer() {
        throw new UnsupportedOperationException("Not implemented yet, please fix when this is called");
    }

    @Override
    public <E> Optional<E> get(final Key<? extends Value<E>> key) {
        throw new UnsupportedOperationException("Not implemented yet, please fix when this is called");
    }

    @Override
    public <E, V extends Value<E>> Optional<V> getValue(final Key<V> key) {
        throw new UnsupportedOperationException("Not implemented yet, please fix when this is called");
    }

    @Override
    public boolean supports(final Key<?> key) {
        throw new UnsupportedOperationException("Not implemented yet, please fix when this is called");
    }

    @Override
    public Set<Key<?>> getKeys() {
        throw new UnsupportedOperationException("Not implemented yet, please fix when this is called");
    }

    @Override
    public Set<Value.Immutable<?>> getValues() {
        throw new UnsupportedOperationException("Not implemented yet, please fix when this is called");
    }
    public Optional<ServerWorld> getServerWorld() {
        if (this.world == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.world.get());
    }

    public Optional<CompoundNBT> getCompound() {
        return this.compound == null ? Optional.<CompoundNBT>empty() : Optional.of(this.compound.copy());
    }

    public SpongeBlockSnapshotBuilder createBuilder() {
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        builder.blockState(this.blockState)
            .position(this.pos)
            .world(this.worldKey);
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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeBlockSnapshot that = (SpongeBlockSnapshot) o;
        return this.changeFlag == that.changeFlag &&
               Objects.equals(this.worldKey, that.worldKey) &&
               Objects.equals(this.pos, that.pos) &&
               Objects.equals(this.compound, that.compound);
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(this.blockState,
                this.worldKey,
                this.pos,
                this.changeFlag,
                this.compound);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SpongeBlockSnapshot.class.getSimpleName() + "[", "]")
            .add("world=" + this.worldKey)
            .add("position=" + this.blockPos)
            .add("blockState=" + this.blockState)
            .toString();
    }
}
