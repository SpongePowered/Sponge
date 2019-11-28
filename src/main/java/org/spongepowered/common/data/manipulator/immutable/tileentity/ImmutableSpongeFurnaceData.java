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
package org.spongepowered.common.data.manipulator.immutable.tileentity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableFurnaceData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.FurnaceData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeFurnaceData;
import org.spongepowered.common.data.value.SpongeValueFactory;

public class ImmutableSpongeFurnaceData extends AbstractImmutableData<ImmutableFurnaceData, FurnaceData> implements ImmutableFurnaceData {

    private final int passedBurnTime; //time (int) the fuel item already burned
    private final int maxBurnTime; //time (int) the fuel can burn until its depleted
    private final int passedCookTime; //time (int) the item already cooked
    private final int maxCookTime; //time (int) the item have to cook

    private final ImmutableBoundedValue<Integer> passedBurnTimeValue; // -> see passedBurnTime
    private final ImmutableBoundedValue<Integer> maxBurnTimeValue; // -> see maxBurnTime
    private final ImmutableBoundedValue<Integer> passedCookTimeValue; // -> see passedCookTime
    private final ImmutableBoundedValue<Integer> maxCookTimeValue; // -> see maxCookTime

    public ImmutableSpongeFurnaceData(int passedBurnTime, int maxBurnTime, int passedCookTime, int maxCookTime) {
        super(ImmutableFurnaceData.class);

        this.passedBurnTime = passedBurnTime;
        this.maxBurnTime = maxBurnTime;
        this.passedCookTime = passedCookTime;
        this.maxCookTime = maxCookTime;

        this.passedBurnTimeValue = SpongeValueFactory.boundedBuilder(Keys.PASSED_BURN_TIME)
                .minimum(0).maximum(this.maxBurnTime).defaultValue(0)
                .actualValue(this.passedBurnTime).build().asImmutable();

        this.maxBurnTimeValue = SpongeValueFactory.boundedBuilder(Keys.MAX_BURN_TIME)
                .minimum(0).maximum(Integer.MAX_VALUE).defaultValue(1600)
                .actualValue(this.maxBurnTime).build().asImmutable();

        this.passedCookTimeValue = SpongeValueFactory.boundedBuilder(Keys.PASSED_COOK_TIME)
                .minimum(0).maximum(this.maxCookTime).defaultValue(0)
                .actualValue(this.passedCookTime).build().asImmutable();

        this.maxCookTimeValue = SpongeValueFactory.boundedBuilder(Keys.MAX_COOK_TIME)
                .minimum(0).maximum(Integer.MAX_VALUE).defaultValue(200)
                .actualValue(this.maxCookTime).build().asImmutable();

        this.registerGetters();
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.PASSED_BURN_TIME, ImmutableSpongeFurnaceData.this::getPassedBurnTime);
        registerKeyValue(Keys.PASSED_BURN_TIME, ImmutableSpongeFurnaceData.this::passedBurnTime);

        registerFieldGetter(Keys.MAX_BURN_TIME, ImmutableSpongeFurnaceData.this::getMaxBurnTime);
        registerKeyValue(Keys.MAX_BURN_TIME, ImmutableSpongeFurnaceData.this::maxBurnTime);

        registerFieldGetter(Keys.PASSED_COOK_TIME, ImmutableSpongeFurnaceData.this::getPassedCookTime);
        registerKeyValue(Keys.PASSED_COOK_TIME, ImmutableSpongeFurnaceData.this::passedCookTime);

        registerFieldGetter(Keys.MAX_COOK_TIME, ImmutableSpongeFurnaceData.this::getMaxCookTime);
        registerKeyValue(Keys.MAX_COOK_TIME, ImmutableSpongeFurnaceData.this::maxCookTime);
    }

    @Override
    public ImmutableBoundedValue<Integer> passedBurnTime() {
        return this.passedBurnTimeValue;
    }

    public int getPassedBurnTime() {
        return this.passedBurnTime;
    }

    @Override
    public ImmutableBoundedValue<Integer> maxBurnTime() {
        return this.maxBurnTimeValue;
    }

    public int getMaxBurnTime() {
        return this.maxBurnTime;
    }

    @Override
    public ImmutableBoundedValue<Integer> passedCookTime() {
        return this.passedCookTimeValue;
    }

    public int getPassedCookTime() {
        return this.passedCookTime;
    }

    @Override
    public ImmutableBoundedValue<Integer> maxCookTime() {
        return this.maxCookTimeValue;
    }

    public int getMaxCookTime() {
        return this.maxCookTime;
    }

    @Override
    public FurnaceData asMutable() {
        return new SpongeFurnaceData(this.passedBurnTime, this.maxBurnTime, this.passedCookTime, this.maxCookTime);
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
