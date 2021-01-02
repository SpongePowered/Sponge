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

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kyori.adventure.text.Component;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.datapack.DataPackType;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.biome.BiomeProvider;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.generation.ChunkGenerator;
import org.spongepowered.api.world.generation.MutableWorldGenerationConfig;
import org.spongepowered.api.world.generation.config.NoiseGeneratorConfig;
import org.spongepowered.api.world.generation.config.WorldGenerationConfig;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.common.AbstractResourceKeyed;
import org.spongepowered.common.accessor.world.gen.DimensionGeneratorSettingsAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.DimensionBridge;
import org.spongepowered.common.serialization.EnumCodec;
import org.spongepowered.common.serialization.UUIDCodec;
import org.spongepowered.common.util.AbstractResourceKeyedBuilder;
import org.spongepowered.common.util.MissingImplementationException;
import org.spongepowered.common.server.BootstrapProperties;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class SpongeWorldTemplate extends AbstractResourceKeyed implements WorldTemplate {

    @Nullable public final Component displayName;
    public final RegistryReference<WorldType> worldType;
    public final org.spongepowered.api.world.generation.ChunkGenerator generator;
    public final WorldGenerationConfig generationSettings;
    public final SerializationBehavior serializationBehavior;
    @Nullable public final UUID uniqueId;
    @Nullable public final RegistryReference<GameMode> gameMode;
    @Nullable public final RegistryReference<Difficulty> difficulty;
    @Nullable public final Integer viewDistance;

    public final boolean enabled, keepLoaded, loadOnStartup, keepSpawnLoaded, generateSpawnOnLoad, hardcore, commands, pvp;

    private static final Codec<SpongeDataSection> SPONGE_CODEC = RecordCodecBuilder
            .create(r -> r
                    .group(
                            SpongeAdventure.STRING_CODEC.optionalFieldOf("display_name").forGetter(v -> Optional.ofNullable(v.displayName)),
                            ResourceLocation.CODEC.optionalFieldOf("game_mode").forGetter(v -> Optional.ofNullable(v.gameMode)),
                            ResourceLocation.CODEC.optionalFieldOf("difficulty").forGetter(v -> Optional.ofNullable(v.difficulty)),
                            EnumCodec.create(SerializationBehavior.class).optionalFieldOf("serialization_behavior").forGetter(v -> Optional.ofNullable(v.serializationBehavior)),
                            Codec.INT.optionalFieldOf("view_distance").forGetter(v -> Optional.ofNullable(v.viewDistance)),
                            Codec.BOOL.optionalFieldOf("enabled").forGetter(v -> Optional.ofNullable(v.enabled)),
                            Codec.BOOL.optionalFieldOf("keep_loaded").forGetter(v -> Optional.ofNullable(v.keepLoaded)),
                            Codec.BOOL.optionalFieldOf("load_on_startup").forGetter(v -> Optional.ofNullable(v.loadOnStartup)),
                            Codec.BOOL.optionalFieldOf("keep_spawn_loaded").forGetter(v -> Optional.ofNullable(v.keepSpawnLoaded)),
                            Codec.BOOL.optionalFieldOf("generate_spawn_on_load").forGetter(v -> Optional.ofNullable(v.generateSpawnOnLoad)),
                            Codec.BOOL.optionalFieldOf("hardcore").forGetter(v -> Optional.ofNullable(v.hardcore)),
                            Codec.BOOL.optionalFieldOf("commands").forGetter(v -> Optional.ofNullable(v.commands)),
                            Codec.BOOL.optionalFieldOf("pvp").forGetter(v -> Optional.ofNullable(v.pvp)),
                            UUIDCodec.create().optionalFieldOf("unique_id").forGetter(v -> Optional.ofNullable(v.uniqueId))
                    )
                    // *Chuckles* I continue to be in danger...
                    .apply(r, (f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14) ->
                            new SpongeDataSection(f1.orElse(null), f2.orElse(null), f3.orElse(null), f4.orElse(null),
                                    f5.orElse(null), f6.orElse(null), f7.orElse(null), f8.orElse(null), f9.orElse(null),
                                    f10.orElse(null), f11.orElse(null), f12.orElse(null), f13.orElse(null), f14.orElse(null))
                    )
            );

    public static final Codec<Dimension> DIRECT_CODEC = RecordCodecBuilder
            .create(r ->  r
                    .group(
                            SpongeWorldTypeTemplate.CODEC.fieldOf("type").forGetter(Dimension::typeSupplier),
                            net.minecraft.world.gen.ChunkGenerator.CODEC.fieldOf("generator").forGetter(Dimension::generator),
                            SpongeWorldTemplate.SPONGE_CODEC.optionalFieldOf("#sponge").forGetter(v -> {
                                final DimensionBridge dimensionBridge = (DimensionBridge) (Object) v;
                                return Optional.of(new SpongeDataSection(dimensionBridge.bridge$displayName(), dimensionBridge.bridge$gameMode(),
                                        dimensionBridge.bridge$difficulty(), dimensionBridge.bridge$serializationBehavior(),
                                        dimensionBridge.bridge$viewDistance(), dimensionBridge.bridge$enabled(), dimensionBridge.bridge$keepLoaded(),
                                        dimensionBridge.bridge$loadOnStartup(), dimensionBridge.bridge$keepSpawnLoaded(),
                                        dimensionBridge.bridge$generateSpawnOnLoad(), dimensionBridge.bridge$hardcore(),
                                        dimensionBridge.bridge$commands(), dimensionBridge.bridge$pvp(), dimensionBridge.bridge$uniqueId()));
                            })
                    )
                    .apply(r, r
                        .stable((f1, f2, f3) ->
                            {
                                final Dimension dimension = new Dimension(f1, f2);
                                f3.ifPresent(((DimensionBridge) (Object) dimension)::bridge$populateFromData);
                                return dimension;
                            }
                        )
                    )
            );

    protected SpongeWorldTemplate(final BuilderImpl builder) {
        super(builder.key);
        this.displayName = builder.displayName;
        this.worldType = builder.worldType;
        this.generator = builder.generator;
        this.generationSettings = builder.generationSettings;
        this.uniqueId = UUID.randomUUID();
        this.gameMode = builder.gameMode;
        this.difficulty = builder.difficulty;
        this.serializationBehavior = builder.serializationBehavior;
        this.viewDistance = builder.viewDistance;
        this.enabled = builder.enabled;
        this.keepLoaded = builder.keepLoaded;
        this.loadOnStartup = builder.loadOnStartup;
        this.keepSpawnLoaded = builder.keepSpawnLoaded;
        this.generateSpawnOnLoad = builder.generateSpawnOnLoad;
        this.hardcore = builder.hardcore;
        this.commands = builder.commands;
        this.pvp = builder.pvp;
    }

    public SpongeWorldTemplate(final ServerWorld world) {
        super((ResourceKey) (Object) world.dimension().location());
        final ServerWorldProperties levelData = (ServerWorldProperties) world.getLevelData();
        this.displayName = levelData.displayName().orElse(null);
        this.worldType = RegistryReference.referenced(Sponge.getServer().registries(), RegistryTypes.WORLD_TYPE, (WorldType) world.dimensionType());
        this.generator = (ChunkGenerator) world.getChunkSource().getGenerator();
        this.generationSettings = MutableWorldGenerationConfig.builder().from(levelData.worldGenerationSettings()).build();
        this.uniqueId = levelData.getUniqueId();
        this.gameMode = RegistryReference.referenced(Sponge.getGame().registries(), RegistryTypes.GAME_MODE, levelData.gameMode());
        this.difficulty = RegistryReference.referenced(Sponge.getGame().registries(), RegistryTypes.DIFFICULTY, levelData.difficulty());
        this.serializationBehavior = levelData.serializationBehavior();
        this.viewDistance = levelData.viewDistance();
        this.enabled = levelData.enabled();
        this.keepLoaded = levelData.keepLoaded();
        this.loadOnStartup = levelData.loadOnStartup();
        this.keepSpawnLoaded = levelData.keepSpawnLoaded();
        this.generateSpawnOnLoad = levelData.generateSpawnOnLoad();
        this.hardcore = levelData.hardcore();
        this.commands = levelData.commands();
        this.pvp = levelData.pvp();
    }

    public SpongeWorldTemplate(final Dimension template) {
        super(((ResourceKeyBridge) (Object) template).bridge$getKey());
        final DimensionBridge templateBridge = (DimensionBridge) (Object) template;
        this.displayName = templateBridge.bridge$displayName();
        this.worldType = RegistryReference.referenced(Sponge.getServer().registries(), RegistryTypes.WORLD_TYPE, (WorldType) template.type());
        this.generator = (ChunkGenerator) template.generator();
        this.generationSettings = MutableWorldGenerationConfig.builder().from((MutableWorldGenerationConfig) BootstrapProperties.dimensionGeneratorSettings).build();
        this.uniqueId = templateBridge.bridge$uniqueId();
        this.gameMode = RegistryKey.of(RegistryTypes.GAME_MODE, (ResourceKey) (Object) templateBridge.bridge$gameMode()).asReference();
        this.difficulty = RegistryKey.of(RegistryTypes.DIFFICULTY, (ResourceKey) (Object) templateBridge.bridge$difficulty()).asReference();
        this.serializationBehavior = templateBridge.bridge$serializationBehavior();
        this.viewDistance = templateBridge.bridge$viewDistance();
        this.enabled = templateBridge.bridge$enabled();
        this.keepLoaded = templateBridge.bridge$keepLoaded();
        this.loadOnStartup = templateBridge.bridge$loadOnStartup();
        this.keepSpawnLoaded = templateBridge.bridge$keepSpawnLoaded();
        this.generateSpawnOnLoad = templateBridge.bridge$generateSpawnOnLoad();
        this.hardcore = templateBridge.bridge$hardcore();
        this.commands = templateBridge.bridge$commands();
        this.pvp = templateBridge.bridge$pvp();
    }

    @Override
    public DataPackType type() {
        return DataPackTypes.WORLD;
    }

    @Override
    public Optional<Component> displayName() {
        return Optional.ofNullable(this.displayName);
    }

    @Override
    public RegistryReference<WorldType> worldType() {
        return this.worldType;
    }

    @Override
    public org.spongepowered.api.world.generation.ChunkGenerator generator() {
        return this.generator;
    }

    @Override
    public WorldGenerationConfig generationConfig() {
        return this.generationSettings;
    }

    @Override
    public Optional<RegistryReference<GameMode>> gameMode() {
        return Optional.ofNullable(this.gameMode);
    }

    @Override
    public Optional<RegistryReference<Difficulty>> difficulty() {
        return Optional.ofNullable(this.difficulty);
    }

    @Override
    public SerializationBehavior serializationBehavior() {
        return this.serializationBehavior;
    }

    @Override
    public boolean enabled() {
        return this.enabled;
    }

    @Override
    public boolean keepLoaded() {
        return this.keepLoaded;
    }

    @Override
    public boolean loadOnStartup() {
        return this.loadOnStartup;
    }

    @Override
    public boolean keepSpawnLoaded() {
        return this.keepSpawnLoaded;
    }

    @Override
    public boolean generateSpawnOnLoad() {
        return this.generateSpawnOnLoad;
    }

    @Override
    public boolean hardcore() {
        return this.hardcore;
    }

    @Override
    public boolean commands() {
        return this.commands;
    }

    @Override
    public boolean pvp() {
        return this.pvp;
    }

    @Override
    public Optional<Integer> viewDistance() {
        return Optional.empty();
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        throw new MissingImplementationException("SpongeWorldTemplate", "toContainer");
    }

    public Dimension asDimension() {
        final Dimension scratch =
                new Dimension(() -> BootstrapProperties.registries.dimensionTypes().get((ResourceLocation) (Object) this.worldType.location()),
                        (net.minecraft.world.gen.ChunkGenerator) this.generator);
        ((DimensionBridge) (Object) scratch).bridge$populateFromTemplate(this);
        return scratch;
    }

    public static final class SpongeDataSection {

        @Nullable public final Component displayName;
        @Nullable public final ResourceLocation gameMode;
        @Nullable public final ResourceLocation difficulty;
        @Nullable public final SerializationBehavior serializationBehavior;
        @Nullable public UUID uniqueId;
        @Nullable public final Integer viewDistance;
        @Nullable public final Boolean enabled, keepLoaded, loadOnStartup, keepSpawnLoaded, generateSpawnOnLoad, hardcore, commands, pvp;

        public SpongeDataSection(@Nullable final Component displayName, @Nullable final ResourceLocation gameMode,
                @Nullable final ResourceLocation difficulty, @Nullable final SerializationBehavior serializationBehavior,
                @Nullable final Integer viewDistance, @Nullable final Boolean enabled, @Nullable final Boolean keepLoaded,
                @Nullable final Boolean loadOnStartup, @Nullable final Boolean keepSpawnLoaded, @Nullable final Boolean generateSpawnOnLoad,
                @Nullable final Boolean hardcore, @Nullable final Boolean commands, @Nullable final Boolean pvp, @Nullable final UUID uniqueId) {
            this.displayName = displayName;
            this.gameMode = gameMode;
            this.difficulty = difficulty;
            this.serializationBehavior = serializationBehavior;
            this.uniqueId = uniqueId;
            this.viewDistance = viewDistance;
            this.enabled = enabled;
            this.keepLoaded = keepLoaded;
            this.loadOnStartup = loadOnStartup;
            this.keepSpawnLoaded = keepSpawnLoaded;
            this.generateSpawnOnLoad = generateSpawnOnLoad;
            this.hardcore = hardcore;
            this.commands = commands;
            this.pvp = pvp;
        }
    }

    public static final class BuilderImpl extends AbstractResourceKeyedBuilder<WorldTemplate, WorldTemplate.Builder> implements WorldTemplate.Builder {

        @Nullable protected Component displayName;
        @Nullable protected RegistryReference<WorldType> worldType;
        @Nullable protected ChunkGenerator generator;
        @Nullable protected WorldGenerationConfig generationSettings;
        @Nullable protected RegistryReference<GameMode> gameMode;
        @Nullable protected RegistryReference<Difficulty> difficulty;
        @Nullable protected SerializationBehavior serializationBehavior;
        @Nullable protected Integer viewDistance;

        protected boolean enabled, keepLoaded, loadOnStartup, keepSpawnLoaded, generateSpawnOnLoad, hardcore, commands, pvp;

        @Override
        public Builder displayName(@Nullable final Component displayName) {
            this.displayName = displayName;
            return this;
        }

        @Override
        public Builder worldType(final RegistryReference<WorldType> worldType) {
            this.worldType = Objects.requireNonNull(worldType, "worldType");
            return this;
        }

        @Override
        public Builder generator(final ChunkGenerator generator) {
            this.generator = Objects.requireNonNull(generator, "generator");
            return this;
        }

        @Override
        public Builder generationConfig(final WorldGenerationConfig generationConfig) {
            this.generationSettings = Objects.requireNonNull(generationConfig, "generationConfig");
            return this;
        }

        @Override
        public Builder gameMode(final RegistryReference<GameMode> gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        @Override
        public Builder difficulty(final RegistryReference<Difficulty> difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        @Override
        public Builder serializationBehavior(final SerializationBehavior serializationBehavior) {
            this.serializationBehavior = Objects.requireNonNull(serializationBehavior, "serializationBehavior");
            return this;
        }

        @Override
        public Builder enabled(final boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        @Override
        public Builder keepLoaded(final boolean keepLoaded) {
            this.keepLoaded = keepLoaded;
            return this;
        }

        @Override
        public Builder loadOnStartup(final boolean loadOnStartup) {
            this.loadOnStartup = loadOnStartup;
            return this;
        }

        @Override
        public Builder keepSpawnLoaded(final boolean keepSpawnLoaded) {
            this.keepSpawnLoaded = keepSpawnLoaded;
            return this;
        }

        @Override
        public Builder generateSpawnOnLoad(final boolean generateSpawnOnLoad) {
            this.generateSpawnOnLoad = generateSpawnOnLoad;
            return this;
        }

        @Override
        public Builder hardcore(final boolean hardcore) {
            this.hardcore = hardcore;
            return this;
        }

        @Override
        public Builder commands(final boolean commands) {
            this.commands = commands;
            return this;
        }

        @Override
        public Builder pvp(final boolean pvp) {
            this.pvp = pvp;
            return this;
        }

        @Override
        public Builder viewDistance(final int distance) {
            this.viewDistance = distance;
            return this;
        }

        @Override
        public Builder reset() {
            super.reset();
            this.displayName = null;
            this.worldType = WorldTypes.OVERWORLD;
            this.generator = ChunkGenerator.noise(BiomeProvider.overworld(false), NoiseGeneratorConfig.overworld());
            final DimensionGeneratorSettings generationSettings = BootstrapProperties.dimensionGeneratorSettings;
            this.generationSettings = (WorldGenerationConfig) DimensionGeneratorSettingsAccessor.invoker$construct(generationSettings.seed(),
                    generationSettings.generateFeatures(), generationSettings.generateBonusChest(),
                    new SimpleRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental()),
                    ((DimensionGeneratorSettingsAccessor) generationSettings).accessor$legacyCustomOptions());
            this.gameMode = BootstrapProperties.gameMode;
            this.difficulty = BootstrapProperties.difficulty;
            this.serializationBehavior = SerializationBehavior.AUTOMATIC;
            this.viewDistance = null;
            this.enabled = true;
            this.keepLoaded = true;
            this.loadOnStartup = true;
            this.keepSpawnLoaded = false;
            this.generateSpawnOnLoad = false;
            this.hardcore = false;
            this.commands = true;
            this.pvp = BootstrapProperties.pvp;
            return this;
        }

        @Override
        public Builder from(final WorldTemplate template) {
            this.displayName = Objects.requireNonNull(template, "template").displayName().orElse(null);
            this.worldType = template.worldType();
            this.generator = template.generator();
            final DimensionGeneratorSettings generationSettings = (DimensionGeneratorSettings) template.generationConfig();
            this.generationSettings = (WorldGenerationConfig) DimensionGeneratorSettingsAccessor.invoker$construct(generationSettings.seed(),
                    generationSettings.generateFeatures(), generationSettings.generateBonusChest(),
                    new SimpleRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental()),
                    ((DimensionGeneratorSettingsAccessor) generationSettings).accessor$legacyCustomOptions());
            this.gameMode = template.gameMode().orElse(null);
            this.difficulty = template.difficulty().orElse(null);
            this.serializationBehavior = template.serializationBehavior();
            this.viewDistance = template.viewDistance().orElse(null);
            this.enabled = template.enabled();
            this.keepLoaded = template.keepLoaded();
            this.loadOnStartup = template.loadOnStartup();
            this.keepSpawnLoaded = template.keepSpawnLoaded();
            this.generateSpawnOnLoad = template.generateSpawnOnLoad();
            this.hardcore = template.hardcore();
            this.commands = template.commands();
            this.pvp = template.pvp();
            return this;
        }

        @Override
        protected WorldTemplate build0() {
            return new SpongeWorldTemplate(this);
        }
    }

    public static final class FactoryImpl implements WorldTemplate.Factory {

        @Override
        public WorldTemplate overworld() {
            return new SpongeWorldTemplate.BuilderImpl()
                    .reset()
                    .key(ResourceKey.minecraft("overworld"))
                    .worldType(WorldTypes.OVERWORLD)
                    .generator(ChunkGenerator.noise(BiomeProvider.overworld(false), NoiseGeneratorConfig.overworld()))
                    .keepSpawnLoaded(true)
                    .generateSpawnOnLoad(true)
                    .build();
        }

        @Override
        public WorldTemplate theNether() {
            return new SpongeWorldTemplate.BuilderImpl()
                    .reset()
                    .key(ResourceKey.minecraft("the_nether"))
                    .worldType(WorldTypes.THE_NETHER)
                    .generator(ChunkGenerator.noise(BiomeProvider.nether(), NoiseGeneratorConfig.nether()))
                    .build();
        }

        @Override
        public WorldTemplate theEnd() {
            return new SpongeWorldTemplate.BuilderImpl()
                    .reset()
                    .key(ResourceKey.minecraft("the_end"))
                    .worldType(WorldTypes.THE_END)
                    .generator(ChunkGenerator.noise(BiomeProvider.end(), NoiseGeneratorConfig.end()))
                    .build();
        }
    }
}
