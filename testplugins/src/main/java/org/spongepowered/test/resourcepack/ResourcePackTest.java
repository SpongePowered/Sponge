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
package org.spongepowered.test.resourcepack;

import com.google.inject.Inject;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.player.ResourcePackStatusEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

@Plugin("resourcepacktests")
public final class ResourcePackTest implements LoadableModule {

    private final PluginContainer pluginContainer;

    @Inject
    public ResourcePackTest(final PluginContainer pluginContainer) {
        this.pluginContainer = pluginContainer;
    }

    @Override
    public void enable(final CommandContext ctx) {
        Sponge.game().eventManager().registerListeners(this.pluginContainer, new Listeners());
    }

    @Listener
    private void registerCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<UUID> uuidParameter = Parameter.uuid().key("uuid").build();
        final Parameter.Value<URL> urlParameter = Parameter.url().key("url").build();
        final Parameter.Value<String> hashParameter = Parameter.string().key("hash").build();
        final Parameter.Value<Boolean> requiredParameter = Parameter.bool().key("required").optional().build();
        final Parameter.Value<Boolean> replaceParameter = Parameter.bool().key("replace").optional().build();
        final Parameter.Value<Component> promptParameter = Parameter.formattingCodeTextOfRemainingElements().key("prompt").optional().build();

        event.register(this.pluginContainer, Command.builder()
                .addParameter(uuidParameter)
                .addParameter(urlParameter)
                .addParameter(hashParameter)
                .addParameter(requiredParameter)
                .addParameter(replaceParameter)
                .addParameter(promptParameter)
                .executor(ctx -> {
                    final UUID uuid = ctx.requireOne(uuidParameter);
                    final URL url = ctx.requireOne(urlParameter);
                    final String hash = ctx.requireOne(hashParameter);
                    final Boolean required = ctx.one(requiredParameter).orElse(false);
                    final Boolean replace = ctx.one(replaceParameter).orElse(false);
                    final @Nullable Component prompt = ctx.one(promptParameter).orElse(null);
                    final ServerPlayer player = ctx.cause().first(ServerPlayer.class).get();
                    try {
                        player.sendResourcePacks(ResourcePackRequest.resourcePackRequest()
                                .packs(ResourcePackInfo.resourcePackInfo(uuid, url.toURI(), hash))
                                .required(required)
                                .replace(replace)
                                .prompt(prompt)
                                .callback((id, status, audience) ->
                                        Sponge.systemSubject().sendMessage(Component.text("ResourcePackCallback: Received resource pack status " + status + " for " + id)))
                                .build());
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                    return CommandResult.success();
                }).build(), "sendResourcePack");

        event.register(this.pluginContainer, Command.builder()
                .addParameter(uuidParameter)
                .executor(ctx -> {
                    final UUID uuid = ctx.requireOne(uuidParameter);
                    final ServerPlayer player = ctx.cause().first(ServerPlayer.class).get();
                    player.removeResourcePacks(List.of(uuid));
                    return CommandResult.success();
                }).build(), "removeResourcePack");

        event.register(this.pluginContainer, Command.builder()
                .executor(ctx -> {
                    final ServerPlayer player = ctx.cause().first(ServerPlayer.class).get();
                    player.clearResourcePacks();
                    return CommandResult.success();
                }).build(), "clearResourcePacks");
    }

    private static final class Listeners {

        @Listener
        private void onResourcePack(final ResourcePackStatusEvent event) {
            Sponge.systemSubject().sendMessage(Component.text("ResourcePackStatusEvent: Received resource pack status " + event.status() + " for " + event.pack().id() + " from " + event.profile()));
        }
    }
}
