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
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
import net.minecraft.world.level.biome.Climate.ParameterList;
import net.minecraft.world.level.biome.Climate.ParameterPoint;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.world.biome.AttributedBiome;
import org.spongepowered.api.world.biome.provider.BiomeProvider;
import org.spongepowered.api.world.biome.provider.CheckerboardBiomeConfig;
import org.spongepowered.api.world.biome.provider.ConfigurableBiomeProvider;
import org.spongepowered.api.world.biome.provider.EndStyleBiomeConfig;
import org.spongepowered.api.world.biome.provider.FixedBiomeProvider;
import org.spongepowered.api.world.biome.provider.MultiNoiseBiomeConfig;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.world.level.biome.TheEndBiomeSourceAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public final class SpongeBiomeProviderFactory implements BiomeProvider.Factory {

    @Override
    public ConfigurableBiomeProvider<MultiNoiseBiomeConfig> overworld() {
        final var registry = SpongeCommon.vanillaRegistry(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
        final var holder = registry.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
        return (ConfigurableBiomeProvider<MultiNoiseBiomeConfig>) MultiNoiseBiomeSource.createFromPreset(holder);
    }

    @Override
    public ConfigurableBiomeProvider<MultiNoiseBiomeConfig> multiNoise(final MultiNoiseBiomeConfig config) {
        final List<Pair<ParameterPoint, Holder<Biome>>> climateParams = new ArrayList<>();
        for (final AttributedBiome attributedBiome : config.attributedBiomes()) {
            climateParams.add(Pair.of((ParameterPoint) (Object) attributedBiome.attributes(), this.biomeHolder(attributedBiome.biome())));
        }
        return (ConfigurableBiomeProvider<MultiNoiseBiomeConfig>) MultiNoiseBiomeSource.createFromList(new ParameterList<>(climateParams));
    }

    @Override
    public ConfigurableBiomeProvider<MultiNoiseBiomeConfig> nether() {
        final var registry = SpongeCommon.vanillaRegistry(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
        final var holder = registry.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER);
        return (ConfigurableBiomeProvider<MultiNoiseBiomeConfig>) MultiNoiseBiomeSource.createFromPreset(holder);
    }

    @Override
    public ConfigurableBiomeProvider<EndStyleBiomeConfig> endStyle(final EndStyleBiomeConfig config) {
        return (ConfigurableBiomeProvider<EndStyleBiomeConfig>) TheEndBiomeSourceAccessor.invoker$new(
                this.biomeHolder(config.endBiome()),
                this.biomeHolder(config.highlandsBiome()),
                this.biomeHolder(config.midlandsBiome()),
                this.biomeHolder(config.islandsBiome()),
                this.biomeHolder(config.barrensBiome())
        );
    }

    @Override
    public ConfigurableBiomeProvider<EndStyleBiomeConfig> end() {
        return (ConfigurableBiomeProvider<EndStyleBiomeConfig>) TheEndBiomeSource.create(this.registry());
    }

    @Override
    public ConfigurableBiomeProvider<CheckerboardBiomeConfig> checkerboard(final CheckerboardBiomeConfig config) {
        final List<Holder<Biome>> biomes = config.biomes().stream().map(this::biomeHolder).collect(Collectors.toList());
        return (ConfigurableBiomeProvider<CheckerboardBiomeConfig>) new CheckerboardColumnBiomeSource(HolderSet.direct(biomes), config.scale());
    }

    @Override
    public FixedBiomeProvider fixed(final RegistryReference<org.spongepowered.api.world.biome.Biome> biome) {
        Objects.requireNonNull(biome, "biome");
        return (FixedBiomeProvider) new FixedBiomeSource(this.biomeHolder(biome));
    }

    private Registry<Biome> registry() {
        return SpongeCommon.vanillaRegistry(Registries.BIOME);
    }

    private Holder<Biome> biomeHolder(final RegistryReference<org.spongepowered.api.world.biome.Biome> biome) {
        return this.registry().getOrThrow(ResourceKey.create(Registries.BIOME, (ResourceLocation) (Object) biome.location()));
    }
}
