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

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.biome.AttributedBiome;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.biome.BiomeAttributes;
import org.spongepowered.api.world.biome.provider.MultiNoiseBiomeConfig;
import org.spongepowered.common.accessor.world.level.biome.MultiNoiseBiomeSourceAccessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class SpongeMultiNoiseBiomeConfig extends AbstractBiomeProviderConfig implements MultiNoiseBiomeConfig {

    private final List<AttributedBiome> biomes;

    private SpongeMultiNoiseBiomeConfig(final BuilderImpl builder) {
        super(builder.biomes.stream().map(AttributedBiome::biome).collect(Collectors.toList()));
        this.biomes = builder.biomes;
    }

    @Override
    public List<AttributedBiome> attributedBiomes() {
        return this.biomes;
    }

    
    public static final class BuilderImpl implements Builder {

        public final List<AttributedBiome> biomes = new ArrayList<>();

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

        public Builder addMcBiomes(final Climate.ParameterList<Supplier<net.minecraft.world.level.biome.Biome>> biomes) {
            for (var pair : biomes.values()) {
                var biome = RegistryTypes.BIOME.referenced(RegistryTypes.BIOME.keyFor(Sponge.server(), (Biome) (Object) pair.getSecond().get()));
                this.biomes.add(AttributedBiome.of(biome, (BiomeAttributes) (Object) pair.getFirst()));
            }
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
        public Builder from(final MultiNoiseBiomeConfig value) {
            this.biomes.clear();
            this.biomes.addAll(value.attributedBiomes());
            return this;
        }

        @Override
        public Builder reset() {
            this.biomes.clear();
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
            var biomeSource = (MultiNoiseBiomeSourceAccessor) MultiNoiseBiomeSource.Preset.NETHER.biomeSource((Registry) Sponge.server().registry(RegistryTypes.BIOME));
            return new BuilderImpl().addMcBiomes(biomeSource.accessor$parameters()).build();
        }

        @Override
        public MultiNoiseBiomeConfig overworld() {
            var biomeSource = (MultiNoiseBiomeSourceAccessor) MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource((Registry) Sponge.server().registry(RegistryTypes.BIOME));
            return new BuilderImpl().addMcBiomes(biomeSource.accessor$parameters()).build();
        }
    }
}
