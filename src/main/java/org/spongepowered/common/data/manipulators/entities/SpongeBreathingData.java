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
package org.spongepowered.common.data.manipulators.entities;

import static org.spongepowered.api.data.DataQuery.of;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.entities.BreathingData;
import org.spongepowered.common.data.manipulators.AbstractIntData;

public class SpongeBreathingData extends AbstractIntData<BreathingData> implements BreathingData {

    public SpongeBreathingData(int max) {
        super(BreathingData.class, 0, 0, max);
    }

    @Override
    public int getRemainingAir() {
        return this.getValue();
    }

    @Override
    public BreathingData setRemainingAir(int air) {
        return setValue(air);
    }

    @Override
    public int getMaxAir() {
        return this.getMaxValue();
    }

    @Override
    public BreathingData setMaxAir(int air) {
        return new SpongeBreathingData(air).setValue(this.getValue());
    }

    @Override
    public BreathingData copy() {
        return new SpongeBreathingData(this.getMaxValue()).setValue(this.getValue());
    }

    @Override
    public int compareTo(BreathingData o) {
        return (o.getMaxAir() - this.getMaxAir()) - (o.getValue() - this.getValue());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(of("RemainingAir"), this.getValue())
                .set(of("MaxAir"), this.getMaxValue());
    }
}
