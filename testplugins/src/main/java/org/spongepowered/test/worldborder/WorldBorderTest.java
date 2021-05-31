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
package org.spongepowered.test.worldborder;

import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.CommonParameters;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.world.border.WorldBorder;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

@Plugin("worldbordertest")
public final class WorldBorderTest implements LoadableModule {

    private final PluginContainer pluginContainer;
    private final WorldBorderListener listener;

    @Inject
    public WorldBorderTest(final PluginContainer pluginContainer, final Logger logger) {
        this.pluginContainer = pluginContainer;
        this.listener = new WorldBorderListener(logger);
    }

    @Override
    public void enable(final CommandContext ctx) {
        Sponge.eventManager().registerListeners(this.pluginContainer, this.listener);
    }

    @Override
    public void disable(final CommandContext ctx) {
        Sponge.eventManager().unregisterListeners(this.listener);
    }

    @Listener
    public void registerCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.pluginContainer, Command.builder()
                .addChild(this.setWorldBorder(), "world")
                .addChild(
                        Command.builder().addChild(this.setPlayerWorldBorder(), "set").addChild(this.unsetPlayerWorldBorder(), "unset").build(),
                        "player")
                .build(), "worldbordertest");
    }

    public Command.Parameterized setWorldBorder() {
        final Parameter.Value<ServerLocation> location = Parameter.location()
                .key("center")
                .build();
        final Parameter.Value<Integer> diameter = Parameter.rangedInteger(1, 300000)
                .key("diameter")
                .build();
        return Command.builder()
                .addParameter(location)
                .addParameter(diameter)
                .executor(ctx -> {
                    final ServerLocation loc = ctx.requireOne(location);
                    final int dia = ctx.requireOne(diameter);
                    final ServerWorld world = loc.world();
                    final WorldBorder worldBorder = world.border();
                    world.setBorder(worldBorder.toBuilder().center(loc.x(), loc.z()).initialDiameter(dia).build());
                    ctx.sendMessage(Identity.nil(),
                            Component.text().content("World border changed for " + loc.worldKey().asString()).build());
                    return CommandResult.success();
                })
                .build();
    }

    public Command.Parameterized setPlayerWorldBorder() {
        final Parameter.Value<Integer> centerX = Parameter.integerNumber()
                .key("x")
                .build();
        final Parameter.Value<Integer> centerZ = Parameter.integerNumber()
                .key("z")
                .build();
        final Parameter.Value<Integer> diameter = Parameter.rangedInteger(1, 300000)
                .key("diameter")
                .build();
        return Command.builder()
                .addParameter(CommonParameters.PLAYER)
                .addParameter(centerX)
                .addParameter(centerZ)
                .addParameter(diameter)
                .executor(ctx -> {
                    final ServerPlayer player = ctx.requireOne(CommonParameters.PLAYER);
                    final WorldBorder border = WorldBorder.builder()
                            .center(ctx.requireOne(centerX), ctx.requireOne(centerZ))
                            .targetDiameter(ctx.requireOne(diameter))
                            .build();
                    player.setWorldBorder(border);
                    ctx.sendMessage(Identity.nil(),
                            Component.text().content("World border set for player " + player.name())
                                    .color(NamedTextColor.GREEN).build());
                    return CommandResult.success();
                })
                .build();
    }

    public Command.Parameterized unsetPlayerWorldBorder() {
        return Command.builder()
                .addParameter(CommonParameters.PLAYER)
                .executor(ctx -> {
                    final ServerPlayer player = ctx.requireOne(CommonParameters.PLAYER);
                    player.setWorldBorder(null);
                    ctx.sendMessage(Identity.nil(),
                            Component.text().content("World border removed for player " + player.name())
                                    .color(NamedTextColor.GREEN).build());
                    return CommandResult.success();
                })
                .build();
    }
}
