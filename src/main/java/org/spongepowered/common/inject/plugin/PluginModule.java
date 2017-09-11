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
package org.spongepowered.common.inject.plugin;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.slf4j.Logger;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.asset.AssetId;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.AsynchronousExecutor;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.SynchronousExecutor;
import org.spongepowered.common.inject.InjectionPointProvider;
import org.spongepowered.common.inject.provider.ChannelBindingProvider;
import org.spongepowered.common.inject.provider.PluginAssetProvider;
import org.spongepowered.common.inject.provider.SpongeExecutorServiceProvider;
import org.spongepowered.common.inject.provider.config.PluginConfigurationModule;

/**
 * A module installed for each plugin.
 */
public class PluginModule extends AbstractModule {

    private final PluginContainer container;
    private final Class<?> pluginClass;

    public PluginModule(final PluginContainer container, final Class<?> pluginClass) {
        this.container = container;
        this.pluginClass = pluginClass;
    }

    @Override
    protected void configure() {
        this.bind(this.pluginClass).in(Scopes.SINGLETON);

        this.install(new InjectionPointProvider());

        this.bind(PluginContainer.class).toInstance(this.container);
        this.bind(Logger.class).toInstance(this.container.getLogger());

        this.bind(SpongeExecutorService.class).annotatedWith(SynchronousExecutor.class).toProvider(SpongeExecutorServiceProvider.Synchronous.class);
        this.bind(SpongeExecutorService.class).annotatedWith(AsynchronousExecutor.class).toProvider(SpongeExecutorServiceProvider.Asynchronous.class);
        this.bind(ChannelBinding.IndexedMessageChannel.class).annotatedWith(ChannelId.class).toProvider(ChannelBindingProvider.Indexed.class);
        this.bind(ChannelBinding.RawDataChannel.class).annotatedWith(ChannelId.class).toProvider(ChannelBindingProvider.Raw.class);
        this.bind(Asset.class).annotatedWith(AssetId.class).toProvider(PluginAssetProvider.class);

        this.install(new PluginConfigurationModule());
    }
}
