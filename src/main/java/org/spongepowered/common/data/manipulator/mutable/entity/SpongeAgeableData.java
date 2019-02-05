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
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAgeableData;
import org.spongepowered.api.data.manipulator.mutable.entity.AgeableData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeAgeableData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.SpongeMutableValue;

public class SpongeAgeableData extends AbstractData<AgeableData, ImmutableAgeableData> implements AgeableData {

    private int age;
    private boolean adult;

    public SpongeAgeableData(int age, boolean adult) {
        super(AgeableData.class);
        this.age = age;
        this.adult = adult;
        registerGettersAndSetters();
    }

    public SpongeAgeableData() {
        this(1, true);
    }

    @Override
    public AgeableData copy() {
        return new SpongeAgeableData(this.age, this.adult);
    }

    @Override
    public ImmutableAgeableData asImmutable() {
        return new ImmutableSpongeAgeableData(this.age, this.adult);
    }

    @Override
    public BoundedValue.Mutable<Integer> age() {
        return SpongeValueFactory.boundedBuilder(Keys.AGE)
                .minimum(Integer.MIN_VALUE)
                .maximum(Integer.MAX_VALUE)
                .value(this.age)
                .build();
    }

    @Override
    public Value.Mutable<Boolean> adult() {
        return new SpongeMutableValue<>(Keys.IS_ADULT, this.adult);
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.AGE, SpongeAgeableData.this::getAge);
        registerFieldSetter(Keys.AGE, SpongeAgeableData.this::setAge);
        registerKeyValue(Keys.AGE, SpongeAgeableData.this::age);

        registerFieldGetter(Keys.IS_ADULT, SpongeAgeableData.this::isAdult);
        registerFieldSetter(Keys.IS_ADULT, SpongeAgeableData.this::setAdult);
        registerKeyValue(Keys.IS_ADULT, SpongeAgeableData.this::adult);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.AGE, this.age)
            .set(Keys.IS_ADULT, this.adult);
    }

    public int getAge() {
        return this.age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isAdult() {
        return this.adult;
    }

    public void setAdult(boolean adult) {
        this.adult = adult;
    }
}
