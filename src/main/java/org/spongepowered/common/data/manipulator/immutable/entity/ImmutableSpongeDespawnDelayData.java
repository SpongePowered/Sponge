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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableDespawnDelayData;
import org.spongepowered.api.data.manipulator.mutable.entity.DespawnDelayData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeDespawnDelayData;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.util.Constants;

public final class ImmutableSpongeDespawnDelayData extends AbstractImmutableData<ImmutableDespawnDelayData, DespawnDelayData>
        implements ImmutableDespawnDelayData {

    private final int value;

    public ImmutableSpongeDespawnDelayData(int value) {
        super(ImmutableDespawnDelayData.class);
        this.value = value;
        registerGetters();
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.INFINITE_DESPAWN_DELAY, this::isInfinite);
        registerKeyValue(Keys.INFINITE_DESPAWN_DELAY, this::infinite);

        registerFieldGetter(Keys.DESPAWN_DELAY, this::getDelay);
        registerKeyValue(Keys.DESPAWN_DELAY, this::delay);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.DESPAWN_DELAY, this.value)
                .set(Keys.INFINITE_DESPAWN_DELAY, this.isInfinite());
    }

    @Override
    public ImmutableValue<Boolean> infinite() {
        return new ImmutableSpongeValue<>(Keys.INFINITE_DESPAWN_DELAY, false, isInfinite());
    }

    private boolean isInfinite() {
        return this.value == Constants.Entity.Item.MAGIC_NO_DESPAWN;
    }

    @Override
    public ImmutableBoundedValue<Integer> delay() {
        return SpongeValueFactory.boundedBuilder(Keys.DESPAWN_DELAY)
                .actualValue(this.value)
                .minimum(Constants.Entity.Item.MIN_DESPAWN_DELAY)
                .maximum(Constants.Entity.Item.MAX_DESPAWN_DELAY)
                .defaultValue(Constants.Entity.Item.DEFAULT_DESPAWN_DELAY)
                .build()
                .asImmutable();
    }

    private int getDelay() {
        return this.value;
    }

    @Override
    public DespawnDelayData asMutable() {
        return new SpongeDespawnDelayData(this.value);
    }
}
