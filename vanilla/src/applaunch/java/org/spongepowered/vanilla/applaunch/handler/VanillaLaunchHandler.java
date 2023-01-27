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
package org.spongepowered.vanilla.applaunch.handler;

import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformingClassLoaderBuilder;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.plugin.PluginResource;
import org.spongepowered.plugin.builtin.jvm.locator.JVMPluginResource;
import org.spongepowered.plugin.builtin.jvm.locator.ResourceType;
import org.spongepowered.vanilla.applaunch.plugin.VanillaPluginPlatform;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.jar.Manifest;

/**
 * The common Sponge {@link ILaunchHandlerService launch handler} for development
 * and production environments.
 */
public abstract class VanillaLaunchHandler extends VanillaBaseLaunchHandler {

    @Override
    public void configureTransformationClassLoader(final ITransformingClassLoaderBuilder builder) {
        // Specifically requested to be available on the launch loader
        final VanillaPluginPlatform platform = AppLaunch.pluginPlatform();
        for (final Path path : platform.getStandardEnvironment().blackboard().getOrCreate(VanillaPluginPlatform.EXTRA_TRANSFORMABLE_PATHS, Collections::emptyList)) {
            builder.addTransformationPath(path);
        }

        super.configureTransformationClassLoader(builder);

        builder.setResourceEnumeratorLocator(this.getResourceLocator());
        builder.setManifestLocator(this.getManifestLocator());
    }

    protected Function<String, Enumeration<URL>> getResourceLocator() {
        return s -> {
            // Save unnecessary searches of plugin classes for things that are definitely not plugins
            // In this case: MC and fastutil
            if (s.startsWith("net/minecraft") || s.startsWith("it/unimi")) {
                return Collections.emptyEnumeration();
            }

            final URI asUri;
            try {
                asUri = new URI(null, null, s, null);
            } catch (final URISyntaxException ex) {
                this.logger.error("Failed to convert resource path {} to a URI", s, ex);
                return Collections.emptyEnumeration();
            }

            return new Enumeration<URL>() {
                final Iterator<Set<PluginResource>> serviceResources = ((VanillaPluginPlatform) AppLaunch.pluginPlatform()).getResources()
                    .values().iterator();
                Iterator<PluginResource> resources;
                URL next = this.computeNext();

                @Override
                public boolean hasMoreElements() {
                    return this.next != null;
                }

                @Override
                public URL nextElement() {
                    final URL next = this.next;
                    if (next == null) {
                        throw new NoSuchElementException();
                    }
                    this.next = this.computeNext();
                    return next;
                }

                private URL computeNext() {
                    while (true) {
                        if (this.resources != null && !this.resources.hasNext()) {
                            this.resources = null;
                        }
                        if (this.resources == null) {
                            if (!this.serviceResources.hasNext()) {
                                return null;
                            }
                            this.resources = this.serviceResources.next().iterator();
                        }

                        if (this.resources.hasNext()) {
                            final PluginResource resource = this.resources.next();
                            if (resource instanceof JVMPluginResource) {
                                if (((JVMPluginResource) resource).type() != ResourceType.JAR) {
                                    continue;
                                }
                            }

                            final Optional<URI> uri = resource.locateResource(asUri);
                            if (uri.isPresent()) {
                                try {
                                    return uri.get().toURL();
                                } catch (final MalformedURLException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        }
                    }
                }
            };
        };
    }

    private final ConcurrentMap<URL, Optional<Manifest>> manifestCache = new ConcurrentHashMap<>();
    private static final Optional<Manifest> UNKNOWN_MANIFEST = Optional.of(new Manifest());

    private Function<URLConnection, Optional<Manifest>> getManifestLocator() {
        return connection -> {
            if (connection instanceof JarURLConnection) {
                final URL jarFileUrl = ((JarURLConnection) connection).getJarFileURL();
                final Optional<Manifest> manifest =  this.manifestCache.computeIfAbsent(jarFileUrl, key -> {
                    for (final Set<PluginResource> resources : ((VanillaPluginPlatform) AppLaunch.pluginPlatform()).getResources().values()) {
                        for (final PluginResource resource : resources) {
                            if (resource instanceof JVMPluginResource) {
                                final JVMPluginResource jvmResource = (JVMPluginResource) resource;
                                try {
                                    if (jvmResource.type() == ResourceType.JAR && ((JVMPluginResource) resource).path().toAbsolutePath().normalize().equals(Paths.get(key.toURI()).toAbsolutePath().normalize())) {
                                        return jvmResource.manifest();
                                    }
                                } catch (final URISyntaxException ex) {
                                    this.logger.error("Failed to load manifest from jar {}: ", key, ex);
                                }
                            }
                        }
                    }
                    return VanillaLaunchHandler.UNKNOWN_MANIFEST;
                });

                try {
                    if (manifest == VanillaLaunchHandler.UNKNOWN_MANIFEST) {
                        return Optional.ofNullable(((JarURLConnection) connection).getManifest());
                    } else {
                        return manifest;
                    }
                } catch (final IOException ex) {
                    this.logger.error("Failed to load manifest from jar {}: ", jarFileUrl, ex);
                }
            }
            return Optional.empty();
        };
    }
}
