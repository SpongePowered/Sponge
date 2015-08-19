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
package org.spongepowered.common.data.manipulator.mutable.block;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableConnectedDirectionData;
import org.spongepowered.api.data.manipulator.mutable.block.ConnectedDirectionData;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeConnectedDirectionData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeSetValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.EnumSet;
import java.util.Set;

public class SpongeConnectedDirectionData extends AbstractData<ConnectedDirectionData, ImmutableConnectedDirectionData> implements ConnectedDirectionData {

    private EnumSet<Direction> connectedDirections = EnumSet.noneOf(Direction.class);

    public SpongeConnectedDirectionData(Set<Direction> connectedDirections) {
        super(ConnectedDirectionData.class);
        registerGettersAndSetters();
        this.connectedDirections = EnumSet.copyOf(connectedDirections);
    }

    @Override
    public SetValue<Direction> connectedDirections() {
        return new SpongeSetValue<Direction>(Keys.CONNECTED_DIRECTIONS, this.connectedDirections);
    }

    @Override
    public Value<Boolean> connectedNorth() {
        return new SpongeValue<Boolean>(Keys.CONNECTED_NORTH, this.connectedDirections.contains(Direction.NORTH));
    }

    @Override
    public Value<Boolean> connectedSouth() {
        return new SpongeValue<Boolean>(Keys.CONNECTED_SOUTH, this.connectedDirections.contains(Direction.SOUTH));
    }

    @Override
    public Value<Boolean> connectedEast() {
        return new SpongeValue<Boolean>(Keys.CONNECTED_EAST, this.connectedDirections.contains(Direction.EAST));
    }

    @Override
    public Value<Boolean> connectedWest() {
        return new SpongeValue<Boolean>(Keys.CONNECTED_WEST, this.connectedDirections.contains(Direction.WEST));
    }

    @Override
    public ConnectedDirectionData copy() {
        return new SpongeConnectedDirectionData(this.connectedDirections);
    }

    @Override
    public ImmutableConnectedDirectionData asImmutable() {
        return new ImmutableSpongeConnectedDirectionData(this.connectedDirections);
    }

    @Override
    public int compareTo(ConnectedDirectionData o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer(); // todo
    }

    @Override
    protected void registerGettersAndSetters() {
        // TODO
    }
}
