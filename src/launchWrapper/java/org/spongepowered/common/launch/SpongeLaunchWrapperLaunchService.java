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
package org.spongepowered.common.launch;

import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.launch.transformer.SpongeSuperclassRegistry;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.function.Supplier;

public class SpongeLaunchWrapperLaunchService implements InternalLaunchService {

    private final Logger logger;

    public SpongeLaunchWrapperLaunchService(final Logger logger) {
        this.logger = logger;
    }


    @Override
    public void addJreExtensionsToClassPath() {
        // Make sure JRE extensions are loaded using the system class loader
        Launch.classLoader.addClassLoaderExclusion("jdk.");

        /*
         * By default Launchwrapper inherits the class path from the system class loader.
         * However, JRE extensions (e.g. Nashorn in the jre/lib/ext directory) are not part
         * of the class path of the system class loader.
         * Instead, they're loaded using a parent class loader (Launcher.ExtClassLoader).
         * Currently, Launchwrapper does not fall back to the parent class loader if it's
         * unable to find a class on its class path. To make the JRE extensions usable for
         * plugins we manually add the URLs from the ExtClassLoader to Launchwrapper's
         * class path.
         */
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        if (classLoader == null) {
            return;
        }

        classLoader = classLoader.getParent(); // Launcher.ExtClassLoader
        if (classLoader instanceof URLClassLoader) {
            for (final URL url : ((URLClassLoader) classLoader).getURLs()) {
                Launch.classLoader.addURL(url);
            }
        }
    }

    @Override
    public Supplier<? extends IExitHandler> getExitHandler() {
        return TerminateVM::new;
    }

    @Override
    public Supplier<? extends Logger> getLaunchLogger() {
        return () -> this.logger;
    }

    @Override
    public boolean isVanilla() {
        try {
            return Launch.classLoader.getClassBytes("net.minecraftforge.common.ForgeVersion") == null;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void registerSuperclassModification(final String targetClass, final String newSuperClass) {
        SpongeSuperclassRegistry.registerSuperclassModification(targetClass, newSuperClass);
    }


}
