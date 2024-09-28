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

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.spi.Element;
import com.google.inject.spi.ElementSource;
import com.google.inject.spi.ExposedBinding;
import com.google.inject.spi.PrivateElements;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.inject.InjectionPointProvider;
import org.spongepowered.common.inject.SpongePluginInjectorProvider;
import org.spongepowered.common.inject.provider.PluginConfigurationModule;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.metadata.model.PluginDependency;

import java.util.ArrayList;

/**
 * A module installed for each plugin.
 */
public final class PluginModule extends PrivateModule {

    private final PluginContainer container;
    private final Class<?> pluginClass;

    private PluginModule(final PluginContainer container, final Class<?> pluginClass) {
        this.container = container;
        this.pluginClass = pluginClass;
    }

    @Override
    protected void configure() {
        this.bind(this.pluginClass).in(Scopes.SINGLETON);
        this.expose(this.pluginClass);

        this.install(new InjectionPointProvider());

        this.bind(PluginContainer.class).toInstance(this.container);
        this.bind(Logger.class).toInstance(this.container.logger());
        this.bind(System.Logger.class).toProvider(() -> System.getLogger(this.container.logger().getName())).in(Scopes.SINGLETON);

        this.install(new PluginConfigurationModule());

        for (final PluginDependency dependency : this.container.metadata().dependencies()) {
            if (dependency.loadOrder() != PluginDependency.LoadOrder.AFTER) {
                continue;
            }

            Sponge.pluginManager().plugin(dependency.id()).ifPresent(dependencyContainer -> {
                if (!(dependencyContainer instanceof final SpongePluginInjectorProvider injectorProvider)) {
                    return;
                }

                for (final Binding binding : injectorProvider.injector().getBindings().values()) {
                    if (!(binding.getSource() instanceof ElementSource)) {
                        continue;
                    }

                    if (binding instanceof final ExposedBinding<?> exposedBinding) {
                        final PrivateElements privateElements = exposedBinding.getPrivateElements();
                        for (final Element privateElement : privateElements.getElements()) {
                            if (!(privateElement instanceof final Binding privateBinding) || !privateElements.getExposedKeys().contains(privateBinding.getKey())) {
                                continue;
                            }

                            this.bind(privateBinding.getKey()).toProvider(() -> injectorProvider.injector().getInstance(privateBinding.getKey()));
                        }

                        continue;
                    }

                    this.bind(binding.getKey()).toProvider(() -> injectorProvider.injector().getInstance(binding.getKey()));
                }
            });
        }
    }

    public static Injector create(final PluginContainer container, final Class<?> pluginClass, final @Nullable Injector platformInjector) {
        final ArrayList<Module> modules = new ArrayList<>(2);
        modules.add(new PluginModule(container, pluginClass));

        final @Nullable Object customModule = container.metadata().property("guice-module").orElse(null);
        if (customModule != null) {
            try {
                final Class<?> moduleClass = Class.forName(customModule.toString(), true, pluginClass.getClassLoader());
                final com.google.inject.Module moduleInstance = (com.google.inject.Module) moduleClass.getConstructor().newInstance();
                modules.add(moduleInstance);
            } catch (final Exception ex) {
                throw new RuntimeException("Failed to instantiate the custom module!", ex);
            }
        }

        if (platformInjector != null) {
            return platformInjector.createChildInjector(modules);
        } else {
            return Guice.createInjector(modules);
        }
    }
}
