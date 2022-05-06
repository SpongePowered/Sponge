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
package org.spongepowered.test.pack;

import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Engine;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.resource.Resource;
import org.spongepowered.api.resource.ResourcePath;
import org.spongepowered.api.resource.pack.Pack;
import org.spongepowered.api.resource.pack.PackType;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Plugin("packtest")
public final class PackTest {

    private final PluginContainer plugin;
    private final Logger logger;

    @Inject
    public PackTest(final PluginContainer plugin, final Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<PluginContainer> pluginContainerParameter = Parameter.plugin().key("plugin").build();
        final Parameter.Value<String> nameParameter = Parameter.string().key("name").build();
        final Parameter.Value<String> pathParameter = Parameter.string().key("path").build();
        event.register(this.plugin,
                Command.builder()
                        .addParameter(Parameter.firstOf(
                                pluginContainerParameter,
                                nameParameter
                        ))
                        .addParameter(pathParameter)
                        .executor(exec -> {
                            final Pack pack;
                            final String path = exec.requireOne(pathParameter);
                            if (exec.hasAny(pluginContainerParameter)) {
                                pack = Sponge.server().packRepository().pack(exec.requireOne(pluginContainerParameter));
                            } else {
                                final String packName = exec.requireOne(nameParameter);
                                pack = Sponge.server().packRepository().pack(packName).orElse(null);
                                if (pack == null) {
                                    return CommandResult.error(Component.text("Pack " + packName + " does not exist."));
                                }
                            }
                            try (final Resource resource =
                                    pack.contents().requireResource(PackType.server(), ResourcePath.of(pack.id(), path))) {
                                final BufferedReader reader = new BufferedReader(new InputStreamReader(resource.inputStream()));
                                reader.lines().map(x -> Component.text(x.substring(0, Math.min(100, x.length()))))
                                        .forEach(x -> exec.sendMessage(Identity.nil(), x));
                            } catch (final Exception e) {
                                e.printStackTrace();
                                return CommandResult.error(Component.text("Could not locate: " + e.getMessage()));
                            }
                            return CommandResult.success();
                        })
                        .build(),
                "packtest"
        );
    }

    @Listener
    public void onStartedEngine(final StartedEngineEvent<@NonNull Engine> event) {
        this.logger.warn("Printing packs for engine: {}", event.engine().toString());
        for (final Pack pack : event.engine().packRepository().all()) {
            this.logger.info(pack.id());
        }
    }

}
