/*
 * This file is part of SpongeAPI, licensed under the MIT License (MIT).
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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.datapack.DataPack;
import org.spongepowered.api.datapack.DataPacks;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.EntityCategory;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.biome.ambient.AdditionalAmbientSound;
import org.spongepowered.api.world.biome.ambient.AmbientMoodSettings;
import org.spongepowered.api.world.biome.ambient.BackgroundMusic;
import org.spongepowered.api.world.biome.climate.GrassColorModifiers;
import org.spongepowered.api.world.biome.climate.TemperatureModifiers;
import org.spongepowered.api.world.biome.spawner.NaturalSpawnCost;
import org.spongepowered.api.world.biome.spawner.NaturalSpawner;
import org.spongepowered.api.world.biome.BiomeTemplate;
import org.spongepowered.api.world.generation.carver.CarvingStep;
import org.spongepowered.api.world.generation.carver.Carver;
import org.spongepowered.api.world.generation.feature.DecorationStep;
import org.spongepowered.api.world.generation.feature.PlacedFeature;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.provider.DataProviderLookup;
import org.spongepowered.common.util.AbstractDataPackEntryBuilder;
import org.spongepowered.common.util.AbstractResourceKeyedBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public record SpongeBiomeTemplate(ResourceKey key, Biome representedBiome, DataPack<BiomeTemplate> pack) implements BiomeTemplate {

    @Override
    public int contentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        final JsonElement serialized = SpongeBiomeTemplate.encode(this, SpongeCommon.server().registryAccess());
        try {
            return DataFormats.JSON.get().read(serialized.toString());
        } catch (IOException e) {
            throw new IllegalStateException("Could not read deserialized Biome:\n" + serialized, e);
        }
    }

    @Override
    public org.spongepowered.api.world.biome.Biome biome() {
        return (org.spongepowered.api.world.biome.Biome) (Object) this.representedBiome;
    }

    public static JsonElement encode(final BiomeTemplate template, final RegistryAccess registryAccess) {
        final RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        return Biome.DIRECT_CODEC.encodeStart(ops, (Biome) (Object) template.biome()).getOrThrow(false, e -> {});
    }

    public static Biome decode(final JsonElement json, final RegistryAccess registryAccess) {
        final RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        return Biome.DIRECT_CODEC.parse(ops, json).getOrThrow(false, e -> {});
    }

    public static BiomeTemplate decode(final DataPack<BiomeTemplate> pack, final ResourceKey key, final JsonElement packEntry, final RegistryAccess registryAccess) {
        final Biome parsed = SpongeBiomeTemplate.decode(packEntry, registryAccess);
        return new SpongeBiomeTemplate(key, parsed, pack);
    }

    public static class BuilderImpl extends AbstractDataPackEntryBuilder<org.spongepowered.api.world.biome.Biome, BiomeTemplate, Builder> implements BiomeTemplate.Builder {

        private static DataProviderLookup PROVIDER_LOOKUP = SpongeDataManager.getProviderRegistry().getProviderLookup(Biome.class);

        private DataManipulator.Mutable manipulator = DataManipulator.mutableOf();

        public BuilderImpl() {
            this.reset();
        }

        @Override
        public Function<BiomeTemplate, org.spongepowered.api.world.biome.Biome> valueExtractor() {
            return BiomeTemplate::biome;
        }

        @Override
        public <V> Builder add(final Key<? extends Value<V>> key, final V value) {
            if (!PROVIDER_LOOKUP.getProvider(key).isSupported(Biome.class)) {
                throw new IllegalArgumentException(key + " is not supported for biomes");
            }
            this.manipulator.set(key, value);
            return this;
        }

        @Override
        public Builder reset() {
            this.manipulator = DataManipulator.mutableOf();
            this.key = null;
            this.pack = DataPacks.BIOME;
            return this;
        }

        @Override
        public Builder fromValue(final org.spongepowered.api.world.biome.Biome biome) {
            this.manipulator.set(biome.getValues());
            return this;
        }

        @Override
        public Builder fromDataPack(final DataView pack) throws IOException {
            final JsonElement json = JsonParser.parseString(DataFormats.JSON.get().write(pack));
            final Biome biome = SpongeBiomeTemplate.decode(json, SpongeCommon.server().registryAccess());
            this.fromValue((org.spongepowered.api.world.biome.Biome) (Object) biome);
            return this;
        }

        @Override
        protected BiomeTemplate build0() {
            final var precipitation = this.manipulator.require(Keys.PRECIPITATION);
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
            final Optional<AmbientMoodSettings> ambientMood = this.manipulator.get(Keys.AMBIENT_MOOD);
            final Optional<AdditionalAmbientSound> additionalSound = this.manipulator.get(Keys.AMBIENT_ADDITIONAL_SOUND);
            final Optional<BackgroundMusic> backgroundMusic = this.manipulator.get(Keys.BACKGROUND_MUSIC);

            final Double spawnChance = this.manipulator.require(Keys.SPAWN_CHANCE);
            final Map<EntityCategory, WeightedTable<NaturalSpawner>> spawners = this.manipulator.getOrElse(Keys.NATURAL_SPAWNERS, Map.of());
            final Map<EntityType<?>, NaturalSpawnCost> spawnerCosts = this.manipulator.getOrElse(Keys.NATURAL_SPAWNER_COST, Map.of());

            final Map<DecorationStep, List<PlacedFeature>> features = this.manipulator.getOrElse(Keys.FEATURES, Map.of());
            final Map<CarvingStep, List<Carver>> carvers = this.manipulator.getOrElse(Keys.CARVERS, Map.of());

            final BiomeSpecialEffects.Builder effectsBuilder = new BiomeSpecialEffects.Builder()
                    .fogColor(fogColor.rgb())
                    .waterColor(waterColor.rgb())
                    .waterFogColor(waterFogColor.rgb())
                    .skyColor(skyColor.rgb())
                    .grassColorModifier((BiomeSpecialEffects.GrassColorModifier) (Object) grassColorModifier);
            foliageColor.ifPresent(c -> effectsBuilder.foliageColorOverride(c.rgb()));
            grassColor.ifPresent(c -> effectsBuilder.grassColorOverride(c.rgb()));
            particleSettings.ifPresent(ps -> effectsBuilder.ambientParticle((AmbientParticleSettings) ps));
            ambientSound.ifPresent(s -> effectsBuilder.ambientLoopSound((SoundEvent) s));
            ambientMood.ifPresent(m -> effectsBuilder.ambientMoodSound((net.minecraft.world.level.biome.AmbientMoodSettings) m));
            additionalSound.ifPresent(s -> effectsBuilder.ambientAdditionsSound((AmbientAdditionsSettings) s));
            backgroundMusic.ifPresent(m -> effectsBuilder.backgroundMusic((Music) m));

            final MobSpawnSettings.Builder spawnerBuilder = new MobSpawnSettings.Builder()
                    .creatureGenerationProbability(spawnChance.floatValue());
            spawners.forEach((cat, spawner) -> spawner.forEach(sp -> {
                if (sp instanceof WeightedObject<NaturalSpawner> wo) {
                    spawnerBuilder.addSpawn((MobCategory) (Object) cat, (MobSpawnSettings.SpawnerData) wo.get());
                } else {
                    throw new IllegalArgumentException("WeightedTable for NATURAL_SPAWNERS must consist of WeightedObjects");
                }
            }));
            spawnerCosts.forEach((type, cost) -> spawnerBuilder.addMobCharge((net.minecraft.world.entity.EntityType<?>) (Object) type, cost.budget(),
                    cost.charge()));

            final BiomeGenerationSettings.Builder generationBuilder = new BiomeGenerationSettings.Builder();
            features.forEach((step, list) -> list.forEach(feature -> generationBuilder.addFeature((GenerationStep.Decoration) (Object) step,
                    Holder.direct((net.minecraft.world.level.levelgen.placement.PlacedFeature) (Object) feature))));
            carvers.forEach((step, list) -> list.forEach(carver -> generationBuilder.addCarver((GenerationStep.Carving) (Object) step,
                    Holder.direct((ConfiguredWorldCarver<?>) (Object) carver))));

            final Biome.BiomeBuilder vanillaBuilder = new Biome.BiomeBuilder()
                    .precipitation((Biome.Precipitation) (Object) precipitation)
                    .temperature(temperature.floatValue())
                    .downfall(downfall.floatValue())
                    .temperatureAdjustment((Biome.TemperatureModifier) (Object) temperatureModifier)
                    .specialEffects(effectsBuilder.build())
                    .mobSpawnSettings(spawnerBuilder.build())
                    .generationSettings(generationBuilder.build());
            return new SpongeBiomeTemplate(this.key, vanillaBuilder.build(), this.pack);
        }
    }
}
