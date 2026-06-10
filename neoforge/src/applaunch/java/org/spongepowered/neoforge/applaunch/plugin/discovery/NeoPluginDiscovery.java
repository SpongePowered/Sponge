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
package org.spongepowered.neoforge.applaunch.plugin.discovery;

import net.neoforged.fml.jarcontents.CompositeJarContents;
import net.neoforged.fml.jarcontents.JarContents;
import org.spongepowered.common.applaunch.plugin.discovery.PluginDiscovery;
import org.spongepowered.common.applaunch.plugin.discovery.SpongeJVMPluginResource;
import org.spongepowered.plugin.Environment;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class NeoPluginDiscovery extends PluginDiscovery {
    private ClassLoader currentLoader;

    public NeoPluginDiscovery(Environment environment) {
        super(environment);
        this.currentLoader = getClass().getClassLoader();
    }

    @Override
    protected <T> ServiceLoader<T> newServiceLoader(final Class<T> serviceClass) {
        return ServiceLoader.load(serviceClass, this.currentLoader);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected void appendDiscoveryServices(final List<SpongeJVMPluginResource> resources, final int batch) throws Exception {
        final ClassLoader parentLoader = this.currentLoader;

        // See FMLLoader#appendLoader
        final List<URL> rootUrls = new ArrayList<>(resources.size());
        for (final SpongeJVMPluginResource resource : resources) {
            final JarContents jar = ((JarContentsPluginResource) resource).jar();
            if (jar instanceof final CompositeJarContents compositeJar && compositeJar.isFiltered()) {
                throw new IllegalArgumentException("Cannot use simple URLClassLoader for filtered content " + jar);
            }
            for (final Path contentRoot : jar.getContentRoots()) {
                rootUrls.add(contentRoot.toUri().toURL());
            }
        }

        final ClassLoader loader = new URLClassLoader("SPONGE-DISCOVERY-BATCH-" + batch, rootUrls.toArray(URL[]::new), parentLoader);
        this.currentLoader = loader;
        this.environment.logger().debug("Built new service layer {} on top of {}.", loader.getName(), parentLoader.getName());
    }
}
