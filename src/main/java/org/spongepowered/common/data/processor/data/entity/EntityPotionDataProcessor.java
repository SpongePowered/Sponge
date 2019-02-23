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
package org.spongepowered.common.data.processor.data.entity;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutablePotionEffectData;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.common.data.manipulator.mutable.SpongePotionEffectData;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;
import org.spongepowered.common.data.util.PotionUtil;
import org.spongepowered.common.data.value.SpongeImmutableListValue;
import org.spongepowered.common.data.value.SpongeMutableListValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class EntityPotionDataProcessor extends AbstractSingleDataSingleTargetProcessor<EntityLivingBase, List<PotionEffect>,
        PotionEffectData, ImmutablePotionEffectData> {

    public EntityPotionDataProcessor() {
        super(Keys.POTION_EFFECTS, EntityLivingBase.class);
    }

    @Override
    protected boolean set(EntityLivingBase dataHolder, List<PotionEffect> value) {
        dataHolder.getActivePotionMap().clear();
        for (PotionEffect effect : value) {
            net.minecraft.potion.PotionEffect mcEffect = PotionUtil.copyToNative(effect);
            dataHolder.addPotionEffect(mcEffect);
        }
        return true;
    }

    @Override
    protected Optional<List<PotionEffect>> getVal(EntityLivingBase dataHolder) {
        Collection<net.minecraft.potion.PotionEffect> effects = dataHolder.getActivePotionEffects();
        if (effects.isEmpty()) {
            return Optional.empty();
        }
        List<PotionEffect> apiEffects = new ArrayList<>();
        for (net.minecraft.potion.PotionEffect potionEffect : effects) {
            apiEffects.add(PotionUtil.copyToApi(potionEffect));
        }
        return Optional.of(apiEffects);
    }

    @Override
    protected Value.Immutable<List<PotionEffect>> constructImmutableValue(List<PotionEffect> value) {
        return new SpongeImmutableListValue<>(Keys.POTION_EFFECTS, ImmutableList.copyOf(value));
    }

    @Override
    protected Value.Mutable<List<PotionEffect>> constructMutableValue(List<PotionEffect> actualValue) {
        return new SpongeMutableListValue<>(Keys.POTION_EFFECTS, actualValue);
    }

    @Override
    protected PotionEffectData createManipulator() {
        return new SpongePotionEffectData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (!(container instanceof EntityLivingBase)) {
            return DataTransactionResult.failNoData();
        }
        Optional<List<PotionEffect>> effects = getVal((EntityLivingBase) container);
        if (effects.isPresent()) {
            ((EntityLivingBase) container).getActivePotionMap().clear();
            return DataTransactionResult.successRemove(constructImmutableValue(effects.get()));
        }
        return DataTransactionResult.successNoData();
    }
}
