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
package org.spongepowered.common.data.manipulator.entity;

import static com.google.common.base.Preconditions.checkArgument;
import static org.spongepowered.api.data.DataQuery.of;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulator.entity.HealthData;
import org.spongepowered.common.data.manipulator.SpongeAbstractData;

public class SpongeHealthData extends SpongeAbstractData<HealthData> implements HealthData {

    public static final DataQuery HEALTH = of("Health");
    public static final DataQuery MAX_HEALTH = of("MaxHealth");
    private double health = 20.0D;
    private double maxHealth = 20.0D;


    public SpongeHealthData() {
        super(HealthData.class);
    }

    @Override
    public HealthData damage(double amount) {
        final double newHealth = this.health - amount;
        if (newHealth < 0) { // we have to validate that we can't have negative health
            this.health = 0;
        } else {
            this.health = newHealth;
        }
        return this;
    }

    @Override
    public double getHealth() {
        return this.health;
    }

    @Override
    public HealthData setHealth(double health) {
        checkArgument(health <= this.maxHealth, "Cannot set health greater than the max health!");
        checkArgument(health >= 0, "Health must be greater than or equal to zero!");
        this.health = health;
        return this;
    }

    @Override
    public double getMaxHealth() {
        return this.maxHealth;
    }

    @Override
    public HealthData setMaxHealth(double maxHealth) {
        checkArgument(maxHealth > 0);
        this.maxHealth = maxHealth;
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
        return this;
    }

    @Override
    public HealthData copy() {
        return new SpongeHealthData().setMaxHealth(this.maxHealth).setHealth(this.health);
    }

    @Override
    public int compareTo(HealthData o) {
        return (int) Math.floor((o.getHealth() - this.health) - (o.getMaxHealth() - this.maxHealth));
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(HEALTH, this.health)
                .set(MAX_HEALTH, this.maxHealth);
    }
}
