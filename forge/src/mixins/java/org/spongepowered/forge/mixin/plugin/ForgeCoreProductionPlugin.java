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
package org.spongepowered.forge.mixin.plugin;

import cpw.mods.modlauncher.Environment;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.TransformingClassLoader;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.mixin.plugin.AbstractMixinConfigPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Predicate;

// just a way to get on the TCL nice and early
public class ForgeCoreProductionPlugin extends AbstractMixinConfigPlugin {

    // :(((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((
    private static final String[] FORCED_ALLOWED_PACKAGES = {
        "com.google.inject.",
        "javax.inject."
    };

    private static final Logger LOGGER = LogManager.getLogger();

    public ForgeCoreProductionPlugin() {
        if (FMLEnvironment.production) { // the language loading is on an isolated CL, so we have to init again
            // todo: too tired for gradle
            try {
                Class.forName("org.spongepowered.forge.applaunch.plugin.ForgePluginPlatform")
                    .getMethod("bootstrap", Environment.class)
                    .invoke(null, Launcher.INSTANCE.environment());
            } catch (final ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException ex) {
                ForgeCoreProductionPlugin.LOGGER.error("Failed to bootstrap the Forge plugin platform on TCL", ex);
            }

            try {
                final TransformingClassLoader tcl = (TransformingClassLoader) this.getClass().getClassLoader();
                final Field targetPackageFilter = tcl.getClass().getDeclaredField("targetPackageFilter");
                targetPackageFilter.setAccessible(true);
                final Predicate<String> existing = (Predicate<String>) targetPackageFilter.get(tcl);
                final Predicate<String> better = pkg -> {
                    for (final String prefix : ForgeCoreProductionPlugin.FORCED_ALLOWED_PACKAGES) {
                        if (pkg.startsWith(prefix)) {
                            return true;
                        }
                    }
                    return existing.test(pkg);
                };
                targetPackageFilter.set(tcl, better);
            } catch (final NoSuchFieldException | IllegalAccessException ex) {
                ForgeCoreProductionPlugin.LOGGER.error("Failed to fix TCL package filter", ex);
            }
        }
    }

}
