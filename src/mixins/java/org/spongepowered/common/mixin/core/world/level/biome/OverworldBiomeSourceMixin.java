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
package org.spongepowered.common.mixin.core.world.level.biome;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.level.biome.BiomeSourceAccessor;
import org.spongepowered.common.bridge.world.level.biome.OverworldBiomeSourceBridge;
import org.spongepowered.common.world.biome.provider.OverworldBiomeSourceHelper;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mixin(OverworldBiomeSource.class)
public abstract class OverworldBiomeSourceMixin extends BiomeSource implements OverworldBiomeSourceBridge {

    // @formatter: off

    @Shadow @Final private static List<ResourceKey<Biome>> POSSIBLE_BIOMES;

    // @formatter: on

    protected OverworldBiomeSourceMixin(final List<Biome> p_i231634_1_) {
        super(p_i231634_1_);
    }

    @Override
    public OverworldBiomeSource bridge$decorateData(final OverworldBiomeSourceHelper.SpongeDataSection data) {
        if (data.biomes == null || data.biomes.isEmpty()) {
            return (OverworldBiomeSource) (Object) this;
        }

        final List<Biome> biomes = ((BiomeSourceAccessor) this).accessor$possibleBiomes();
        biomes.clear();
        data.biomes.forEach(biome -> biomes.add(biome.get()));

        return (OverworldBiomeSource) (Object) this;
    }

    @Override
    public OverworldBiomeSourceHelper.SpongeDataSection bridge$createData() {
        final List<Biome> biomes = ((BiomeSourceAccessor) this).accessor$possibleBiomes();
        final List<Supplier<Biome>> suppliedBiomes = biomes.stream()
            // We only want to include what Vanilla doesn't ship by default, it is safe to allow reference equality in this scenario
            .filter(biome -> !OverworldBiomeSourceMixin.POSSIBLE_BIOMES.contains(biome))
            .map(biome -> (Supplier<Biome>) () -> biome)
            .collect(Collectors.toList());
        return new OverworldBiomeSourceHelper.SpongeDataSection(suppliedBiomes.isEmpty() ? null : suppliedBiomes);
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return OverworldBiomeSourceHelper.DIRECT_CODEC;
    }
}
