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

import static com.google.common.base.Preconditions.checkArgument;
import static org.spongepowered.common.data.util.ComparatorUtil.doubleComparator;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHealthData;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeHealthData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeBoundedValue;
import org.spongepowered.common.util.GetterFunction;
import org.spongepowered.common.util.SetterFunction;

public class SpongeHealthData extends AbstractData<HealthData, ImmutableHealthData> implements HealthData {

    private double health;
    private double maxHealth;

    public SpongeHealthData(double health, double maxHealth) {
        super(HealthData.class);
        this.health = health;
        checkArgument(maxHealth > 0);
        this.maxHealth = maxHealth;
        registerStuff();
    }

    public SpongeHealthData() {
        this(20D, 20D);
    }

    @Override
    public MutableBoundedValue<Double> health() {
        return new SpongeBoundedValue<Double>(Keys.HEALTH, this.maxHealth, doubleComparator(), 0D, (double) Float.MAX_VALUE, this.health);
    }

    @Override
    public MutableBoundedValue<Double> maxHealth() {
        return new SpongeBoundedValue<Double>(Keys.MAX_HEALTH, this.maxHealth, doubleComparator(), 0D, (double) Float.MAX_VALUE, this.maxHealth);
    }

    @Override
    public HealthData copy() {
        return new SpongeHealthData(this.health, this.maxHealth);
    }

    @Override
    public ImmutableHealthData asImmutable() {
        return new ImmutableSpongeHealthData(this.health, this.maxHealth);
    }

    @Override
    public int compareTo(HealthData o) {
        return ComparisonChain.start()
            .compare(o.health().get().doubleValue(), this.health)
            .compare(o.maxHealth().get().doubleValue(), this.maxHealth)
            .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
            .set(Keys.HEALTH, this.health)
            .set(Keys.MAX_HEALTH, this.maxHealth);
    }

    public double getHealth() {
        return this.health;
    }

    public SpongeHealthData setHealth(double value) {
        this.health = value;
        return this;
    }

    public double getMaxHealth() {
        return this.maxHealth;
    }

    public SpongeHealthData setMaxHealth(double value) {
        this.maxHealth = value;
        return this;
    }

    protected void registerStuff() {
        registerFieldGetter(Keys.HEALTH, new GetterFunction<Object>() {
            @Override
            public Object get() {
                return getHealth();
            }
        });
        registerFieldSetter(Keys.HEALTH, new SetterFunction<Object>() {
            @Override
            public void set(Object value) {
                setHealth(((Number) value).doubleValue());
            }
        });
        registerKeyValue(Keys.HEALTH, new GetterFunction<Value<?>>() {
            @Override
            public Value<?> get() {
                return health();
            }
        });

        registerFieldGetter(Keys.MAX_HEALTH, new GetterFunction<Object>() {
            @Override
            public Object get() {
                return getMaxHealth();
            }
        });
        registerFieldSetter(Keys.MAX_HEALTH, new SetterFunction<Object>() {
            @Override
            public void set(Object value) {
                setMaxHealth(((Number) value).doubleValue());
            }
        });
        registerKeyValue(Keys.MAX_HEALTH, new GetterFunction<Value<?>>() {
            @Override
            public Value<?> get() {
                return maxHealth();
            }
        });
    }
}
