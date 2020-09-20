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
package org.spongepowered.test.raytracetest;

import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameters;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.util.TypeTokens;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

@Plugin("raytracetest")
public final class RayTraceTest {

    private final PluginContainer pluginContainer;

    @Inject
    public RayTraceTest(final PluginContainer pluginContainer) {
        this.pluginContainer = pluginContainer;
    }

    @Listener
    public void registerCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<ServerLocation> serverLocationParameter = Parameter.builder(TypeTokens.SERVER_LOCATION_TOKEN)
                .setKey("target_location")
                .parser(CatalogedValueParameters.TARGET_BLOCK)
                .build();
        final Parameter.Value<Entity> entityParameter = Parameter.builder(TypeTokens.ENTITY_TOKEN)
                .setKey("target_entity")
                .parser(CatalogedValueParameters.TARGET_ENTITY)
                .build();

        event.register(
                this.pluginContainer,
                Command.builder()
                        .parameter(serverLocationParameter)
                        .setExecutor(context -> {
                            final ServerLocation serverLocation = context.requireOne(serverLocationParameter);
                            context.sendMessage(Identity.nil(), Component.text("Location: " + serverLocation.toString()));
                            context.sendMessage(Identity.nil(), Component.text("Block: " + serverLocation.getBlock().toString()));
                            return CommandResult.success();
                        })
                        .build(),
                "targetblock"
        );
        event.register(
                this.pluginContainer,
                Command.builder()
                        .parameter(entityParameter)
                        .setExecutor(context -> {
                            final Entity entity = context.requireOne(entityParameter);
                            context.sendMessage(Identity.nil(), Component.text("Location: " + entity.getLocation().toString()));
                            context.sendMessage(Identity.nil(), Component.text("Entity Type: " + entity.getType().getKey().asString()));
                            return CommandResult.success();
                        })
                        .build(),
                "targetentity"
        );
    }

}
