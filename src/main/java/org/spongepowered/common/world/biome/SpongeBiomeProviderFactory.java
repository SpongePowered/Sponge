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

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.provider.CheckerboardBiomeProvider;
import net.minecraft.world.biome.provider.EndBiomeProvider;
import net.minecraft.world.biome.provider.NetherBiomeProvider;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.world.biome.AttributedBiome;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.biome.provider.BiomeProvider;
import org.spongepowered.api.world.biome.provider.CheckerboardBiomeConfig;
import org.spongepowered.api.world.biome.provider.ConfigurableBiomeProvider;
import org.spongepowered.api.world.biome.provider.EndStyleBiomeConfig;
import org.spongepowered.api.world.biome.provider.LayeredBiomeConfig;
import org.spongepowered.api.world.biome.provider.MultiNoiseBiomeConfig;
import org.spongepowered.common.accessor.world.biome.provider.BiomeProviderAccessor;
import org.spongepowered.common.accessor.world.biome.provider.EndBiomeProviderAccessor;
import org.spongepowered.common.accessor.world.biome.provider.NetherBiomeProviderAccessor;
import org.spongepowered.common.server.BootstrapProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public final class SpongeBiomeProviderFactory implements BiomeProvider.Factory {

    @Override
    public <T extends LayeredBiomeConfig> ConfigurableBiomeProvider<T> layered(final T config) {
        final MutableRegistry<net.minecraft.world.biome.Biome> biomeRegistry = BootstrapProperties.registries.registryOrThrow(Registry.BIOME_REGISTRY);
        final OverworldBiomeProvider layeredBiomeProvider = new OverworldBiomeProvider(config.seed(), config.largeBiomes(), false,
                biomeRegistry);
        final List<net.minecraft.world.biome.Biome> biomes = new ArrayList<>();
        for (final RegistryReference<Biome> biome : config.biomes()) {
            biomes.add(biomeRegistry.get((ResourceLocation) (Object) biome.location()));
        }
        ((BiomeProviderAccessor) layeredBiomeProvider).accessor$possibleBiomes(biomes);
        return (ConfigurableBiomeProvider<T>) layeredBiomeProvider;
    }

    @Override
    public ConfigurableBiomeProvider<LayeredBiomeConfig> overworld() {
        return (ConfigurableBiomeProvider<LayeredBiomeConfig>) new OverworldBiomeProvider(BootstrapProperties.dimensionGeneratorSettings.seed(), false, false,
            BootstrapProperties.registries.registryOrThrow(Registry.BIOME_REGISTRY));
    }

    @Override
    public <T extends MultiNoiseBiomeConfig> ConfigurableBiomeProvider<T> multiNoise(final T config) {
        final MutableRegistry<net.minecraft.world.biome.Biome> biomeRegistry = BootstrapProperties.registries.registryOrThrow(Registry.BIOME_REGISTRY);
        final List<Pair<net.minecraft.world.biome.Biome.Attributes, Supplier<net.minecraft.world.biome.Biome>>> attributedBiomes = new ArrayList<>();
        for (final AttributedBiome attributedBiome : config.attributedBiomes()) {
            attributedBiomes.add(Pair.of((net.minecraft.world.biome.Biome.Attributes) attributedBiome.attributes(),
                    () -> biomeRegistry.get((ResourceLocation) (Object) attributedBiome.biome().location())));
        }
        return (ConfigurableBiomeProvider<T>) NetherBiomeProviderAccessor.invoker$new(config.seed(), attributedBiomes,
                (NetherBiomeProvider.Noise) config.temperatureConfig(), (NetherBiomeProvider.Noise) config.humidityConfig(),
                (NetherBiomeProvider.Noise) config.altitudeConfig(), (NetherBiomeProvider.Noise) config.weirdnessConfig(), Optional.empty());
    }

    @Override
    public ConfigurableBiomeProvider<MultiNoiseBiomeConfig> nether() {
        return (ConfigurableBiomeProvider<MultiNoiseBiomeConfig>) NetherBiomeProvider.Preset.NETHER.biomeSource(BootstrapProperties.registries.registryOrThrow(Registry.BIOME_REGISTRY), BootstrapProperties.dimensionGeneratorSettings.seed());
    }

    @Override
    public <T extends EndStyleBiomeConfig> ConfigurableBiomeProvider<T> endStyle(final T config) {
        final MutableRegistry<net.minecraft.world.biome.Biome> registry = BootstrapProperties.registries.registryOrThrow(Registry.BIOME_REGISTRY);
        return (ConfigurableBiomeProvider<T>) EndBiomeProviderAccessor.invoker$new(registry,
                config.seed(),
                registry.get((ResourceLocation) (Object) config.endBiome().location()),
                registry.get((ResourceLocation) (Object) config.highlandsBiome().location()),
                registry.get((ResourceLocation) (Object) config.midlandsBiome().location()),
                registry.get((ResourceLocation) (Object) config.islandsBiome().location()),
                registry.get((ResourceLocation) (Object) config.barrensBiome().location())
        );
    }

    @Override
    public ConfigurableBiomeProvider<EndStyleBiomeConfig> end() {
        return (ConfigurableBiomeProvider<EndStyleBiomeConfig>) new EndBiomeProvider(BootstrapProperties.registries.registryOrThrow(Registry.BIOME_REGISTRY), BootstrapProperties.dimensionGeneratorSettings.seed());
    }

    @Override
    public <T extends CheckerboardBiomeConfig> ConfigurableBiomeProvider<T> checkerboard(final T config) {
        final List<Supplier<net.minecraft.world.biome.Biome>> biomes = new ArrayList<>();
        for (final RegistryReference<Biome> biome : config.biomes()) {
            biomes.add(() -> BootstrapProperties.registries.registryOrThrow(Registry.BIOME_REGISTRY).get((ResourceLocation) (Object) biome.location()));
        }

        return (ConfigurableBiomeProvider<T>) new CheckerboardBiomeProvider(biomes, config.scale());
    }

    @Override
    public BiomeProvider fixed(final RegistryReference<Biome> biome) {
        Objects.requireNonNull(biome, "biome");

        return (BiomeProvider) new SingleBiomeProvider(() -> (net.minecraft.world.biome.Biome) (Object) biome.get(Sponge.getServer().registries()));
    }
}
