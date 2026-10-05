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
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.common.launch.plugin.SpongePluginContainer;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.metadata.model.PluginDependency;

/**
 * A module installed for each plugin.
 * Contains the values that are publicly visible and shared
 * across dependencies.
 */
public final class PluginDependencyModule extends AbstractModule {

    private final PluginContainer container;

    PluginDependencyModule(final PluginContainer container) {
        this.container = container;
    }

    @Override
    protected void configure() {
        final BindingHelper bindingHelper = new BindingHelper(this.binder().withSource(BindingHelper.class));
        final PluginManager manager = Sponge.pluginManager();

        for (final PluginDependency dependency : this.container.metadata().dependencies()) {
            if (dependency.loadOrder() != PluginDependency.LoadOrder.AFTER) {
                continue;
            }

            if (manager.plugin(dependency.id()).orElse(null) instanceof SpongePluginContainer spongeContainer) {
                spongeContainer.injector().ifPresent(bindingHelper::bindFrom);
            }
        }

        // Indirect dependencies
        for (final PluginContainer other : manager.plugins()) {
            if (other.metadata().dependency(this.container.metadata().id()).map(PluginDependency::loadOrder).orElse(PluginDependency.LoadOrder.UNDEFINED) != PluginDependency.LoadOrder.BEFORE) {
                continue;
            }

            if (other instanceof SpongePluginContainer spongeContainer) {
                spongeContainer.injector().ifPresent(bindingHelper::bindFrom);
            }
        }

        bindingHelper.bind();
    }
}
