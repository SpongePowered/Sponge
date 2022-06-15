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

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.biome.Biomes;
import org.spongepowered.api.world.biome.provider.BiomeProvider;
import org.spongepowered.api.world.biome.provider.CheckerboardBiomeConfig;
import org.spongepowered.api.world.biome.provider.ConfigurableBiomeProvider;
import org.spongepowered.api.world.biome.provider.EndStyleBiomeConfig;
import org.spongepowered.api.world.biome.provider.FixedBiomeProvider;
import org.spongepowered.api.world.biome.provider.MultiNoiseBiomeConfig;
import org.spongepowered.api.world.generation.ChunkGenerator;
import org.spongepowered.api.world.generation.ConfigurableChunkGenerator;
import org.spongepowered.api.world.generation.config.flat.FlatGeneratorConfig;
import org.spongepowered.api.world.generation.config.flat.FlatGeneratorConfigs;
import org.spongepowered.api.world.generation.config.noise.NoiseConfig;
import org.spongepowered.api.world.generation.config.noise.NoiseConfigs;
import org.spongepowered.api.world.generation.config.noise.NoiseGeneratorConfig;
import org.spongepowered.api.world.generation.config.noise.NoiseGeneratorConfigs;

import java.util.List;

public final class ChunkGeneratorTest {

    private CommandResult listGenerators(CommandContext ctx, final Parameter.Value<String> filterParam) {
        final ConfigurableBiomeProvider<MultiNoiseBiomeConfig> provider = BiomeProvider.overworld();

        ctx.sendMessage(Identity.nil(), Component.text("NoiseGeneratorConfig:", NamedTextColor.DARK_AQUA));
        NoiseGeneratorConfigs.registry().streamEntries().forEach(cfg -> {
            final ConfigurableChunkGenerator<NoiseGeneratorConfig> gen = ChunkGenerator.noise(provider, cfg.value());
            ctx.sendMessage(Identity.nil(), Component.text(" - " + cfg.key(), NamedTextColor.GRAY));
        });
        ctx.sendMessage(Identity.nil(), Component.text("FlatGeneratorConfig:", NamedTextColor.DARK_AQUA));
        FlatGeneratorConfigs.registry().streamEntries().forEach(cfg -> {
            final ConfigurableChunkGenerator<FlatGeneratorConfig> gen = ChunkGenerator.flat(cfg.value());
            ctx.sendMessage(Identity.nil(), Component.text(" - " + cfg.key(), NamedTextColor.GRAY));
        });

        ctx.sendMessage(Identity.nil(), Component.text("NoiseConfigs:", NamedTextColor.DARK_AQUA));
        NoiseConfigs.registry().streamEntries().forEach(cfg -> {
            final NoiseConfig config = cfg.value();
            ctx.sendMessage(Identity.nil(), Component.text(" - " + cfg.key(), NamedTextColor.GRAY));
        });

        return CommandResult.success();
    }

    private CommandResult listProviders(CommandContext ctx, final Parameter.Value<String> filterParam) {
        final ConfigurableBiomeProvider<MultiNoiseBiomeConfig> overworld = BiomeProvider.overworld();
        final ConfigurableBiomeProvider<MultiNoiseBiomeConfig> nether = BiomeProvider.nether();
        final ConfigurableBiomeProvider<EndStyleBiomeConfig> end = BiomeProvider.end();
        final ConfigurableBiomeProvider<CheckerboardBiomeConfig> checkerboard = BiomeProvider.checkerboard(CheckerboardBiomeConfig.builder().addBiomes(List.of(Biomes.PLAINS, Biomes.BIRCH_FOREST)).build());
        final FixedBiomeProvider fixedPlains = BiomeProvider.fixed(Biomes.PLAINS);

        ctx.sendMessage(Identity.nil(), Component.text("BiomeProviders:", NamedTextColor.DARK_AQUA));
        ctx.sendMessage(Identity.nil(), Component.text(" - " + overworld.getClass().getSimpleName() + " " + overworld.config().getClass().getSimpleName(), NamedTextColor.GRAY));
        ctx.sendMessage(Identity.nil(), Component.text(" - " + nether.getClass().getSimpleName() + " " + nether.config().getClass().getSimpleName(), NamedTextColor.GRAY));
        ctx.sendMessage(Identity.nil(), Component.text(" - " + end.getClass().getSimpleName() + " " + end.config().getClass().getSimpleName(), NamedTextColor.GRAY));
        ctx.sendMessage(Identity.nil(), Component.text(" - " + checkerboard.getClass().getSimpleName() + " " + checkerboard.config().getClass().getSimpleName(), NamedTextColor.GRAY));
        ctx.sendMessage(Identity.nil(), Component.text(" - " + fixedPlains.getClass().getSimpleName() + " " + RegistryTypes.BIOME.get().valueKey(fixedPlains.biome()), NamedTextColor.GRAY));

        return CommandResult.success();
    }


    public Command.Parameterized chunkGenCmd() {
        final Parameter.Value<String> filter = Parameter.string().key("filter").optional().build();
        return Command.builder()
                .addChild(Command.builder().addParameter(filter).executor(ctx -> this.listGenerators(ctx, filter)).build(), "listGenerators")
                .addChild(Command.builder().addParameter(filter).executor(ctx -> this.listProviders(ctx, filter)).build(), "listProviders")
                .build();
    }
}
