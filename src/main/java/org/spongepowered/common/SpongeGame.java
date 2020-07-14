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
package org.spongepowered.common;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.Client;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.command.manager.CommandManager;
import org.spongepowered.api.config.ConfigManager;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.network.channel.ChannelRegistry;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.registry.GameRegistry;
import org.spongepowered.api.service.ServiceProvider;
import org.spongepowered.api.sql.SqlManager;
import org.spongepowered.api.util.metric.MetricsConfigManager;
import org.spongepowered.common.scheduler.AsyncScheduler;
import org.spongepowered.common.util.LocaleCache;

import java.nio.file.Path;
import java.util.Locale;

@Singleton
public final class SpongeGame implements Game {

    private final Platform platform;
    private final GameRegistry registry;
    private final DataManager dataManager;
    private final PluginManager pluginManager;
    private final EventManager eventManager;
    private final AssetManager assetManager;
    private final ConfigManager configManager;
    private final ChannelRegistry channelRegistry;
    private final MetricsConfigManager metricsConfigManager;
    private final CommandManager commandManager;
    private final SqlManager sqlManager;
    private final ServiceProvider serviceProvider;
    private final AsyncScheduler asyncScheduler = new AsyncScheduler();

    private Client client;
    private Server server;

    @Inject
    public SpongeGame(final Platform platform, final GameRegistry registry, final DataManager dataManager, final PluginManager pluginManager,
        final EventManager eventManager, final AssetManager assetManager, final ConfigManager configManager, final ChannelRegistry channelRegistry,
        final MetricsConfigManager metricsConfigManager, final CommandManager commandManager, final SqlManager sqlManager,
        final ServiceProvider serviceProvider) {

        this.platform = platform;
        this.registry = registry;
        this.dataManager = dataManager;
        this.pluginManager = pluginManager;
        this.eventManager = eventManager;
        this.assetManager = assetManager;
        this.configManager = configManager;
        this.channelRegistry = channelRegistry;
        this.metricsConfigManager = metricsConfigManager;
        this.commandManager = commandManager;
        this.sqlManager = sqlManager;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public Path getGameDirectory() {
        return SpongeCommon.getGameDirectory();
    }

    @Override
    public SystemSubject getSystemSubject() {
        return (SystemSubject) SpongeCommon.getServer();
    }

    @Override
    public Platform getPlatform() {
        return this.platform;
    }

    @Override
    public GameRegistry getRegistry() {
        return this.registry;
    }

    @Override
    public DataManager getDataManager() {
        return this.dataManager;
    }

    @Override
    public PluginManager getPluginManager() {
        return this.pluginManager;
    }

    @Override
    public EventManager getEventManager() {
        return this.eventManager;
    }

    @Override
    public AssetManager getAssetManager() {
        return this.assetManager;
    }

    @Override
    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    @Override
    public ChannelRegistry getChannelRegistry() {
        return this.channelRegistry;
    }

    @Override
    public MetricsConfigManager getMetricsConfigManager() {
        return this.metricsConfigManager;
    }

    @Override
    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    @Override
    public SqlManager getSqlManager() {
        return this.sqlManager;
    }

    @Override
    public ServiceProvider getServiceProvider() {
        return this.serviceProvider;
    }

    @Override
    public AsyncScheduler getAsyncScheduler() {
        return this.asyncScheduler;
    }

    @Override
    public Locale getLocale(final String locale) {
        return LocaleCache.getLocale(Preconditions.checkNotNull(locale));
    }

    @Override
    public boolean isServerAvailable() {
        if (this.client != null) {
            return this.client.getServer().isPresent();
        }

        return this.server != null;
    }

    @Override
    public Server getServer() {
        if (this.client != null) {
            return this.client.getServer().orElseThrow(() -> new IllegalStateException("The singleplayer server is not available!"));
        }

        Preconditions.checkState(this.server != null, "The dedicated server is not available!");
        return this.server;
    }

    public void setServer(final Server server) {
        this.server = server;
    }

    @Override
    public boolean isClientAvailable() {
        return this.client != null;
    }

    @Override
    public Client getClient() {
        Preconditions.checkState(this.client != null, "The client is not available!");
        return this.client;
    }

    public void setClient(final Client client) {
        this.client = client;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("platform", this.platform)
                .toString();
    }
}
