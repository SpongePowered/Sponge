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
package org.spongepowered.test;

import com.google.inject.Inject;
import net.kyori.adventure.text.TextComponent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Plugin("test")
public final class TestPlugin {

    private final PluginContainer plugin;
    private final Set<String> enabledPlugins = new HashSet<>();

    @Inject
    public TestPlugin(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<PluginContainer> pluginKey = Parameter.plugin().setKey("plugin").setSuggestions(
                context -> Sponge.getPluginManager().getPlugins().stream()
                        .filter(pc -> pc.getInstance() instanceof LoadableModule)
                        .map(x -> x.getMetadata().getId()).collect(Collectors.toList())).build();
        final Command.Parameterized enableCommand = Command.builder().parameter(pluginKey)
                .setExecutor(context -> {
                    final PluginContainer pc = context.requireOne(pluginKey);
                    if (pc.getInstance() instanceof LoadableModule) {
                        if (this.enabledPlugins.add(pc.getMetadata().getId())) {
                            ((LoadableModule) pc.getInstance()).enable(context);
                            context.sendMessage(TextComponent.of("Enabled " + pc.getMetadata().getId()));
                        } else {
                            context.sendMessage(TextComponent.of("Already enabled " + pc.getMetadata().getId()));
                        }
                    }
                    return CommandResult.success();
                }).build();
        final Command.Parameterized disableCommand = Command.builder().parameter(pluginKey)
                .setExecutor(context -> {
                    final PluginContainer pc = context.requireOne(pluginKey);
                    if (pc.getInstance() instanceof LoadableModule) {
                        if (this.enabledPlugins.remove(pc.getMetadata().getId())) {
                            ((LoadableModule) pc.getInstance()).disable(context);
                            context.sendMessage(TextComponent.of("Disabled " + pc.getMetadata().getId()));
                        } else {
                            context.sendMessage(TextComponent.of("Already disabled " + pc.getMetadata().getId()));
                        }
                    }
                    return CommandResult.success();
                }).build();
        final Command.Parameterized toggleCommand = Command.builder().parameter(pluginKey)
                .setExecutor(context -> {
                    final PluginContainer pc = context.requireOne(pluginKey);
                    if (pc.getInstance() instanceof LoadableModule) {
                        if (this.enabledPlugins.contains(pc.getMetadata().getId())) {
                            this.enabledPlugins.remove(pc.getMetadata().getId());
                            ((LoadableModule) pc.getInstance()).disable(context);
                            context.sendMessage(TextComponent.of("Disabled " + pc.getMetadata().getId()));
                        } else {
                            this.enabledPlugins.add(pc.getMetadata().getId());
                            ((LoadableModule) pc.getInstance()).enable(context);
                            context.sendMessage(TextComponent.of("Enabled " + pc.getMetadata().getId()));
                        }
                    }
                    return CommandResult.success();
                }).build();

        final Command.Parameterized testPluginCommand = Command.builder()
                .child(enableCommand, "enable")
                .child(disableCommand, "disable")
                .child(toggleCommand, "toggle")
                .parameter(pluginKey)
                .setExecutor(toggleCommand)
                .build();
        event.register(this.plugin, testPluginCommand, "testplugins");
    }
}
