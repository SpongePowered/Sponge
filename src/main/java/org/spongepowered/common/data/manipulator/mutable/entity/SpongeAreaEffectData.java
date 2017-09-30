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
package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAreaEffectCloudData;
import org.spongepowered.api.data.manipulator.mutable.entity.AreaEffectCloudData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeAreaEffectCloudData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.util.ImplementationRequiredForTest;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.mutable.SpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.ArrayList;
import java.util.List;

@ImplementationRequiredForTest
public class SpongeAreaEffectData extends AbstractData<AreaEffectCloudData, ImmutableAreaEffectCloudData> implements AreaEffectCloudData {

    private Color color;
    private double radius;
    private ParticleType particleType;
    private int duration;
    private int waitTime;
    private double radiusOnUse;
    private double radiusPerTick;
    private int durationOnUse;
    private int reapplicationDelay;
    private List<PotionEffect> potionEffects;
    private int age;

    public SpongeAreaEffectData(Color color,
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
        super(AreaEffectCloudData.class);
        this.color = color;
        this.radius = radius;
        this.particleType = particleType;
        this.duration = duration;
        this.waitTime = waitTime;
        this.radiusOnUse = radiusOnUse;
        this.radiusPerTick = radiusPerTick;
        this.durationOnUse = durationOnUse;
        this.reapplicationDelay = reapplicationDelay;
        this.potionEffects = new ArrayList<>(potionEffects.size());
        this.potionEffects.addAll(potionEffects);
        this.age = age;
        registerGettersAndSetters();
    }

    public SpongeAreaEffectData() {
        super(AreaEffectCloudData.class);
        registerGettersAndSetters();
    }


    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.AREA_EFFECT_CLOUD_AGE, () -> this.age);
        registerFieldSetter(Keys.AREA_EFFECT_CLOUD_AGE, (age) -> this.age = age < 0 ? 0 : age);
        registerKeyValue(Keys.AREA_EFFECT_CLOUD_AGE, this::age);

        registerFieldGetter(Keys.AREA_EFFECT_CLOUD_REAPPLICATION_DELAY, () -> this.reapplicationDelay);
        registerFieldSetter(Keys.AREA_EFFECT_CLOUD_REAPPLICATION_DELAY, (age) -> this.reapplicationDelay = age < 0 ? 0 : age);
        registerKeyValue(Keys.AREA_EFFECT_CLOUD_REAPPLICATION_DELAY, this::applicationDelay);

        registerFieldGetter(Keys.AREA_EFFECT_CLOUD_COLOR, () -> this.color);
        registerFieldSetter(Keys.AREA_EFFECT_CLOUD_COLOR, (color) -> this.color = color);
        registerKeyValue(Keys.AREA_EFFECT_CLOUD_COLOR, this::color);

        registerFieldGetter(Keys.AREA_EFFECT_CLOUD_DURATION, () -> this.duration);
        registerFieldSetter(Keys.AREA_EFFECT_CLOUD_DURATION, (duration) -> this.duration = duration < 0 ? 0 : duration);
        registerKeyValue(Keys.AREA_EFFECT_CLOUD_DURATION, this::duration);

        registerFieldGetter(Keys.AREA_EFFECT_CLOUD_DURATION_ON_USE, () -> this.durationOnUse);
        registerFieldSetter(Keys.AREA_EFFECT_CLOUD_DURATION_ON_USE, (durationOnUse) -> this.durationOnUse = durationOnUse < 0 ? 0 : durationOnUse);
        registerKeyValue(Keys.AREA_EFFECT_CLOUD_DURATION_ON_USE, this::durationOnUse);

        registerFieldGetter(Keys.AREA_EFFECT_CLOUD_PARTICLE_TYPE, () -> this.particleType);
        registerFieldSetter(Keys.AREA_EFFECT_CLOUD_PARTICLE_TYPE, (particleType) -> this.particleType = particleType);
        registerKeyValue(Keys.AREA_EFFECT_CLOUD_PARTICLE_TYPE, this::particleType);

        registerFieldGetter(Keys.AREA_EFFECT_CLOUD_RADIUS, () -> this.radius);
        registerFieldSetter(Keys.AREA_EFFECT_CLOUD_RADIUS, (radius) -> this.radius = radius < 0 ? 0 : radius);
        registerKeyValue(Keys.AREA_EFFECT_CLOUD_RADIUS, this::radius);

        registerFieldGetter(Keys.AREA_EFFECT_CLOUD_RADIUS_ON_USE, () -> this.radiusOnUse);
        registerFieldSetter(Keys.AREA_EFFECT_CLOUD_RADIUS_ON_USE, (radiusOnUse) -> this.radiusOnUse = radiusOnUse < 0 ? 0 : radiusOnUse);
        registerKeyValue(Keys.AREA_EFFECT_CLOUD_RADIUS_ON_USE, this::radiusOnUse);

        registerFieldGetter(Keys.AREA_EFFECT_CLOUD_WAIT_TIME, () -> this.waitTime);
        registerFieldSetter(Keys.AREA_EFFECT_CLOUD_WAIT_TIME, (waitTime) -> this.waitTime = waitTime < 0 ? 0 : waitTime);
        registerKeyValue(Keys.AREA_EFFECT_CLOUD_WAIT_TIME, this::waitTime);

        registerFieldGetter(Keys.POTION_EFFECTS, () -> this.potionEffects);
        registerFieldSetter(Keys.POTION_EFFECTS, (potionEffects1) -> this.potionEffects = potionEffects1);
        registerKeyValue(Keys.POTION_EFFECTS, this::effects);


    }

    @Override
    public AreaEffectCloudData copy() {
        return new SpongeAreaEffectData(this.color,
                this.radius,
                this.particleType,
                this.duration, this.waitTime,
                this.radiusOnUse,
                this.radiusPerTick,
                this.durationOnUse,
                this.reapplicationDelay,
                this.potionEffects,
                this.age);
    }

    @Override
    public ImmutableAreaEffectCloudData asImmutable() {
        return new ImmutableSpongeAreaEffectCloudData(this.color,
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
                .set(Keys.POTION_EFFECTS, this.potionEffects)
                ;
    }

    @Override
    public Value<Color> color() {
        return new SpongeValue<>(Keys.AREA_EFFECT_CLOUD_COLOR, Color.WHITE, this.color);
    }

    @Override
    public MutableBoundedValue<Double> radius() {
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_RADIUS)
                .minimum(0D)
                .maximum((double) Float.MAX_VALUE)
                .defaultValue(0.5D)
                .actualValue(this.radius)
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
                .actualValue(this.radiusOnUse)
                .build();
    }

    @Override
    public MutableBoundedValue<Double> radiusPerTick() {
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_RADIUS_PER_TICK)
                .minimum(0.0D)
                .maximum((double) Float.MAX_VALUE)
                .defaultValue(0.0D)
                .actualValue(this.radiusPerTick)
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
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_REAPPLICATION_DELAY)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(0)
                .actualValue(this.reapplicationDelay)
                .build();
    }

    @Override
    public ListValue<PotionEffect> effects() {
        return new SpongeListValue<>(Keys.POTION_EFFECTS, this.potionEffects);
    }

    @Override
    public MutableBoundedValue<Integer> age() {
        return SpongeValueFactory.boundedBuilder(Keys.AREA_EFFECT_CLOUD_AGE)
                .defaultValue(0)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .actualValue(this.age)
                .build();
    }
}
