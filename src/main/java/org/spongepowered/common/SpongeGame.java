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
import net.minecraft.tags.StaticTagHelper;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Client;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.network.channel.ChannelManager;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.registry.BuilderProvider;
import org.spongepowered.api.registry.FactoryProvider;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.service.ServiceProvider;
import org.spongepowered.api.sql.SqlManager;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.util.metric.MetricsConfigManager;
import org.spongepowered.common.config.PluginConfigManager;
import org.spongepowered.common.registry.InitialRegistryData;
import org.spongepowered.common.registry.RegistryHolderLogic;
import org.spongepowered.common.registry.SpongeRegistryHolder;
import org.spongepowered.common.scheduler.AsyncScheduler;
import org.spongepowered.common.server.ServerConsoleSystemSubject;
import org.spongepowered.common.util.LocaleCache;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

@Singleton
public final class SpongeGame implements Game, SpongeRegistryHolder {

    private final Platform platform;
    private final BuilderProvider builderProvider;
    private final FactoryProvider factoryProvider;
    private final DataManager dataManager;
    private final PluginManager pluginManager;
    private final EventManager eventManager;
    private final PluginConfigManager configManager;
    private final ChannelManager channelManager;
    private final MetricsConfigManager metricsConfigManager;
    private final SqlManager sqlManager;
    private final ServiceProvider.GameScoped serviceProvider;

    private final AsyncScheduler asyncScheduler;
    private RegistryHolderLogic registryHolder;

    private Client client;
    private Server server;

    private ServerConsoleSystemSubject systemSubject;

    @Inject
    public SpongeGame(final Platform platform, final BuilderProvider builderProvider,
            final FactoryProvider factoryProvider, final DataManager dataManager, final PluginManager pluginManager,
            final EventManager eventManager, final PluginConfigManager configManager, final ChannelManager channelManager,
            final MetricsConfigManager metricsConfigManager, final SqlManager sqlManager, final ServiceProvider.GameScoped serviceProvider) {

        this.platform = platform;
        this.builderProvider = builderProvider;
        this.factoryProvider = factoryProvider;
        this.dataManager = dataManager;
        this.pluginManager = pluginManager;
        this.eventManager = eventManager;
        this.configManager = configManager;
        this.channelManager = channelManager;
        this.metricsConfigManager = metricsConfigManager;
        this.sqlManager = sqlManager;
        this.serviceProvider = serviceProvider;

        this.asyncScheduler = new AsyncScheduler();
    }

    @Override
    public Path gameDirectory() {
        return SpongeCommon.gameDirectory();
    }

    @Override
    public ServerConsoleSystemSubject systemSubject() {
        if (this.systemSubject == null) {
            this.systemSubject = new ServerConsoleSystemSubject();
        }
        return this.systemSubject;
    }

    @Override
    public Platform platform() {
        return this.platform;
    }

    @Override
    public BuilderProvider builderProvider() {
        return this.builderProvider;
    }

    @Override
    public FactoryProvider factoryProvider() {
        return this.factoryProvider;
    }

    @Override
    public DataManager dataManager() {
        return this.dataManager;
    }

    @Override
    public PluginManager pluginManager() {
        return this.pluginManager;
    }

    @Override
    public EventManager eventManager() {
        return this.eventManager;
    }

    @Override
    public PluginConfigManager configManager() {
        return this.configManager;
    }

    @Override
    public ChannelManager channelManager() {
        return this.channelManager;
    }

    @Override
    public MetricsConfigManager metricsConfigManager() {
        return this.metricsConfigManager;
    }

    @Override
    public SqlManager sqlManager() {
        return this.sqlManager;
    }

    @Override
    public ServiceProvider.GameScoped serviceProvider() {
        return this.serviceProvider;
    }

    @Override
    public AsyncScheduler asyncScheduler() {
        return this.asyncScheduler;
    }

    @Override
    public Locale locale(final String locale) {
        return LocaleCache.getLocale(Preconditions.checkNotNull(locale));
    }

    @Override
    public boolean isServerAvailable() {
        if (this.client != null) {
            return this.client.server().isPresent();
        }

        return this.server != null;
    }

    @Override
    public Server server() {
        if (this.client != null) {
            return this.client.server().orElseThrow(() -> new IllegalStateException("The singleplayer server is not available!"));
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
    public Client client() {
        Preconditions.checkState(this.client != null, "The client is not available!");
        return this.client;
    }

    public void setClient(final Client client) {
        this.client = client;
    }

    @Override
    public <T> Registry<T> registry(final RegistryType<T> type) {
        return this.registryHolder().registry(Objects.requireNonNull(type, "type"));
    }

    @Override
    public <T> Optional<Registry<T>> findRegistry(final RegistryType<T> type) {
        return this.registryHolder().findRegistry(Objects.requireNonNull(type, "type"));
    }

    @Override
    public Stream<Registry<?>> streamRegistries(final ResourceKey root) {
        return this.registryHolder().streamRegistries(Objects.requireNonNull(root, "root"));
    }

    private RegistryHolderLogic registryHolder() {
        if (this.registryHolder == null) {
            this.registryHolder = new RegistryHolderLogic();
        }

        return this.registryHolder;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("platform", this.platform)
                .toString();
    }

    @Override
    public void setRootMinecraftRegistry(final net.minecraft.core.Registry<net.minecraft.core.Registry<?>> registry) {
        this.registryHolder().setRootMinecraftRegistry(registry);
    }

    @Override
    public <T> Registry<T> createRegistry(final RegistryType<T> type, @Nullable final InitialRegistryData<T> defaultValues, final boolean isDynamic,
        @Nullable final BiConsumer<net.minecraft.resources.ResourceKey<T>, T> callback) {
        return this.registryHolder().createRegistry(type, defaultValues, isDynamic, callback);
    }

    @Override
    public <T> void wrapTagHelperAsRegistry(final RegistryType<Tag<T>> type, final StaticTagHelper<T> helper) {
        this.registryHolder().wrapTagHelperAsRegistry(type, helper);
    }
}
