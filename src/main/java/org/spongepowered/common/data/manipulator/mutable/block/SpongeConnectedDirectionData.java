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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Sets;
import org.spongepowered.api.data.DataContainer;
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

import java.util.Set;

public class SpongeConnectedDirectionData extends AbstractData<ConnectedDirectionData, ImmutableConnectedDirectionData>
        implements ConnectedDirectionData {

    private Set<Direction> connectedDirections = Sets.newHashSet();

    public SpongeConnectedDirectionData(Set<Direction> connectedDirections) {
        super(ConnectedDirectionData.class);
        registerGettersAndSetters();
        this.connectedDirections = Sets.newHashSet(connectedDirections);
    }

    public SpongeConnectedDirectionData() {
        this(Sets.newHashSet());
    }

    @Override
    public SetValue<Direction> connectedDirections() {
        return new SpongeSetValue<>(Keys.CONNECTED_DIRECTIONS, this.connectedDirections);
    }

    @Override
    public Value<Boolean> connectedNorth() {
        return new SpongeValue<>(Keys.CONNECTED_NORTH, this.connectedDirections.contains(Direction.NORTH));
    }

    @Override
    public Value<Boolean> connectedSouth() {
        return new SpongeValue<>(Keys.CONNECTED_SOUTH, this.connectedDirections.contains(Direction.SOUTH));
    }

    @Override
    public Value<Boolean> connectedEast() {
        return new SpongeValue<>(Keys.CONNECTED_EAST, this.connectedDirections.contains(Direction.EAST));
    }

    @Override
    public Value<Boolean> connectedWest() {
        return new SpongeValue<>(Keys.CONNECTED_WEST, this.connectedDirections.contains(Direction.WEST));
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
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.CONNECTED_DIRECTIONS.getQuery(), this.connectedDirections)
                .set(Keys.CONNECTED_NORTH.getQuery(), this.connectedDirections.contains(Direction.NORTH))
                .set(Keys.CONNECTED_SOUTH.getQuery(), this.connectedDirections.contains(Direction.SOUTH))
                .set(Keys.CONNECTED_EAST.getQuery(), this.connectedDirections.contains(Direction.EAST))
                .set(Keys.CONNECTED_WEST.getQuery(), this.connectedDirections.contains(Direction.WEST));
    }

    private Set<Direction> getDirections() {
        return this.connectedDirections;
    }

    private boolean isNorth() {
        return this.connectedDirections.contains(Direction.NORTH);
    }

    private boolean isSouth() {
        return this.connectedDirections.contains(Direction.SOUTH);
    }

    private boolean isEast() {
        return this.connectedDirections.contains(Direction.EAST);
    }

    private boolean isWest() {
        return this.connectedDirections.contains(Direction.WEST);
    }

    private void setDirections(Set<Direction> directions) {
        this.connectedDirections = Sets.newHashSet(directions);
    }

    private void setNorth(Boolean north) {
        if (checkNotNull(north)) {
            this.connectedDirections.add(Direction.NORTH);
        } else {
            this.connectedDirections.remove(Direction.NORTH);
        }
    }

    private void setSouth(Boolean south) {
        if (checkNotNull(south)) {
            this.connectedDirections.add(Direction.SOUTH);
        } else {
            this.connectedDirections.remove(Direction.SOUTH);
        }
    }

    private void setEast(Boolean east) {
        if (checkNotNull(east)) {
            this.connectedDirections.add(Direction.EAST);
        } else {
            this.connectedDirections.remove(Direction.EAST);
        }
    }

    private void setWest(Boolean west) {
        if (checkNotNull(west)) {
            this.connectedDirections.add(Direction.WEST);
        } else {
            this.connectedDirections.remove(Direction.WEST);
        }
    }

    @Override
    protected void registerGettersAndSetters() {
        registerKeyValue(Keys.CONNECTED_DIRECTIONS, SpongeConnectedDirectionData.this::connectedDirections);
        registerKeyValue(Keys.CONNECTED_NORTH, SpongeConnectedDirectionData.this::connectedNorth);
        registerKeyValue(Keys.CONNECTED_SOUTH, SpongeConnectedDirectionData.this::connectedSouth);
        registerKeyValue(Keys.CONNECTED_EAST, SpongeConnectedDirectionData.this::connectedEast);
        registerKeyValue(Keys.CONNECTED_WEST, SpongeConnectedDirectionData.this::connectedWest);

        registerFieldGetter(Keys.CONNECTED_DIRECTIONS, SpongeConnectedDirectionData.this::getDirections);
        registerFieldGetter(Keys.CONNECTED_NORTH, SpongeConnectedDirectionData.this::isNorth);
        registerFieldGetter(Keys.CONNECTED_SOUTH, SpongeConnectedDirectionData.this::isSouth);
        registerFieldGetter(Keys.CONNECTED_EAST, SpongeConnectedDirectionData.this::isEast);
        registerFieldGetter(Keys.CONNECTED_WEST, SpongeConnectedDirectionData.this::isWest);

        registerFieldSetter(Keys.CONNECTED_DIRECTIONS, SpongeConnectedDirectionData.this::setDirections);
        registerFieldSetter(Keys.CONNECTED_NORTH, SpongeConnectedDirectionData.this::setNorth);
        registerFieldSetter(Keys.CONNECTED_SOUTH, SpongeConnectedDirectionData.this::setSouth);
        registerFieldSetter(Keys.CONNECTED_EAST, SpongeConnectedDirectionData.this::setEast);
        registerFieldSetter(Keys.CONNECTED_WEST, SpongeConnectedDirectionData.this::setWest);
    }
}
