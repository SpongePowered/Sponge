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
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector3i;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class SpongeLocatableBlock implements LocatableBlock {

    private final BlockState blockState;
    private final Vector3i position;
    private final UUID worldId;
    private final WeakReference<World> worldReference;
    @Nullable private Location location;

    SpongeLocatableBlock(SpongeLocatableBlockBuilder builder) {
        this.blockState = checkNotNull(builder.blockState, "blockstate");
        this.position = checkNotNull(builder.position, "position");
        this.worldId = checkNotNull(builder.worldId, "worldid");
        this.worldReference = checkNotNull(builder.worldReference, "reference");
    }

    @Override
    public BlockState getBlockState() {
        return this.blockState;
    }

    @Override
    public Location getLocation() {
        if (this.location == null) {
            this.location = Location.of(this.worldReference.get(), this.position);
        }
        return this.location;
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
    public List<Immutable<?, ?>> getManipulators() {
        return this.blockState.getManipulators();
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
    public <T extends Immutable<?, ?>> Optional<T> get(Class<T> containerClass) {
        return this.blockState.get(containerClass);
    }

    @Override
    public <T extends Immutable<?, ?>> Optional<T> getOrCreate(Class<T> containerClass) {
        return this.blockState.getOrCreate(containerClass);
    }

    @Override
    public boolean supports(Class<? extends Immutable<?, ?>> containerClass) {
        return this.blockState.supports(containerClass);
    }

    @Override
    public <E> Optional<LocatableBlock> transform(Key<? extends Value<E>> key, Function<E, E> function) {
        return this.blockState.transform(key, function)
                .map(state -> LocatableBlock.builder()
                        .from(this)
                        .state(state)
                        .build());
    }

    @Override
    public <E> Optional<LocatableBlock> with(Key<? extends Value<E>> key, E value) {
        return this.blockState.with(key, value)
                .map(state -> LocatableBlock.builder()
                        .from(this)
                        .state(state)
                        .build());
    }

    @Override
    public Optional<LocatableBlock> with(Value<?> value) {
        return this.blockState.with(value)
                .map(state -> LocatableBlock.builder()
                        .from(this)
                        .state(state)
                        .build());
    }

    @Override
    public Optional<LocatableBlock> with(Immutable<?, ?> valueContainer) {
        return this.blockState.with(valueContainer)
                .map(state -> LocatableBlock.builder()
                        .from(this)
                        .state(state)
                        .build());
    }

    @Override
    public Optional<LocatableBlock> with(Iterable<Immutable<?, ?>> valueContainers) {
        return this.blockState.with(valueContainers)
                .map(state -> LocatableBlock.builder()
                        .from(this)
                        .state(state)
                        .build());
    }

    @Override
    public Optional<LocatableBlock> without(Class<? extends Immutable<?, ?>> containerClass) {
        return Optional.empty();
    }

    @Override
    public LocatableBlock merge(LocatableBlock that) {
        return that;
    }

    @Override
    public LocatableBlock merge(LocatableBlock that, MergeFunction function) {
        return that;
    }

    @Override
    public List<Immutable<?, ?>> getContainers() {
        return this.blockState.getContainers();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("blockState", this.blockState)
                .add("position", this.position)
                .add("worldReference", this.worldReference.get() == null ? "null" : this.worldReference.get().getName())
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
