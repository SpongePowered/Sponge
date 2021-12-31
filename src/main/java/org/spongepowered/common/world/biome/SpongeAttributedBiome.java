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
package org.spongepowered.common.world.biome;

import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.world.biome.AttributedBiome;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.biome.BiomeAttributes;

import java.util.Objects;

public final class SpongeAttributedBiome implements AttributedBiome {

    private final RegistryReference<Biome> biome;
    private final BiomeAttributes attributes;

    private SpongeAttributedBiome(final RegistryReference<Biome> biome, final BiomeAttributes attributes) {
        this.biome = biome;
        this.attributes = attributes;
    }

    @Override
    public RegistryReference<Biome> biome() {
        return this.biome;
    }

    @Override
    public BiomeAttributes attributes() {
        return this.attributes;
    }

    public static final class FactoryImpl implements AttributedBiome.Factory {

        @Override
        public AttributedBiome of(final RegistryReference<Biome> biome, final BiomeAttributes attributes) {
            return new SpongeAttributedBiome(Objects.requireNonNull(biome, "biome"), Objects.requireNonNull(attributes, "attributes"));
        }
    }
}
