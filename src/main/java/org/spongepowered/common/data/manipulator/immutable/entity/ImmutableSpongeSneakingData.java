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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSneakingData;
import org.spongepowered.api.data.manipulator.mutable.entity.SneakingData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSneakingData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeSneakingData extends AbstractImmutableSingleData<Boolean, ImmutableSneakingData, SneakingData> implements ImmutableSneakingData {

    public ImmutableSpongeSneakingData(boolean sneaking) {
        super(ImmutableSneakingData.class, sneaking, Keys.IS_SNEAKING);
    }

    @Override
    public SneakingData asMutable() {
        return new SpongeSneakingData(this.value.booleanValue());
    }

    @Override
    public int compareTo(ImmutableSneakingData o) {
        if (value.booleanValue() && !o.sneaking().get().booleanValue()) return 1;
        if (value.booleanValue() && o.sneaking().get().booleanValue()) return -1;
        return 0;
    }

    @Override
    public ImmutableValue<Boolean> sneaking() {
        return new ImmutableSpongeValue<Boolean>(Keys.IS_SNEAKING, false, this.value);
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return sneaking();
    }
}
