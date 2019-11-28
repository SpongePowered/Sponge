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
package org.spongepowered.common.data.processor.multi.entity;

import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.util.EnumParticleTypes;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAreaEffectCloudData;
import org.spongepowered.api.data.manipulator.mutable.entity.AreaEffectCloudData;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAreaEffectData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.effect.particle.SpongeParticleType;
import org.spongepowered.common.mixin.core.entity.EntityAreaEffectCloudAccessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AreaEffectCloudDataProcessor extends AbstractEntityDataProcessor<EntityAreaEffectCloud, AreaEffectCloudData, ImmutableAreaEffectCloudData> {

    public AreaEffectCloudDataProcessor() {
        super(EntityAreaEffectCloud.class);
    }

    @Override
    protected AreaEffectCloudData createManipulator() {
        return new SpongeAreaEffectData();
    }

    @Override
    protected boolean doesDataExist(EntityAreaEffectCloud dataHolder) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean set(EntityAreaEffectCloud dataHolder, Map<Key<?>, Object> keyValues) {
        final int age = (int) keyValues.get(Keys.AREA_EFFECT_CLOUD_AGE);
        final Color color = (Color) keyValues.get(Keys.AREA_EFFECT_CLOUD_COLOR);
        final double radius = (double) keyValues.get(Keys.AREA_EFFECT_CLOUD_RADIUS);
        final double radiusOnUse = (double) keyValues.get(Keys.AREA_EFFECT_CLOUD_RADIUS_ON_USE);
        final int duration = (int) keyValues.get(Keys.AREA_EFFECT_CLOUD_DURATION);
        final int durationOnUse = (int) keyValues.get(Keys.AREA_EFFECT_CLOUD_DURATION_ON_USE);
        final int waitTime = (int) keyValues.get(Keys.AREA_EFFECT_CLOUD_WAIT_TIME);
        final int reapplicationDelay = (int) keyValues.get(Keys.AREA_EFFECT_CLOUD_REAPPLICATION_DELAY);
        final List<PotionEffect> potionEffects = (List<PotionEffect>) keyValues.get(Keys.POTION_EFFECTS);
        final ParticleType particleType = (ParticleType) keyValues.get(Keys.AREA_EFFECT_CLOUD_PARTICLE_TYPE);
        dataHolder.field_70173_aa = age;
        dataHolder.func_184482_a(color.getRgb());
        dataHolder.func_184483_a((float) radius);
        dataHolder.func_184495_b((float) radiusOnUse);
        dataHolder.func_184486_b(duration);
        ((EntityAreaEffectCloudAccessor) dataHolder).setDurationOnUse(durationOnUse);
        dataHolder.func_184485_d(waitTime);
        final EnumParticleTypes internalType = ((SpongeParticleType) particleType).getInternalType();
        dataHolder.func_184491_a(internalType == null ? EnumParticleTypes.SPELL_MOB : internalType);

        final List<net.minecraft.potion.PotionEffect> effects = new ArrayList<>();
        for (PotionEffect effect : potionEffects) {
            effects.add((net.minecraft.potion.PotionEffect) effect);
        }
        ((EntityAreaEffectCloudAccessor) dataHolder).setPotionEffects(effects);
        ((EntityAreaEffectCloudAccessor) dataHolder).setReapplicationDelay(reapplicationDelay);
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(EntityAreaEffectCloud dataHolder) {
        final HashMap<Key<?>, Object> map = new HashMap<>();
        map.put(Keys.AREA_EFFECT_CLOUD_AGE, dataHolder.field_70173_aa);
        map.put(Keys.AREA_EFFECT_CLOUD_COLOR, Color.ofRgb(dataHolder.func_184492_k()));
        map.put(Keys.AREA_EFFECT_CLOUD_RADIUS, dataHolder.func_184490_j());
        final EntityAreaEffectCloudAccessor mixinAreaEffect = (EntityAreaEffectCloudAccessor) dataHolder;
        final List<net.minecraft.potion.PotionEffect> potionEffects = mixinAreaEffect.getPotionEffects();
        final List<PotionEffect> effects = new ArrayList<>(potionEffects.size());
        for (net.minecraft.potion.PotionEffect potionEffect : potionEffects) {
            effects.add((PotionEffect) potionEffect);
        }
        map.put(Keys.POTION_EFFECTS, effects);
        map.put(Keys.AREA_EFFECT_CLOUD_RADIUS_ON_USE, mixinAreaEffect.getRadiusOnUse());
        map.put(Keys.AREA_EFFECT_CLOUD_RADIUS_PER_TICK, mixinAreaEffect.getRadiusPerTick());
        map.put(Keys.AREA_EFFECT_CLOUD_DURATION, dataHolder.func_184489_o());
        map.put(Keys.AREA_EFFECT_CLOUD_DURATION_ON_USE, mixinAreaEffect.getDurationOnUse());
        map.put(Keys.AREA_EFFECT_CLOUD_REAPPLICATION_DELAY, mixinAreaEffect.getReapplicationDelay());
        map.put(Keys.AREA_EFFECT_CLOUD_WAIT_TIME, mixinAreaEffect.getWaitTime());
        map.put(Keys.AREA_EFFECT_CLOUD_PARTICLE_TYPE, ParticleTypes.MOB_SPELL);

        return map;
    }

    @Override
    public Optional<AreaEffectCloudData> fill(DataContainer container, AreaEffectCloudData areaEffectCloudData) {
        return null;
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return null;
    }
}
