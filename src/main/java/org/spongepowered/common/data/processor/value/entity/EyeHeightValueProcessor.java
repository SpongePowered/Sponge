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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.ValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.interfaces.IMixinEntity;

@SuppressWarnings("ConstantConditions")
public class EyeHeightValueProcessor implements ValueProcessor<Double, Value<Double>> {

    @Override
    public Key<? extends BaseValue<Double>> getKey() {
        return Keys.EYE_HEIGHT;
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof Entity;
    }

    @Override
    public Optional<Double> getValueFromContainer(ValueContainer<?> container) {
        if (supports(container)) {
            final Entity entity = (Entity) container;
            return Optional.of((double) entity.getEyeHeight());
        }
        return Optional.absent();
    }

    @Override
    public Optional<Value<Double>> getApiValueFromContainer(ValueContainer<?> container) {
        if (supports(container)) {
            final Entity entity = (Entity) container;
            return Optional.<Value<Double>>of(new SpongeValue<Double>(Keys.EYE_HEIGHT, 0d, (double) entity.getEyeHeight()));
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult transform(ValueContainer<?> container, Function<Double, Double> function) {
        if (supports(container)) {
            final Entity entity = (Entity) container;
            final Double oldValue = (double) entity.getEyeHeight();
            final Double newValue = function.apply(oldValue);
            ((IMixinEntity) entity).setEyeHeight(newValue);
            return DataTransactionBuilder.successReplaceResult(new ImmutableSpongeValue<Double>(Keys.EYE_HEIGHT, newValue),
                new ImmutableSpongeValue<Double>(Keys.EYE_HEIGHT, oldValue));
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, BaseValue<?> value) {
        final Object object = value.get();
        if (object instanceof Number) {
            return offerToStore(container, ((Number) object).doubleValue());
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, Double value) {
        if (supports(container)) {
            final Entity entity = (Entity) container;
            final Double oldValue = (double) entity.getEyeHeight();
            ((IMixinEntity) entity).setEyeHeight(value);
            return DataTransactionBuilder.successReplaceResult(new ImmutableSpongeValue<Double>(Keys.EYE_HEIGHT, value),
                new ImmutableSpongeValue<Double>(Keys.EYE_HEIGHT, oldValue));
        }
        return DataTransactionBuilder.failResult(new ImmutableSpongeValue<Double>(Keys.EYE_HEIGHT, value));
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }

}
