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
package org.spongepowered.common.world.server;


import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector3i;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;

public final class SpongeLocatableBlock implements LocatableBlock {

    private final BlockState blockState;
    private final ResourceKey world;
    private final Vector3i position;
    private final WeakReference<ServerWorld> worldRef;
    private @Nullable ServerLocation location;

    SpongeLocatableBlock(final SpongeLocatableBlockBuilder builder) {
        this.blockState = Objects.requireNonNull(builder.blockState.get(), "blockstate");
        this.position = Objects.requireNonNull(builder.position.get(), "position");
        this.world = Objects.requireNonNull(builder.world.get(), "world");
        this.worldRef = new WeakReference<>(Objects.requireNonNull(builder.worldReference.get(), "reference"));
    }

    SpongeLocatableBlock(final ServerWorld world, final int x, final int y, final int z) {
        this.world = world.key();
        this.worldRef = new WeakReference<>(world);
        this.position = new Vector3i(x, y, z);
        this.blockState = world.block(x, y, z);
    }

    @Override
    public BlockState blockState() {
        return this.blockState;
    }

    @Override
    public World<?, ?> world() {
        return Objects.requireNonNull(this.worldRef.get(), "World was de-referenced!");
    }

    @Override
    public ServerLocation location() {
        if (this.location == null) {
            this.location = ServerLocation.of(Objects.requireNonNull(this.worldRef.get(), "World was de-referenced!"), this.position);
        }
        return this.location;
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED)
                .set(Queries.CONTENT_VERSION, 1)
                .set(Queries.WORLD_KEY, this.world)
                .set(Queries.POSITION_X, this.position.x())
                .set(Queries.POSITION_Y, this.position.y())
                .set(Queries.POSITION_Z, this.position.z())
                .set(Constants.Block.BLOCK_STATE, this.blockState);
    }

    @Override
    public <E> Optional<E> get(final Key<? extends Value<E>> key) {
        return this.blockState.get(key);
    }

    @Override
    public <E, V extends Value<E>> Optional<V> getValue(final Key<V> key) {
        return this.blockState.getValue(key);
    }

    @Override
    public boolean supports(final Key<?> key) {
        return this.blockState.supports(key);
    }

    @Override
    public LocatableBlock copy() {
        return this;
    }

    @Override
    public Set<Key<?>> getKeys() {
        return this.blockState.getKeys();
    }

    @Override
    public Set<org.spongepowered.api.data.value.Value.Immutable<?>> getValues() {
        return this.blockState.getValues();
    }

    @Override
    public <E> Optional<LocatableBlock> transform(final Key<? extends Value<E>> key, final Function<E, E> function) {
        return this.blockState.transform(key, function).map(state -> LocatableBlock.builder().from(this).state(state).build());
    }

    @Override
    public <E> Optional<LocatableBlock> with(final Key<? extends Value<E>> key, final E value) {
        return this.blockState.with(key, value).map(state -> LocatableBlock.builder().from(this).state(state).build());
    }

    @Override
    public Optional<LocatableBlock> with(final Value<?> value) {
        return this.blockState.with(value).map(state -> LocatableBlock.builder().from(this).state(state).build());
    }

    @Override
    public Optional<LocatableBlock> without(final Key<?> key) {
        return this.blockState.without(key).map(state -> LocatableBlock.builder().from(this).state(state).build());
    }

    @Override
    public LocatableBlock withRawData(final DataView container) throws InvalidDataException {
        return LocatableBlock.builder().from(this).state(this.blockState.withRawData(container)).build();
    }

    @Override
    public LocatableBlock mergeWith(final LocatableBlock that, final MergeFunction function) {
        return LocatableBlock.builder().from(this).state(this.blockState.mergeWith(that.blockState(), function)).build();
    }

    @Override
    public boolean validateRawData(final DataView container) {
        return this.blockState.validateRawData(container);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeLocatableBlock that = (SpongeLocatableBlock) o;
        return Objects.equals(this.blockState, that.blockState) &&
                Objects.equals(this.position, that.position) &&
                Objects.equals(this.world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.blockState, this.position, this.world);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SpongeLocatableBlock.class.getSimpleName() + "[", "]")
                .add("blockState=" + this.blockState)
                .add("world=" + this.world)
                .add("position=" + this.position)
                .toString();
    }
}
