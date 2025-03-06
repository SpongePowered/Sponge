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

import net.minecraft.core.Holder;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.EntityCategory;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.biome.ambient.SoundConfig;
import org.spongepowered.api.world.biome.climate.GrassColorModifiers;
import org.spongepowered.api.world.biome.climate.TemperatureModifiers;
import org.spongepowered.api.world.biome.spawner.NaturalSpawnCost;
import org.spongepowered.api.world.biome.spawner.NaturalSpawner;
import org.spongepowered.api.world.generation.carver.Carver;
import org.spongepowered.api.world.generation.feature.DecorationStep;
import org.spongepowered.api.world.generation.feature.PlacedFeature;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.provider.DataProviderLookup;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SpongeBiomeBuilder implements Biome.Builder {

    private static DataProviderLookup PROVIDER_LOOKUP = SpongeDataManager.getProviderRegistry().getProviderLookup(net.minecraft.world.level.biome.Biome.class);

    private DataManipulator.Mutable manipulator = DataManipulator.mutableOf();

    public SpongeBiomeBuilder() {
        this.reset();
    }

    @Override
    public <V> Biome.Builder add(final Key<? extends Value<V>> key, final V value) {
        if (!PROVIDER_LOOKUP.getProvider(key).isSupported(net.minecraft.world.level.biome.Biome.class)) {
            throw new IllegalArgumentException(key + " is not supported for biomes");
        }
        this.manipulator.set(key, value);
        return this;
    }

    @Override
    public Biome.Builder reset() {
        this.manipulator = DataManipulator.mutableOf();
        return this;
    }

    @Override
    public Biome.Builder from(final org.spongepowered.api.world.biome.Biome biome) {
        this.manipulator.set(biome.getValues());
        return this;
    }

    @Override
    public Biome build() {
        final var precipitation = this.manipulator.require(Keys.HAS_PRECIPITATION);
        final Double temperature = this.manipulator.require(Keys.BIOME_TEMPERATURE);
        final Double downfall = this.manipulator.require(Keys.HUMIDITY);
        final var temperatureModifier = this.manipulator.getOrElse(Keys.TEMPERATURE_MODIFIER, TemperatureModifiers.NONE.get());

        final var fogColor = this.manipulator.require(Keys.FOG_COLOR);
        final var waterColor = this.manipulator.require(Keys.WATER_COLOR);
        final var waterFogColor = this.manipulator.require(Keys.WATER_FOG_COLOR);
        final var skyColor = this.manipulator.require(Keys.SKY_COLOR);
        final var foliageColor = this.manipulator.get(Keys.FOLIAGE_COLOR);
        final var grassColor = this.manipulator.get(Keys.GRASS_COLOR);
        final var grassColorModifier = this.manipulator.getOrElse(Keys.GRASS_COLOR_MODIFIER, GrassColorModifiers.NONE.get());
        final var particleSettings = this.manipulator.get(Keys.AMBIENT_PARTICLE);
        final Optional<SoundType> ambientSound = this.manipulator.get(Keys.AMBIENT_SOUND);
        final Optional<SoundConfig.Mood> ambientMood = this.manipulator.get(Keys.AMBIENT_MOOD);
        final Optional<SoundConfig.Additional> additionalSound = this.manipulator.get(Keys.AMBIENT_ADDITIONAL_SOUND);
        final Optional<SoundConfig.BackgroundMusic> backgroundMusic = this.manipulator.get(Keys.BACKGROUND_MUSIC);

        final Double spawnChance = this.manipulator.require(Keys.SPAWN_CHANCE);
        final Map<EntityCategory, List<NaturalSpawner>> spawners = this.manipulator.getOrElse(Keys.NATURAL_SPAWNERS, Map.of());
        final Map<EntityType<?>, NaturalSpawnCost> spawnerCosts = this.manipulator.getOrElse(Keys.NATURAL_SPAWNER_COST, Map.of());

        final Map<DecorationStep, List<PlacedFeature>> features = this.manipulator.getOrElse(Keys.FEATURES, Map.of());
        final List<Carver> carvers = this.manipulator.getOrElse(Keys.CARVERS, List.of());

        final BiomeSpecialEffects.Builder effectsBuilder = new BiomeSpecialEffects.Builder()
            .fogColor(fogColor.rgb())
            .waterColor(waterColor.rgb())
            .waterFogColor(waterFogColor.rgb())
            .skyColor(skyColor.rgb())
            .grassColorModifier((BiomeSpecialEffects.GrassColorModifier) (Object) grassColorModifier);
        foliageColor.ifPresent(c -> effectsBuilder.foliageColorOverride(c.rgb()));
        grassColor.ifPresent(c -> effectsBuilder.grassColorOverride(c.rgb()));
        particleSettings.ifPresent(ps -> effectsBuilder.ambientParticle((AmbientParticleSettings) ps));
        ambientSound.ifPresent(s -> effectsBuilder.ambientLoopSound(Holder.direct((SoundEvent) (Object) s)));
        ambientMood.ifPresent(m -> effectsBuilder.ambientMoodSound((net.minecraft.world.level.biome.AmbientMoodSettings) m));
        additionalSound.ifPresent(s -> effectsBuilder.ambientAdditionsSound((AmbientAdditionsSettings) s));
        backgroundMusic.ifPresent(m -> effectsBuilder.backgroundMusic((Music) m));

        final MobSpawnSettings.Builder spawnerBuilder = new MobSpawnSettings.Builder()
            .creatureGenerationProbability(spawnChance.floatValue());
        spawners.forEach((cat, spawner) -> spawner.forEach(sp -> {
            spawnerBuilder.addSpawn((MobCategory) (Object) cat, (MobSpawnSettings.SpawnerData) sp);
        }));
        spawnerCosts.forEach((type, cost) -> spawnerBuilder.addMobCharge((net.minecraft.world.entity.EntityType<?>) (Object) type, cost.budget(),
            cost.charge()));

        final BiomeGenerationSettings.PlainBuilder generationBuilder = new BiomeGenerationSettings.PlainBuilder();
        features.forEach((step, list) -> list.forEach(feature -> generationBuilder.addFeature((GenerationStep.Decoration) (Object) step,
            Holder.direct((net.minecraft.world.level.levelgen.placement.PlacedFeature) (Object) feature))));
        carvers.forEach((carver) -> generationBuilder.addCarver(Holder.direct((ConfiguredWorldCarver<?>) (Object) carver)));

        final net.minecraft.world.level.biome.Biome.BiomeBuilder vanillaBuilder = new net.minecraft.world.level.biome.Biome.BiomeBuilder()
            .hasPrecipitation(precipitation)
            .temperature(temperature.floatValue())
            .downfall(downfall.floatValue())
            .temperatureAdjustment((net.minecraft.world.level.biome.Biome.TemperatureModifier) (Object) temperatureModifier)
            .specialEffects(effectsBuilder.build())
            .mobSpawnSettings(spawnerBuilder.build())
            .generationSettings(generationBuilder.build());
        return (Biome) (Object) vanillaBuilder.build();
    }
}
