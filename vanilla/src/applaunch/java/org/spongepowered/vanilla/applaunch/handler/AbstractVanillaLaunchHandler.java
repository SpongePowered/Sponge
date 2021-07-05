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

import cpw.mods.gross.Java9ClassLoaderUtil;
import cpw.mods.modlauncher.TransformingClassLoader;
import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformingClassLoader;
import cpw.mods.modlauncher.api.ITransformingClassLoaderBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.plugin.PluginResource;
import org.spongepowered.plugin.jvm.locator.JVMPluginResource;
import org.spongepowered.plugin.jvm.locator.ResourceType;
import org.spongepowered.vanilla.applaunch.plugin.VanillaPluginPlatform;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * The common Sponge {@link ILaunchHandlerService launch handler} for development
 * and production environments.
 */
public abstract class AbstractVanillaLaunchHandler implements ILaunchHandlerService {

    private static final String JAVA_HOME_PATH = System.getProperty("java.home");
    protected final Logger logger = LogManager.getLogger("launch");

    /**
     * Classes or packages that mark jar files that should be excluded from the transformation path
     */
    protected static final String[] NON_TRANSFORMABLE_PATHS = {
        "org/spongepowered/asm/", // Mixin (for obvious reasons)
        // because NIO Paths use different normalization than Instrumentation.appendToSystemClassLoaderSearch()
        // (NIO uses uppercase URL encoding (ex. %2D), Instrumentation does not (ex. %2d)), this cannot appear in the transformer path at all
        // This suppresses a warning from LoggerFactory.findPossibleStaticLoggerBinderPathSet
        "org/slf4j/impl/", // slf4j
    };

    /**
     * A list of packages to exclude from the {@link TransformingClassLoader transforming class loader},
     * to be registered with {@link ITransformingClassLoader#addTargetPackageFilter(Predicate)}.
     * <p>
     * Packages should be scoped as tightly as possible - for example {@code "com.google.common."} is
     * preferred over {@code "com.google."}.
     * <p>
     * Packages should always include a trailing full stop - for example if {@code "org.neptune"} was
     * excluded, classes in {@code "org.neptunepowered"} would also be excluded. The correct usage would
     * be to exclude {@code "org.neptune."}.
     */
    private static final String[] EXCLUDED_PACKAGES = {
            "org.spongepowered.plugin.",
            "org.spongepowered.common.applaunch.",
            "org.spongepowered.vanilla.applaunch.",
            // configurate 4
            "io.leangen.geantyref.",
            "org.spongepowered.configurate.",
            // terminal console bits
            "org.jline.",
            "org.fusesource.",
            "net.minecrell.terminalconsole.",
            // Guice (for easier opening to reflection)
            "com.google.inject.",
            "org.slf4j."
    };

    @Override
    public void configureTransformationClassLoader(final ITransformingClassLoaderBuilder builder) {
        for (final URL url : Java9ClassLoaderUtil.getSystemClassPathURLs()) {
            try {
                final URI uri = url.toURI();
                if (!this.isTransformable(uri)) {
                    this.logger.debug("Non-transformable system classpath entry: {}", uri);
                    continue;
                }

                builder.addTransformationPath(Paths.get(uri));
                this.logger.debug("Transformable system classpath entry: {}", uri);
            } catch (final URISyntaxException | IOException ex) {
                this.logger.error("Failed to add {} to transformation path", url, ex);
            }
        }

        builder.setResourceEnumeratorLocator(this.getResourceLocator());
        builder.setManifestLocator(this.getManifestLocator());
    }

    protected boolean isTransformable(final URI uri) throws URISyntaxException, IOException {
        final File file = new File(uri);

        // in Java 8 ONLY, the system classpath contains JVM internals
        // let's make sure those don't get transformed
        if (file.getAbsolutePath().startsWith(AbstractVanillaLaunchHandler.JAVA_HOME_PATH)) {
            return false;
        }

        if (file.isDirectory()) {
            for (final String test : AbstractVanillaLaunchHandler.NON_TRANSFORMABLE_PATHS) {
                if (new File(file, test).exists()) {
                    return false;
                }
            }
        } else if (file.isFile()) {
            try (final JarFile jf = new JarFile(new File(uri))) {
                for (final String test : AbstractVanillaLaunchHandler.NON_TRANSFORMABLE_PATHS) {
                    if (jf.getEntry(test) != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public Callable<Void> launchService(final String[] arguments, final ITransformingClassLoader launchClassLoader) {
        this.logger.info("Transitioning to Sponge launch, please wait...");

        launchClassLoader.addTargetPackageFilter(klass -> {
            for (final String pkg : AbstractVanillaLaunchHandler.EXCLUDED_PACKAGES) {
                if (klass.startsWith(pkg)) {
                    return false;
                }
            }
            return true;
        });

        return () -> {
            this.launchService0(arguments, launchClassLoader);
            return null;
        };
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
                    return AbstractVanillaLaunchHandler.UNKNOWN_MANIFEST;
                });

                try {
                    if (manifest == AbstractVanillaLaunchHandler.UNKNOWN_MANIFEST) {
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

    /**
     * Launch the service (Minecraft).
     * <p>
     * <strong>Take care</strong> to <strong>ONLY</strong> load classes on the provided
     * {@link ClassLoader class loader}, which can be retrieved with {@link ITransformingClassLoader#getInstance()}.
     *
     * @param arguments The arguments to launch the service with
     * @param launchClassLoader The transforming class loader to load classes with
     * @throws Exception This can be any exception that occurs during the launch process
     */
    protected abstract void launchService0(final String[] arguments, final ITransformingClassLoader launchClassLoader) throws Exception;
}
