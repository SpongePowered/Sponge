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
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutablePotionEffectData;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.common.data.manipulator.mutable.SpongePotionEffectData;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;

public class EntityPotionDataProcessor extends AbstractSingleDataSingleTargetProcessor<LivingEntity, List<PotionEffect>, ListValue<PotionEffect>,
        PotionEffectData, ImmutablePotionEffectData> {

    public EntityPotionDataProcessor() {
        super(Keys.POTION_EFFECTS, LivingEntity.class);
    }

    @Override
    protected boolean set(LivingEntity dataHolder, List<PotionEffect> value) {
        dataHolder.clearActivePotions();
        for (PotionEffect effect : value) {
            net.minecraft.potion.EffectInstance mcEffect =
                new net.minecraft.potion.EffectInstance(((net.minecraft.potion.EffectInstance) effect).getPotion(), effect.getDuration(),
                    effect.getAmplifier(), effect.isAmbient(),
                    effect.getShowParticles());
            dataHolder.addPotionEffect(mcEffect);
        }
        return true;
    }

    @Override
    protected Optional<List<PotionEffect>> getVal(LivingEntity dataHolder) {
        Collection<net.minecraft.potion.EffectInstance> effects = dataHolder.getActivePotionEffects();
        if (effects.isEmpty()) {
            return Optional.empty();
        }
        List<PotionEffect> apiEffects = new ArrayList<>();
        for (net.minecraft.potion.EffectInstance potionEffect : effects) {
            apiEffects.add((PotionEffect) new net.minecraft.potion.EffectInstance(potionEffect.getPotion(), potionEffect.getDuration(),
                potionEffect.getAmplifier(),
                potionEffect.isAmbient(), potionEffect.doesShowParticles()));
        }
        return Optional.of(apiEffects);
    }

    @Override
    protected ImmutableValue<List<PotionEffect>> constructImmutableValue(List<PotionEffect> value) {
        return new ImmutableSpongeListValue<>(Keys.POTION_EFFECTS, ImmutableList.copyOf(value));
    }

    @Override
    protected ListValue<PotionEffect> constructValue(List<PotionEffect> actualValue) {
        return new SpongeListValue<>(Keys.POTION_EFFECTS, actualValue);
    }

    @Override
    protected PotionEffectData createManipulator() {
        return new SpongePotionEffectData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (!(container instanceof LivingEntity)) {
            return DataTransactionResult.failNoData();
        }
        Optional<List<PotionEffect>> effects = getVal((LivingEntity) container);
        if (effects.isPresent()) {
            ((LivingEntity) container).clearActivePotions();
            return DataTransactionResult.successRemove(constructImmutableValue(effects.get()));
        }
        return DataTransactionResult.successNoData();
    }
}
