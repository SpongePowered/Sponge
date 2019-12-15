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
package org.spongepowered.common.data.provider.entity.areaeffectcloud;

import net.minecraft.entity.AreaEffectCloudEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Optional;

public class AreaEffectCloudEntityParticleEffectProvider extends GenericMutableDataProvider<AreaEffectCloudEntity, ParticleEffect> {

    public AreaEffectCloudEntityParticleEffectProvider() {
        super(Keys.AREA_EFFECT_CLOUD_PARTICLE_EFFECT);
    }

    @Override
    protected Optional<ParticleEffect> getFrom(AreaEffectCloudEntity dataHolder) {
        // TODO: Update particle effects and convert particle effect from particle data
        return Optional.of(ParticleEffect.builder().type(ParticleTypes.ENTITY_EFFECT).build());
    }

    @Override
    protected boolean set(AreaEffectCloudEntity dataHolder, ParticleEffect value) {
        // TODO: Update particle effects and convert particle effect to particle data
        return false;
    }
}
