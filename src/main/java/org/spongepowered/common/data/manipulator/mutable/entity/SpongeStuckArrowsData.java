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
package org.spongepowered.common.data.manipulator.mutable.entity;

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableStuckArrowsData;
import org.spongepowered.api.data.manipulator.mutable.entity.StuckArrowsData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeStuckArrowsData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractIntData;
import org.spongepowered.common.data.value.SpongeValueFactory;

public class SpongeStuckArrowsData extends AbstractIntData<StuckArrowsData, ImmutableStuckArrowsData> implements StuckArrowsData {

    public SpongeStuckArrowsData() {
        this(0);
    }

    public SpongeStuckArrowsData(int arrows) {
        super(StuckArrowsData.class, arrows, Keys.STUCK_ARROWS);
    }

    @Override
    protected Value<?> getValueGetter() {
        return this.stuckArrows();
    }

    @Override
    public MutableBoundedValue<Integer> stuckArrows() {
        return SpongeValueFactory.boundedBuilder(Keys.STUCK_ARROWS)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(0)
                .actualValue(this.getValue())
                .build();
    }

    @Override
    public StuckArrowsData setValue(Integer value) {
        checkArgument(value >= 0, "Stuck arrows must be greater than or equal to zero");
        return super.setValue(value);
    }

    @Override
    public StuckArrowsData copy() {
        return new SpongeStuckArrowsData(this.getValue());
    }

    @Override
    public ImmutableStuckArrowsData asImmutable() {
        return new ImmutableSpongeStuckArrowsData(this.getValue());
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(Keys.STUCK_ARROWS, this.getValue());
    }

}
