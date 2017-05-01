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
import org.spongepowered.api.data.DataContainer;
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

    private final ImmutableSetValue<Direction> directionsValue;
    private final ImmutableValue<Boolean> northValue;
    private final ImmutableValue<Boolean> southValue;
    private final ImmutableValue<Boolean> eastValue;
    private final ImmutableValue<Boolean> westValue;

    public ImmutableSpongeConnectedDirectionData(Set<Direction> directions) {
        super(ImmutableConnectedDirectionData.class);
        this.directions = ImmutableSet.copyOf(directions);

        this.directionsValue = new ImmutableSpongeSetValue<>(Keys.CONNECTED_DIRECTIONS, this.directions);
        this.northValue = ImmutableSpongeValue.cachedOf(Keys.CONNECTED_NORTH, false, this.directions.contains(Direction.NORTH));
        this.southValue = ImmutableSpongeValue.cachedOf(Keys.CONNECTED_SOUTH, false, this.directions.contains(Direction.SOUTH));
        this.eastValue = ImmutableSpongeValue.cachedOf(Keys.CONNECTED_EAST, false, this.directions.contains(Direction.EAST));
        this.westValue = ImmutableSpongeValue.cachedOf(Keys.CONNECTED_WEST, false, this.directions.contains(Direction.WEST));

        registerGetters();
    }

    @Override
    public ImmutableSetValue<Direction> connectedDirections() {
        return this.directionsValue;
    }

    @Override
    public ImmutableValue<Boolean> connectedNorth() {
        return this.northValue;
    }

    @Override
    public ImmutableValue<Boolean> connectedSouth() {
        return this.southValue;
    }

    @Override
    public ImmutableValue<Boolean> connectedEast() {
        return this.eastValue;
    }

    @Override
    public ImmutableValue<Boolean> connectedWest() {
        return this.westValue;
    }

    @Override
    public ConnectedDirectionData asMutable() {
        return new SpongeConnectedDirectionData(this.directions);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.CONNECTED_DIRECTIONS.getQuery(), this.directions)
            .set(Keys.CONNECTED_NORTH.getQuery(), this.directions.contains(Direction.NORTH))
            .set(Keys.CONNECTED_SOUTH.getQuery(), this.directions.contains(Direction.SOUTH))
            .set(Keys.CONNECTED_EAST.getQuery(), this.directions.contains(Direction.EAST))
            .set(Keys.CONNECTED_WEST.getQuery(), this.directions.contains(Direction.WEST));
    }

    private Set<Direction> getDirections() {
        return this.directions;
    }

    private boolean isNorth() {
        return this.directions.contains(Direction.NORTH);
    }

    private boolean isSouth() {
        return this.directions.contains(Direction.SOUTH);
    }

    private boolean isEast() {
        return this.directions.contains(Direction.EAST);
    }

    private boolean isWest() {
        return this.directions.contains(Direction.WEST);
    }

    @Override
    protected void registerGetters() {
        registerKeyValue(Keys.CONNECTED_DIRECTIONS, ImmutableSpongeConnectedDirectionData.this::connectedDirections);
        registerKeyValue(Keys.CONNECTED_NORTH, ImmutableSpongeConnectedDirectionData.this::connectedNorth);
        registerKeyValue(Keys.CONNECTED_SOUTH, ImmutableSpongeConnectedDirectionData.this::connectedSouth);
        registerKeyValue(Keys.CONNECTED_EAST, ImmutableSpongeConnectedDirectionData.this::connectedEast);
        registerKeyValue(Keys.CONNECTED_WEST, ImmutableSpongeConnectedDirectionData.this::connectedWest);

        registerFieldGetter(Keys.CONNECTED_DIRECTIONS, ImmutableSpongeConnectedDirectionData.this::getDirections);
        registerFieldGetter(Keys.CONNECTED_NORTH, ImmutableSpongeConnectedDirectionData.this::isNorth);
        registerFieldGetter(Keys.CONNECTED_SOUTH, ImmutableSpongeConnectedDirectionData.this::isSouth);
        registerFieldGetter(Keys.CONNECTED_EAST, ImmutableSpongeConnectedDirectionData.this::isEast);
        registerFieldGetter(Keys.CONNECTED_WEST, ImmutableSpongeConnectedDirectionData.this::isWest);
    }
}
