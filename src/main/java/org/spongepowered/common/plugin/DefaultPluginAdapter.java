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
package org.spongepowered.common.plugin;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import org.spongepowered.api.plugin.PluginAdapter;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.ArrayList;
import java.util.List;

public class DefaultPluginAdapter implements PluginAdapter.Default {

    public static DefaultPluginAdapter INSTANCE = new DefaultPluginAdapter();

    @Override
    public <T> Injector createInjector(PluginContainer pluginContainer, Class<T> pluginClass, Injector defaultInjector, List<Module> pluginModules) {
        final Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(pluginClass).in(Scopes.SINGLETON);
            }
        };
        final List<Module> modules = new ArrayList<>(pluginModules);
        modules.add(0, module);
        return defaultInjector.createChildInjector(modules);
    }
}
