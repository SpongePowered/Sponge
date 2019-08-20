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

import static org.spongepowered.common.util.Constants.Functional.intComparator;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableMoistureData;
import org.spongepowered.api.data.manipulator.mutable.block.MoistureData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeMoistureData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractBoundedComparableData;

public class SpongeMoistureData extends AbstractBoundedComparableData<Integer, MoistureData, ImmutableMoistureData> implements MoistureData {

    public SpongeMoistureData() {
        this(0);
    }

    public SpongeMoistureData(int value) {
        this(value, 0, Integer.MAX_VALUE);
    }

    public SpongeMoistureData(int value, int lowerBound, int upperBound) {
        this(value, lowerBound, upperBound, 0);
    }

    public SpongeMoistureData(int value, int lowerBound, int upperBound, int defaultValue) {
        super(MoistureData.class, value, Keys.MOISTURE, intComparator(), ImmutableSpongeMoistureData.class, lowerBound, upperBound, defaultValue);
    }

    @Override
    public MutableBoundedValue<Integer> moisture() {
        return getValueGetter();
    }
}
