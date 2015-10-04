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
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseColors;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.processor.common.HorseUtils;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.entity.SpongeHorseColor;
import org.spongepowered.common.entity.SpongeHorseStyle;

import java.util.Optional;

public class HorseColorValueProcessor extends AbstractSpongeValueProcessor<HorseColor, Value<HorseColor>> {

    public HorseColorValueProcessor() {
        super(Keys.HORSE_COLOR);
    }

    @Override
    protected Value<HorseColor> constructValue(HorseColor defaultValue) {
        return new SpongeValue<HorseColor>(Keys.HORSE_COLOR, defaultValue);
    }

    @Override
    public Optional<HorseColor> getValueFromContainer(ValueContainer<?> container) {
        if (this.supports(container)) {
            return Optional.of(HorseUtils.getHorseColor((EntityHorse) container));
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof EntityHorse;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, HorseColor value) {
        ImmutableValue<HorseColor> newValue = ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.HORSE_COLOR, value, HorseColors.WHITE);

        if (this.supports(container)) {
            EntityHorse horse = (EntityHorse) container;

            HorseColor old = HorseUtils.getHorseColor(horse);
            SpongeHorseStyle style = (SpongeHorseStyle) HorseUtils.getHorseStyle(horse);

            ImmutableValue<HorseColor> oldValue = ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.HORSE_COLOR, old, HorseColors.WHITE);

            horse.setHorseVariant(HorseUtils.getInternalVariant((SpongeHorseColor) value, style));
            return DataTransactionBuilder.successReplaceResult(newValue, oldValue);
        }
        return DataTransactionBuilder.failResult(newValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}
