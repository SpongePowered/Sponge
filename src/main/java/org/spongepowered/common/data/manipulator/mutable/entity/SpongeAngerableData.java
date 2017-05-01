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

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAngerableData;
import org.spongepowered.api.data.manipulator.mutable.entity.AngerableData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeAngerableData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractIntData;
import org.spongepowered.common.data.value.SpongeValueFactory;

public class SpongeAngerableData extends AbstractIntData<AngerableData, ImmutableAngerableData> implements AngerableData {

    /* Non-Javadoc Required for tests */
    public SpongeAngerableData(int value, int lowerBound, int upperBound, int defaultValue) {
        this(value);
    }

    public SpongeAngerableData(int value) {
        super(AngerableData.class, value, Keys.ANGER);
    }

    public SpongeAngerableData() {
        this(0);
    }

    @Override
    public MutableBoundedValue<Integer> angerLevel() {
        return SpongeValueFactory.boundedBuilder(Keys.ANGER)
                .actualValue(getValue())
                .defaultValue(0)
                .minimum(Integer.MIN_VALUE)
                .maximum(Integer.MAX_VALUE)
                .build();
    }

    @Override
    protected Value<?> getValueGetter() {
        return angerLevel();
    }

    @Override
    public ImmutableAngerableData asImmutable() {
        return new ImmutableSpongeAngerableData(getValue());
    }

}
