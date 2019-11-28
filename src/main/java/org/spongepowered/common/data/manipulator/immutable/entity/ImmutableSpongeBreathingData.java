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

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableBreathingData;
import org.spongepowered.api.data.manipulator.mutable.entity.BreathingData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeBreathingData;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.util.Constants;

public class ImmutableSpongeBreathingData extends AbstractImmutableData<ImmutableBreathingData, BreathingData> implements ImmutableBreathingData {

    private final int maxAir;
    private final int remainingAir;

    private final ImmutableBoundedValue<Integer> remainingAirValue;
    private final ImmutableBoundedValue<Integer> maxAirValue;

    public ImmutableSpongeBreathingData(int maxAir, int remainingAir) {
        super(ImmutableBreathingData.class);
        this.maxAir = maxAir;
        this.remainingAir = remainingAir;

        this.remainingAirValue = SpongeValueFactory.boundedBuilder(Keys.REMAINING_AIR)
                .actualValue(remainingAir)
                .defaultValue(this.maxAir)
                .minimum(-20)
                .maximum(this.maxAir)
                .build()
                .asImmutable();

        this.maxAirValue = SpongeValueFactory.boundedBuilder(Keys.MAX_AIR)
                .actualValue(this.maxAir)
                .defaultValue(Constants.Sponge.Entity.DEFAULT_MAX_AIR)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .build()
                .asImmutable();

        registerGetters();
    }

    @Override
    public BreathingData asMutable() {
        return new SpongeBreathingData(this.maxAir, this.remainingAir);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.MAX_AIR.getQuery(), this.maxAir)
                .set(Keys.REMAINING_AIR.getQuery(), this.remainingAir);
    }

    @Override
    public ImmutableBoundedValue<Integer> remainingAir() {

        return this.remainingAirValue;
    }

    @Override
    public ImmutableBoundedValue<Integer> maxAir() {
        return this.maxAirValue;
    }

    public int getMaxAir() {
        return this.maxAir;
    }

    public int getRemainingAir() {
        return this.remainingAir;
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.MAX_AIR, ImmutableSpongeBreathingData.this::getMaxAir);
        registerKeyValue(Keys.MAX_AIR, ImmutableSpongeBreathingData.this::maxAir);

        registerFieldGetter(Keys.REMAINING_AIR, ImmutableSpongeBreathingData.this::getRemainingAir);
        registerKeyValue(Keys.REMAINING_AIR, ImmutableSpongeBreathingData.this::remainingAir);
    }
}
