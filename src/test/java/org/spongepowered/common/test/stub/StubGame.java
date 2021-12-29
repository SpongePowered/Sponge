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
package org.spongepowered.common.test.stub;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.config.ConfigManager;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.network.channel.ChannelManager;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.service.ServiceProvider;
import org.spongepowered.api.sql.SqlManager;
import org.spongepowered.api.util.metric.MetricsConfigManager;
import org.spongepowered.common.registry.SpongeBuilderProvider;
import org.spongepowered.common.registry.SpongeFactoryProvider;
import org.spongepowered.common.test.stub.registry.StubRegistryHolder;

import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.Locale;

@Singleton
public class StubGame extends StubRegistryHolder implements Game {

    private final SpongeFactoryProvider provider = new SpongeFactoryProvider();
    private final SpongeBuilderProvider builder = new SpongeBuilderProvider();

    @Override
    public Scheduler asyncScheduler() {
        return null;
    }

    @Override
    public Path gameDirectory() {
        return null;
    }

    @Override
    public boolean isServerAvailable() {
        return false;
    }

    @Override
    public Server server() {
        return null;
    }

    @Override
    public SystemSubject systemSubject() {
        return null;
    }

    @Override
    public Locale locale(@NonNull String locale) {
        return null;
    }

    @Override
    public Platform platform() {
        return null;
    }

    @Override
    public SpongeBuilderProvider builderProvider() {
        return this.builder;
    }

    @Override
    public SpongeFactoryProvider factoryProvider() {
        return this.provider;
    }

    @Override
    public DataManager dataManager() {
        return null;
    }

    @Override
    public PluginManager pluginManager() {
        return null;
    }

    @Override
    public EventManager eventManager() {
        return null;
    }

    @Override
    public ConfigManager configManager() {
        return null;
    }

    @Override
    public ChannelManager channelManager() {
        return null;
    }

    @Override
    public MetricsConfigManager metricsConfigManager() {
        return null;
    }

    @Override
    public SqlManager sqlManager() {
        return null;
    }

    @Override
    public ServiceProvider.GameScoped serviceProvider() {
        return null;
    }

}
