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

import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.NoiseChunkGenerator;
import org.spongepowered.api.world.biome.provider.BiomeProvider;
import org.spongepowered.api.world.generation.ChunkGenerator;
import org.spongepowered.api.world.generation.ConfigurableChunkGenerator;
import org.spongepowered.api.world.generation.config.FlatGeneratorConfig;
import org.spongepowered.api.world.generation.config.NoiseGeneratorConfig;
import org.spongepowered.common.server.BootstrapProperties;

import java.util.Objects;

public final class SpongeChunkGeneratorFactory implements ChunkGenerator.Factory {

    @Override
    public <T extends FlatGeneratorConfig> ConfigurableChunkGenerator<T> flat(final T config) {
        return (ConfigurableChunkGenerator<T>) new FlatChunkGenerator((FlatGenerationSettings) config);
    }

    @Override
    public <T extends NoiseGeneratorConfig> ConfigurableChunkGenerator<T> noise(final BiomeProvider provider, final T config) {
        return (ConfigurableChunkGenerator<T>) (Object) new NoiseChunkGenerator((net.minecraft.world.biome.provider.BiomeProvider) Objects
                .requireNonNull(provider, "provider"), BootstrapProperties.dimensionGeneratorSettings.seed(), () ->
                (DimensionSettings) (Object) Objects.requireNonNull(config, "config"));
    }

    @Override
    public <T extends NoiseGeneratorConfig> ConfigurableChunkGenerator<T> noise(final BiomeProvider provider, final long seed, final T config) {
        return (ConfigurableChunkGenerator<T>) (Object) new NoiseChunkGenerator((net.minecraft.world.biome.provider.BiomeProvider)
                Objects.requireNonNull(provider, "provider"), seed, () ->
                (DimensionSettings) (Object) Objects.requireNonNull(config, "config"));
    }
}
