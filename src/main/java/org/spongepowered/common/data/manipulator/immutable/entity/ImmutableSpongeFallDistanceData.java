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
package org.spongepowered.common.data.manipulator.immutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFallDistanceData;
import org.spongepowered.api.data.manipulator.mutable.entity.FallDistanceData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableBoundedComparableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFallDistanceData;

public class ImmutableSpongeFallDistanceData extends AbstractImmutableBoundedComparableData<Float, ImmutableFallDistanceData, FallDistanceData>
        implements ImmutableFallDistanceData {

    public ImmutableSpongeFallDistanceData() {
        this(0);
    }

    public ImmutableSpongeFallDistanceData(float fallDistance) {
        this(fallDistance, 0F, Float.MAX_VALUE, 0F);
    }

    public ImmutableSpongeFallDistanceData(float distance, float minimum, float maximum, float defaultValue) {
        super(ImmutableFallDistanceData.class, distance, Keys.FALL_DISTANCE, Float::compare, SpongeFallDistanceData.class, minimum, maximum, defaultValue);
    }


    @Override
    public ImmutableBoundedValue<Float> fallDistance() {
        return this.getValueGetter();
    }

    @Override
    public FallDistanceData asMutable() {
        return new SpongeFallDistanceData(this.value, this.lowerBound, this.upperBound);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.FALL_DISTANCE, this.value);
    }
}
