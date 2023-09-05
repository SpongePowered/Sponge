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
package org.spongepowered.common.test.stub.block;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.state.StateProperty;
import org.spongepowered.api.util.Cycleable;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.mirror.Mirror;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.common.data.holder.SpongeImmutableDataHolder;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public final class StubState implements BlockState, SpongeImmutableDataHolder<BlockState> {

    private final BlockType mocked;
    public final ResourceKey key;
    public final Vector3i deducedPos;

    public StubState(final BlockType mocked, final ResourceKey key, final Vector3i deducedPos) {
        this.mocked = mocked;
        this.key = key;
        this.deducedPos = deducedPos;
    }

    @Override
    public BlockType type() {
        return this.type();
    }

    @Override
    public FluidState fluidState() {
        return null;
    }

    @Override
    public BlockSnapshot snapshotFor(final ServerLocation location) {
        return null;
    }

    @Override
    public BlockState rotate(final Rotation rotation) {
        return this;
    }

    @Override
    public BlockState mirror(final Mirror mirror) {
        return this;
    }

    @Override
    public String asString() {
        return null;
    }

    @Override
    public <E> Optional<E> get(
        final Direction direction, final Key<? extends Value<E>> key
    ) {
        return Optional.empty();
    }

    @Override
    public DataContainer rawData() {
        return null;
    }

    @Override
    public boolean validateRawData(final DataView container) {
        return false;
    }

    @Override
    public BlockState withRawData(final DataView container) throws InvalidDataException {
        return this;
    }

    @Override
    public BlockState copy() {
        return this;
    }

    @Override
    public int contentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return null;
    }

    @Override
    public <T extends Comparable<T>> Optional<T> stateProperty(final StateProperty<T> stateProperty) {
        return Optional.empty();
    }

    @Override
    public Optional<StateProperty<?>> findStateProperty(final String name) {
        return Optional.empty();
    }

    @Override
    public <T extends Comparable<T>, V extends T> Optional<BlockState> withStateProperty(
        final StateProperty<T> stateProperty, final V value
    ) {
        return Optional.empty();
    }

    @Override
    public <T extends Comparable<T>> Optional<BlockState> cycleStateProperty(
        final StateProperty<T> stateProperty
    ) {
        return Optional.empty();
    }

    @Override
    public <T extends Cycleable<T>> Optional<BlockState> cycleValue(
        final Key<? extends Value<T>> key
    ) {
        return Optional.empty();
    }

    @Override
    public Collection<StateProperty<?>> stateProperties() {
        return Collections.emptySet();
    }

    @Override
    public Collection<?> statePropertyValues() {
        return Collections.emptySet();
    }

    @Override
    public Map<StateProperty<?>, ?> statePropertyMap() {
        return Collections.emptyMap();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", StubState.class.getSimpleName() + "[", "]")
            .add("mocked=" + this.mocked)
            .add("key=" + this.key)
            .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final StubState stubState = (StubState) o;
        return this.mocked.equals(stubState.mocked) && this.key.equals(stubState.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.mocked, this.key);
    }
}
