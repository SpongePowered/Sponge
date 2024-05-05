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


import com.google.common.collect.ImmutableList;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.data.DataDeserializer;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.Preconditions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

@SuppressWarnings("unchecked")
public final class SpongeParticleEffectBuilder extends AbstractDataBuilder<ParticleEffect> implements ParticleEffect.Builder {

    private ParticleType type;
    private Map<ParticleOption<?>, Object> options;

    public SpongeParticleEffectBuilder() {
        super(ParticleEffect.class, 1);
        this.reset();
    }

    @Override
    protected Optional<ParticleEffect> buildContent(DataView container) throws InvalidDataException {
        if (!container.contains(Constants.Particles.PARTICLE_TYPE, Constants.Particles.PARTICLE_OPTIONS)) {
            return Optional.empty();
        }
        ParticleType particleType =
                container.getRegistryValue(Constants.Particles.PARTICLE_TYPE, RegistryTypes.PARTICLE_TYPE, Sponge.game()).get();
        Map<ParticleOption<?>, Object> options = new HashMap<>();
        container.getViewList(Constants.Particles.PARTICLE_OPTIONS).get().forEach(view -> {
            ParticleOption<?> option = view.getRegistryValue(Constants.Particles.PARTICLE_OPTION_KEY, RegistryTypes.PARTICLE_OPTION, Sponge.game()).get();
            final BiFunction<DataView, DataQuery, Optional<Object>> deserializer = DataDeserializer.deserializer(option.valueType());
            final Object value = deserializer.apply(view, Constants.Particles.PARTICLE_OPTION_VALUE).get();
            options.put(option, value);
        });
        return Optional.of(new SpongeParticleEffect(particleType, options));
    }

    @Override
    public ParticleEffect.Builder from(ParticleEffect particleEffect) {
        this.type = particleEffect.type();
        this.options = new HashMap<>(particleEffect.options());
        return this;
    }

    @Override
    public ParticleEffect.Builder type(ParticleType particleType) {
        this.type = Objects.requireNonNull(particleType, "particleType");
        return this;
    }

    @Override
    public ParticleEffect.Builder reset() {
        this.type = null;
        this.options = new HashMap<>();
        return this;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <V> ParticleEffect.Builder option(ParticleOption<V> option, V value) throws IllegalArgumentException {
        Objects.requireNonNull(option, "option");
        Objects.requireNonNull(value, "value");
        IllegalArgumentException exception = ((SpongeParticleOption<V>) option).validateValue(value);
        if (exception != null) {
            throw exception;
        }
        if (value instanceof List) {
            value = (V) ImmutableList.copyOf((List) value);
        }
        this.options.put(option, value);
        return this;
    }

    @Override
    public ParticleEffect build() {
        Preconditions.checkArgument(this.type != null, "ParticleType must be set");
        return new SpongeParticleEffect(this.type, this.options);
    }
}
