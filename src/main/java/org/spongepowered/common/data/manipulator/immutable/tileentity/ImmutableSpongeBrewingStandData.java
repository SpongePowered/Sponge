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
package org.spongepowered.common.data.manipulator.immutable.tileentity;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBrewingStandData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BrewingStandData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBrewingStandData;
import org.spongepowered.common.data.value.SpongeValueBuilder;

public class ImmutableSpongeBrewingStandData extends AbstractImmutableSingleData<Integer, ImmutableBrewingStandData, BrewingStandData> implements
        ImmutableBrewingStandData {

    public ImmutableSpongeBrewingStandData(Integer value) {
        super(ImmutableBrewingStandData.class, value, Keys.REMAINING_BREW_TIME);
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return this.remainingBrewTime();
    }

    @Override
    public BrewingStandData asMutable() {
        return new SpongeBrewingStandData(this.getValue());
    }

    @Override
    public ImmutableBoundedValue<Integer> remainingBrewTime() {
        return SpongeValueBuilder.boundedBuilder(Keys.REMAINING_BREW_TIME)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(400)
                .actualValue(this.getValue())
                .build()
                .asImmutable();
    }

    @Override
    public int compareTo(ImmutableBrewingStandData o) {
        return ComparisonChain
                .start()
                .compare(o.remainingBrewTime().get(), this.getValue())
                .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Keys.REMAINING_BREW_TIME, this.getValue());
    }
}
