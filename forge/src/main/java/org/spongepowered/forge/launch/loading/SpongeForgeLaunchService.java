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
package org.spongepowered.forge.launch.loading;

import cpw.mods.modlauncher.TransformingClassLoader;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class SpongeForgeLaunchService implements ILaunchPluginService {

    private static final String NAME = "sponge launch";
    private static final Logger log = LogManager.getLogger();

    protected static final List<String> EXCLUDED_PACKAGES = Arrays.asList(
            "org.spongepowered.plugin.",
            "org.spongepowered.forge.launch.loading.SpongeForgeLoader",
            "org.spongepowered.forge.launch.loading.locating."
    );

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public EnumSet<Phase> handlesClass(final Type classType, final boolean isEmpty) {
        return EnumSet.noneOf(Phase.class);
    }

    @Override
    public boolean processClass(final Phase phase, final ClassNode classNode, final Type classType) {
        return false;
    }

    @Override
    public void initializeLaunch(final ITransformerLoader transformerLoader, final Path[] specialPaths) {
        // hacks r us
        for (final Field field : transformerLoader.getClass().getDeclaredFields()) {
            if (field.getType().getName().equals("cpw.mods.modlauncher.TransformingClassLoader")) {
                field.setAccessible(true);

                try {
                    final TransformingClassLoader classLoader = (TransformingClassLoader) field.get(transformerLoader);

                    for (final String pkg : EXCLUDED_PACKAGES) {
                        classLoader.addTargetPackageFilter(klass -> !klass.startsWith(pkg));
                    }
                }
                catch (final IllegalAccessException ex) {
                    log.error("Failed to hack into TransformingClassLoader", ex);
                }
            }
        }
    }

}
