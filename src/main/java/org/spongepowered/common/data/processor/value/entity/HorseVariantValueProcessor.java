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

import net.minecraft.entity.passive.EntityHorse;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HorseVariant;
import org.spongepowered.api.data.type.HorseVariants;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.processor.common.HorseUtils;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.entity.SpongeHorseVariant;

import java.util.Optional;

public class HorseVariantValueProcessor extends AbstractSpongeValueProcessor<HorseVariant, Value<HorseVariant>> {

    public HorseVariantValueProcessor() {
        super(Keys.HORSE_VARIANT);
    }

    @Override
    protected Value<HorseVariant> constructValue(HorseVariant defaultValue) {
        return new SpongeValue<HorseVariant>(Keys.HORSE_VARIANT, defaultValue);
    }

    @Override
    public Optional<HorseVariant> getValueFromContainer(ValueContainer<?> container) {
        if (this.supports(container)) {
            return Optional.of(HorseUtils.getHorseVariant(((EntityHorse) container).getHorseType()));
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof EntityHorse;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, HorseVariant value) {
        ImmutableValue<HorseVariant> newValue = ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.HORSE_VARIANT, value, HorseVariants.HORSE);

        if (this.supports(container)) {
            EntityHorse horse = (EntityHorse) container;

            ImmutableValue<HorseVariant> oldValue = ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.HORSE_VARIANT, HorseUtils.getHorseVariant(horse.getHorseType()), HorseVariants.HORSE);
            horse.setHorseType(((SpongeHorseVariant) value).type);

            return DataTransactionBuilder.successReplaceResult(newValue, oldValue);
        }
        return DataTransactionBuilder.failResult(newValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}
