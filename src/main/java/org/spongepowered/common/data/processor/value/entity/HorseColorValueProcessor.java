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
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeImmutableValue;
import org.spongepowered.common.data.value.SpongeMutableValue;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.SpongeHorseColor;
import org.spongepowered.common.entity.SpongeHorseStyle;
import org.spongepowered.common.registry.type.entity.HorseColorRegistryModule;
import org.spongepowered.common.registry.type.entity.HorseStyleRegistryModule;

import java.util.Optional;

public class HorseColorValueProcessor extends AbstractSpongeValueProcessor<EntityHorse, HorseColor> {

    public HorseColorValueProcessor() {
        super(EntityHorse.class, Keys.HORSE_COLOR);
    }

    @Override
    protected Value.Mutable<HorseColor> constructMutableValue(HorseColor defaultValue) {
        return new SpongeMutableValue<>(Keys.HORSE_COLOR, defaultValue);
    }

    @Override
    protected boolean set(EntityHorse container, HorseColor value) {
        final SpongeHorseStyle style = (SpongeHorseStyle) HorseStyleRegistryModule.getHorseStyle(container);
        container.setHorseVariant(EntityUtil.getHorseInternalVariant((SpongeHorseColor) value, style));
        return true;
    }

    @Override
    protected Optional<HorseColor> getVal(EntityHorse container) {
        return Optional.of(HorseColorRegistryModule.getHorseColor(container));
    }

    @Override
    protected Value.Immutable<HorseColor> constructImmutableValue(HorseColor value) {
        return SpongeImmutableValue.cachedOf(Keys.HORSE_COLOR, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
