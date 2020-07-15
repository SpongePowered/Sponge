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
package org.spongepowered.forge.launch.plugin;

import com.google.inject.Injector;
import org.spongepowered.common.inject.plugin.PluginModule;
import org.spongepowered.plugin.InvalidPluginException;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.PluginEnvironment;
import org.spongepowered.plugin.PluginKeys;
import org.spongepowered.plugin.jvm.JVMPluginContainer;
import org.spongepowered.plugin.jvm.JVMPluginLanguageService;

import java.util.Optional;

public class JavaPluginLanguageService extends JVMPluginLanguageService<JVMPluginContainer> {

    private final static String NAME = "java_plain";

    @Override
    public String getName() {
        return JavaPluginLanguageService.NAME;
    }

    @Override
    public Optional<JVMPluginContainer> createPluginContainer(final PluginCandidate candidate, final PluginEnvironment environment) {
        return Optional.of(new JVMPluginContainer(candidate));
    }

    @Override
    protected Object createPluginInstance(final PluginEnvironment environment, final JVMPluginContainer container, final ClassLoader targetClassLoader) throws InvalidPluginException {
        try {
            final String mainClass = container.getMetadata().getMainClass();
            final Class<?> pluginClass = Class.forName(mainClass, true, targetClassLoader);
            final Injector parentInjector = environment.getBlackboard().get(PluginKeys.PARENT_INJECTOR).orElse(null);
            if (parentInjector != null) {
                final Injector childInjector = parentInjector.createChildInjector(new PluginModule(container, pluginClass));
                return childInjector.getInstance(pluginClass);
            }
            return pluginClass.newInstance();
        }
        catch (final Exception ex) {
            throw new InvalidPluginException("An error occurred creating an instance of plugin '" + container.getMetadata().getId() + "'!", ex);
        }
    }

}
