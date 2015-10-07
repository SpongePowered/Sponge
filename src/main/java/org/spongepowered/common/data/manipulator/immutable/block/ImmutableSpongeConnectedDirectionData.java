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
package org.spongepowered.common.data.manipulator.immutable.block;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableConnectedDirectionData;
import org.spongepowered.api.data.manipulator.mutable.block.ConnectedDirectionData;
import org.spongepowered.api.data.value.immutable.ImmutableSetValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeConnectedDirectionData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeSetValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Set;

public class ImmutableSpongeConnectedDirectionData extends AbstractImmutableData<ImmutableConnectedDirectionData, ConnectedDirectionData>  implements ImmutableConnectedDirectionData {

    private final ImmutableSet<Direction> directions;

    public ImmutableSpongeConnectedDirectionData(Set<Direction> directions) {
        super(ImmutableConnectedDirectionData.class);
        this.directions = Sets.immutableEnumSet(directions);
        registerGetters();
    }

    @Override
    public ImmutableSetValue<Direction> connectedDirections() {
        return new ImmutableSpongeSetValue<>(Keys.CONNECTED_DIRECTIONS, this.directions);
    }

    @Override
    public ImmutableValue<Boolean> conntectedNorth() {
        return ImmutableSpongeValue.cachedOf(Keys.CONNECTED_NORTH, false, this.directions.contains(Direction.NORTH));
    }

    @Override
    public ImmutableValue<Boolean> connectedSouth() {
        return ImmutableSpongeValue.cachedOf(Keys.CONNECTED_SOUTH, false, this.directions.contains(Direction.SOUTH));
    }

    @Override
    public ImmutableValue<Boolean> connectedEast() {
        return ImmutableSpongeValue.cachedOf(Keys.CONNECTED_EAST, false, this.directions.contains(Direction.EAST));
    }

    @Override
    public ImmutableValue<Boolean> connectedWest() {
        return ImmutableSpongeValue.cachedOf(Keys.CONNECTED_WEST, false, this.directions.contains(Direction.WEST));
    }

    @Override
    public ConnectedDirectionData asMutable() {
        return new SpongeConnectedDirectionData(this.directions);
    }

    @Override
    public int compareTo(ImmutableConnectedDirectionData o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
            .set(Keys.CONNECTED_DIRECTIONS.getQuery(), this.directions)
            .set(Keys.CONNECTED_NORTH.getQuery(), this.directions.contains(Direction.NORTH))
            .set(Keys.CONNECTED_SOUTH.getQuery(), this.directions.contains(Direction.SOUTH))
            .set(Keys.CONNECTED_EAST.getQuery(), this.directions.contains(Direction.EAST))
            .set(Keys.CONNECTED_WEST.getQuery(), this.directions.contains(Direction.WEST));
    }

    @Override
    protected void registerGetters() {
        // TODO
    }
}
