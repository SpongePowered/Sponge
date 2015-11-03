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
package org.spongepowered.common.data.manipulator.immutable;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableCooldownData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.CooldownData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeCooldownData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeCooldownData extends AbstractImmutableSingleData<Integer, ImmutableCooldownData, CooldownData>
        implements ImmutableCooldownData {

    private final ImmutableValue<Integer> cooldown;

    public ImmutableSpongeCooldownData(int value) {
        super(ImmutableCooldownData.class, value, Keys.COOLDOWN);
        this.cooldown = ImmutableSpongeValue.cachedOf(Keys.COOLDOWN, -1, value);
    }

    public ImmutableSpongeCooldownData() {
        this(-1);
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return cooldown();
    }

    @Override
    public CooldownData asMutable() {
        return new SpongeCooldownData(this.getValue());
    }

    @Override
    public ImmutableValue<Integer> cooldown() {
        return this.cooldown;
    }

    @Override
    public int compareTo(ImmutableCooldownData o) {
        return ComparisonChain.start()
                .compare(this.getValue(), o.cooldown().get())
                .result();
    }
}
