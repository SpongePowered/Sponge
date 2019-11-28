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
package org.spongepowered.common.mixin.api.mcp.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumParticleTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.AreaEffectCloudData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;
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
import org.spongepowered.common.data.value.mutable.SpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("unchecked")
@Mixin(AreaEffectCloudEntity.class)
public abstract class EntityAreaEffectCloudMixin_API extends EntityMixin_API implements AreaEffectCloud {

    @Shadow private Potion potion;
    @Shadow @Final private List<net.minecraft.potion.EffectInstance> effects;
    @Shadow private int duration;
    @Shadow private int waitTime;
    @Shadow private int reapplicationDelay;
    @Shadow private boolean colorSet;
    @Shadow private int durationOnUse;
    @Shadow private float radiusOnUse;
    @Shadow private float radiusPerTick;
    @Shadow private LivingEntity owner;
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
                (List<PotionEffect>) (List<?>) this.effects,
                this.ticksExisted
                );
    }

    @Override
    public Value<Color> color() {
        return new SpongeValue<>(Keys.AREA_EFFECT_CLOUD_COLOR, Color.WHITE, Color.ofRgb(getColor()));
    }

    @Override
    public MutableBoundedValue<Double> radius() {
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_RADIUS)
                .minimum(0D)
                .maximum((double) Float.MAX_VALUE)
                .defaultValue(0.5D)
                .actualValue((double) getRadius())
                .build();
    }

    @Override
    public Value<ParticleType> particleType() {
        return new SpongeValue<>(Keys.AREA_EFFECT_CLOUD_PARTICLE_TYPE, ParticleTypes.MOB_SPELL, ParticleTypes.MOB_SPELL);
    }

    @Override
    public MutableBoundedValue<Integer> duration() {
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_DURATION)
                .minimum(Integer.MIN_VALUE)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(600)
                .actualValue(this.duration)
                .build();
    }

    @Override
    public MutableBoundedValue<Integer> waitTime() {
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_WAIT_TIME)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(20)
                .actualValue(this.waitTime)
                .build();
    }

    @Override
    public MutableBoundedValue<Double> radiusOnUse() {
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_RADIUS_ON_USE)
                .minimum(0.0D)
                .maximum((double) Float.MAX_VALUE)
                .defaultValue(0.0D)
                .actualValue((double) this.radiusOnUse)
                .build();
    }

    @Override
    public MutableBoundedValue<Double> radiusPerTick() {
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_RADIUS_PER_TICK)
                .minimum(0.0D)
                .maximum((double) Float.MAX_VALUE)
                .defaultValue(0.0D)
                .actualValue((double) this.radiusPerTick)
                .build();
    }

    @Override
    public MutableBoundedValue<Integer> durationOnUse() {
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_DURATION_ON_USE)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(0)
                .actualValue(this.durationOnUse)
                .build();
    }

    @Override
    public MutableBoundedValue<Integer> applicationDelay() {
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_DURATION_ON_USE)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(0)
                .actualValue(this.reapplicationDelay)
                .build();
    }

    @Override
    public ListValue<PotionEffect> effects() {
        return new SpongeListValue<>(Keys.POTION_EFFECTS, (List<PotionEffect>) (List<?>) this.effects);
    }

    @Override
    public MutableBoundedValue<Integer> age() {
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_AGE)
                .defaultValue(0)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .actualValue(this.ticksExisted)
                .build();
    }

}
