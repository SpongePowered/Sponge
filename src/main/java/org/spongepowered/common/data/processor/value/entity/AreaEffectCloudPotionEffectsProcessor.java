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
import org.spongepowered.api.data.value.ListValue.Immutable;
import org.spongepowered.api.data.value.ListValue.Mutable;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.mutable.SpongeListValue;
import org.spongepowered.common.mixin.accessor.entity.AreaEffectCloudEntityAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.AreaEffectCloudEntity;

public class AreaEffectCloudPotionEffectsProcessor extends AbstractSpongeValueProcessor<AreaEffectCloudEntity, List<PotionEffect>, Mutable<PotionEffect>> {

    public AreaEffectCloudPotionEffectsProcessor() {
        super(AreaEffectCloudEntity.class, Keys.POTION_EFFECTS);
    }

    @Override
    protected Mutable<PotionEffect> constructValue(List<PotionEffect> actualValue) {
        return new SpongeListValue<>(Keys.POTION_EFFECTS, actualValue);
    }

    @Override
    protected boolean set(AreaEffectCloudEntity container, List<PotionEffect> value) {
        final List<net.minecraft.potion.EffectInstance> effects = new ArrayList<>(value.size());
        for (PotionEffect effect : value) {
            effects.add((net.minecraft.potion.EffectInstance) effect);
        }
        ((AreaEffectCloudEntityAccessor) container).accessor$setEffects(effects);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Optional<List<PotionEffect>> getVal(AreaEffectCloudEntity container) {
        return Optional.of(((List<PotionEffect>) (List<?>) ((AreaEffectCloudEntityAccessor) container).accessor$getEffects()));
    }

    @Override
    protected Immutable<PotionEffect> constructImmutableValue(List<PotionEffect> value) {
        return this.constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
