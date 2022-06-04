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
package org.spongepowered.common.world.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kyori.adventure.text.Component;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.datapack.DataPackType;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.biome.provider.BiomeProvider;
import org.spongepowered.api.world.generation.ChunkGenerator;
import org.spongepowered.api.world.generation.config.NoiseGeneratorConfig;
import org.spongepowered.api.world.generation.config.WorldGenerationConfig;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.common.AbstractResourceKeyed;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.world.gen.DimensionGeneratorSettingsAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.world.level.dimension.LevelStemBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.fixer.SpongeDataCodec;
import org.spongepowered.common.data.holder.SpongeDataHolder;
import org.spongepowered.common.data.provider.DataProviderLookup;
import org.spongepowered.common.serialization.EnumCodec;
import org.spongepowered.common.serialization.MathCodecs;
import org.spongepowered.common.util.AbstractResourceKeyedBuilder;
import org.spongepowered.math.vector.Vector3i;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class SpongeWorldTemplate extends AbstractResourceKeyed implements WorldTemplate, SpongeDataHolder {

    private final LevelStem levelStem;
    private final WorldGenerationConfig generationConfig;

    public static final Codec<LevelStem> CODEC = RecordCodecBuilder.create(
            ($$0) -> $$0.group(DimensionType.CODEC.fieldOf("type").forGetter(LevelStem::typeHolder),
                            net.minecraft.world.level.chunk.ChunkGenerator.CODEC.fieldOf("generator").forGetter(LevelStem::generator))
                    .apply($$0, $$0.stable(LevelStem::new)));

    private static final Codec<SpongeDataSection> SPONGE_CODEC = RecordCodecBuilder
            .create(r -> r
                    .group(
                            SpongeAdventure.STRING_CODEC.optionalFieldOf("display_name").forGetter(v -> Optional.ofNullable(v.displayName)),
                            ResourceLocation.CODEC.optionalFieldOf("game_mode").forGetter(v -> Optional.ofNullable(v.gameMode)),
                            ResourceLocation.CODEC.optionalFieldOf("difficulty").forGetter(v -> Optional.ofNullable(v.difficulty)),
                            EnumCodec.create(SerializationBehavior.class).optionalFieldOf("serialization_behavior").forGetter(v -> Optional.ofNullable(v.serializationBehavior)),
                            Codec.INT.optionalFieldOf("view_distance").forGetter(v -> Optional.ofNullable(v.viewDistance)),
                            MathCodecs.VECTOR_3i.optionalFieldOf("spawn_position").forGetter(v -> Optional.ofNullable(v.spawnPosition)),
                            Codec.BOOL.optionalFieldOf("load_on_startup").forGetter(v -> Optional.ofNullable(v.loadOnStartup)),
                            Codec.BOOL.optionalFieldOf("performs_spawn_logic").forGetter(v -> Optional.ofNullable(v.performsSpawnLogic)),
                            Codec.BOOL.optionalFieldOf("hardcore").forGetter(v -> Optional.ofNullable(v.hardcore)),
                            Codec.BOOL.optionalFieldOf("commands").forGetter(v -> Optional.ofNullable(v.commands)),
                            Codec.BOOL.optionalFieldOf("pvp").forGetter(v -> Optional.ofNullable(v.pvp))
                    )
                    // *Chuckles* I continue to be in danger...
                    .apply(r, (f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11) ->
                            new SpongeDataSection(f1.orElse(null), f2.orElse(null), f3.orElse(null), f4.orElse(null),
                                    f5.orElse(null), f6.orElse(null), f7.orElse(null), f8.orElse(null), f9.orElse(null),
                                    f10.orElse(null), f11.orElse(null))
                    )
            );

    public static final Codec<LevelStem> DIRECT_CODEC = new MapCodec.MapCodecCodec<LevelStem>(new SpongeDataCodec<>(LevelStem.CODEC,
        SpongeWorldTemplate.SPONGE_CODEC, (type, data) -> ((LevelStemBridge) (Object) type).bridge$decorateData(data),
        type -> ((LevelStemBridge) (Object) type).bridge$createData()));

    public SpongeWorldTemplate(final ResourceKey key, final LevelStem levelStem, final WorldGenerationConfig generationConfig) {
        super(key);
        this.levelStem = levelStem;
        this.generationConfig = SpongeWorldTemplate.cloneGenerationConfig(generationConfig);
    }

    @Override
    public List<DataHolder> impl$delegateDataHolder() {
        return List.of((DataHolder) (Object) this.levelStem, this);
    }

    public static LevelStem viewToStem(JsonElement pack) {
        // TODO catch & rethrow exceptions in CODEC?
        final DataResult<LevelStem> parsed = LevelStem.CODEC.parse(JsonOps.INSTANCE, pack);
        return parsed.getOrThrow(false, e -> {});
    }

    @Override
    public DataPackType<WorldTemplate> type() {
        return DataPackTypes.WORLD;
    }

    @Override
    public int contentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        final JsonElement serialized = SpongeWorldTemplate.serialize(this, SpongeCommon.server().registryAccess());
        try {
            final DataContainer container = DataFormats.JSON.get().read(serialized.toString());
            container.set(Queries.CONTENT_VERSION, this.contentVersion());
            return container;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read deserialized LevelStem:\n" + serialized, e);
        }
    }

    public LevelStem levelStem() {
        return this.levelStem;
    }

    public static JsonElement serialize(final WorldTemplate s, final RegistryAccess registryAccess) {
        if (s instanceof SpongeWorldTemplate t) {
            final RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
            return SpongeWorldTemplate.DIRECT_CODEC.encodeStart(ops, t.levelStem).getOrThrow(false, e -> {});
        }
        throw new IllegalArgumentException("WorldTemplate is not a SpongeWorldTemplate");
    }

    public static WorldGenerationConfig cloneGenerationConfig(WorldGenerationConfig cfg) {
        return (WorldGenerationConfig) DimensionGeneratorSettingsAccessor.invoker$new(cfg.seed(),
                cfg.generateFeatures(), cfg.generateBonusChest(),
                new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable(), null),
                ((DimensionGeneratorSettingsAccessor) cfg).accessor$legacyCustomOptions());
    }

    public WorldGenerationConfig getGenerationConfig() {
        return this.generationConfig;
    }

    public static final class SpongeDataSection {
        @Nullable public final Component displayName;
        @Nullable public final ResourceLocation gameMode;
        @Nullable public final ResourceLocation difficulty;
        @Nullable public final SerializationBehavior serializationBehavior;
        @Nullable public final Integer viewDistance;
        @Nullable public final Vector3i spawnPosition;

        @Nullable public final Boolean loadOnStartup, performsSpawnLogic, hardcore, commands, pvp;
        public SpongeDataSection(final @Nullable Component displayName, final @Nullable ResourceLocation gameMode,
            final @Nullable ResourceLocation difficulty, final @Nullable SerializationBehavior serializationBehavior,
            final @Nullable Integer viewDistance, final @Nullable Vector3i spawnPosition, final @Nullable Boolean loadOnStartup,
            final @Nullable Boolean performsSpawnLogic, final @Nullable Boolean hardcore, final @Nullable Boolean commands,
            final @Nullable Boolean pvp)
        {
            this.displayName = displayName;
            this.gameMode = gameMode;
            this.difficulty = difficulty;
            this.serializationBehavior = serializationBehavior;
            this.viewDistance = viewDistance;
            this.spawnPosition = spawnPosition;
            this.loadOnStartup = loadOnStartup;
            this.performsSpawnLogic = performsSpawnLogic;
            this.hardcore = hardcore;
            this.commands = commands;
            this.pvp = pvp;
        }

    }


    public static final class BuilderImpl extends AbstractResourceKeyedBuilder<WorldTemplate, WorldTemplate.Builder> implements WorldTemplate.Builder {

        private static DataProviderLookup PROVIDER_LOOKUP = SpongeDataManager.getProviderRegistry().getProviderLookup(LevelStem.class);
        private static DataProviderLookup PROVIDER_LOOKUP_TEMPLATE = SpongeDataManager.getProviderRegistry().getProviderLookup(WorldTemplate.class);

        private DataManipulator.Mutable data = DataManipulator.mutableOf();

        @Override
        public <V> Builder add(final Key<? extends Value<V>> key, final V value) {
            if (!PROVIDER_LOOKUP.getProvider(key).isSupported(LevelStem.class)) {
                throw new IllegalArgumentException(key + " is not supported for world templates");
            }
            this.data.set(key, value);
            return this;
        }

        @Override
        public Builder reset() {
            super.reset();
            this.data = DataManipulator.mutableOf();
            this.data.set(Keys.WORLD_TYPE, WorldTypes.OVERWORLD);
            this.data.set(Keys.CHUNK_GENERATOR, ChunkGenerator.overworld());
            this.data.set(Keys.WORLD_GEN_CONFIG, (WorldGenerationConfig) SpongeCommon.server().getWorldData().worldGenSettings());
            return this;
        }

        @Override
        public Builder from(final WorldTemplate template) {
            this.key = Objects.requireNonNull(template, "template").key();
            this.data = DataManipulator.mutableOf(template.getValues());
            return this;
        }

        @Override
        public Builder fromDataPack(DataView pack) throws IOException {
            // TODO maybe accept JsonElement instead?
            final JsonElement json = JsonParser.parseString(DataFormats.JSON.get().write(pack));
            final LevelStem levelStem = SpongeWorldTemplate.viewToStem(json);
            return this.from(levelStem);

        }

        @Override
        public Builder from(ServerWorld world) {
            this.from(world.properties());
            this.data.set(Keys.CHUNK_GENERATOR, world.generator());
            return this;
        }

        @Override
        public Builder from(ServerWorldProperties properties) {
            PrimaryLevelDataBridge bridge = (PrimaryLevelDataBridge) properties;
            this.key = properties.key();
            properties.displayName().ifPresent(name -> this.data.set(Keys.DISPLAY_NAME, name));
            this.data.set(Keys.WORLD_TYPE, properties.worldType().asDefaultedReference(RegistryTypes.WORLD_TYPE));
            this.data.set(Keys.WORLD_GEN_CONFIG, properties.worldGenerationConfig());
            if (bridge.bridge$customGameType()) {
                this.data.set(Keys.GAME_MODE_REFERENCE, properties.gameMode().asDefaultedReference(RegistryTypes.GAME_MODE));
            }
            if (bridge.bridge$customDifficulty()) {
                this.data.set(Keys.WORLD_DIFFICULTY, properties.difficulty().asDefaultedReference(RegistryTypes.DIFFICULTY));
            }
            bridge.bridge$serializationBehavior().ifPresent(s -> this.data.set(Keys.SERIALIZATION_BEHAVIOR, s));
            bridge.bridge$viewDistance().ifPresent(v -> this.data.set(Keys.VIEW_DISTANCE, v));
            if (bridge.bridge$customSpawnPosition()) {
                this.data.set(Keys.SPAWN_POSITION, properties.spawnPosition());
            }
            this.data.set(Keys.IS_LOAD_ON_STARTUP, properties.loadOnStartup());
            this.data.set(Keys.PERFORM_SPAWN_LOGIC, properties.performsSpawnLogic());
            this.data.set(Keys.HARDCORE, properties.hardcore());
            this.data.set(Keys.COMMANDS, properties.commands());
            this.data.set(Keys.PVP, properties.pvp());
            return this;
        }

        private Builder from(LevelStem levelStem) {
            this.data.set(((DataHolder) (Object) levelStem).getValues());
            return this;
        }

        @Override
        protected WorldTemplate build0() {
            final ChunkGenerator chunkGenerator = this.data.require(Keys.CHUNK_GENERATOR);
            final Holder<DimensionType> dimensionType = BuilderImpl.dimensionType(this.data.require(Keys.WORLD_TYPE));
            final LevelStem levelStem = new LevelStem(dimensionType, (net.minecraft.world.level.chunk.ChunkGenerator) chunkGenerator);
            ((LevelStemBridge) (Object) levelStem).bridge$decorateData(this.data);
            final WorldGenerationConfig generationConfig = this.data.getOrElse(Keys.WORLD_GEN_CONFIG, (WorldGenerationConfig) SpongeCommon.server().getWorldData().worldGenSettings());
            return new SpongeWorldTemplate(this.key, levelStem, generationConfig);
        }

        @NotNull
        private static Holder<DimensionType> dimensionType(final RegistryReference<WorldType> worldType) {
            final Registry<DimensionType> dimensionTypeRegistry = SpongeCommon.server().registryAccess().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
            final net.minecraft.resources.ResourceKey<DimensionType> key = net.minecraft.resources.ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, (ResourceLocation) (Object) worldType.location());
            return dimensionTypeRegistry.getHolderOrThrow(key);
        }

    }

    public static final class FactoryImpl implements WorldTemplate.Factory {

        @Override
        public WorldTemplate overworld() {
            return new BuilderImpl()
                    .reset()
                    .key(ResourceKey.minecraft("overworld"))
                    .add(Keys.WORLD_TYPE, WorldTypes.OVERWORLD)
                    .add(Keys.CHUNK_GENERATOR, ChunkGenerator.overworld())
                    .add(Keys.PERFORM_SPAWN_LOGIC, true)
                    .build();
        }

        @Override
        public WorldTemplate overworldCaves() {
            return new BuilderImpl()
                    .reset()
                    .key(ResourceKey.minecraft("overworld_caves"))
                    .add(Keys.WORLD_TYPE, WorldTypes.OVERWORLD)
                    .add(Keys.CHUNK_GENERATOR, ChunkGenerator.noise(BiomeProvider.overworld(), NoiseGeneratorConfig.caves()))
                    .add(Keys.PERFORM_SPAWN_LOGIC, true)
                    .build();
        }

        @Override
        public WorldTemplate theNether() {
            return new BuilderImpl()
                    .reset()
                    .key(ResourceKey.minecraft("the_nether"))
                    .add(Keys.WORLD_TYPE, WorldTypes.THE_NETHER)
                    .add(Keys.CHUNK_GENERATOR, ChunkGenerator.theNether())
                    .build();
        }

        @Override
        public WorldTemplate theEnd() {
            return new BuilderImpl()
                    .reset()
                    .key(ResourceKey.minecraft("the_end"))
                    .add(Keys.WORLD_TYPE, WorldTypes.THE_END)
                    .add(Keys.CHUNK_GENERATOR, ChunkGenerator.theEnd())
                    .build();
        }

    }

}
