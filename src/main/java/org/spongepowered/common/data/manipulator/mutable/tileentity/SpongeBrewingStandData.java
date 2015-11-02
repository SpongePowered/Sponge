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
package org.spongepowered.common.data.manipulator.mutable.tileentity;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBrewingStandData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BrewingStandData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeBrewingStandData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.common.data.value.SpongeValueBuilder;

public class SpongeBrewingStandData extends AbstractSingleData<Integer, BrewingStandData, ImmutableBrewingStandData> implements BrewingStandData {

    public SpongeBrewingStandData(Integer value) {
        super(BrewingStandData.class, value, Keys.REMAINING_BREW_TIME);
    }

    public SpongeBrewingStandData() {
        this(0);
    }

    @Override
    protected Value<?> getValueGetter() {
        return this.remainingBrewTime();
    }

    @Override
    public BrewingStandData copy() {
        return new SpongeBrewingStandData(this.getValue());
    }

    @Override
    public ImmutableBrewingStandData asImmutable() {
        return new ImmutableSpongeBrewingStandData(this.getValue());
    }

    @Override
    public int compareTo(BrewingStandData o) {
        return ComparisonChain
                .start()
                .compare(o.remainingBrewTime().get(), this.getValue())
                .result();
    }

    @Override
    public MutableBoundedValue<Integer> remainingBrewTime() {
        return SpongeValueBuilder.boundedBuilder(Keys.REMAINING_BREW_TIME)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(400)
                .actualValue(this.getValue())
                .build();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Keys.REMAINING_BREW_TIME, this.getValue());
    }
}
