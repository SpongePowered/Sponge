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
package org.spongepowered.vanilla.installer;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Agent, used to add downloaded jars to the system classpath and open modules
 * for deep reflection.
 *
 * <p>This needs to be compiled for exactly java 9, since it runs before we have
 * an opportunity to provide a friendly warning message.</p>
 */
public class Agent {

    private static Instrumentation instrumentation;
    private static boolean usingFallback;

    public static void premain(final String agentArgs, final Instrumentation instrumentation) {
        Agent.instrumentation = instrumentation;
    }

    public static void agentmain(final String agentArgs, final Instrumentation instrumentation) {
        Agent.instrumentation = instrumentation;
    }

    static void addJarToClasspath(final Path jar) {
        if (Agent.instrumentation == null) {
            throw new IllegalStateException("The SpongeVanilla jar must be run as a java agent in order to add downloaded libraries to the classpath!");
        }
        try {
            final Path normalized = Paths.get(jar.toRealPath().toUri().toURL().toURI());

            if (Agent.usingFallback) {
                Fallback.addToSystemClasspath(jar);
                return;
            }

            try {
                // x.X The URL escaping done by appendToSystemClassLoaderSearch differs from the
                try (final JarFile jf = new JarFile(new File(normalized.toUri()))) {
                    Agent.instrumentation.appendToSystemClassLoaderSearch(jf);
                }
            } catch (final IllegalArgumentException ex) {
                // For some reason, the Agent method on Windows can't handle some non-ASCII characters
                // This is fairly awful, but it makes things work (and hopefully won't be reached often)
                Logger.debug(ex, "Failed to add library {} to classpath, transitioning to fallback (more unsafe!) method", jar);
                Agent.usingFallback = true;
                Fallback.addToSystemClasspath(jar);
            }
        } catch (final IOException | URISyntaxException ex) {
            Logger.error(ex, "Failed to create jar file for archive '{}'!", jar);
        }
    }

    static void crackModules() {
        final Set<Module> systemUnnamed = Set.of(ClassLoader.getSystemClassLoader().getUnnamedModule());
        Agent.instrumentation.redefineModule(
            Manifest.class.getModule(),
            Set.of(),
            Map.of("sun.security.util", systemUnnamed), // ModLauncher
            Map.of(
                // ModLauncher -- needs Manifest.jv, and various JarVerifier methods
                "java.util.jar", systemUnnamed
            ),
            Set.of(),
            Map.of()
        );
    }

    static final class Fallback {

        private static final Object SYSTEM_CLASS_PATH; /* a URLClassPath */
        private static final Method ADD_URL; /* URLClassPath.addURL(java.net.URL) */

        static {
            Logger.debug("Initializing fallback classpath modification. This is only expected when using non-ASCII characters in file paths on Windows");
            // Crack the java.base module to allow us to use reflection
            final Set<Module> systemUnnamed = Set.of(ClassLoader.getSystemClassLoader().getUnnamedModule());
            Agent.instrumentation.redefineModule(
                ClassLoader.class.getModule(), /* java.base */
                Set.of(),
                Map.of("jdk.internal.loader", systemUnnamed),
                Map.of("jdk.internal.loader", systemUnnamed),
                Set.of(),
                Map.of()
            );

            final ClassLoader loader = ClassLoader.getSystemClassLoader();

            Field ucp = Fallback.fieldOrNull(loader.getClass(), "ucp");
            if (ucp == null) {
                ucp = Fallback.fieldOrNull(loader.getClass().getSuperclass(), "ucp");
            }

            if (ucp == null) {
                // Did they change something?
                throw new ExceptionInInitializerError("Unable to initialize fallback classpath handling on your system. Perhaps try a different Java version?");
            }

            try {
                SYSTEM_CLASS_PATH = ucp.get(loader);
                ADD_URL = Fallback.SYSTEM_CLASS_PATH.getClass().getDeclaredMethod("addURL", URL.class);
            } catch (final NoSuchMethodException | IllegalAccessException ex) {
                throw new ExceptionInInitializerError(ex);
            }
        }

        private static @Nullable Field fieldOrNull(final @Nullable Class<?> clazz, final String name) {
            if (clazz == null) {
                return null;
            }

            try {
                final Field f = clazz.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (final NoSuchFieldException ex) {
                return null;
            }
        }

        static void addToSystemClasspath(final Path file) {
            try {
                Fallback.ADD_URL.invoke(Fallback.SYSTEM_CLASS_PATH, file.toUri().toURL());
            } catch (final IllegalAccessException | InvocationTargetException | IOException ex) {
                Logger.error(ex, "Failed to add file {} to the system classpath", file);
                throw new RuntimeException(ex);
            }
        }

    }

}
