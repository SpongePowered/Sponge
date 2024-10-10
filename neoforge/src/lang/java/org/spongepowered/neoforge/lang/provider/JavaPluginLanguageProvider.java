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
package org.spongepowered.neoforge.lang.provider;

import net.neoforged.fml.Logging;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.loading.BuiltInLanguageLoader;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;

public final class JavaPluginLanguageProvider extends BuiltInLanguageLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Type PLUGIN_ANNOTATION = Type.getType("Lorg/spongepowered/plugin/builtin/jvm/Plugin;");

    @Override
    public String name() {
        return "java_plain";
    }

    @Override
    public ModContainer loadMod(final IModInfo info, final ModFileScanData modFileScanResults, final ModuleLayer layer) {
        final var modClasses = modFileScanResults.getAnnotations().stream()
            .filter(ad -> ad.annotationType().equals(JavaPluginLanguageProvider.PLUGIN_ANNOTATION))
            .peek(ad -> LOGGER.debug(Logging.SCAN, "Found @Plugin class {} with id {}", ad.clazz().getClassName(), ad.annotationData().get("value")))
            .filter(data -> JavaPluginLanguageProvider.fixPluginId((String) data.annotationData().get("value")).equals(info.getModId()))
            .map(ad -> ad.clazz().getClassName())
            .toList();

        if (modClasses.size() != 1) {
            throw new ModLoadingException(ModLoadingIssue.error("Found {0} plugin entrypoints", modClasses.size()));
        }
        return JavaPluginLanguageProvider.newPluginModContainer(info, modClasses.get(0), modFileScanResults, layer);
    }

    private static ModContainer newPluginModContainer(final IModInfo info, final String className, final ModFileScanData modFileScanData, final ModuleLayer gameLayer) {
        try {
            final Module module = gameLayer.findModule("spongeneo").orElseThrow(() -> new NoSuchElementException("Module spongeneo"));
            final Class<?> containerClass = Class.forName(module, "org.spongepowered.neoforge.launch.plugin.PluginModContainer");
            final Constructor<?> constructor = containerClass.getConstructor(IModInfo.class, String.class, ModFileScanData.class, ModuleLayer.class);
            return (ModContainer) constructor.newInstance(info, className, modFileScanData, gameLayer);
        } catch (final InvocationTargetException e) {
            LOGGER.fatal(Logging.LOADING, "Failed to build plugin", e);
            throw new ModLoadingException(ModLoadingIssue.error("Failed to build plugin"));
        } catch (final Exception e) {
            LOGGER.fatal(Logging.LOADING, "Unable to load PluginModContainer", e);
            throw new ModLoadingException(ModLoadingIssue.error("Unable to load PluginModContainer"));
        }
    }

    private static String fixPluginId(final String id) {
        return id.replace('-', '_');
    }
}
