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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.plugin.PluginContainer;

public final class PluginGuice {

    public static Injector create(final PluginContainer container, final Class<?> pluginClass, final @Nullable Injector platformInjector) {
        Module module = Modules.combine(new PrivatePluginModule(container, pluginClass), new PublicPluginModule(container));

        final @Nullable Object customModule = container.metadata().property("guice-module").orElse(null);
        if (customModule != null) {
            try {
                final Class<?> moduleClass = Class.forName(customModule.toString(), true, pluginClass.getClassLoader());
                final Module moduleInstance = (Module) moduleClass.getConstructor().newInstance();
                module = Modules.override(module).with(moduleInstance);
            } catch (final Exception ex) {
                throw new RuntimeException("Failed to instantiate the custom module!", ex);
            }
        }

        if (platformInjector != null) {
            return platformInjector.createChildInjector(module);
        } else {
            return Guice.createInjector(module);
        }
    }
}
