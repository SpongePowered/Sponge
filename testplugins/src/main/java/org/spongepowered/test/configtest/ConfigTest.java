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
package org.spongepowered.test.configtest;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.plugin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.io.IOException;

@Plugin("configtest")
public class ConfigTest implements LoadableModule {

    private final Logger logger;
    private final ConfigurationLoader<CommentedConfigurationNode> loader;
    private ConfigurationNode node;
    private ExampleConfiguration config;

    @Inject
    ConfigTest(final Logger logger, final @DefaultConfig(sharedRoot = true) ConfigurationLoader<CommentedConfigurationNode> loader) {
        this.logger = logger;
        this.loader = loader;
    }

    @Listener
    public void onConstruction(final ConstructPluginEvent event) {
        try {
            this.node = this.loader.load();
            this.config = this.node.getValue(TypeToken.of(ExampleConfiguration.class), new ExampleConfiguration());
            this.node.setValue(TypeToken.of(ExampleConfiguration.class), this.config);
            this.loader.save(node);
        } catch (IOException | ObjectMappingException e) {
            this.logger.error("Unable to load test configuration", e);
        }
    }

    @Listener
    public void clientConnected(final ServerSideConnectionEvent.Join event) {
        final Component motd = this.config.getMotd();
        if (motd == null || motd == Component.empty()) {
            return;
        }

        event.getPlayer().sendMessage(Identity.nil(), motd);
    }


    @Override
    public void enable(final CommandContext ctx) {

    }
}
