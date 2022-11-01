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
package org.spongepowered.test.worldgen;

import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.tag.BlockTypeTags;
import org.spongepowered.api.util.RandomProvider;
import org.spongepowered.api.util.Range;
import org.spongepowered.api.world.DefaultWorldKeys;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.WorldTypeEffects;
import org.spongepowered.api.world.WorldTypeTemplate;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.biome.AttributedBiome;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.biome.BiomeAttributes;
import org.spongepowered.api.world.biome.Biomes;
import org.spongepowered.api.world.biome.provider.BiomeProvider;
import org.spongepowered.api.world.biome.provider.MultiNoiseBiomeConfig;
import org.spongepowered.api.world.generation.ChunkGenerator;
import org.spongepowered.api.world.generation.config.SurfaceRule;
import org.spongepowered.api.world.generation.config.noise.NoiseConfig;
import org.spongepowered.api.world.generation.config.noise.NoiseGeneratorConfig;
import org.spongepowered.api.world.generation.config.noise.NoiseGeneratorConfigs;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldManager;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.world.WorldTest;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Plugin("worldgentest")
public final class WorldGenTest {

    private final PluginContainer plugin;
    private final FeatureTest featureTest = new FeatureTest();
    private final StructureTest structureTest = new StructureTest();
    private final NoiseTest noiseTest = new NoiseTest();
    private final CarverTest carverTest = new CarverTest();
    private final BiomeTest biomeTest = new BiomeTest();
    private final ChunkGeneratorTest chunkGenTest = new ChunkGeneratorTest();

    @Inject
    public WorldGenTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Listener
    private void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.plugin,
                Command.builder()
                        .addChild(this.featureTest.featureCmd(), "feature")
                        .addChild(this.structureTest.structureCmd(), "structure")
                        .addChild(this.structureTest.setsCmd(), "sets")
                        .addChild(this.structureTest.schematicCmd(), "schematic")
                        .addChild(this.structureTest.jigsawCmd(), "jigsaw")
                        .addChild(this.structureTest.processorCmd(), "processor")
                        .addChild(this.noiseTest.noiseCmd(), "noise")
                        .addChild(this.carverTest.carverCmd(), "carver")
                        .addChild(this.biomeTest.biomeCmd(), "biome")
                        .addChild(this.chunkGenTest.chunkGenCmd(), "chunkgen")
                        .addChild(Command.builder().executor(this::createRandomWorld).build(), "createrandomworld", "crw")
                        .addChild(Command.builder().executor(this::world).build(), "world")
                        .addChild(Command.builder().executor(this::worldType).build(), "worldtype")
                        .build()
                ,"wgentest")
        ;
    }

    private CommandResult worldType(final CommandContext commandContext) {
        final WorldTypeTemplate.Builder builder = WorldTypeTemplate.builder()
                .add(Keys.WORLD_TYPE_EFFECT, WorldTypeEffects.OVERWORLD)
                .add(Keys.SCORCHING, true)
                .add(Keys.NATURAL_WORLD_TYPE, false)
                .add(Keys.COORDINATE_MULTIPLIER, 128d)
                .add(Keys.HAS_SKYLIGHT, false)
                .add(Keys.HAS_CEILING, false)
                .add(Keys.PIGLIN_SAFE, true)
                .add(Keys.BEDS_USABLE, false)
                .add(Keys.RESPAWN_ANCHOR_USABLE, false)
                .add(Keys.INFINIBURN, BlockTypeTags.WOOL)
                .add(Keys.WORLD_FLOOR, 0)
                .add(Keys.HAS_RAIDS, false)
                .add(Keys.WORLD_HEIGHT, 2000)
                .add(Keys.WORLD_LOGICAL_HEIGHT, 2000)
                .add(Keys.SPAWN_LIGHT_LIMIT, 5)
                .add(Keys.SPAWN_LIGHT_RANGE, Range.intRange(5, 10))
                .add(Keys.AMBIENT_LIGHTING, 0.5f)
                .add(Keys.CREATE_DRAGON_FIGHT, true)
                .key(ResourceKey.of(this.plugin, "customworldtype"));
        final WorldTypeTemplate template = builder.build();
        Sponge.server().dataPackManager().save(template);
        return CommandResult.success();
    }

    private CommandResult world(CommandContext commandContext) {
        final WorldManager wm = Sponge.server().worldManager();
        final ResourceKey worldKey = ResourceKey.of(BiomeTest.NAMESPACE, BiomeTest.CUSTOM_PLAINS);
        final Optional<Biome> customBiome = this.biomeRegistry().findValue(ResourceKey.of(BiomeTest.NAMESPACE, BiomeTest.CUSTOM_PLAINS));
        final Optional<Biome> customBiome2 = this.biomeRegistry().findValue(ResourceKey.of(BiomeTest.NAMESPACE, BiomeTest.CUSTOM_FOREST));
        if (customBiome.isPresent()) {
            final RegistryReference<Biome> ref = RegistryReference.referenced(Sponge.server(), RegistryTypes.BIOME, customBiome.get());
            final RegistryReference<Biome> ref2 = RegistryReference.referenced(Sponge.server(), RegistryTypes.BIOME, customBiome2.get());
            final MultiNoiseBiomeConfig cfg = MultiNoiseBiomeConfig.builder()
                    .addBiome(AttributedBiome.of(ref, BiomeAttributes.point(0.1f, -0.6f, 0.1f, -0.5f, 0f, -1f, 0.0f)))
                    .addBiome(AttributedBiome.of(ref2, BiomeAttributes.point(0.4f, -1f, 0.5f, -0.2f, 0f, -0.95f, 0.0f)))
                    .build();
            final var chunkGen = ChunkGenerator.noise(BiomeProvider.multiNoise(cfg), ChunkGenerator.overworld().config());
            final WorldTemplate template = WorldTemplate.builder().add(Keys.CHUNK_GENERATOR, chunkGen).key(worldKey).build();
            final Optional<ServerPlayer> optPlayer = commandContext.cause().first(ServerPlayer.class);
            wm.loadWorld(template).thenAccept(w -> optPlayer.ifPresent(player -> WorldTest.transportToWorld(player, w)));
        }
        return CommandResult.success();
    }

    private Registry<Biome> biomeRegistry() {
        return Sponge.server().registry(RegistryTypes.BIOME);
    }

    private CommandResult createRandomWorld(final CommandContext context) {
        final WorldManager wm = Sponge.server().worldManager();
        final ServerPlayer player = (ServerPlayer) context.cause().root();
        final String owner = player.name();
        final RandomProvider.Source random = player.random();

        final List<RegistryReference<Biome>> allBiomes = Sponge.server().registry(RegistryTypes.BIOME)
                .streamEntries()
                .map(RegistryEntry::asReference).toList();
        final List<RegistryReference<Biome>> biomes = IntStream.range(0, Math.min(allBiomes.size(), random.nextInt(5, 15)))
                .mapToObj(i -> allBiomes.get(random.nextInt(allBiomes.size())))
                .toList();

        if (biomes.isEmpty()) {
            biomes.add(Biomes.PLAINS);
        }

        final NoiseConfig noiseConfig = NoiseConfig.builder().build();

        final SurfaceRule.Factory surfaceRuleFactory = Sponge.game().factoryProvider().provide(SurfaceRule.Factory.class);
        final SurfaceRule customSurfaceRule;
        if (random.nextBoolean()) {
            customSurfaceRule =
                surfaceRuleFactory.firstOf(
                    surfaceRuleFactory.nearSurface().then(
                        surfaceRuleFactory.firstOf(
                            surfaceRuleFactory.steep().then(
                                surfaceRuleFactory.firstOf(
                                    // On steep slopes generate diamond ore under some stones
                                    surfaceRuleFactory.onFloor().then(surfaceRuleFactory.block(BlockTypes.COBBLESTONE.get().defaultState())),
                                    surfaceRuleFactory.underFloor(5).then(surfaceRuleFactory.block(BlockTypes.COBBLED_DEEPSLATE.get().defaultState())),
                                    surfaceRuleFactory.underFloor(7).then(surfaceRuleFactory.block(BlockTypes.DEEPSLATE.get().defaultState())),
                                    surfaceRuleFactory.underFloor(8).then(surfaceRuleFactory.block(BlockTypes.DEEPSLATE_DIAMOND_ORE.get().defaultState()))
                                )),
                            // Generate ShroomLight holes?
                            surfaceRuleFactory.hole().then(surfaceRuleFactory.floor(1, true, 10).then(surfaceRuleFactory.block(BlockTypes.SHROOMLIGHT.get().defaultState())))

                        )
                    ),
                    surfaceRuleFactory.onFloor().then(
                        surfaceRuleFactory.firstOf(
                                // Under water warped
                                surfaceRuleFactory.liquidDepthFromSurface(-10, 2).not().then(surfaceRuleFactory.block(BlockTypes.WARPED_WART_BLOCK.get().defaultState())),
                                surfaceRuleFactory.liquidDepthFromSurface(0, 0).not().then(surfaceRuleFactory.block(BlockTypes.WARPED_NYLIUM.get().defaultState())),
                                // On land crimson
                                surfaceRuleFactory.blockAbove(surfaceRuleFactory.absolute(90), 5).then(surfaceRuleFactory.block(BlockTypes.NETHER_WART_BLOCK.get().defaultState())),
                                surfaceRuleFactory.block(BlockTypes.CRIMSON_NYLIUM.get().defaultState())
                        ))
            );
        } else {
            customSurfaceRule = SurfaceRule.overworldLike(random.nextBoolean(), random.nextBoolean(), random.nextBoolean());
        }


        final NoiseGeneratorConfig noiseGenConfig = NoiseGeneratorConfig.builder()
                .spawnTargets(NoiseGeneratorConfigs.OVERWORLD.get().spawnTargets())
                .noiseConfig(noiseConfig)
                .surfaceRule(customSurfaceRule)
//                .seaLevel(random.nextInt(61 - 1) + 1 + random.nextInt(30)) // 2 rolls
                .seaLevel(63)
                .key(ResourceKey.of(this.plugin, "test"))
                .build().config();

        final ResourceKey worldKey = ResourceKey.of(this.plugin, owner.toLowerCase());
        final List<AttributedBiome> attributedBiomes = biomes.stream().map(biomeRef -> {
                    final Biome biome = biomeRef.get(Sponge.server());
                    final Optional<BiomeAttributes> attr = BiomeAttributes.defaultAttributes(biomeRef);
                    if (attr.isPresent()) {
                        return AttributedBiome.of(biomeRef, attr.get());
                    }
                    final BiomeAttributes randomAttr = BiomeAttributes.point((float) biome.temperature(),
                            (float) biome.humidity(),
                            random.nextFloat() * 2 - 1,
                            random.nextFloat() * 2 - 1,
                            random.nextFloat() * 2 - 1,
                            random.nextFloat() / 5,
                            0f);
                    System.out.println("No BiomeAttributes found for: " + biomeRef.location());
                    return AttributedBiome.of(biomeRef, randomAttr);
                }
        ).toList();

        final MultiNoiseBiomeConfig biomeCfg = MultiNoiseBiomeConfig.builder().addBiomes(attributedBiomes).build();
        final Optional<WorldType> customworldtype = WorldTypes.registry().findValue(ResourceKey.of(this.plugin, "customworldtype"));
        final WorldTemplate customTemplate = WorldTemplate.builder()
                .from(WorldTemplate.overworld())
                .key(worldKey)
                .add(Keys.WORLD_TYPE, customworldtype.orElse(WorldTypes.OVERWORLD.get()))
                .add(Keys.SERIALIZATION_BEHAVIOR, SerializationBehavior.NONE)
                .add(Keys.IS_LOAD_ON_STARTUP, false)
                .add(Keys.PERFORM_SPAWN_LOGIC, true)
                .add(Keys.SEED, random.nextLong())
                .add(Keys.DISPLAY_NAME, Component.text("Custom world by " + owner))
                .add(Keys.CHUNK_GENERATOR, ChunkGenerator.noise(BiomeProvider.multiNoise(biomeCfg), noiseGenConfig))
                .build();

        if (player.world().key().equals(worldKey)) {
            final ServerWorld world = wm.world(DefaultWorldKeys.DEFAULT).get();
            player.setLocation(ServerLocation.of(world, world.properties().spawnPosition()));
        }
        context.sendMessage(Identity.nil(), Component.text("Generating your world...").append(Component.newline())
                .append(Component.text("with " + biomes.size() + " biomes")).append(Component.newline())
        );
        wm.deleteWorld(worldKey).thenCompose(b -> wm.loadWorld(customTemplate)).thenAccept(w -> WorldTest.transportToWorld(player, w)).exceptionally(e -> {
                    context.sendMessage(Identity.nil(), Component.text("Failed to teleport!", NamedTextColor.DARK_RED));
                    e.printStackTrace();
                    return null;
                }
        );


        return CommandResult.success();
    }

}
