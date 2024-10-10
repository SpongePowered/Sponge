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
import org.spongepowered.api.Sponge;
import org.spongepowered.common.launch.plugin.SpongePluginContainer;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.metadata.model.PluginDependency;

/**
 * A module installed for each plugin.
 * Contains the values that are publicly visible and shared
 * across dependencies.
 */
public final class PublicPluginModule extends AbstractModule {

    private final PluginContainer container;

    PublicPluginModule(final PluginContainer container) {
        this.container = container;
    }

    @Override
    protected void configure() {
        this.requestStaticInjection(PreserveHelper.class);

        final BindingHelper bindingHelper = new BindingHelper(this.binder().withSource(BindingHelper.class));
        for (final PluginDependency dependency : this.container.metadata().dependencies()) {
            if (dependency.loadOrder() != PluginDependency.LoadOrder.AFTER) {
                continue;
            }

            Sponge.pluginManager().plugin(dependency.id())
                .flatMap(p -> ((SpongePluginContainer) p).injector())
                .ifPresent(bindingHelper::bindFrom);
        }

        // Indirect dependencies
        Sponge.pluginManager()
            .plugins()
            .stream()
            .filter(p -> p.metadata().dependency(this.container.metadata().id())
                .map(PluginDependency::loadOrder)
                .orElse(PluginDependency.LoadOrder.UNDEFINED) == PluginDependency.LoadOrder.BEFORE)
            .flatMap(p -> ((SpongePluginContainer) p).injector().stream())
            .forEach(bindingHelper::bindFrom);

        bindingHelper.bind();
    }

    /**
     * If no public module has any bindings, Guice will silently promote
     * the private module as the "main" one which leads to everything
     * being marked as private, including the plugin provided custom module.
     */
    private static final class PreserveHelper {
    }
}
