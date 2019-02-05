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
package org.spongepowered.common.data.manipulator.immutable.entity;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAreaEffectCloudData;
import org.spongepowered.api.data.manipulator.mutable.entity.AreaEffectCloudData;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAreaEffectData;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.SpongeImmutableListValue;
import org.spongepowered.common.data.value.SpongeMutableValue;

import java.util.List;

public class ImmutableSpongeAreaEffectCloudData extends AbstractImmutableData<ImmutableAreaEffectCloudData, AreaEffectCloudData> implements ImmutableAreaEffectCloudData {

    private final Color color;
    private final double radius;
    private final ParticleType particleType;
    private final int duration;
    private final int waitTime;
    private final double radiusOnUse;
    private final double radiusPerTick;
    private final int durationOnUse;
    private final int reapplicationDelay;
    private final ImmutableList<PotionEffect> potionEffects;
    private final int age;

    private final BoundedValue.Immutable<Integer> durationOnUseValue;
    private final Value.Immutable<Color> colorImmutableValue;
    private final Value.Immutable<ParticleType> particleTypeImmutableValue;
    private final BoundedValue.Immutable<Integer> immutableAge;
    private final SpongeImmutableListValue<PotionEffect> immutablePotionEffectsValue;
    private final BoundedValue.Immutable<Integer> reapplicationDelayValue;
    private final BoundedValue.Immutable<Double> radiusValue;
    private final BoundedValue.Immutable<Integer> durationValue;
    private final BoundedValue.Immutable<Integer> waitTimeValue;
    private final BoundedValue.Immutable<Double> radiusOnUseValue;
    private final BoundedValue.Immutable<Double> radiusPerTickValue;

    public ImmutableSpongeAreaEffectCloudData(Color color,
            double radius,
            ParticleType particleType,
            int duration,
            int waitTime,
            double radiusOnUse,
            double radiusPerTick,
            int durationOnUse,
            int reapplicationDelay,
            List<PotionEffect> potionEffects,
            int age) {
        super(ImmutableAreaEffectCloudData.class);
        this.color = color;
        this.radius = radius;
        this.particleType = particleType;
        this.duration = duration;
        this.waitTime = waitTime;
        this.radiusOnUse = radiusOnUse;
        this.radiusPerTick = radiusPerTick;
        this.durationOnUse = durationOnUse;
        this.reapplicationDelay = reapplicationDelay;
        this.potionEffects = ImmutableList.copyOf(potionEffects);
        this.age = age;
        this.radiusOnUseValue = SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_RADIUS_ON_USE)
                .minimum(0.0D)
                .maximum((double) Float.MAX_VALUE)
                .value(this.radiusOnUse)
                .build()
                .asImmutable();
        this.radiusPerTickValue =
                SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_RADIUS_PER_TICK)
                        .minimum(0.0D)
                        .maximum((double) Float.MAX_VALUE)
                        .value(this.radiusPerTick)
                        .build()
                        .asImmutable();
        this.waitTimeValue = SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_WAIT_TIME)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .value(this.waitTime)
                .build()
                .asImmutable();
        this.durationValue = SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_DURATION)
                .minimum(Integer.MIN_VALUE)
                .maximum(Integer.MAX_VALUE)
                .value(this.duration)
                .build()
                .asImmutable();
        this.radiusValue = SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_RADIUS)
                .minimum(0D)
                .maximum((double) Float.MAX_VALUE)
                .value(this.radius)
                .build()
                .asImmutable();
        this.reapplicationDelayValue = SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_DURATION_ON_USE)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .value(this.reapplicationDelay)
                .build()
                .asImmutable();
        this.immutablePotionEffectsValue = new SpongeImmutableListValue<>(Keys.POTION_EFFECTS, this.potionEffects);
        this.immutableAge = SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_AGE)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .value(this.age)
                .build()
                .asImmutable();
        this.particleTypeImmutableValue =
                new SpongeMutableValue<>(Keys.AREA_EFFECT_CLOUD_PARTICLE_TYPE, ParticleTypes.MOB_SPELL).asImmutable();
        this.colorImmutableValue =
                new SpongeMutableValue<>(Keys.AREA_EFFECT_CLOUD_COLOR, this.color).asImmutable();
        this.durationOnUseValue = SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_DURATION_ON_USE)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .value(this.durationOnUse)
                .build()
                .asImmutable();
        registerGetters();
    }

    public ImmutableSpongeAreaEffectCloudData() {
        this(Color.WHITE,
                0.5D,
                ParticleTypes.MOB_SPELL,
                600,
                20,
                0,
                0,
                0,
                0,
                ImmutableList.of(),
                0);
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.AREA_EFFECT_CLOUD_AGE, () -> this.age);
        registerKeyValue(Keys.AREA_EFFECT_CLOUD_AGE, this::age);

        registerFieldGetter(Keys.AREA_EFFECT_CLOUD_REAPPLICATION_DELAY, () -> this.reapplicationDelay);
        registerKeyValue(Keys.AREA_EFFECT_CLOUD_REAPPLICATION_DELAY, this::applicationDelay);

        registerFieldGetter(Keys.AREA_EFFECT_CLOUD_COLOR, () -> this.color);
        registerKeyValue(Keys.AREA_EFFECT_CLOUD_COLOR, this::color);

        registerFieldGetter(Keys.AREA_EFFECT_CLOUD_DURATION, () -> this.duration);
        registerKeyValue(Keys.AREA_EFFECT_CLOUD_DURATION, this::duration);

        registerFieldGetter(Keys.AREA_EFFECT_CLOUD_DURATION_ON_USE, () -> this.durationOnUse);
        registerKeyValue(Keys.AREA_EFFECT_CLOUD_DURATION_ON_USE, this::durationOnUse);

        registerFieldGetter(Keys.AREA_EFFECT_CLOUD_PARTICLE_TYPE, () -> this.particleType);
        registerKeyValue(Keys.AREA_EFFECT_CLOUD_PARTICLE_TYPE, this::particleType);

        registerFieldGetter(Keys.AREA_EFFECT_CLOUD_RADIUS, () -> this.radius);
        registerKeyValue(Keys.AREA_EFFECT_CLOUD_RADIUS, this::radius);

        registerFieldGetter(Keys.AREA_EFFECT_CLOUD_RADIUS_ON_USE, () -> this.radiusOnUse);
        registerKeyValue(Keys.AREA_EFFECT_CLOUD_RADIUS_ON_USE, this::radiusOnUse);

        registerFieldGetter(Keys.AREA_EFFECT_CLOUD_WAIT_TIME, () -> this.waitTime);
        registerKeyValue(Keys.AREA_EFFECT_CLOUD_WAIT_TIME, this::waitTime);
    }

    @Override
    public AreaEffectCloudData asMutable() {
        return new SpongeAreaEffectData(this.color,
                this.radius,
                this.particleType,
                this.duration,
                this.waitTime,
                this.radiusOnUse,
                this.radiusPerTick,
                this.durationOnUse,
                this.reapplicationDelay,
                this.potionEffects,
                this.age);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.AREA_EFFECT_CLOUD_AGE, this.age)
                .set(Keys.AREA_EFFECT_CLOUD_REAPPLICATION_DELAY, this.reapplicationDelay)
                .set(Keys.AREA_EFFECT_CLOUD_COLOR, this.color)
                .set(Keys.AREA_EFFECT_CLOUD_DURATION, this.duration)
                .set(Keys.AREA_EFFECT_CLOUD_DURATION_ON_USE, this.durationOnUse)
                .set(Keys.AREA_EFFECT_CLOUD_PARTICLE_TYPE, this.particleType)
                .set(Keys.AREA_EFFECT_CLOUD_RADIUS, this.radius)
                .set(Keys.AREA_EFFECT_CLOUD_RADIUS_ON_USE, this.radiusOnUse)
                .set(Keys.AREA_EFFECT_CLOUD_WAIT_TIME, this.waitTime)
                ;
    }

    @Override
    public Value.Immutable<Color> color() {
        return this.colorImmutableValue;
    }

    @Override
    public BoundedValue.Immutable<Double> radius() {
        return this.radiusValue;
    }

    @Override
    public Value.Immutable<ParticleType> particleType() {
        return this.particleTypeImmutableValue;
    }

    @Override
    public BoundedValue.Immutable<Integer> duration() {
        return this.durationValue;
    }

    @Override
    public BoundedValue.Immutable<Integer> waitTime() {
        return this.waitTimeValue;
    }

    @Override
    public BoundedValue.Immutable<Double> radiusOnUse() {
        return this.radiusOnUseValue;
    }

    @Override
    public BoundedValue.Immutable<Double> radiusPerTick() {
        return this.radiusPerTickValue;
    }

    @Override
    public BoundedValue.Immutable<Integer> durationOnUse() {
        return this.durationOnUseValue;
    }

    @Override
    public BoundedValue.Immutable<Integer> applicationDelay() {
        return this.reapplicationDelayValue;
    }

    @Override
    public ListValue.Immutable<PotionEffect> effects() {
        return this.immutablePotionEffectsValue;
    }

    @Override
    public BoundedValue.Immutable<Integer> age() {
        return this.immutableAge;
    }
}
