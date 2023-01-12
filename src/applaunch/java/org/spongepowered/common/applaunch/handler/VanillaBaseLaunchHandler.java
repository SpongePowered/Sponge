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
package org.spongepowered.common.applaunch.handler;

import cpw.mods.gross.Java9ClassLoaderUtil;
import cpw.mods.modlauncher.TransformingClassLoader;
import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformingClassLoader;
import cpw.mods.modlauncher.api.ITransformingClassLoaderBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.jar.JarFile;

/**
 * The common Sponge {@link ILaunchHandlerService launch handler} for development
 * and production environments.
 */
public abstract class VanillaBaseLaunchHandler implements ILaunchHandlerService {

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
            "org.slf4j.",
            // Maven artifacts -- specifically for versioning
            "org.apache.maven.artifact."
    };

    private static final String[] EXCLUSION_EXCEPTIONS = {
            "org.spongepowered.configurate.objectmapping.guice.",
            "org.spongepowered.configurate.yaml.",
            "org.spongepowered.configurate.gson.",
            "org.spongepowered.configurate.jackson.",
            "org.spongepowered.configurate.xml.",
    };

    @Override
    public void configureTransformationClassLoader(final ITransformingClassLoaderBuilder builder) {
        // Plus everything else on the system loader
        // todo: we might be able to eliminate this at some point, but that causes complications
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
    }

    protected boolean isTransformable(final URI uri) throws URISyntaxException, IOException {
        final File file = new File(uri);

        // in Java 8 ONLY, the system classpath contains JVM internals
        // let's make sure those don't get transformed
        if (file.getAbsolutePath().startsWith(VanillaBaseLaunchHandler.JAVA_HOME_PATH)) {
            return false;
        }

        if (file.isDirectory()) {
            for (final String test : VanillaBaseLaunchHandler.NON_TRANSFORMABLE_PATHS) {
                if (new File(file, test).exists()) {
                    return false;
                }
            }
        } else if (file.isFile()) {
            try (final JarFile jf = new JarFile(new File(uri))) {
                for (final String test : VanillaBaseLaunchHandler.NON_TRANSFORMABLE_PATHS) {
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
            outer: for (final String pkg : VanillaBaseLaunchHandler.EXCLUDED_PACKAGES) {
                if (klass.startsWith(pkg)) {
                    for (final String exception : VanillaBaseLaunchHandler.EXCLUSION_EXCEPTIONS) {
                        if (klass.startsWith(exception)) {
                            break outer;
                        }
                    }
                    return false;
                }
            }
            return true;
        });
        VanillaBaseLaunchHandler.fixPackageExclusions(launchClassLoader);

        return () -> {
            this.launchService0(arguments, launchClassLoader);
            return null;
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void fixPackageExclusions(final ITransformingClassLoader tcl) {
        try {
            final Field prefixField = tcl.getClass().getDeclaredField("SKIP_PACKAGE_PREFIXES");
            prefixField.setAccessible(true);
            ((List) prefixField.get(null)).set(1, "__javax__noplswhy.");
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException("Failed to fix strange transformer exclusions", ex);
        }
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
