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

import net.minecraft.entity.passive.EntityRabbit;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.RabbitType;
import org.spongepowered.api.data.type.RabbitTypes;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.entity.SpongeEntityConstants;
import org.spongepowered.common.entity.SpongeRabbitType;

import java.util.Optional;

public class RabbitTypeValueProcessor extends AbstractSpongeValueProcessor<EntityRabbit, RabbitType, Value<RabbitType>> {

    public RabbitTypeValueProcessor() {
        super(EntityRabbit.class, Keys.RABBIT_TYPE);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected Value<RabbitType> constructValue(RabbitType defaultValue) {
        return new SpongeValue<>(Keys.RABBIT_TYPE, defaultValue, RabbitTypes.BROWN);
    }

    @Override
    protected boolean set(EntityRabbit container, RabbitType value) {
        if (value instanceof SpongeRabbitType) {
            container.setRabbitType(((SpongeRabbitType) value).type);
            return true;
        }
        return false;
    }

    @Override
    protected Optional<RabbitType> getVal(EntityRabbit container) {
        return Optional.ofNullable(SpongeEntityConstants.RABBIT_IDMAP.get(container.getRabbitType()));
    }
    
    @Override
    protected ImmutableValue<RabbitType> constructImmutableValue(RabbitType value) {
        return ImmutableSpongeValue.cachedOf(Keys.RABBIT_TYPE, RabbitTypes.BROWN, value);
    }

}
