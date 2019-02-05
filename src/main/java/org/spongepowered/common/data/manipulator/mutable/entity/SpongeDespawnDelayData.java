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

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableDespawnDelayData;
import org.spongepowered.api.data.manipulator.mutable.DespawnDelayData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeDespawnDelayData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractIntData;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.SpongeMutableValue;

public final class SpongeDespawnDelayData extends AbstractIntData<DespawnDelayData, ImmutableDespawnDelayData> implements DespawnDelayData {

    public SpongeDespawnDelayData() {
        this(DataConstants.Entity.Item.DEFAULT_DESPAWN_DELAY);
    }

    public SpongeDespawnDelayData(int value) {
        super(DespawnDelayData.class, value, Keys.DESPAWN_DELAY);
    }

    public SpongeDespawnDelayData(int value, int minimum, int maximum, int defaultValue) {
        this(value);
    }


    @Override
    public BoundedValue.Mutable<Integer> delay() {
        return SpongeValueFactory.boundedBuilder(Keys.DESPAWN_DELAY) // this.usedKey does not work here
                .value(this.getValue())
                .minimum(DataConstants.Entity.Item.MIN_DESPAWN_DELAY)
                .maximum(DataConstants.Entity.Item.MAX_DESPAWN_DELAY)
                .build();
    }

    @Override
    public boolean supports(Key<?> key) {
        return super.supports(key) || key == Keys.INFINITE_DESPAWN_DELAY;
    }

    @Override
    public Value.Mutable<Boolean> infinite() {
        return new SpongeMutableValue<>(Keys.INFINITE_DESPAWN_DELAY, isInfinite());
    }

    private boolean isInfinite() {
        return this.getValue() == DataConstants.Entity.Item.MAGIC_NO_DESPAWN;
    }

    @Override
    protected Value.Mutable<?> getValueGetter() {
        return this.delay();
    }

    @Override
    public ImmutableDespawnDelayData asImmutable() {
        return new ImmutableSpongeDespawnDelayData(this.getValue());
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.INFINITE_DESPAWN_DELAY, isInfinite());
    }
}
