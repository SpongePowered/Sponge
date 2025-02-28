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
package org.spongepowered.common.event.cause.entity.damage;

import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DeathMessageType;
import org.spongepowered.api.event.cause.entity.damage.DamageEffect;
import org.spongepowered.api.event.cause.entity.damage.DamageScaling;
import org.spongepowered.api.event.cause.entity.damage.DamageType;

import java.util.Objects;

public final class SpongeDamageTypeBuilder implements DamageType.Builder {

    private String msgId;
    private net.minecraft.world.damagesource.DamageScaling scaling;
    private float exhaustion;
    private DamageEffects effects;
    private DeathMessageType deathMessageType;

    public SpongeDamageTypeBuilder() {
        this.reset();
    }

    @Override
    public DamageType.Builder from(final DamageType value) {
        var mcValue = (net.minecraft.world.damagesource.DamageType) (Object) value;
        this.msgId = mcValue.msgId();
        this.exhaustion = mcValue.exhaustion();
        this.scaling = mcValue.scaling();
        this.effects = mcValue.effects();
        this.deathMessageType = mcValue.deathMessageType();
        return this;
    }

    @Override
    public DamageType.Builder name(final String name) {
        this.msgId = name;
        return this;
    }

    @Override
    public DamageType.Builder scaling(final DamageScaling scaling) {
        this.scaling = (net.minecraft.world.damagesource.DamageScaling) (Object) scaling;
        return this;
    }

    @Override
    public DamageType.Builder exhaustion(final double exhaustion) {
        this.exhaustion = (float) exhaustion;
        return this;
    }

    @Override
    public DamageType.Builder effect(final DamageEffect effect) {
        this.effects = ((DamageEffects) (Object) effect);
        return this;
    }

    @Override
    public DamageType.Builder reset() {
        this.msgId = null;
        this.scaling = net.minecraft.world.damagesource.DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER;
        this.effects = DamageEffects.HURT;
        this.deathMessageType = DeathMessageType.DEFAULT;
        return this;
    }

    @Override
    public DamageType build() {
        Objects.requireNonNull(this.msgId, "name");
        Objects.requireNonNull(this.scaling, "scaling");
        Objects.requireNonNull(this.effects, "effects");
        Objects.requireNonNull(this.deathMessageType, "deathMessageType");
        return (DamageType) (Object) new net.minecraft.world.damagesource.DamageType(this.msgId, this.scaling, this.exhaustion, this.effects, this.deathMessageType);
    }
}
