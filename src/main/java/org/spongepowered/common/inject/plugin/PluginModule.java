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
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import org.slf4j.Logger;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.AsynchronousExecutor;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.SynchronousExecutor;

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

        this.bind(PluginContainer.class).toInstance(this.container);
        this.bind(Logger.class).toInstance(this.container.getLogger());

        this.bind(SpongeExecutorService.class).annotatedWith(SynchronousExecutor.class).toProvider(new SpongeExecutorServiceProvider() {
            @Override
            public SpongeExecutorService get() {
                return this.scheduler.createSyncExecutor(PluginModule.this.container);
            }
        });
        this.bind(SpongeExecutorService.class).annotatedWith(AsynchronousExecutor.class).toProvider(new SpongeExecutorServiceProvider() {
            @Override
            public SpongeExecutorService get() {
                return this.scheduler.createAsyncExecutor(PluginModule.this.container);
            }
        });

        this.install(new PluginConfigurationModule(this.container));
    }

    private static abstract class SpongeExecutorServiceProvider implements Provider<SpongeExecutorService> {

        protected Scheduler scheduler;

        @Inject
        void init(Scheduler scheduler) {
            this.scheduler = scheduler;
        }
    }
}
