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

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePickupDelayData;
import org.spongepowered.api.data.manipulator.mutable.entity.PickupDelayData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongePickupDelayData;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.util.Constants;

public final class ImmutableSpongePickupDelayData extends AbstractImmutableData<ImmutablePickupDelayData, PickupDelayData>
        implements ImmutablePickupDelayData {

    private final int value;

    public ImmutableSpongePickupDelayData(int value) {
        super(ImmutablePickupDelayData.class);
        this.value = value;
        registerGetters();
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.INFINITE_PICKUP_DELAY, this::isInifinitePickup);
        registerKeyValue(Keys.INFINITE_PICKUP_DELAY, this::infinite);

        registerFieldGetter(Keys.PICKUP_DELAY, this::getDelay);
        registerKeyValue(Keys.PICKUP_DELAY, this::delay);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.PICKUP_DELAY, this.value)
                .set(Keys.INFINITE_PICKUP_DELAY, this.isInifinitePickup());
    }

    @Override
    public ImmutableBoundedValue<Integer> delay() {
        return SpongeValueFactory.boundedBuilder(Keys.PICKUP_DELAY) // this.usedKey does not work here
                .actualValue(this.value)
                .minimum(Constants.Entity.Item.MIN_PICKUP_DELAY)
                .maximum(Constants.Entity.Item.MAX_PICKUP_DELAY)
                .defaultValue(Constants.Entity.Item.DEFAULT_PICKUP_DELAY)
                .build()
                .asImmutable();
    }

    private int getDelay() {
        return this.value;
    }

    @Override
    public ImmutableValue<Boolean> infinite() {
        return new ImmutableSpongeValue<>(Keys.INFINITE_PICKUP_DELAY, false, isInifinitePickup());
    }

    private boolean isInifinitePickup() {
        return this.value == Constants.Entity.Item.MAGIC_NO_PICKUP;
    }

    @Override
    public PickupDelayData asMutable() {
        return new SpongePickupDelayData(this.value);
    }
}
