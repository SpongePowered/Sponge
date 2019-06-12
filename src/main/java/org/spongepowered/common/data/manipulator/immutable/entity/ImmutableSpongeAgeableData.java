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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAgeableData;
import org.spongepowered.api.data.manipulator.mutable.entity.AgeableData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAgeableData;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeAgeableData extends AbstractImmutableData<ImmutableAgeableData, AgeableData> implements ImmutableAgeableData {

    private int age;
    private boolean adult;

    private final ImmutableBoundedValue<Integer> ageValue;

    public ImmutableSpongeAgeableData(int age, boolean adult) {
        super(ImmutableAgeableData.class);
        this.age = age;
        this.adult = adult;

        this.ageValue = SpongeValueFactory.boundedBuilder(Keys.AGE)
                .actualValue(this.age)
                .defaultValue(Constants.Entity.Ageable.ADULT)
                .minimum(Constants.Entity.Ageable.CHILD)
                .maximum(Constants.Entity.Ageable.ADULT)
                .build().asImmutable();

        this.registerGetters();
    }

    @Override
    public AgeableData asMutable() {
        return new SpongeAgeableData(this.age, this.adult);
    }

    @Override
    public ImmutableBoundedValue<Integer> age() {
        return this.ageValue;
    }

    @Override
    public ImmutableValue<Boolean> adult() {
        return ImmutableSpongeValue.cachedOf(Keys.IS_ADULT, true, this.adult);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.AGE, this.age)
            .set(Keys.IS_ADULT, this.adult);
    }

    @Override
    protected void registerGetters() {
        this.registerFieldGetter(Keys.AGE, ImmutableSpongeAgeableData.this::getAge);
        this.registerKeyValue(Keys.AGE, ImmutableSpongeAgeableData.this::age);

        this.registerFieldGetter(Keys.IS_ADULT, ImmutableSpongeAgeableData.this::isAdult);
        this.registerKeyValue(Keys.IS_ADULT, ImmutableSpongeAgeableData.this::adult);
    }

    public int getAge() {
        return this.age;
    }

    public boolean isAdult() {
        return this.adult;
    }
}
