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
package org.spongepowered.test.world;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterDataPackValueEvent;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.biome.BiomeProviderTemplate;
import org.spongepowered.api.world.WorldTypeEffects;
import org.spongepowered.api.world.WorldTypeTemplate;
import org.spongepowered.api.world.WorldTypeTemplates;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.generation.ChunkGeneratorTemplate;
import org.spongepowered.api.world.generation.Structures;
import org.spongepowered.api.world.generation.config.NoiseGeneratorConfig;
import org.spongepowered.api.world.generation.config.noise.NoiseConfig;
import org.spongepowered.api.world.generation.config.noise.SamplingConfig;
import org.spongepowered.api.world.generation.config.noise.SlideConfig;
import org.spongepowered.api.world.generation.config.structure.SeparatedStructureConfig;
import org.spongepowered.api.world.generation.config.structure.SpacedStructureConfig;
import org.spongepowered.api.world.generation.config.structure.StructureGenerationConfig;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.api.world.server.WorldTemplates;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

@Plugin("dimensiontest")
public final class DimensionTest {

    private final PluginContainer plugin;

    @Inject
    public DimensionTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onRegisterWorldTypeTemplates(final RegisterDataPackValueEvent<@NonNull WorldTypeTemplate> event) {
        event
            .register(WorldTypeTemplate
                .builder()
                    .from(WorldTypeTemplates.THE_NETHER)
                    .key(ResourceKey.of(this.plugin, "test_one"))
                    .effect(WorldTypeEffects.END)
                    .createDragonFight(true)
                    .build()
            )
        ;
    }

    @Listener
    public void onRegisterWorldTemplates(final RegisterDataPackValueEvent<@NonNull WorldTemplate> event) {
        event
                .register(WorldTemplate
                        .builder()
                        .from(WorldTemplates.OVERWORLD)
                        .key(ResourceKey.of(this.plugin, "more_difficult_overworld"))
                        .worldType(RegistryKey.of(RegistryTypes.WORLD_TYPE, ResourceKey.of(this.plugin, "test_one")).asReference())
                        .displayName(Component.text("Mean World", NamedTextColor.RED))
                        .generator(ChunkGeneratorTemplate.noise(
                                BiomeProviderTemplate.overworld(), NoiseGeneratorConfig.builder()
                                        .structureConfig(StructureGenerationConfig.builder()
                                                .stronghold(SpacedStructureConfig.of(10, 10, 1))
                                                .structure(Structures.IGLOO, SeparatedStructureConfig.of(5, 5, 10))
                                                .build()
                                        )
                                        .noiseConfig(NoiseConfig.builder()
                                                .bottom(SlideConfig.of(1, 1, 1))
                                                .top(SlideConfig.of(1, 1, 1))
                                                .sampling(SamplingConfig.of(1, 1, 1, 1))
                                                .height(128)
                                                .build()
                                        )
                                        .seaLevel(200)
                                        .build()
                                )
                        )
                        .difficulty(Difficulties.HARD)
                        .build()
                )
        ;
    }
}
