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
package org.spongepowered.worldtest;

import com.google.inject.Inject;
import net.kyori.adventure.text.TextComponent;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.dimension.DimensionType;
import org.spongepowered.api.world.portal.PortalType;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

@Plugin("worldtest")
public final class WorldTest {

    private final PluginContainer plugin;

    @Inject
    public WorldTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<ServerPlayer> playerParameter = Parameter.playerOrSource().setKey("player").build();
        final Parameter.Value<WorldProperties> worldParameter = Parameter.worldProperties().setKey("world").build();
        final Parameter.Value<ServerLocation> locationParameter = Parameter.location().setKey("location").build();
        final Parameter.Value<PortalType> portalTypeParameter = Parameter.catalogedElement(PortalType.class).setKey("portal_type").build();
        final Parameter.Value<DimensionType> dimensionTypeParameter =
                Parameter.catalogedElement(DimensionType.class).setKey("dimension_type").build();

        event.register(this.plugin, Command
                        .builder()
                        .parameter(locationParameter)
                        .parameter(portalTypeParameter)
                        .setPermission(this.plugin.getMetadata().getId() + ".command.portal.create")
                        .setExecutor(context -> {
                            final ServerLocation location = context.requireOne(locationParameter);
                            final PortalType portalType = context.requireOne(portalTypeParameter);
                            portalType.generatePortal(location);
                            return CommandResult.success();
                        })
                        .build()
                , "cp", "createportal"
        );

        event.register(this.plugin, Command
                        .builder()
                        .parameter(playerParameter)
                        .parameter(locationParameter)
                        .parameter(portalTypeParameter)
                        .setPermission(this.plugin.getMetadata().getId() + ".command.portal.use")
                        .setExecutor(context -> {
                            final ServerPlayer player = context.requireOne(playerParameter);
                            final ServerLocation location = context.requireOne(locationParameter);
                            final PortalType portalType = context.requireOne(portalTypeParameter);
                            return portalType.teleport(player, location, true) ? CommandResult.success() : CommandResult
                                    .error(TextComponent.of("Could not teleport!"));
                        })
                        .build()
                , "up", "useportal"
        );

        event.register(this.plugin, Command
                        .builder()
                        .parameter(playerParameter)
                        .parameter(dimensionTypeParameter)
                        .setPermission(this.plugin.getMetadata().getId() + ".command.environment.change")
                        .setExecutor(context -> {
                            final ServerPlayer player = context.requireOne(playerParameter);
                            final DimensionType dimensionType = context.requireOne(dimensionTypeParameter);
                            player.sendEnvironment(dimensionType);
                            return CommandResult.success();
                        })
                        .build()
                , "ce", "changeenvironment"
        );

        event.register(this.plugin, Command
                        .builder()
                        .parameter(worldParameter)
                        .parameter(dimensionTypeParameter)
                        .setPermission(this.plugin.getMetadata().getId() + ".command.dimension.change")
                        .setExecutor(context -> {
                            final WorldProperties world = context.requireOne(worldParameter);
                            final DimensionType dimensionType = context.requireOne(dimensionTypeParameter);
                            world.setDimensionType(dimensionType);
                            return CommandResult.success();
                        })
                        .build()
                , "cd", "changedimension"
        );

        event.register(this.plugin, Command
                        .builder()
                        .parameter(playerParameter)
                        .parameter(locationParameter)
                        .setPermission(this.plugin.getMetadata().getId() + ".command.position.change")
                        .setExecutor(context -> {
                            final ServerPlayer player = context.requireOne(playerParameter);
                            final ServerLocation location = context.requireOne(locationParameter);
                            return player.setLocation(location) ? CommandResult.success() : CommandResult.error(TextComponent.of("Could not "
                                    + "teleport!"));
                        })
                        .build()
                , "cl", "changelocation"
        );
    }

    @Listener
    public void onChangeWorld(final ChangeEntityWorldEvent event) {
        System.err.println(event);
    }
}
