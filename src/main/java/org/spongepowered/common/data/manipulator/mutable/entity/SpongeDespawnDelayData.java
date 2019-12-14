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

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableDespawnDelayData;
import org.spongepowered.api.data.manipulator.mutable.entity.DespawnDelayData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.value.Value.Mutable;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeDespawnDelayData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractIntData;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public final class SpongeDespawnDelayData extends AbstractData<DespawnDelayData, ImmutableDespawnDelayData> implements DespawnDelayData {

    private int value;

    public SpongeDespawnDelayData() {
        this(Constants.Entity.Item.DEFAULT_DESPAWN_DELAY);
    }

    public SpongeDespawnDelayData(int value) {
        super(DespawnDelayData.class);
        this.value = value;
        this.registerGettersAndSetters();
    }

    public SpongeDespawnDelayData(int value, int minimum, int maximum, int defaultValue) {
        this(value);
    }

    @Override
    protected void registerGettersAndSetters() {
        this.registerFieldGetter(Keys.INFINITE_DESPAWN_DELAY, this::isInfinite);
        this.registerFieldSetter(Keys.INFINITE_DESPAWN_DELAY, (value) -> this.value = value ? Constants.Entity.Item.MAGIC_NO_DESPAWN : this.value);
        this.registerKeyValue(Keys.INFINITE_DESPAWN_DELAY, this::infinite);

        this.registerFieldGetter(Keys.DESPAWN_DELAY, this::getDelay);
        this.registerFieldSetter(Keys.DESPAWN_DELAY, (value) -> this.value = value);
        this.registerKeyValue(Keys.DESPAWN_DELAY, this::delay);
    }

    @Override
    public Mutable<Boolean> infinite() {
        return new SpongeValue<>(Keys.INFINITE_DESPAWN_DELAY, false, this.isInfinite());
    }

    private boolean isInfinite() {
        return this.value == Constants.Entity.Item.MAGIC_NO_DESPAWN;
    }

    @Override
    public org.spongepowered.api.data.value.BoundedValue.Mutable<Integer> delay() {
        return SpongeValueFactory.boundedBuilder(Keys.DESPAWN_DELAY) // this.usedKey does not work here
                .actualValue(this.value)
                .minimum(Constants.Entity.Item.MIN_DESPAWN_DELAY)
                .maximum(Constants.Entity.Item.MAX_DESPAWN_DELAY)
                .defaultValue(Constants.Entity.Item.DEFAULT_DESPAWN_DELAY)
                .build();
    }

    private int getDelay() {
        return this.value;
    }

    @Override
    public DespawnDelayData copy() {
        return new SpongeDespawnDelayData(this.value);
    }

    @Override
    public ImmutableDespawnDelayData asImmutable() {
        return new ImmutableSpongeDespawnDelayData(this.value);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.DESPAWN_DELAY, this.value)
                .set(Keys.INFINITE_DESPAWN_DELAY, this.isInfinite());
    }
}
