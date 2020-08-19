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
package org.spongepowered.vanilla.modlauncher.bootstrap;

import cpw.mods.modlauncher.TransformingClassLoader;
import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformingClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * The common Sponge {@link ILaunchHandlerService launch handler} for development
 * and production environments.
 */
public abstract class AbstractVanillaLaunchHandler implements ILaunchHandlerService {

    protected static final Logger log = LogManager.getLogger("Sponge Launch");

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
    protected static final List<String> EXCLUDED_PACKAGES = Arrays.asList(
            "org.spongepowered.plugin.",
            "org.spongepowered.common.launch.plugin.loader.",
            "org.spongepowered.vanilla.launch.plugin.loader."
    );

    @Override
    public Callable<Void> launchService(final String[] arguments, final ITransformingClassLoader launchClassLoader) {
        log.info("Transitioning to Sponge launcher, please wait...");

        launchClassLoader.addTargetPackageFilter(klass -> {
            for (final String pkg : EXCLUDED_PACKAGES) {
                if (klass.startsWith(pkg)) return false;
            }
            return true;
        });

        return () -> {
            this.launchService0(arguments, launchClassLoader);
            return null;
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
