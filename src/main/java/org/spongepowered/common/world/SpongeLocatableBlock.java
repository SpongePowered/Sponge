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
package org.spongepowered.common.world;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.World;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector3i;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class SpongeLocatableBlock implements LocatableBlock {

    private final BlockState blockState;
    private final Vector3i position;
    private final UUID worldId;
    private final WeakReference<World> worldReference;
    @Nullable private ServerLocation location;

    SpongeLocatableBlock(SpongeLocatableBlockBuilder builder) {
        this.blockState = checkNotNull(builder.blockState.get(), "blockstate");
        this.position = checkNotNull(builder.position.get(), "position");
        this.worldId = checkNotNull(builder.worldId.get(), "worldid");
        this.worldReference = checkNotNull(builder.worldReference.get(), "reference");
    }

    @Override
    public BlockState getBlockState() {
        return this.blockState;
    }

    @Override
    public ServerLocation getLocation() {
        if (this.location == null) {
            this.location = ServerLocation.of(this.worldReference.get(), this.position);
        }
        return this.location;
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, 1)
                .set(Queries.WORLD_ID, this.worldId)
                .set(Queries.POSITION_X, this.position.getX())
                .set(Queries.POSITION_Y, this.position.getY())
                .set(Queries.POSITION_Z, this.position.getZ())
                .set(Constants.Block.BLOCK_STATE, this.blockState);
    }

    @Override
    public <E> Optional<E> get(Key<? extends Value<E>> key) {
        return this.blockState.get(key);
    }

    @Override
    public <E, V extends Value<E>> Optional<V> getValue(Key<V> key) {
        return this.blockState.getValue(key);
    }

    @Override
    public boolean supports(Key<?> key) {
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
    public <E> Optional<LocatableBlock> transform(Key<? extends Value<E>> key, Function<E, E> function) {
        return this.blockState.transform(key, function).map(state -> LocatableBlock.builder().from(this).state(state).build());
    }

    @Override
    public <E> Optional<LocatableBlock> with(Key<? extends Value<E>> key, E value) {
        return this.blockState.with(key, value).map(state -> LocatableBlock.builder().from(this).state(state).build());
    }

    @Override
    public Optional<LocatableBlock> with(Value<?> value) {
        return this.blockState.with(value).map(state -> LocatableBlock.builder().from(this).state(state).build());
    }

    @Override
    public Optional<LocatableBlock> without(Key<?> key) {
        return this.blockState.without(key).map(state -> LocatableBlock.builder().from(this).state(state).build());
    }

    @Override
    public LocatableBlock withRawData(DataView container) throws InvalidDataException {
        return LocatableBlock.builder().from(this).state(this.blockState.withRawData(container)).build();
    }

    @Override
    public LocatableBlock mergeWith(LocatableBlock that, MergeFunction function) {

        return LocatableBlock.builder().from(this).state(this.blockState.mergeWith(that.getBlockState(), function)).build();
    }

    @Override
    public boolean validateRawData(DataView container) {
        return this.blockState.validateRawData(container);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("blockState", this.blockState)
                .add("position", this.position)
                .add("worldReference", this.worldReference.get() == null ? "null" : this.worldReference.get().getProperties().getDirectoryName())
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        SpongeLocatableBlock that = (SpongeLocatableBlock) o;
        return Objects.equal(this.blockState, that.blockState) &&
               Objects.equal(this.position, that.position) &&
               Objects.equal(this.worldId, that.worldId) &&
               Objects.equal(this.worldReference, that.worldReference);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.blockState, this.position, this.worldId, this.worldReference);
    }
}
