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
package org.spongepowered.common.world.biome.provider;

import com.google.common.collect.Lists;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.biome.Biomes;
import org.spongepowered.api.world.biome.provider.EndStyleBiomeConfig;
import org.spongepowered.common.server.BootstrapProperties;

import java.util.Objects;

public final class SpongeEndStyleBiomeConfig extends AbstractBiomeProviderConfig implements EndStyleBiomeConfig {

    private final long seed;
    private final RegistryReference<Biome> endBiome, highlandsBiome, midlandsBiome, islandsBiome, barrensBiome;

    protected SpongeEndStyleBiomeConfig(final BuilderImpl builder) {
        super(Lists.newArrayList(builder.endBiome, builder.highlandsBiome, builder.midlandsBiome, builder.islandsBiome, builder.barrensBiome));
        this.seed = builder.seed;
        this.endBiome = builder.endBiome;
        this.highlandsBiome = builder.highlandsBiome;
        this.midlandsBiome = builder.midlandsBiome;
        this.islandsBiome = builder.islandsBiome;
        this.barrensBiome = builder.barrensBiome;
    }

    @Override
    public long seed() {
        return this.seed;
    }

    @Override
    public RegistryReference<Biome> endBiome() {
        return this.endBiome;
    }

    @Override
    public RegistryReference<Biome> highlandsBiome() {
        return this.highlandsBiome;
    }

    @Override
    public RegistryReference<Biome> midlandsBiome() {
        return this.midlandsBiome;
    }

    @Override
    public RegistryReference<Biome> islandsBiome() {
        return this.islandsBiome;
    }

    @Override
    public RegistryReference<Biome> barrensBiome() {
        return this.barrensBiome;
    }
    
    public static final class BuilderImpl implements Builder {

        public long seed;
        public RegistryReference<Biome> endBiome, highlandsBiome, midlandsBiome, islandsBiome, barrensBiome;

        @Override
        public Builder seed(final long seed) {
            this.seed = seed;
            return this;
        }

        @Override
        public Builder endBiome(final RegistryReference<Biome> endBiome) {
            this.endBiome = Objects.requireNonNull(endBiome, "endBiome");
            return this;
        }

        @Override
        public Builder highlandsBiome(final RegistryReference<Biome> highlandsBiome) {
            this.highlandsBiome = Objects.requireNonNull(highlandsBiome, "highlandsBiome");
            return this;
        }

        @Override
        public Builder midlandsBiome(final RegistryReference<Biome> midlandsBiome) {
            this.midlandsBiome = Objects.requireNonNull(midlandsBiome, "midlandsBiome");
            return this;
        }

        @Override
        public Builder islandsBiome(final RegistryReference<Biome> islandsBiome) {
            this.islandsBiome = Objects.requireNonNull(islandsBiome, "islandsBiome");
            return this;
        }

        @Override
        public Builder barrensBiome(final RegistryReference<Biome> barrensBiome) {
            this.barrensBiome = Objects.requireNonNull(barrensBiome, "barrensBiome");
            return this;
        }

        @Override
        public Builder from(final EndStyleBiomeConfig value) {
            this.seed = Objects.requireNonNull(value, "value").seed();
            this.endBiome = value.endBiome();
            this.highlandsBiome = value.highlandsBiome();
            this.midlandsBiome = value.midlandsBiome();
            this.islandsBiome = value.islandsBiome();
            this.barrensBiome = value.barrensBiome();
            return this;
        }

        @Override
        public Builder reset() {
            this.seed = BootstrapProperties.worldGenSettings.seed();
            this.endBiome = Biomes.THE_END;
            this.highlandsBiome = Biomes.END_HIGHLANDS;
            this.midlandsBiome = Biomes.END_MIDLANDS;
            this.islandsBiome = Biomes.SMALL_END_ISLANDS;
            this.barrensBiome = Biomes.END_BARRENS;
            return this;
        }

        @Override
        public @NonNull EndStyleBiomeConfig build() {
            return new SpongeEndStyleBiomeConfig(this);
        }
    }
}
