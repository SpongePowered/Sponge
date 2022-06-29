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

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.util.Range;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.biome.BiomeAttributes;
import org.spongepowered.common.accessor.world.level.biome.OverworldBiomeBuilderAccessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class SpongeBiomeAttributesFactory implements BiomeAttributes.Factory {

    private final Climate.Parameter FULL_RANGE = Climate.Parameter.span(-1.0F, 1.0F);
    private static Map<ResourceKey, BiomeAttributes> DEFAULT_ATTRIBUTES;

    @Override
    public BiomeAttributes ofPoint(final float temperature, final float humidity, final float continentalness, final float erosion, final float depth, final float weirdness, final float offset) {
        var mcTemperature = Climate.Parameter.point(temperature);
        var mcHumidity = Climate.Parameter.point(humidity);
        var mcContinentalness = Climate.Parameter.point(continentalness);
        var mcErosion = Climate.Parameter.point(erosion);
        var mcDepth = Climate.Parameter.point(depth);
        var mcWeirdness = Climate.Parameter.point(weirdness);
        var mcOffset = Climate.quantizeCoord(offset);

        return (BiomeAttributes) (Object) new Climate.ParameterPoint(mcTemperature, mcHumidity, mcContinentalness, mcErosion, mcDepth, mcWeirdness, mcOffset);
    }

    @Override
    public BiomeAttributes ofRange(final Range<Float> temperature, final Range<Float> humidity, final Range<Float> continentalness,
            final Range<Float> erosion,
            final Range<Float> depth, final Range<Float> weirdness, final float offset) {
        var mcTemperature = Climate.Parameter.span(temperature.min(), temperature.max());
        var mcHumidity = Climate.Parameter.span(humidity.min(), humidity.max());
        var mcContinentalness = Climate.Parameter.span(continentalness.min(), continentalness.max());
        var mcErosion = Climate.Parameter.span(erosion.min(), erosion.max());
        var mcDepth = Climate.Parameter.span(depth.min(), depth.max());
        var mcWeirdness = Climate.Parameter.span(weirdness.min(), weirdness.max());
        var mcOffset = Climate.quantizeCoord(offset);

        return (BiomeAttributes) (Object) new Climate.ParameterPoint(mcTemperature, mcHumidity, mcContinentalness, mcErosion, mcDepth, mcWeirdness, mcOffset);
    }

    @Override
    public Range<Float> fullRange() {
        return (Range) (Object) FULL_RANGE;
    }

    @Override
    public Optional<BiomeAttributes> defaultAttributes(final RegistryReference<Biome> biome) {
        if (SpongeBiomeAttributesFactory.DEFAULT_ATTRIBUTES == null) {
            SpongeBiomeAttributesFactory.DEFAULT_ATTRIBUTES = new HashMap<>();
            ImmutableList.Builder<Pair<Climate.ParameterPoint, net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome>>> list = ImmutableList.builder();
            ((OverworldBiomeBuilderAccessor) (Object) new OverworldBiomeBuilder()).accessor$addBiomes(list::add);
            for (final var pair : list.build()) {
                DEFAULT_ATTRIBUTES.put((ResourceKey) (Object) pair.getSecond().location(), (BiomeAttributes) (Object) pair.getFirst());
            }
            // MultiNoiseBiomeSource.Preset#NETHER
            DEFAULT_ATTRIBUTES.put((ResourceKey) (Object) Biomes.SOUL_SAND_VALLEY.location(), (BiomeAttributes) (Object) Climate.parameters(0.0F, -0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            DEFAULT_ATTRIBUTES.put((ResourceKey) (Object) Biomes.CRIMSON_FOREST.location(), (BiomeAttributes) (Object) Climate.parameters(0.4F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            DEFAULT_ATTRIBUTES.put((ResourceKey) (Object) Biomes.WARPED_FOREST.location(), (BiomeAttributes) (Object) Climate.parameters(0.0F, 0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.375F));
            DEFAULT_ATTRIBUTES.put((ResourceKey) (Object) Biomes.BASALT_DELTAS.location(), (BiomeAttributes) (Object) Climate.parameters(-0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.175F));
        }
        return Optional.ofNullable(SpongeBiomeAttributesFactory.DEFAULT_ATTRIBUTES.get(biome.location()));
    }

}
