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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Plugin(id = "main_test", name = "Main Test", version = "0.0.0", description = "main-test")
public class MainTestPlugin {

    private final Set<String> testPlugins = new HashSet<>();

    @Inject private PluginContainer container;

    @Listener
    public void onInit(GameInitializationEvent event) {
        Parameter.Key<PluginContainer> keyPlugin = Parameter.key("plugin", PluginContainer.class);
        Command enable = Command.builder()
                .parameters(Parameter.plugin().setKey(keyPlugin).optional().build())
                .setExecutor(((ctx) -> {
                    Collection<? extends PluginContainer> plugins = ctx.getAll(keyPlugin);
                    if (plugins.isEmpty()) {
                        plugins = Sponge.getPluginManager().getPlugins();
                    }
                    for (PluginContainer pc : plugins) {
                        pc.getInstance().ifPresent(plugin -> {
                            if (plugin instanceof LoadableModule) {
                                if (this.testPlugins.add(pc.getId())) {
                                    ((LoadableModule) plugin).enable(ctx.getMessageReceiver());
                                    Sponge.getServer().getBroadcastChannel().send(Text.of(pc.getId(), " enabled"));
                                } else {
                                    Sponge.getServer().getBroadcastChannel().send(Text.of(pc.getId(), "was already enabled"));
                                }
                            }
                        });
                    }
                    return CommandResult.success();
                }))
                .build();

        Command disable = Command.builder()
                .parameters(Parameter.plugin().setKey(keyPlugin).optional().build())
                .setExecutor(((ctx) -> {
                    Collection<? extends PluginContainer> plugins = ctx.getAll(keyPlugin);
                    if (plugins.isEmpty()) {
                        plugins = this.testPlugins.stream().map(s -> Sponge.getPluginManager().getPlugin(s).get()).collect(Collectors.toSet());
                    }
                    for (PluginContainer pc : plugins) {
                        pc.getInstance().ifPresent(plugin -> {
                            if (plugin instanceof LoadableModule) {
                                if (this.testPlugins.remove(pc.getId())) {
                                    ((LoadableModule) plugin).disable(ctx.getMessageReceiver());
                                    Sponge.getServer().getBroadcastChannel().send(Text.of(pc.getId(), " disabled"));
                                } else {
                                    Sponge.getServer().getBroadcastChannel().send(Text.of(pc.getId(), " is already disabled"));
                                }
                            }
                        });
                    }
                    return CommandResult.success();
                }))
                .build();

        Command status = Command.builder()
                .parameters()
                .setExecutor(((ctx) -> {
                    if (this.testPlugins.isEmpty()) {
                        ctx.getMessageReceiver().sendMessage(Text.of("There are no enabled testplugins"));
                        return CommandResult.success();
                    }
                    PaginationList.Builder builder = PaginationList.builder();
                    builder.title(Text.of("Enabled testplugins"))
                            .padding(Text.of(TextColors.GREEN, "="))
                            .contents(this.testPlugins.stream().map(Text::of).collect(Collectors.toSet()))
                            .build().sendTo(ctx.getMessageReceiver());
                    return CommandResult.success();
                }))
                .build();

        Command test = Command.builder()
                .child(enable, "enable", "e", "on")
                .child(disable, "disable", "d", "off")
                .child(status, "status")
                .build();

        Sponge.getCommandManager().register(this.container, test, "test", "testplugins");
    }

}
