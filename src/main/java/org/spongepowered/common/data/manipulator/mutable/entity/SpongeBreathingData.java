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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableBreathingData;
import org.spongepowered.api.data.manipulator.mutable.entity.BreathingData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeBreathingData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.util.Constants;

public class SpongeBreathingData extends AbstractData<BreathingData, ImmutableBreathingData> implements BreathingData {

    private int maxAir;
    private int remainingAir;

    public SpongeBreathingData(int maxAir, int remainingAir) {
        super(BreathingData.class);
        this.maxAir = maxAir;
        this.remainingAir = remainingAir;
        registerGettersAndSetters();
    }

    public SpongeBreathingData() {
        this(Constants.Sponge.Entity.DEFAULT_MAX_AIR, Constants.Sponge.Entity.DEFAULT_MAX_AIR);
    }

    @Override
    public BreathingData copy() {
        return new SpongeBreathingData(this.maxAir, this.remainingAir);
    }

    @Override
    public ImmutableBreathingData asImmutable() {
        return new ImmutableSpongeBreathingData(this.maxAir, this.remainingAir);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.MAX_AIR.getQuery(), this.maxAir)
                .set(Keys.REMAINING_AIR.getQuery(), this.remainingAir);
    }

    @Override
    public MutableBoundedValue<Integer> remainingAir() {
        return SpongeValueFactory.boundedBuilder(Keys.REMAINING_AIR)
            .defaultValue(Constants.Sponge.Entity.DEFAULT_MAX_AIR)
            .minimum(-20)
            .maximum(Integer.MAX_VALUE)
            .actualValue(this.remainingAir)
            .build();
    }

    @Override
    public MutableBoundedValue<Integer> maxAir() {
        return SpongeValueFactory.boundedBuilder(Keys.MAX_AIR)
            .defaultValue(this.maxAir)
            .minimum(0)
            .maximum(Integer.MAX_VALUE)
            .actualValue(this.maxAir)
            .build();
    }

    private int getMaxAir() {
        return this.maxAir;
    }

    private void setMaxAir(int maxAir) {
        this.maxAir = maxAir;
    }

    private int getRemainingAir() {
        return this.remainingAir;
    }

    private void setRemainingAir(int remainingAir) {
        this.remainingAir = remainingAir;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.MAX_AIR, SpongeBreathingData.this::getMaxAir);
        registerFieldSetter(Keys.MAX_AIR, SpongeBreathingData.this::setMaxAir);
        registerKeyValue(Keys.MAX_AIR, SpongeBreathingData.this::maxAir);

        registerFieldGetter(Keys.REMAINING_AIR, SpongeBreathingData.this::getRemainingAir);
        registerFieldSetter(Keys.REMAINING_AIR, SpongeBreathingData.this::setRemainingAir);
        registerKeyValue(Keys.REMAINING_AIR, SpongeBreathingData.this::remainingAir);
    }
}
