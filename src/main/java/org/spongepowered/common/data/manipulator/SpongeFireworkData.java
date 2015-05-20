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
package org.spongepowered.common.data.manipulator;

import static com.google.common.base.Preconditions.checkArgument;
import static org.spongepowered.api.data.DataQuery.of;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulator.FireworkData;
import org.spongepowered.api.item.FireworkEffect;

public class SpongeFireworkData extends AbstractListData<FireworkEffect, FireworkData> implements FireworkData {

    public static final DataQuery FIREWORK_EFFECTS = of("FireworkEffects");
    public static final DataQuery FLIGHT_MODIFIER = of("FlightModifier");
    private int modifier = 0;

    public SpongeFireworkData() {
        super(FireworkData.class);
    }

    @Override
    public int getFlightModifier() {
        return this.modifier;
    }

    @Override
    public FireworkData setFlightModifier(int flightModifier) {
        checkArgument(flightModifier >= 0);
        this.modifier = flightModifier;
        return this;
    }

    @Override
    public FireworkData copy() {
        return new SpongeFireworkData().set(this.elementList);
    }

    @Override
    public int compareTo(FireworkData o) {
        return 0; //TODO
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(FIREWORK_EFFECTS, this.elementList)
                .set(FLIGHT_MODIFIER, this.modifier);
    }
}
