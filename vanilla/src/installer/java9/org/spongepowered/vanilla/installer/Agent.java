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

import org.spongepowered.mlpatcher.AsmFixerAgent;
import org.tinylog.Logger;

import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.Set;
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

    public static void premain(final String agentArgs, final Instrumentation instrumentation) {
        Agent.instrumentation = instrumentation;
        Agent.runModLauncherFixer(instrumentation);
    }

    public static void agentmain(final String agentArgs, final Instrumentation instrumentation) {
        Agent.instrumentation = instrumentation;
        Agent.runModLauncherFixer(instrumentation);
    }

    private static void runModLauncherFixer(final Instrumentation instrumentation) {
        try {
            AsmFixerAgent.agentmain("", instrumentation);
        } catch (final NoClassDefFoundError ex) {
            Logger.warn("Failed to load ModLauncher fixer agent for some reason. Proceed with caution.");
        }
    }

    static void crackModules(final ClassLoader modLauncherLoader) {
        final Set<Module> loaderUnnamed = Set.of(modLauncherLoader.getUnnamedModule());
        Agent.instrumentation.redefineModule(
            Manifest.class.getModule(),
            Set.of(),
            Map.of("sun.security.util", loaderUnnamed), // ModLauncher
            Map.of(
                // ModLauncher -- needs Manifest.jv, and various JarVerifier methods
                "java.util.jar", loaderUnnamed
            ),
            Set.of(),
            Map.of()
        );
    }
}
