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
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.world.biome.AttributedBiome;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.biome.BiomeAttributes;
import org.spongepowered.api.world.biome.Biomes;
import org.spongepowered.api.world.biome.provider.MultiNoiseBiomeConfig;
import org.spongepowered.api.world.biome.provider.multinoise.MultiNoiseConfig;
import org.spongepowered.common.accessor.world.level.biome.MultiNoiseBiomeSourceAccessor;
import org.spongepowered.common.server.BootstrapProperties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class SpongeMultiNoiseBiomeConfig extends AbstractBiomeProviderConfig implements MultiNoiseBiomeConfig {

    private final long seed;
    private final List<AttributedBiome> biomes;
    private final MultiNoiseConfig temperatureConfig, humidityConfig, altitudeConfig, weirdnessConfig;

    protected SpongeMultiNoiseBiomeConfig(final BuilderImpl builder) {
        super(builder.biomes.stream().map(b -> b.biome()).collect(Collectors.toList()));
        this.seed = builder.seed;
        this.biomes = builder.biomes;
        this.temperatureConfig = builder.temperatureConfig;
        this.humidityConfig = builder.humidityConfig;
        this.altitudeConfig = builder.altitudeConfig;
        this.weirdnessConfig = builder.weirdnessConfig;
    }

    @Override
    public long seed() {
        return this.seed;
    }

    @Override
    public List<AttributedBiome> attributedBiomes() {
        return this.biomes;
    }

    @Override
    public MultiNoiseConfig temperatureConfig() {
        return this.temperatureConfig;
    }

    @Override
    public MultiNoiseConfig humidityConfig() {
        return this.humidityConfig;
    }

    @Override
    public MultiNoiseConfig altitudeConfig() {
        return this.altitudeConfig;
    }

    @Override
    public MultiNoiseConfig weirdnessConfig() {
        return this.weirdnessConfig;
    }
    
    public static final class BuilderImpl implements Builder {

        public long seed;
        public final List<AttributedBiome> biomes = new ArrayList<>();
        public MultiNoiseConfig temperatureConfig, humidityConfig, altitudeConfig, weirdnessConfig;

        @Override
        public Builder seed(final long seed) {
            this.seed = seed;
            return this;
        }

        @Override
        public Builder addBiome(final AttributedBiome biome) {
            this.biomes.add(Objects.requireNonNull(biome, "biome"));
            return this;
        }

        @Override
        public Builder addBiomes(final List<AttributedBiome> biomes) {
            this.biomes.addAll(Objects.requireNonNull(biomes, "biomes"));
            return this;
        }

        @Override
        public Builder removeBiome(final RegistryReference<Biome> biome) {
            Objects.requireNonNull(biome, "biome");

            final Iterator<AttributedBiome> iter = this.biomes.iterator();
            while (iter.hasNext()) {
                if (iter.next().biome().equals(biome)) {
                    iter.remove();
                    break;
                }
            }
            return this;
        }

        @Override
        public Builder temperatureConfig(final MultiNoiseConfig temperatureConfig) {
            this.temperatureConfig = Objects.requireNonNull(temperatureConfig, "temperatureConfig");
            return this;
        }

        @Override
        public Builder humidityConfig(final MultiNoiseConfig humidityConfig) {
            this.humidityConfig = Objects.requireNonNull(humidityConfig, "humidityConfig");
            return this;
        }

        @Override
        public Builder altitudeConfig(final MultiNoiseConfig altitudeConfig) {
            this.altitudeConfig = Objects.requireNonNull(altitudeConfig, "altitudeConfig");
            return this;
        }

        @Override
        public Builder weirdnessConfig(final MultiNoiseConfig weirdnessConfig) {
            this.weirdnessConfig = Objects.requireNonNull(weirdnessConfig, "weirdnessConfig");
            return this;
        }

        @Override
        public Builder from(final MultiNoiseBiomeConfig value) {
            this.biomes.clear();
            this.seed = Objects.requireNonNull(value, "value").seed();
            this.biomes.addAll(value.attributedBiomes());
            this.temperatureConfig = value.temperatureConfig();
            this.humidityConfig = value.humidityConfig();
            this.altitudeConfig = value.altitudeConfig();
            this.weirdnessConfig = value.weirdnessConfig();
            return this;
        }

        @Override
        public Builder reset() {
            this.biomes.clear();
            this.seed = BootstrapProperties.worldGenSettings.seed();
            final MultiNoiseBiomeSource.NoiseParameters defaultNoise = MultiNoiseBiomeSourceAccessor.accessor$DEFAULT_NOISE_PARAMETERS();
            this.temperatureConfig = (MultiNoiseConfig) defaultNoise;
            this.humidityConfig = (MultiNoiseConfig) defaultNoise;
            this.altitudeConfig = (MultiNoiseConfig) defaultNoise;
            this.weirdnessConfig = (MultiNoiseConfig) defaultNoise;
            return this;
        }

        @Override
        public @NonNull MultiNoiseBiomeConfig build() {
            if (this.biomes.isEmpty()) {
                throw new IllegalStateException("MultiNoise biome config requires at least one biome!");
            }
            return new SpongeMultiNoiseBiomeConfig(this);
        }
    }

    public static final class FactoryImpl implements Factory {

        @Override
        public MultiNoiseBiomeConfig nether() {
            return new BuilderImpl()
                    .addBiomes(Lists.newArrayList(
                            AttributedBiome.of(Biomes.NETHER_WASTES, BiomeAttributes.of(0, 0, 0, 0, 0)),
                            AttributedBiome.of(Biomes.SOUL_SAND_VALLEY, BiomeAttributes.of(0, -0.5F, 0, 0, 0)),
                            AttributedBiome.of(Biomes.CRIMSON_FOREST, BiomeAttributes.of(0.4F, 0, 0, 0, 0)),
                            AttributedBiome.of(Biomes.WARPED_FOREST, BiomeAttributes.of(0, 0.5F, 0, 0, 0.375F)),
                            AttributedBiome.of(Biomes.BASALT_DELTAS, BiomeAttributes.of(-0.5F, 0, 0, 0, 0.175F))
                    ))
                    .build();
        }
    }
}
