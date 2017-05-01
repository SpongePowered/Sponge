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
package org.spongepowered.common.data.manipulator.mutable.tileentity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableFurnaceData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.FurnaceData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeFurnaceData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.SpongeValueFactory;

public class SpongeFurnaceData extends AbstractData<FurnaceData, ImmutableFurnaceData> implements FurnaceData {

    private int passedBurnTime; //time (int) the fuel item already burned
    private int maxBurnTime; //time (int) the fuel can burn until its depleted
    private int passedCookTime; //time (int) the item already cooked
    private int maxCookTime; //time (int) the item have to cook

    public SpongeFurnaceData() {
        this(0, 0, 0, 0);
    }

    public SpongeFurnaceData(int passedBurnTime, int maxBurnTime, int passedCookTime, int maxCookTime) {

        super(FurnaceData.class);

        this.passedBurnTime = passedBurnTime;
        this.maxBurnTime = maxBurnTime;
        this.passedCookTime = passedCookTime;
        this.maxCookTime = maxCookTime;

        this.registerGettersAndSetters();
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.PASSED_BURN_TIME, SpongeFurnaceData.this::getPassedBurnTime);
        registerFieldSetter(Keys.PASSED_BURN_TIME, SpongeFurnaceData.this::setPassedBurnTime);
        registerKeyValue(Keys.PASSED_BURN_TIME, SpongeFurnaceData.this::passedBurnTime);

        registerFieldGetter(Keys.MAX_BURN_TIME, SpongeFurnaceData.this::getMaxBurnTime);
        registerFieldSetter(Keys.MAX_BURN_TIME, SpongeFurnaceData.this::setMaxBurnTime);
        registerKeyValue(Keys.MAX_BURN_TIME, SpongeFurnaceData.this::maxBurnTime);

        registerFieldGetter(Keys.PASSED_COOK_TIME, SpongeFurnaceData.this::getPassedCookTime);
        registerFieldSetter(Keys.PASSED_COOK_TIME, SpongeFurnaceData.this::setPassedCookTime);
        registerKeyValue(Keys.PASSED_COOK_TIME, SpongeFurnaceData.this::passedCookTime);

        registerFieldGetter(Keys.MAX_COOK_TIME, SpongeFurnaceData.this::getMaxCookTime);
        registerFieldSetter(Keys.MAX_COOK_TIME, SpongeFurnaceData.this::setMaxCookTime);
        registerKeyValue(Keys.MAX_COOK_TIME, SpongeFurnaceData.this::maxCookTime);
    }

    @Override
    public MutableBoundedValue<Integer> passedBurnTime() {
        return SpongeValueFactory.boundedBuilder(Keys.PASSED_BURN_TIME)
                .minimum(0)
                .maximum(this.maxBurnTime)
                .defaultValue(0)
                .actualValue(this.passedBurnTime)
                .build();
    }

    public int getPassedBurnTime() {
        return this.passedBurnTime;
    }

    public void setPassedBurnTime(int passedBurnTime) {
        this.passedBurnTime = passedBurnTime;
    }

    @Override
    public MutableBoundedValue<Integer> maxBurnTime() {
        return SpongeValueFactory.boundedBuilder(Keys.MAX_BURN_TIME)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(1600)
                .actualValue(this.maxBurnTime)
                .build();
    }

    public int getMaxBurnTime() {
        return this.maxBurnTime;
    }

    public void setMaxBurnTime(int maxBurnTime) {
        this.maxBurnTime = maxBurnTime;
    }

    @Override
    public MutableBoundedValue<Integer> passedCookTime() {
        return SpongeValueFactory.boundedBuilder(Keys.PASSED_COOK_TIME)
                .minimum(0)
                .maximum(this.maxCookTime)
                .defaultValue(0)
                .actualValue(this.passedCookTime)
                .build();
    }

    public int getPassedCookTime() {
        return this.passedCookTime;
    }

    public void setPassedCookTime(int passedCookTime) {
        this.passedCookTime = passedCookTime;
    }

    @Override
    public MutableBoundedValue<Integer> maxCookTime() {
        return SpongeValueFactory.boundedBuilder(Keys.MAX_COOK_TIME)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(200)
                .actualValue(this.maxCookTime)
                .build();
    }

    public int getMaxCookTime() {
        return this.maxCookTime;
    }

    public void setMaxCookTime(int maxCookTime) {
        this.maxCookTime = maxCookTime;
    }

    @Override
    public FurnaceData copy() {
        return new SpongeFurnaceData(this.passedBurnTime, this.maxBurnTime, this.passedCookTime, this.maxCookTime);
    }

    @Override
    public ImmutableFurnaceData asImmutable() {
        return new ImmutableSpongeFurnaceData(this.passedBurnTime, this.maxBurnTime, this.passedCookTime, this.maxCookTime);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.PASSED_BURN_TIME, this.passedBurnTime)
                .set(Keys.MAX_BURN_TIME, this.maxBurnTime)
                .set(Keys.PASSED_COOK_TIME, this.passedCookTime)
                .set(Keys.MAX_COOK_TIME, this.maxCookTime);
    }
}
