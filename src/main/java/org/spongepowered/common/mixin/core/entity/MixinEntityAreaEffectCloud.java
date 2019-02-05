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
package org.spongepowered.common.mixin.core.entity;

import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionType;
import net.minecraft.util.EnumParticleTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.AreaEffectCloudData;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.AreaEffectCloud;
import org.spongepowered.api.util.Color;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAreaEffectData;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.SpongeMutableListValue;
import org.spongepowered.common.data.value.SpongeMutableValue;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("unchecked")
@Mixin(EntityAreaEffectCloud.class)
public abstract class MixinEntityAreaEffectCloud extends MixinEntity implements AreaEffectCloud, IMixinAreaEffectCloud {

    @Shadow private PotionType potion;
    @Shadow @Final private List<net.minecraft.potion.PotionEffect> effects;
    @Shadow private int duration;
    @Shadow private int waitTime;
    @Shadow private int reapplicationDelay;
    @Shadow private boolean colorSet;
    @Shadow private int durationOnUse;
    @Shadow private float radiusOnUse;
    @Shadow private float radiusPerTick;
    @Shadow private EntityLivingBase owner;
    @Shadow private UUID ownerUniqueId;

    @Shadow public abstract float getRadius();
    @Shadow public abstract int getColor();
    @Shadow public abstract EnumParticleTypes getParticle();

    @Override
    public AreaEffectCloudData getAreaEffectCloudData() {
        return new SpongeAreaEffectData(Color.ofRgb(this.getColor()),
                this.getRadius(),
                ParticleTypes.MOB_SPELL,
                this.duration,
                this.waitTime,
                this.radiusOnUse,
                this.radiusPerTick,
                this.durationOnUse,
                this.reapplicationDelay,
                (List<PotionEffect>) (List<?>) this.getPotionEffects(),
                this.ticksExisted
                );
    }

    @Override
    public Value.Mutable<Color> color() {
        return new SpongeMutableValue<>(Keys.AREA_EFFECT_CLOUD_COLOR, Color.ofRgb(getColor()));
    }

    @Override
    public BoundedValue.Mutable<Double> radius() {
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_RADIUS)
                .minimum(0D)
                .maximum((double) Float.MAX_VALUE)
                .defaultValue(0.5D)
                .value((double) getRadius())
                .build();
    }

    @Override
    public Value.Mutable<ParticleType> particleType() {
        return new SpongeMutableValue<>(Keys.AREA_EFFECT_CLOUD_PARTICLE_TYPE, ParticleTypes.MOB_SPELL);
    }

    @Override
    public BoundedValue.Mutable<Integer> duration() {
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_DURATION)
                .minimum(Integer.MIN_VALUE)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(600)
                .value(this.duration)
                .build();
    }

    @Override
    public BoundedValue.Mutable<Integer> waitTime() {
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_WAIT_TIME)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(20)
                .value(this.waitTime)
                .build();
    }

    @Override
    public BoundedValue.Mutable<Double> radiusOnUse() {
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_RADIUS_ON_USE)
                .minimum(0.0D)
                .maximum((double) Float.MAX_VALUE)
                .defaultValue(0.0D)
                .value((double) this.radiusOnUse)
                .build();
    }

    @Override
    public BoundedValue.Mutable<Double> radiusPerTick() {
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_RADIUS_PER_TICK)
                .minimum(0.0D)
                .maximum((double) Float.MAX_VALUE)
                .defaultValue(0.0D)
                .value((double) this.radiusPerTick)
                .build();
    }

    @Override
    public BoundedValue.Mutable<Integer> durationOnUse() {
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_DURATION_ON_USE)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(0)
                .value(this.durationOnUse)
                .build();
    }

    @Override
    public BoundedValue.Mutable<Integer> applicationDelay() {
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_DURATION_ON_USE)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(0)
                .value(this.reapplicationDelay)
                .build();
    }

    @Override
    public ListValue.Mutable<PotionEffect> effects() {
        return new SpongeMutableListValue<>(Keys.POTION_EFFECTS, (List<PotionEffect>) (List<?>) this.effects);
    }

    @Override
    public BoundedValue.Mutable<Integer> age() {
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_AGE)
                .defaultValue(0)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .value(this.ticksExisted)
                .build();
    }

}
