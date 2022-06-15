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
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.world.generation.config.noise.NoiseGeneratorConfigTemplate;
import org.spongepowered.api.world.generation.config.noise.NoiseGeneratorConfigs;
import org.spongepowered.api.world.generation.config.noise.NoiseTemplate;
import org.spongepowered.api.world.generation.config.noise.Noises;
import org.spongepowered.api.world.server.DataPackManager;

import java.util.Optional;

public class NoiseTest {

    private CommandResult listNoise(final CommandContext ctx, final Parameter.Value<String> filterParam) {
        final Optional<String> rawFilter = ctx.one(filterParam);
        final String filter = rawFilter.orElse("minecraft:").toUpperCase();
        final boolean invert = rawFilter.isPresent();

        ctx.sendMessage(Identity.nil(), Component.text("Noise:", NamedTextColor.DARK_AQUA));
        Noises.registry().streamEntries().filter(e -> invert == e.key().toString().toUpperCase().contains(filter))
                .forEach(e -> ctx.sendMessage(Identity.nil(), Component.text(" - " + e.key(), NamedTextColor.GRAY)));

        ctx.sendMessage(Identity.nil(), Component.text("Noise Generator Configs:", NamedTextColor.DARK_AQUA));
        NoiseGeneratorConfigs.registry().streamEntries().filter(e -> invert == e.key().toString().toUpperCase().contains(filter))
                .forEach(e -> ctx.sendMessage(Identity.nil(), Component.text(" - " + e.key(), NamedTextColor.GRAY)));

        return CommandResult.success();
    }

    private CommandResult registerNoise(final CommandContext commandContext) {
        final DataPackManager dpm = Sponge.server().dataPackManager();

        final NoiseGeneratorConfigTemplate noiseGenCfgTemplate = NoiseGeneratorConfigTemplate.builder().from(NoiseGeneratorConfigs.CAVES.get())
                .key(ResourceKey.of("noisetest", "test"))
                .build();

        final NoiseTemplate noiseTemplate = NoiseTemplate.builder().from(Noises.CALCITE.get())
                .key(ResourceKey.of("noisetest", "calcite_copy"))
                .build();

        try {
            dpm.save(noiseGenCfgTemplate);
            dpm.save(noiseTemplate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return CommandResult.success();
    }

    Command.Parameterized noiseCmd() {
        final Parameter.Value<String> filter = Parameter.string().key("filter").optional().build();
        return Command.builder()
                .addChild(Command.builder().addParameter(filter).executor(ctx -> this.listNoise(ctx, filter)).build(), "list")
                .addChild(Command.builder().executor(this::registerNoise).build(), "register")
                .build();
    }

}
