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
package org.spongepowered.common.applaunch.plugin;

import org.spongepowered.plugin.Environment;
import org.spongepowered.plugin.PluginService;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Predicate;

public abstract class PluginServiceLoader {
    protected final Environment environment;

    protected PluginServiceLoader(final Environment environment) {
        this.environment = environment;
    }

    protected final <T extends PluginService> List<T> loadServices(final String serviceDescription, final Class<T> serviceClass) {
        return this.loadServices(serviceDescription, serviceClass, c -> true);
    }

    protected final <T extends PluginService> List<T> loadServices(final String serviceDescription, final Class<T> serviceClass, final Predicate<Class<? extends T>> filter) {
        final List<T> services = new ArrayList<>();
        this.newServiceLoader(serviceClass).stream()
            .filter(provider -> filter.test(provider.type()))
            .forEach(provider -> {
                final T service;
                try {
                    service = provider.get();
                } catch (final ServiceConfigurationError e) {
                    this.environment.logger().error("Failed to initialize {}: {}", serviceDescription, provider.type().getName(), e);
                    return;
                }

                this.environment.logger().info("Found {} '{}'.", serviceDescription, service.name());
                services.add(service);
            });
        return services;
    }

    protected abstract <T> ServiceLoader<T> newServiceLoader(final Class<T> serviceClass);
}
