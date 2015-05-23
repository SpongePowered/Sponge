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
package org.spongepowered.common.data.manipulator.entity;

import static org.spongepowered.api.data.DataQuery.of;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulator.entity.RespawnLocationData;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.data.manipulator.AbstractSingleValueData;

public class SpongeRespawnLocationData extends AbstractSingleValueData<Location, RespawnLocationData> implements RespawnLocationData {

    public static final DataQuery RESPAWN_LOCATION = of("RespawnLocation");

    public SpongeRespawnLocationData(Location defaultLocation) {
        super(RespawnLocationData.class, defaultLocation);
    }

    @Override
    public Location getRespawnLocation() {
        return this.getValue();
    }

    @Override
    public RespawnLocationData setRespawnLocation(Location location) {
        return this.setValue(location);
    }

    @Override
    public RespawnLocationData copy() {
        return new SpongeRespawnLocationData(this.getValue());
    }

    @Override
    public int compareTo(RespawnLocationData o) {
        return o.getValue().getPosition().compareTo(this.getValue().getPosition());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(RESPAWN_LOCATION, this.getValue());
    }
}
