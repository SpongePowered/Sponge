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

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.provider.CheckerboardBiomeProvider;
import net.minecraft.world.biome.provider.EndBiomeProvider;
import net.minecraft.world.biome.provider.NetherBiomeProvider;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.biome.BiomeProvider;
import org.spongepowered.common.server.BootstrapProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class SpongeBiomeProviderFactory implements BiomeProvider.Factory {

    @Override
    public BiomeProvider overworld(final boolean largeBiomes) {
        return (BiomeProvider) new OverworldBiomeProvider(BootstrapProperties.dimensionGeneratorSettings.seed(), false, largeBiomes, BootstrapProperties.registries.registryOrThrow(Registry.BIOME_REGISTRY));
    }

    @Override
    public BiomeProvider overworld(final long seed, final boolean largeBiomes) {
        return (BiomeProvider) new OverworldBiomeProvider(seed, false, largeBiomes, BootstrapProperties.registries.registryOrThrow(Registry.BIOME_REGISTRY));
    }

    @Override
    public BiomeProvider nether() {
        return (BiomeProvider) NetherBiomeProvider.Preset.NETHER.biomeSource(BootstrapProperties.registries.registryOrThrow(Registry.BIOME_REGISTRY), BootstrapProperties.dimensionGeneratorSettings.seed());
    }

    @Override
    public BiomeProvider nether(final long seed) {
        return (BiomeProvider) NetherBiomeProvider.Preset.NETHER.biomeSource(BootstrapProperties.registries.registryOrThrow(Registry.BIOME_REGISTRY), seed);
    }

    @Override
    public BiomeProvider end() {
        return (BiomeProvider) new EndBiomeProvider(BootstrapProperties.registries.registryOrThrow(Registry.BIOME_REGISTRY),
                BootstrapProperties.dimensionGeneratorSettings.seed());
    }

    @Override
    public BiomeProvider end(final long seed) {
        return (BiomeProvider) new EndBiomeProvider(BootstrapProperties.registries.registryOrThrow(Registry.BIOME_REGISTRY), seed);
    }

    @Override
    public BiomeProvider checkerboard(final List<RegistryReference<Biome>> biomes, final int scale) {
        if (Objects.requireNonNull(biomes, "biomes").isEmpty()) {
            throw new IllegalStateException("Checkboard biome provider requires at least one biome!");
        }
        final List<Supplier<net.minecraft.world.biome.Biome>> suppliedBiomes = new ArrayList<>();
        for (final RegistryReference<Biome> biome : biomes) {
            suppliedBiomes.add(() -> BootstrapProperties.registries.registryOrThrow(Registry.BIOME_REGISTRY).get((ResourceLocation) (Object) biome.location()));
        }

        return (BiomeProvider) new CheckerboardBiomeProvider(suppliedBiomes, scale);
    }

    @Override
    public BiomeProvider single(final RegistryReference<Biome> biome) {
        return (BiomeProvider) new SingleBiomeProvider(() -> BootstrapProperties.registries.registryOrThrow(Registry.BIOME_REGISTRY).get((ResourceLocation) (Object) Objects.requireNonNull(biome, "biome").location()));
    }
}
