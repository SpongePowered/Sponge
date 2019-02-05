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
import org.spongepowered.api.data.manipulator.immutable.ImmutableHealthData;
import org.spongepowered.api.data.manipulator.mutable.HealthData;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHealthData;
import org.spongepowered.common.data.value.SpongeValueFactory;

public class ImmutableSpongeHealthData extends AbstractImmutableData<ImmutableHealthData, HealthData> implements ImmutableHealthData {

    private final double health;
    private final double maxHealth;

    private final BoundedValue.Immutable<Double> healthValue;
    private final BoundedValue.Immutable<Double> maxHealthValue;

    public ImmutableSpongeHealthData(double health, double maxHealth) {
        super(ImmutableHealthData.class);
        this.health = health;
        this.maxHealth = maxHealth;

        this.healthValue = SpongeValueFactory.boundedBuilder(Keys.HEALTH)
                .value(this.health)
                .minimum(0D)
                .maximum(this.maxHealth)
                .build()
                .asImmutable();

        this.maxHealthValue = SpongeValueFactory.boundedBuilder(Keys.MAX_HEALTH)
                .value(this.maxHealth)
                .minimum(0D)
                .maximum((double) Float.MAX_VALUE)
                .build()
                .asImmutable();

        registerGetters();
    }

    @Override
    public BoundedValue.Immutable<Double> health() {
        return this.healthValue;
    }

    @Override
    public BoundedValue.Immutable<Double> maxHealth() {
        return this.maxHealthValue;
    }

    @Override
    public HealthData asMutable() {
        return new SpongeHealthData(this.health, this.maxHealth);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.HEALTH.getQuery(), this.health)
            .set(Keys.MAX_HEALTH.getQuery(), this.maxHealth);
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.HEALTH, ImmutableSpongeHealthData.this::getHealth);
        registerKeyValue(Keys.HEALTH, ImmutableSpongeHealthData.this::health);

        registerFieldGetter(Keys.MAX_HEALTH, ImmutableSpongeHealthData.this::getMaxHealth);
        registerKeyValue(Keys.MAX_HEALTH, ImmutableSpongeHealthData.this::maxHealth);
    }

    public double getHealth() {
        return this.health;
    }

    public double getMaxHealth() {
        return this.maxHealth;
    }
}
