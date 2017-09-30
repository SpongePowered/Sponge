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
package org.spongepowered.common.data.manipulator.immutable.block;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDelayableData;
import org.spongepowered.api.data.manipulator.mutable.block.DelayableData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableIntData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeDelayableData;

public class ImmutableSpongeDelayableData extends AbstractImmutableIntData<ImmutableDelayableData, DelayableData> implements ImmutableDelayableData {

    public ImmutableSpongeDelayableData(int value) {
        this(value, 1, 4);
    }

    public ImmutableSpongeDelayableData(int value, int lowerBound, int upperBound) {
        this(value, lowerBound, upperBound, 1);
    }

    public ImmutableSpongeDelayableData(int value, int lowerBound, int upperBound, int defaultValue) {
        super(ImmutableDelayableData.class, value, Keys.DELAY, SpongeDelayableData.class, lowerBound, upperBound, defaultValue);
    }

    @Override
    public ImmutableBoundedValue<Integer> delay() {
        return getValueGetter();
    }

    @Override
    public DelayableData asMutable() {
        return new SpongeDelayableData(this.value);
    }
}
