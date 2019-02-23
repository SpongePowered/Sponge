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

import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.util.EnumParticleTypes;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeMutableValue;
import org.spongepowered.common.effect.particle.SpongeParticleType;

import java.util.Optional;

public class AreaEffectCloudParticleTypeProcessor extends AbstractSpongeValueProcessor<EntityAreaEffectCloud, ParticleType> {

    public AreaEffectCloudParticleTypeProcessor() {
        super(EntityAreaEffectCloud.class, Keys.AREA_EFFECT_CLOUD_PARTICLE_TYPE);
    }

    @Override
    protected Value.Mutable<ParticleType> constructMutableValue(ParticleType actualValue) {
        return new SpongeMutableValue<ParticleType>(Keys.AREA_EFFECT_CLOUD_PARTICLE_TYPE, actualValue);
    }

    @Override
    protected boolean set(EntityAreaEffectCloud container, ParticleType value) {

        final net.minecraft.particles.ParticleType internalType = ((SpongeParticleType) value).getInternalType();
        if (internalType == null) {
            return false;
        }
        container.func_195059_a(internalType);
        return true;
    }

    @Override
    protected Optional<ParticleType> getVal(EntityAreaEffectCloud container) {
        return Optional.of(ParticleTypes.MOB_SPELL);
    }

    @Override
    protected Value.Immutable<ParticleType> constructImmutableValue(ParticleType value) {
        return constructMutableValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
