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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePickupDelayData;
import org.spongepowered.api.data.manipulator.mutable.entity.PickupDelayData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableIntData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongePickupDelayData;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public final class ImmutableSpongePickupDelayData extends AbstractImmutableIntData<ImmutablePickupDelayData, PickupDelayData>
        implements ImmutablePickupDelayData {

    private final ImmutableValue<Boolean> infinite;

    public ImmutableSpongePickupDelayData(int value) {
        super(ImmutablePickupDelayData.class, value, Keys.PICKUP_DELAY, SpongePickupDelayData.class, DataConstants.Entity.Item.MIN_PICKUP_DELAY,
                DataConstants.Entity.Item.MAX_PICKUP_DELAY, DataConstants.Entity.Item.DEFAULT_PICKUP_DELAY);
        this.infinite = ImmutableSpongeValue.cachedOf(Keys.INFINITE_PICKUP_DELAY, false, this.getValue() == DataConstants.Entity.Item.MAGIC_NO_PICKUP);
    }

    @Override
    public ImmutableBoundedValue<Integer> delay() {
        return this.getValueGetter();
    }

    @Override
    public ImmutableValue<Boolean> infinite() {
        return this.infinite;
    }

}
