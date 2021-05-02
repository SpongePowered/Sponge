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

import org.checkerframework.checker.nullness.qual.Nullable;

import org.spongepowered.common.accessor.world.level.biome.BiomeSourceAccessor;
import org.spongepowered.common.accessor.world.level.biome.OverworldBiomeSourceAccessor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.OverworldBiomeSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class SpongeLayeredBiomeProviderHelper {

    private static final Codec<SpongeDataSection> SPONGE_CODEC = RecordCodecBuilder
            .create(r -> r
                    .group(
                            Biome.LIST_CODEC.optionalFieldOf("biomes").forGetter(v -> Optional.of(v.biomes))
                    )
                    .apply(r, f1 -> new SpongeDataSection(f1.orElse(null)))
            );

    public static final Codec<OverworldBiomeSource> DIRECT_CODEC = RecordCodecBuilder
            .create(r -> r
                    .group(
                            Codec.LONG.fieldOf("seed").stable().forGetter(v -> ((OverworldBiomeSourceAccessor) v).accessor$seed()),
                            Codec.BOOL.optionalFieldOf("legacy_biome_init_layer", Boolean.FALSE, Lifecycle.stable()).forGetter(v -> ((OverworldBiomeSourceAccessor) v).accessor$legacyBiomeInitLayer()),
                            Codec.BOOL.fieldOf("large_biomes").orElse(false).stable().forGetter(v -> ((OverworldBiomeSourceAccessor) v).accessor$largeBiomes()),
                            RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(v -> ((OverworldBiomeSourceAccessor) v).accessor$biomes()),
                            SpongeLayeredBiomeProviderHelper.SPONGE_CODEC.optionalFieldOf("#sponge").forGetter(v -> {
                                final List<Biome> biomes = ((BiomeSourceAccessor) v).accessor$possibleBiomes();
                                final List<Supplier<Biome>> supplied = new ArrayList<>();
                                biomes.forEach(biome -> supplied.add(() -> biome));

                                return Optional.of(new SpongeDataSection(supplied));
                            })
                    )
                    .apply(r, r.stable((f1, f2, f3, f4, f5) -> {
                        final OverworldBiomeSource provider = new OverworldBiomeSource(f1, f2, f3, f4);
                        f5.ifPresent(v -> {
                            final List<Biome> biomes = new ArrayList<>();
                            v.biomes.forEach(biome -> biomes.add(biome.get()));

                            ((BiomeSourceAccessor) provider).accessor$possibleBiomes(biomes);
                        });
                        return provider;
                    }))
            );

    private static final class SpongeDataSection {
        public final List<Supplier<Biome>> biomes;

        private SpongeDataSection(final @Nullable List<Supplier<Biome>> biomes) {
            this.biomes = biomes;
        }
    }

}
