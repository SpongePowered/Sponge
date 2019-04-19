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
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import com.google.inject.spi.Element;
import com.google.inject.spi.ElementVisitor;
import com.google.inject.spi.Elements;
import org.spongepowered.api.plugin.PluginAdapter;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.inject.InjectionPointProvider;
import org.spongepowered.common.inject.plugin.PluginModule;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public final class PluginConstructor {

    private static Map<Class<?>, PluginAdapter> adapterCache = new ConcurrentHashMap<>();

    public static <P> Tuple<P, Injector> constructPlugin(Injector parentInjector, PluginContainer pluginContainer, Class<P> pluginClass,
            Class<? extends PluginAdapter> adapterClass, List<Class<? extends Module>> pluginModuleClasses) {
        final Stage stage = parentInjector.getInstance(Stage.class);

        final PluginAdapter adapter = adapterCache.computeIfAbsent(adapterClass,
                clazz -> adapterClass == PluginAdapter.Default.class ? DefaultPluginAdapter.INSTANCE : parentInjector.getInstance(adapterClass));

        // Copy the parent injector bindings to allow complete
        // control with injectors. If there's a parent injector,
        // implicit bindings may be delegated to it, skipping
        // registered TypeListeners, etc. in the child injector
        final AbstractModule globalModule = new AbstractModule() {
            @Override
            protected void configure() {
                for (Map.Entry<Key<?>, Binding<?>> entry : parentInjector.getBindings().entrySet()) {
                    final Key<?> key = entry.getKey();
                    // Some default bindings we need to skip
                    if (key.equals(Key.get(Stage.class)) ||
                            key.equals(Key.get(Injector.class)) ||
                            key.equals(Key.get(java.util.logging.Logger.class))) {
                        continue;
                    }
                    bind((Key) entry.getKey()).toProvider(entry.getValue().getProvider());
                }
                install(new InjectionPointProvider());
            }
        };

        Module module = adapter.createGlobalModule(globalModule);
        final Module pluginModule = new PluginModule(pluginContainer, pluginModuleClasses);

        List<Element> elements = Elements.getElements(module, pluginModule);
        final Injector pluginModuleInjector = Guice.createInjector(stage, Elements.getModule(elements));

        final List<Module> pluginModules = new ArrayList<>();
        for (Class<?> moduleClass : pluginModuleClasses) {
            pluginModules.add((Module) pluginModuleInjector.getInstance(moduleClass));
        }

        elements = new ArrayList<>(elements);
        final ListIterator<Element> it = elements.listIterator();
        while (it.hasNext()) {
            final Element element = it.next();
            if (element instanceof Binding) {
                final Binding<?> binding = (Binding<?>) element;
                // Forward all the singleton bindings to the plugin module
                // injector, this allows explicit defined bindings to be shared
                if (Scopes.isSingleton(binding)) {
                    it.set(new Element() {
                        @Override
                        public Object getSource() {
                            return element.getSource();
                        }

                        @Override
                        public <T> T acceptVisitor(ElementVisitor<T> visitor) {
                            return (T) this;
                        }

                        @Override
                        public void applyTo(Binder binder) {
                            binder.bind((Key) binding.getKey()).toProvider(pluginModuleInjector.getProvider(binding.getKey()));
                        }
                    });
                }
            }
        }

        final Module baseModule = Elements.getModule(elements);
        module = new AbstractModule() {
            @Override
            protected void configure() {
                install(baseModule);

                // Install all plugin modules
                pluginModules.forEach(this::install);
            }
        };

        module = adapter.createPluginModule(pluginContainer, pluginClass, module);
        final Injector injector = Guice.createInjector(stage, module);

        final Binding<?> binding = injector.getBinding(pluginClass);
        if (!Scopes.isSingleton(binding)) {
            throw new IllegalStateException("The plugin instance (" + pluginClass.getName() + ") must be bound in the singleton scope.");
        }

        final P pluginInstance = injector.getInstance(pluginClass);
        return new Tuple<>(pluginInstance, injector);
    }
}
