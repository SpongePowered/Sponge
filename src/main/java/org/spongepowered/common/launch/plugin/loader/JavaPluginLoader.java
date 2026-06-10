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
package org.spongepowered.common.launch.plugin.loader;

import com.google.inject.Injector;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.inject.plugin.PluginGuice;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.launch.plugin.SpongePluginContainer;
import org.spongepowered.plugin.*;
import org.spongepowered.plugin.discovery.PluginResource;
import org.spongepowered.plugin.metadata.PluginMetadata;

public final class JavaPluginLoader implements PluginLoader {
    private static final ArtifactVersion version = new DefaultArtifactVersion("1.0");

    @Override
    public ArtifactVersion version() {
        return JavaPluginLoader.version;
    }

    @Override
    public String name() {
        return "java_plain";
    }

    @Override
    public PluginContainer loadPlugin(Environment environment, PluginResource resource, PluginMetadata metadata) throws Exception {
        final SpongePluginContainer container = new SpongePluginContainer(resource, metadata);
        final String mainClass = container.metadata().entrypoint();
        final Class<?> pluginClass = Class.forName(mainClass);

        final Injector pluginInjector = PluginGuice.create(container, pluginClass, Launch.instance().lifecycle().platformInjector());
        final Object plugin = pluginInjector.getInstance(pluginClass);
        container.setInjector(pluginInjector);
        container.initializeInstance(plugin);

        Sponge.eventManager().registerListeners(container, plugin);
        return container;
    }
}
