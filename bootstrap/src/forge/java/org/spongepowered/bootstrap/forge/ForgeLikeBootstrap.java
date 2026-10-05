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
package org.spongepowered.bootstrap.forge;

import cpw.mods.jarhandling.SecureJar;
import net.minecraftforge.securemodules.SecureModuleClassLoader;
import net.minecraftforge.securemodules.SecureModuleFinder;
import org.spongepowered.bootstrap.Bootstrap;
import org.spongepowered.bootstrap.dev.DevClasspath;
import org.spongepowered.bootstrap.forge.classloader.FilteringPassthroughClassLoader;
import org.spongepowered.bootstrap.forge.classloader.ResourceClassLoader;

import java.io.File;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ForgeLikeBootstrap {
    private final String[] args;

    public ForgeLikeBootstrap(final String[] args) {
        this.args = args;
    }

    public abstract String name();

    /**
     * When filtered, the jar content is still accessible via the application classloader but is not loaded as a module.
     */
    protected boolean filterApplicationModule(final SecureJar jar) {
        return false;
    }

    protected void runApplication(final ModuleLayer layer) throws Exception {
        final Class<?> appClass = layer.findLoader("cpw.mods.modlauncher").loadClass("cpw.mods.modlauncher.Launcher");
        appClass.getDeclaredMethod("main", String[].class).invoke(null, (Object) this.args);
    }

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
        final List<SecureJar> resourceJars = new ArrayList<>();
        final List<SecureJar> appJars = new ArrayList<>();

        for (final Path[] paths : classpath) {
            final SecureJar jar = SecureJar.from(paths);
            final String name = jar.name();

            // Ignore modules already present
            if (ModuleLayer.boot().findModule(name).isPresent()) {
                if (Bootstrap.DEBUG) {
                    System.out.println("Boot: " + name + " " + ForgeLikeBootstrap.formatUnion(paths));
                }
                continue;
            }

            if (this.filterApplicationModule(jar)) {
                if (Bootstrap.DEBUG) {
                    System.out.println("Filtered: " + name + " " + ForgeLikeBootstrap.formatUnion(paths));
                }
                resourceJars.add(jar);
                resourcePaths.addAll(Arrays.asList(paths));
                continue;
            }

            if (!moduleNames.add(name)) {
                if (Bootstrap.DEBUG) {
                    System.out.println("Duplicate: " + name + " " + ForgeLikeBootstrap.formatUnion(paths));
                }
                resourceJars.add(jar);
                resourcePaths.addAll(Arrays.asList(paths));
                continue;
            }

            if (Bootstrap.DEBUG) {
                System.out.println("App: " + name + " " + ForgeLikeBootstrap.formatUnion(paths));
            }
            appJars.add(jar);
        }

        // Create the layer configuration
        final ModuleFinder finder = SecureModuleFinder.of(appJars);
        final Configuration config = ModuleLayer.boot().configuration().resolveAndBind(finder, ModuleFinder.ofSystem(), moduleNames);
        final List<ModuleLayer> parentLayers = List.of(ModuleLayer.boot());

        final ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();

        // Isolation
        ClassLoader parentLoader;
        if (isolated) {
            parentLoader = ClassLoader.getPlatformClassLoader();
        } else {
            // Don't leak classes that we manage already
            final List<ModuleReference> modules = new ArrayList<>();
            modules.addAll(finder.findAll());
            modules.addAll(SecureModuleFinder.of(resourceJars).findAll());

            final String resources = System.getProperty("sponge.resources");
            if (resources != null) {
                final List<SecureJar> spongeJars = new ArrayList<>();
                for (final String entry : resources.split(File.pathSeparator)) {
                    final Path[] paths = Stream.of(entry.split("&")).map(Path::of).toArray(Path[]::new);
                    spongeJars.add(SecureJar.from(paths));
                }
                modules.addAll(SecureModuleFinder.of(spongeJars).findAll());
            }

            parentLoader = new FilteringPassthroughClassLoader(contextLoader, modules);
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
        final ClassLoader loader = new SecureModuleClassLoader("APP-BOOTSTRAP", null, config, parentLayers, List.of(parentLoader));
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
