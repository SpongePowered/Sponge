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

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseColors;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.entity.SpongeHorseColor;
import org.spongepowered.common.entity.SpongeHorseStyle;
import org.spongepowered.common.registry.type.entity.HorseColorRegistryModule;
import org.spongepowered.common.registry.type.entity.HorseStyleRegistryModule;

import java.util.Optional;
import net.minecraft.entity.passive.horse.HorseEntity;

public class HorseColorValueProcessor extends AbstractSpongeValueProcessor<HorseEntity, HorseColor, Value<HorseColor>> {

    public HorseColorValueProcessor() {
        super(HorseEntity.class, Keys.HORSE_COLOR);
    }

    @Override
    protected Value<HorseColor> constructValue(HorseColor defaultValue) {
        return new SpongeValue<>(Keys.HORSE_COLOR, defaultValue);
    }

    @Override
    protected boolean set(HorseEntity container, HorseColor value) {
        final SpongeHorseStyle style = (SpongeHorseStyle) HorseStyleRegistryModule.getHorseStyle(container);
        container.func_110235_q(((SpongeHorseColor) value).getBitMask() | style.getBitMask());
        return true;
    }

    @Override
    protected Optional<HorseColor> getVal(HorseEntity container) {
        return Optional.of(HorseColorRegistryModule.getHorseColor(container));
    }

    @Override
    protected ImmutableValue<HorseColor> constructImmutableValue(HorseColor value) {
        return ImmutableSpongeValue.cachedOf(Keys.HORSE_COLOR, HorseColors.WHITE, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
