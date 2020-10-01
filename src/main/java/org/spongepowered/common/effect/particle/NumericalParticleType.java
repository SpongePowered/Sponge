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
package org.spongepowered.common.effect.particle;

import com.google.common.collect.ImmutableMap;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.common.SpongeCatalogType;

import java.util.Map;
import java.util.Optional;

public final class NumericalParticleType extends SpongeCatalogType implements ParticleType {

    private final int id;
    private final Map<ParticleOption<?>, Object> defaultOptions;
    @Nullable
    private final DataCalculator dataCalculator;

    public NumericalParticleType(int id, ResourceKey key, Map<ParticleOption<?>, Object> defaultOptions, @Nullable DataCalculator dataCalculator) {
        super(key);
        this.id = id;
        this.defaultOptions = ImmutableMap.copyOf(defaultOptions);
        this.dataCalculator = dataCalculator;
    }

    public NumericalParticleType(int id, ResourceKey key) {
        super(key);
        this.id = id;
        this.defaultOptions = ImmutableMap.of();
        this.dataCalculator = null;
    }

    public int getId() {
        return this.id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> Optional<V> getDefaultOption(final ParticleOption<V> option) {
        return Optional.ofNullable((V) this.defaultOptions.get(option));
    }

    @Override
    public Map<ParticleOption<?>, Object> getDefaultOptions() {
        return this.defaultOptions;
    }

    public int getData(ParticleEffect effect) {
        if (this.dataCalculator == null) {
            return 0;
        } else {
            return this.dataCalculator.getData(effect);
        }
    }

    public interface DataCalculator {
        int getData(ParticleEffect effect);
    }
}