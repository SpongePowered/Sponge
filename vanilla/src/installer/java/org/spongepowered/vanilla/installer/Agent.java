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

import org.tinylog.Logger;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.jar.JarFile;

/**
 * Agent, used to add downloaded jars to the system classpath and open modules
 * for deep reflection.
 *
 * <p>See the JDK9+ counterpart in src/installer/java9</p>
 */
public class Agent {

    private static Instrumentation instrumentation;
    private static final Method URL_CLASS_LOADER_ADD_URL;

    static {
        Method addUrl;
        try {
            addUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addUrl.setAccessible(true);
        } catch (NoSuchMethodException e) {
            addUrl = null;
        }

        URL_CLASS_LOADER_ADD_URL = addUrl;
    }

    public static void premain(final String agentArgs, final Instrumentation instrumentation) {
        Agent.instrumentation = instrumentation;
    }

    public static void agentmain(final String agentArgs, final Instrumentation instrumentation) {
        Agent.instrumentation = instrumentation;
    }

    static void addJarToClasspath(final Path jar) {
        if (Agent.instrumentation != null) {
            try {
                Agent.instrumentation.appendToSystemClassLoaderSearch(new JarFile(jar.toRealPath().toString()));
                return;
            } catch (final IOException ex) {
                Logger.error(ex, "Failed to create jar file for archive '{}'!", jar);
            }
        }

        final ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
        if (systemLoader instanceof URLClassLoader && Agent.URL_CLASS_LOADER_ADD_URL != null) {
            try {
                Agent.URL_CLASS_LOADER_ADD_URL.invoke(systemLoader, jar.toRealPath().toUri().toURL());
                return;
            } catch (final IllegalAccessException | InvocationTargetException | IOException ex) {
                Logger.error(ex, "Failed to add path {} to the system ClassLoader", jar);
            }
        }

        throw new IllegalStateException("No agent was registered, and could not reflectively add the URL to the system URLClassLoader!");
    }

    static void crackModules() {
        // Only used in java 9+
    }

}
