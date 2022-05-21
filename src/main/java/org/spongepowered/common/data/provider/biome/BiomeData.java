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
package org.spongepowered.common.data.provider.biome;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.EntityCategory;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.generation.biome.CarvingStep;
import org.spongepowered.api.world.generation.biome.DecorationStep;
import org.spongepowered.api.world.biome.spawner.NaturalSpawnCost;
import org.spongepowered.api.world.biome.spawner.NaturalSpawner;
import org.spongepowered.api.world.biome.climate.Precipitation;
import org.spongepowered.api.world.biome.climate.TemperatureModifier;
import org.spongepowered.api.world.biome.BiomeCategory;
import org.spongepowered.api.world.generation.biome.ConfiguredCarver;
import org.spongepowered.api.world.generation.feature.PlacedFeature;
import org.spongepowered.common.accessor.world.level.biome.BiomeAccessor;
import org.spongepowered.common.accessor.world.level.biome.MobSpawnSettingsAccessor;
import org.spongepowered.common.bridge.world.level.biome.BiomeBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class BiomeData {

    private BiomeData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asImmutable(Biome.class)
                    .create(Keys.BIOME_TEMPERATURE)
                        .get(h -> (double) h.getBaseTemperature())
                    .create(Keys.HUMIDITY)
                        .get(h -> (double) h.getDownfall())
                    .create(Keys.PRECIPITATION)
                        .get(h -> (Precipitation) (Object) h.getPrecipitation())
                    .create(Keys.CARVERS)
                        .get(BiomeData::carvers)
                    .create(Keys.FEATURES)
                        .get(BiomeData::features)
                    .create(Keys.SPAWN_CHANCE)
                        .get(h -> (double) h.getMobSettings().getCreatureProbability())
                    .create(Keys.NATURAL_SPAWNERS)
                        .get(BiomeData::naturalSpawners)
                    .create(Keys.NATURAL_SPAWNER_COST)
                        .get(BiomeData::naturalSpawnerCost)
                    .create(Keys.FOG_COLOR)
                        .get(h -> Color.ofRgb(h.getSpecialEffects().getFogColor()))
                    .create(Keys.WATER_COLOR)
                        .get(h -> Color.ofRgb(h.getSpecialEffects().getWaterColor()))
                    .create(Keys.WATER_FOG_COLOR)
                        .get(h -> Color.ofRgb(h.getSpecialEffects().getWaterFogColor()))
                    .create(Keys.FOLIAGE_COLOR)
                        .get(h -> h.getSpecialEffects().getFoliageColorOverride().map(Color::ofRgb).orElse(null))
                    .create(Keys.GRASS_COLOR)
                        .get(h -> h.getSpecialEffects().getGrassColorOverride().map(Color::ofRgb).orElse(null))
                .asImmutable(BiomeAccessor.class)
                    .create(Keys.BIOME_CATEGORY)
                        .get(h -> (BiomeCategory) (Object) h.accessor$biomeCategory())
                .asImmutable(BiomeBridge.class)
                    .create(Keys.TEMPERATURE_MODIFIER)
                        .get(h -> (TemperatureModifier) h.bridge$temperatureModifier())
        ;

    }
    // @formatter:on

    private static Map<CarvingStep, List<ConfiguredCarver<?, ?>>> carvers(final Biome biome) {
        final var settings = biome.getGenerationSettings();
        return Arrays.stream(GenerationStep.Carving.values())
                .collect(Collectors.toMap(step -> (CarvingStep) (Object) step,
                                          step -> BiomeData.carverList(settings, step)));
    }

    private static List<ConfiguredCarver<?, ?>> carverList(final BiomeGenerationSettings settings, final GenerationStep.Carving step) {
        final var carvers = settings.getCarvers(step);
        return StreamSupport.stream(carvers.spliterator(), false)
                .map(carver -> (ConfiguredCarver<?, ?>) (Object) carver.value())
                .collect(Collectors.toList());
    }

    private static Map<DecorationStep, List<PlacedFeature>> features(final Biome biome) {
        final var settings = biome.getGenerationSettings();
        return Arrays.stream(GenerationStep.Decoration.values())
                .collect(Collectors.toMap(step -> (DecorationStep) (Object) step,
                                          step -> BiomeData.featureList(settings, step)));
    }

    private static List<PlacedFeature> featureList(final BiomeGenerationSettings settings, final GenerationStep.Decoration step) {
        final var features = settings.features();
        final var holders = features.get(step.ordinal());
        return holders.stream().map(Holder::value).map(f -> (PlacedFeature) (Object) f).toList();
    }

    private static Map<EntityCategory, WeightedTable<NaturalSpawner>> naturalSpawners(Biome biome) {
        return Arrays.stream(MobCategory.values())
                .collect(Collectors.toMap(cat -> (EntityCategory) (Object) cat,
                                          cat -> BiomeData.naturalSpawner(biome, cat)));
    }

    private static WeightedTable<NaturalSpawner> naturalSpawner(Biome biome, MobCategory cat) {
        final List<MobSpawnSettings.SpawnerData> unwrap = biome.getMobSettings().getMobs(cat).unwrap();
        final WeightedTable<NaturalSpawner> result = new WeightedTable<>();
        unwrap.forEach(data -> result.add((NaturalSpawner) data, data.getWeight().asInt()));
        return result;
    }

    private static Map<EntityType<?>, NaturalSpawnCost> naturalSpawnerCost(Biome biome) {
        final var costs = ((MobSpawnSettingsAccessor) biome.getMobSettings()).accessor$mobSpawnCosts();
        return costs.entrySet().stream()
                        .collect(Collectors.toMap(e -> (EntityType<?>) e.getKey(),
                                                  e -> (NaturalSpawnCost) e.getValue()));
    }
}
