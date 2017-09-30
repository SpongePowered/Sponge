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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.EnumParticleTypes;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.common.SpongeCatalogType;

import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeParticleType extends SpongeCatalogType implements ParticleType {

    private final String name;
    @Nullable private final EnumParticleTypes internalType;
    private final Map<ParticleOption<?>, Object> options;

    public SpongeParticleType(String id, String name, @Nullable EnumParticleTypes internalType,
            Map<ParticleOption<?>, Object> options) {
        super(id);
        this.options = ImmutableMap.copyOf(options);
        this.internalType = internalType;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Nullable
    public EnumParticleTypes getInternalType() {
        return this.internalType;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .omitNullValues()
                .add("internalType", this.internalType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> Optional<V> getDefaultOption(ParticleOption<V> option) {
        return Optional.ofNullable((V) this.options.get(option));
    }

    @Override
    public Map<ParticleOption<?>, Object> getDefaultOptions() {
        return this.options;
    }

}
