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

import static org.spongepowered.common.data.util.ComparatorUtil.intComparator;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExpOrbData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExpOrbData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExpOrbData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;

public class ImmutableSpongeExpOrbData extends AbstractImmutableSingleData<Integer, ImmutableExpOrbData, ExpOrbData> implements ImmutableExpOrbData {

    final ImmutableBoundedValue<Integer> cachedValue;

    public ImmutableSpongeExpOrbData(Integer value) {
        super(ImmutableExpOrbData.class, value, Keys.CONTAINED_EXPERIENCE);
        this.cachedValue = new ImmutableSpongeBoundedValue<>(Keys.CONTAINED_EXPERIENCE, this.value, 0, intComparator(), 0, Integer.MAX_VALUE);
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return experience();
    }

    @Override
    public ExpOrbData asMutable() {
        return new SpongeExpOrbData(getValue());
    }

    @Override
    public ImmutableValue<Integer> experience() {
        return this.cachedValue;
    }

    @Override
    public int compareTo(ImmutableExpOrbData o) {
        return o.experience().get().compareTo(this.value);
    }
}
