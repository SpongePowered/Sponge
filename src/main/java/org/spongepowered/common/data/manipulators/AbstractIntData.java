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
package org.spongepowered.common.data.manipulators;

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.data.manipulators.IntData;

public abstract class AbstractIntData<T extends IntData<T>> extends AbstractSingleValueData<Integer, T> implements IntData<T> {

    private final int minValue;
    private final int maxValue;

    protected AbstractIntData(Class<T> manipulatorClass, Integer defaultValue, int minValue, int maxValue) {
        super(manipulatorClass, defaultValue);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public T setValue(Integer value) {
        checkArgument(value >= this.getMinValue(), "Must be greater than the min value!");
        checkArgument(value <= this.getMaxValue(), "Must be less than the max value!");
        return super.setValue(value);
    }

    @Override
    public Integer getMinValue() {
        return this.minValue;
    }

    @Override
    public Integer getMaxValue() {
        return this.maxValue;
    }

    @Override
    public int compareTo(T o) {
        return o.getValue() - this.getValue();
    }

}
