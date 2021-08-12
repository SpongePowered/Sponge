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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.biome.provider.LayeredBiomeConfig;
import org.spongepowered.common.server.BootstrapProperties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public final class SpongeLayeredBiomeConfg extends AbstractBiomeProviderConfig implements LayeredBiomeConfig {

    private final long seed;
    private final boolean largeBiomes;

    protected SpongeLayeredBiomeConfg(final BuilderImpl builder) {
        super(builder.biomes);
        this.seed = builder.seed;
        this.largeBiomes = builder.largeBiomes;
    }

    @Override
    public long seed() {
        return this.seed;
    }

    @Override
    public boolean largeBiomes() {
        return this.largeBiomes;
    }
    
    public static final class BuilderImpl implements Builder {

        public List<RegistryReference<Biome>> biomes = new ArrayList<>();
        public long seed;
        public boolean largeBiomes;

        @Override
        public Builder seed(final long seed) {
            this.seed = seed;
            return this;
        }

        @Override
        public Builder addBiome(final RegistryReference<Biome> biome) {
            this.biomes.add(Objects.requireNonNull(biome, "biome"));
            return this;
        }

        @Override
        public Builder addBiomes(final List<RegistryReference<Biome>> biomes) {
            this.biomes.addAll(Objects.requireNonNull(biomes, "biomes"));
            return this;
        }

        @Override
        public Builder removeBiome(final RegistryReference<Biome> biome) {
            Objects.requireNonNull(biome, "biome");

            final Iterator<RegistryReference<Biome>> iter = this.biomes.iterator();
            while (iter.hasNext()) {
                if (iter.next().equals(biome)) {
                    iter.remove();
                    break;
                }
            }
            return this;
        }

        @Override
        public Builder largeBiomes(final boolean largeBiomes) {
            this.largeBiomes = largeBiomes;
            return this;
        }

        @Override
        public Builder from(final LayeredBiomeConfig value) {
            this.seed = Objects.requireNonNull(value, "value").seed();
            return this;
        }

        @Override
        public Builder reset() {
            this.seed = BootstrapProperties.worldGenSettings.seed();
            this.biomes.clear();
            this.largeBiomes = false;
            return this;
        }

        @Override
        public @NonNull LayeredBiomeConfig build() {
            if (this.biomes.isEmpty()) {
                throw new IllegalStateException("Layered biome config requires at least one biome!");
            }
            return new SpongeLayeredBiomeConfg(this);
        }
    }
}
