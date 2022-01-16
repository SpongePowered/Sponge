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
package org.spongepowered.common.world.generation;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.biome.provider.BiomeProvider;
import org.spongepowered.api.world.generation.ChunkGenerator;
import org.spongepowered.api.world.generation.ConfigurableChunkGenerator;
import org.spongepowered.api.world.generation.config.FlatGeneratorConfig;
import org.spongepowered.api.world.generation.config.NoiseGeneratorConfig;
import org.spongepowered.common.server.BootstrapProperties;
import org.spongepowered.common.util.SeedUtil;

import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public final class SpongeChunkGeneratorFactory implements ChunkGenerator.Factory {

    @Override
    public <T extends FlatGeneratorConfig> ConfigurableChunkGenerator<T> flat(final T config) {
        return (ConfigurableChunkGenerator<T>) new FlatLevelSource((FlatLevelGeneratorSettings) config);
    }

    private <T extends NoiseGeneratorConfig> ConfigurableChunkGenerator<T> noiseBasedChunkGenerator(final long seed, final BiomeSource biomeSource, final Supplier<NoiseGeneratorSettings> noiseGeneratorSettings) {
        var noiseRegistry = BootstrapProperties.registries.registryOrThrow(Registry.NOISE_REGISTRY);
        return (ConfigurableChunkGenerator<T>) (Object)
                new NoiseBasedChunkGenerator(noiseRegistry, biomeSource, seed, noiseGeneratorSettings);
    }

    @Override
    public <T extends NoiseGeneratorConfig> ConfigurableChunkGenerator<T> noise(final BiomeProvider provider, final T config) {
        var seed = BootstrapProperties.worldGenSettings.seed();
        var biomeSource = (BiomeSource) Objects.requireNonNull(provider, "provider");
        var noiseGeneratorSettings = (NoiseGeneratorSettings) (Object) Objects.requireNonNull(config, "config");
        return this.noiseBasedChunkGenerator(seed, biomeSource, () -> noiseGeneratorSettings);
    }

    @Override
    public <T extends NoiseGeneratorConfig> ConfigurableChunkGenerator<T> noise(final BiomeProvider provider, final long seed, final T config) {
        var biomeSource = (net.minecraft.world.level.biome.BiomeSource) Objects.requireNonNull(provider, "provider");
        var noiseGeneratorSettings = (NoiseGeneratorSettings) (Object) Objects.requireNonNull(config, "config");
        return this.noiseBasedChunkGenerator(seed, biomeSource, () -> noiseGeneratorSettings);
    }

    @Override
    public <T extends NoiseGeneratorConfig> ConfigurableChunkGenerator<T> noise(final BiomeProvider provider, final String seed, final T config) {
        return this.noise(provider, SeedUtil.compute(seed), config);
    }

    @Override
    public ConfigurableChunkGenerator<NoiseGeneratorConfig> overworld() {
        var seed = BootstrapProperties.worldGenSettings.seed();
        return (ConfigurableChunkGenerator<NoiseGeneratorConfig>) (Object)
                WorldGenSettings.makeDefaultOverworld(BootstrapProperties.registries, seed);
    }

    @Override
    public ConfigurableChunkGenerator<NoiseGeneratorConfig> theNether() {
        var biomeRegistry = (Registry<Biome>) Sponge.server().registry(RegistryTypes.BIOME);
        var noiseGeneratorSettingsRegistry = BootstrapProperties.registries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);

        var seed = BootstrapProperties.worldGenSettings.seed();
        var biomeSource = MultiNoiseBiomeSource.Preset.NETHER.biomeSource(biomeRegistry);

        return this.noiseBasedChunkGenerator(seed, biomeSource, () -> noiseGeneratorSettingsRegistry.getOrThrow(NoiseGeneratorSettings.NETHER));
    }

    @Override
    public ConfigurableChunkGenerator<NoiseGeneratorConfig> theEnd() {
        var biomeRegistry = (Registry<Biome>) Sponge.server().registry(RegistryTypes.BIOME);
        var noiseGeneratorSettingsRegistry = BootstrapProperties.registries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);

        var seed = BootstrapProperties.worldGenSettings.seed();
        var biomeSource = new TheEndBiomeSource(biomeRegistry, seed);

        return this.noiseBasedChunkGenerator(seed, biomeSource, () -> noiseGeneratorSettingsRegistry.getOrThrow(NoiseGeneratorSettings.END));
    }
}
