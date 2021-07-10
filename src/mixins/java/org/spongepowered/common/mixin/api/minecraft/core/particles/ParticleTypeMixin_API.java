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
package org.spongepowered.common.mixin.api.minecraft.core.particles;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.util.ParticleOptionUtil;

import java.util.Map;
import java.util.Optional;
import net.minecraft.core.particles.ParticleType;

@Mixin(net.minecraft.core.particles.ParticleType.class)
public abstract class ParticleTypeMixin_API implements org.spongepowered.api.effect.particle.ParticleType {

    private ImmutableMap<ParticleOption<?>, Object> api$defaultOptions = null;

    @SuppressWarnings("unchecked")
    @Override
    public <V> Optional<V> defaultOption(ParticleOption<V> option) {
        if (this.api$defaultOptions == null) {
            this.api$defaultOptions = ParticleOptionUtil.generateDefaultsForNamed((ParticleType<?>) (Object) this);
        }
        return Optional.ofNullable((V) this.api$defaultOptions.get(option));
    }

    @Override
    public Map<ParticleOption<?>, Object> defaultOptions() {
        if (this.api$defaultOptions == null) {
            this.api$defaultOptions = ParticleOptionUtil.generateDefaultsForNamed((ParticleType<?>) (Object) this);
        }
        return this.api$defaultOptions;
    }
}