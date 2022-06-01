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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.Climate.Parameter;
import net.minecraft.world.level.biome.Climate.ParameterList;
import net.minecraft.world.level.biome.Climate.ParameterPoint;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.biome.AttributedBiome;
import org.spongepowered.api.world.biome.BiomeAttributes;
import org.spongepowered.api.world.biome.provider.BiomeProvider;
import org.spongepowered.api.world.biome.provider.CheckerboardBiomeConfig;
import org.spongepowered.api.world.biome.provider.ConfigurableBiomeProvider;
import org.spongepowered.api.world.biome.provider.EndStyleBiomeConfig;
import org.spongepowered.api.world.biome.provider.MultiNoiseBiomeConfig;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.world.level.biome.MultiNoiseBiomeSourceAccessor;
import org.spongepowered.common.accessor.world.level.biome.TheEndBiomeSourceAccessor;
import org.spongepowered.common.server.BootstrapProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public final class SpongeBiomeProviderFactory implements BiomeProvider.Factory {

    @Override
    public ConfigurableBiomeProvider<MultiNoiseBiomeConfig> overworld() {
        final Registry<Biome> biomeRegistry = (Registry<Biome>) Sponge.server().registry(RegistryTypes.BIOME);
        return (ConfigurableBiomeProvider<MultiNoiseBiomeConfig>) MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(biomeRegistry);
    }

    @Override
    public <T extends MultiNoiseBiomeConfig> ConfigurableBiomeProvider<T> multiNoise(final T config) {
        final Registry<Biome> biomeRegistry = (Registry<Biome>) Sponge.server().registry(RegistryTypes.BIOME);
        final List<Pair<ParameterPoint, Holder<Biome>>> climateParams = new ArrayList<>();
        for (final AttributedBiome attributedBiome : config.attributedBiomes()) {
            final BiomeAttributes attr = attributedBiome.attributes();
            final ParameterPoint point = Climate.parameters(
                    Parameter.span(attr.temperature().min(), attr.temperature().max()),
                    Parameter.span(attr.humidity().min(), attr.humidity().max()),
                    Parameter.span(attr.continentalness().min(), attr.continentalness().max()),
                    Parameter.span(attr.erosion().min(), attr.erosion().max()),
                    Parameter.span(attr.depth().min(), attr.depth().max()),
                    Parameter.span(attr.weirdness().min(), attr.weirdness().max()),
                    attr.offset());
            final Holder<Biome> biome = biomeRegistry.getHolderOrThrow(
                    ResourceKey.create(Registry.BIOME_REGISTRY, (ResourceLocation) (Object) attributedBiome.biome().location()));
            climateParams.add(Pair.of(point, biome));
        }
        return (ConfigurableBiomeProvider<T>) MultiNoiseBiomeSourceAccessor.invoker$new(new ParameterList<>(climateParams));
    }

    @Override
    public ConfigurableBiomeProvider<MultiNoiseBiomeConfig> nether() {
        final Registry<Biome> biomeRegistry = (Registry<Biome>) Sponge.server().registry(RegistryTypes.BIOME);
        return (ConfigurableBiomeProvider<MultiNoiseBiomeConfig>) MultiNoiseBiomeSource.Preset.NETHER.biomeSource(biomeRegistry);
    }

    @Override
    public <T extends EndStyleBiomeConfig> ConfigurableBiomeProvider<T> endStyle(final T config) {
        final Registry<Biome> biomeRegistry = (Registry<Biome>) Sponge.server().registry(RegistryTypes.BIOME);
        return (ConfigurableBiomeProvider<T>) TheEndBiomeSourceAccessor.invoker$new(
                biomeRegistry.getHolderOrThrow(ResourceKey.create(Registry.BIOME_REGISTRY, (ResourceLocation) (Object) config.endBiome().location())),
                biomeRegistry.getHolderOrThrow(ResourceKey.create(Registry.BIOME_REGISTRY, (ResourceLocation) (Object) config.highlandsBiome().location())),
                biomeRegistry.getHolderOrThrow(ResourceKey.create(Registry.BIOME_REGISTRY, (ResourceLocation) (Object) config.midlandsBiome().location())),
                biomeRegistry.getHolderOrThrow(ResourceKey.create(Registry.BIOME_REGISTRY, (ResourceLocation) (Object) config.islandsBiome().location())),
                biomeRegistry.getHolderOrThrow(ResourceKey.create(Registry.BIOME_REGISTRY, (ResourceLocation) (Object) config.barrensBiome().location()))
        );
    }

    @Override
    public ConfigurableBiomeProvider<EndStyleBiomeConfig> end() {
        final Registry<Biome> biomeRegistry = (Registry<Biome>) Sponge.server().registry(RegistryTypes.BIOME);
        final long seed = SpongeCommon.server().getWorldData().worldGenSettings().seed(); // TODO no more custom seed?
        return (ConfigurableBiomeProvider<EndStyleBiomeConfig>) new TheEndBiomeSource(biomeRegistry);
    }

    @Override
    public <T extends CheckerboardBiomeConfig> ConfigurableBiomeProvider<T> checkerboard(final T config) {
        final Registry<Biome> biomeRegistry = (Registry<Biome>) Sponge.server().registry(RegistryTypes.BIOME);
        final List<Holder<Biome>> biomes = new ArrayList<>();
        for (final RegistryReference<org.spongepowered.api.world.biome.Biome> biome : config.biomes()) {
            final ResourceKey<Biome> key = ResourceKey.create(Registry.BIOME_REGISTRY, (ResourceLocation) (Object) biome.location());
            biomes.add(biomeRegistry.getHolderOrThrow(key));
        }

        return (ConfigurableBiomeProvider<T>) new CheckerboardColumnBiomeSource(HolderSet.direct(biomes), config.scale());
    }

    @Override
    public BiomeProvider fixed(final RegistryReference<org.spongepowered.api.world.biome.Biome> biome) {
        Objects.requireNonNull(biome, "biome");

        return (BiomeProvider) new FixedBiomeSource((Holder<Biome>) (Object) biome);
    }
}
