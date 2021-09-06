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
package org.spongepowered.test.config;

import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.net.URL;

@Plugin("configtest")
public final class ConfigTest implements LoadableModule {

    private final Logger logger;
    private final ConfigurationReference<CommentedConfigurationNode> reference;
    private ValueReference<ExampleConfiguration, CommentedConfigurationNode> config;

    @Inject
    ConfigTest(final Logger logger, final @DefaultConfig(sharedRoot = true) ConfigurationReference<CommentedConfigurationNode> reference) {
        this.logger = logger;
        this.reference = reference;
    }

    @Listener
    public void onConstruction(final ConstructPluginEvent event) {
        final URL testOne = this.getClass().getResource("/configtest/test.txt");
        final URL testTwo = this.getClass().getResource("/configtest/test2.txt");
        this.logger.info("Asset one: {}, asset two: {}", testOne, testTwo);
        try {
            this.config = this.reference.referenceTo(ExampleConfiguration.class);
            this.reference.save();
        } catch (final ConfigurateException ex) {
            this.logger.error("Unable to load test configuration", ex);
        }
    }

    @Listener
    public void clientConnected(final ServerSideConnectionEvent.Join event) {
        final Component motd = this.config.get().getMotd();
        if (motd == null || motd == Component.empty()) {
            return;
        }

        event.player().sendMessage(Identity.nil(), motd);
    }


    @Override
    public void enable(final CommandContext ctx) {

    }
}
