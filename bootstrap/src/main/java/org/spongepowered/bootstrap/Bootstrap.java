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
package org.spongepowered.bootstrap;

import org.spongepowered.bootstrap.dev.DevClasspath;

import java.io.File;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Bootstrap<Jar> {
    public static final boolean DEBUG = Boolean.getBoolean("sponge.bootstrap.debug");

    public abstract String name();

    protected abstract Jar createJar(Path[] paths);

    protected abstract String getModuleName(Jar jar);

    protected abstract ModuleFinder createModuleFinder(Collection<Jar> jars);

    /**
     * When filtered, the jar content is still accessible via the application classloader but is not loaded as a module.
     */
    protected boolean filterApplicationModule(final Jar jar) {
        return false;
    }

    protected abstract ClassLoader createApplicationClassLoader(Configuration config, List<ModuleLayer> parentLayers, ClassLoader parentLoader);

    protected abstract void runApplication(ModuleLayer layer) throws Exception;

    public void devBoot(final boolean isolated) throws Exception {
        this.boot(DevClasspath.resolve(), isolated);
    }

    public void boot(final List<Path[]> classpath, final boolean isolated) throws Exception {
        if (Bootstrap.DEBUG) {
            System.out.println("Bootstrapping " + this.name() + " from classloader: " + getClass().getClassLoader());
            System.out.println("Isolated: " + isolated);
        }

        // Collect the jars
        final Set<String> moduleNames = new HashSet<>();
        final List<Path> resourcePaths = new ArrayList<>();
        final List<Jar> resourceJars = new ArrayList<>();
        final List<Jar> appJars = new ArrayList<>();

        for (final Path[] paths : classpath) {
            final Jar jar = this.createJar(paths);
            final String name = this.getModuleName(jar);

            // Ignore modules already present
            if (ModuleLayer.boot().findModule(name).isPresent()) {
                if (Bootstrap.DEBUG) {
                    System.out.println("Boot: " + name + " " + Bootstrap.formatUnion(paths));
                }
                continue;
            }

            if (this.filterApplicationModule(jar)) {
                if (Bootstrap.DEBUG) {
                    System.out.println("Filtered: " + name + " " + Bootstrap.formatUnion(paths));
                }
                resourceJars.add(jar);
                resourcePaths.addAll(Arrays.asList(paths));
                continue;
            }

            if (!moduleNames.add(name)) {
                if (Bootstrap.DEBUG) {
                    System.out.println("Duplicate: " + name + " " + Bootstrap.formatUnion(paths));
                }
                resourceJars.add(jar);
                resourcePaths.addAll(Arrays.asList(paths));
                continue;
            }

            if (Bootstrap.DEBUG) {
                System.out.println("App: " + name + " " + Bootstrap.formatUnion(paths));
            }
            appJars.add(jar);
        }

        // Create the layer configuration
        final ModuleFinder finder = this.createModuleFinder(appJars);
        final Configuration config = ModuleLayer.boot().configuration().resolveAndBind(finder, ModuleFinder.ofSystem(), moduleNames);
        final List<ModuleLayer> parentLayers = List.of(ModuleLayer.boot());

        // Isolation
        final ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader parentLoader = ClassLoader.getPlatformClassLoader();
        if (!isolated) {
            // Make sure we don't leak classes that are supposed to be
            // in the game layer from the boostrap layer.
            final String resources = System.getProperty("sponge.resources");
            if (resources != null) {
                final List<Jar> spongeJars = new ArrayList<>();
                for (final String entry : resources.split(File.pathSeparator)) {
                    final Path[] paths = Stream.of(entry.split("&")).map(Path::of).toArray(Path[]::new);
                    spongeJars.add(this.createJar(paths));
                }
                final ModuleFinder spongeFinder = this.createModuleFinder(spongeJars);
                final ModuleFinder resourceFinder = this.createModuleFinder(resourceJars);
                parentLoader = new FilteringPassthroughClassLoader(contextLoader,
                    Stream.concat(spongeFinder.findAll().stream(), resourceFinder.findAll().stream()));
            }
        }

        // Intermediate classloader to include resources but not modules
        if (!resourcePaths.isEmpty()) {
            final URL[] urls = new URL[resourcePaths.size()];
            for (int i = 0; i < urls.length; i++) {
                urls[i] = resourcePaths.get(i).toUri().toURL();
            }
            parentLoader = new ResourceClassLoader("BOOTSTRAP-RESOURCES", urls, parentLoader);
        }

        // Create the application classloader
        final ClassLoader loader = this.createApplicationClassLoader(config, parentLayers, parentLoader);
        final ModuleLayer layer = ModuleLayer.boot().defineModules(config, moduleName -> loader);

        // Run the application
        try {
            Thread.currentThread().setContextClassLoader(loader);
            if (Bootstrap.DEBUG) {
                System.out.println("Starting application ...");
            }
            this.runApplication(layer);
        } finally {
            Thread.currentThread().setContextClassLoader(contextLoader);
        }
    }

    public static String formatUnion(final Path[] paths) {
        return Arrays.stream(paths).map(Path::toString).collect(Collectors.joining("&"));
    }
}
