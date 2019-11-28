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
package org.spongepowered.common.data.processor.value.entity;

import net.minecraft.entity.EntityAreaEffectCloud;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.mixin.core.entity.EntityAreaEffectCloudAccessor;

import java.util.Optional;

public class AreaEffectCloudRadiusOnUseProcessor extends AbstractSpongeValueProcessor<EntityAreaEffectCloud, Double, MutableBoundedValue<Double>> {

    public AreaEffectCloudRadiusOnUseProcessor() {
        super(EntityAreaEffectCloud.class, Keys.AREA_EFFECT_CLOUD_RADIUS_ON_USE);
    }

    @Override
    protected MutableBoundedValue<Double> constructValue(Double actualValue) {
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_RADIUS_ON_USE)
                .minimum(0.0D)
                .maximum(((double) Float.MAX_VALUE))
                .defaultValue(0.0D)
                .actualValue(actualValue)
                .build();
    }

    @Override
    protected boolean set(EntityAreaEffectCloud container, Double value) {
        container.func_184495_b(value.floatValue());
        return true;
    }

    @Override
    protected Optional<Double> getVal(EntityAreaEffectCloud container) {
        return Optional.of((double) ((EntityAreaEffectCloudAccessor) container).getRadiusOnUse());
    }

    @Override
    protected ImmutableValue<Double> constructImmutableValue(Double value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
