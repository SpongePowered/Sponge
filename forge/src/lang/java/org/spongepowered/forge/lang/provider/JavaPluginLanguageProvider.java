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
package org.spongepowered.forge.lang.provider;

import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import net.minecraftforge.fml.Logging;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLanguageProvider;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class JavaPluginLanguageProvider extends FMLJavaModLanguageProvider {

    private static final Type PLUGIN_ANNOTATION = Type.getType("Lorg/spongepowered/plugin/builtin/jvm/Plugin;");

    private final Logger logger;

    public JavaPluginLanguageProvider() {
        this.logger = LogManager.getLogger();
    }

    @Override
    public String name() {
        return "java_plain";
    }

    @Override
    public Consumer<ModFileScanData> getFileVisitor() {
        return scanResult -> {
            final Map<String, IModLanguageLoader> modTargetMap = scanResult.getAnnotations().stream()
                    .filter(ad -> ad.annotationType().equals(JavaPluginLanguageProvider.PLUGIN_ANNOTATION))
                    .peek(ad -> this.logger.debug(Logging.SCAN, "Found @Plugin class {} with id {}", ad.clazz().getClassName(), ad.annotationData().get("value")))
                    .map(ad -> new PluginTarget(ad.clazz().getClassName(), JavaPluginLanguageProvider.fixPluginId((String) ad.annotationData().get("value"))))
                    .collect(Collectors.toMap(PluginTarget::getPlugin, Function.identity(), (a,b)->a));
            scanResult.addLanguageLoader(modTargetMap);
        };
    }

    private static String fixPluginId(final String id) {
        return id.replace('-', '_');
    }

    private static final class PluginTarget implements IModLanguageProvider.IModLanguageLoader {

        private final Logger logger;
        private final String className;
        private final String plugin;

        private PluginTarget(final String className, final String plugin) {
            this.logger = LogManager.getLogger();
            this.className = className;
            this.plugin = plugin;
        }

        @Override
        public <T> T loadMod(final IModInfo info, final ModFileScanData modFileScanData, final ModuleLayer gameLayer) {
            // The following is adapted from FMLJavaModLanguageProvider.FMLModTarget

            // This language class is loaded in the system level classloader - before the game even starts
            // So we must treat container construction as an arms length operation, and load the container
            // in the classloader of the game - the context classloader is appropriate here.
            try {
                final Class<?> pluginContainer = Class.forName(
                        "org.spongepowered.forge.launch.plugin.PluginModContainer", true, Thread.currentThread().getContextClassLoader());
                this.logger.debug(Logging.LOADING, "Loading PluginModContainer from classloader {} - got {}", Thread.currentThread().getContextClassLoader(), pluginContainer.getClassLoader());
                final Constructor<?> constructor = pluginContainer.getConstructor(IModInfo.class, String.class, ModFileScanData.class, ModuleLayer.class);
                return (T) constructor.newInstance(info, className, modFileScanData, gameLayer);
            }
            // ALL exception handling has to be done through the classloader, because we're loaded in the wrong context, so any classes we just blind load will be in the wrong
            // class loading context. Funky but works.
            catch (final InvocationTargetException e) {
                this.logger.fatal(Logging.LOADING, "Failed to build plugin", e);
                final Class<RuntimeException> mle = (Class<RuntimeException>) LamdbaExceptionUtils.uncheck(()->Class.forName("net.minecraftforge.fml.ModLoadingException", true, Thread.currentThread().getContextClassLoader()));
                if (mle.isInstance(e.getTargetException())) {
                    throw mle.cast(e.getTargetException());
                } else {
                    final Class<ModLoadingStage> mls = (Class<ModLoadingStage>) LamdbaExceptionUtils.uncheck(()->Class.forName("net.minecraftforge.fml.ModLoadingStage", true, Thread.currentThread().getContextClassLoader()));
                    throw LamdbaExceptionUtils.uncheck(()->LamdbaExceptionUtils.uncheck(()->mle.getConstructor(IModInfo.class, mls, String.class, Throwable.class)).newInstance(info, Enum.valueOf(mls, "CONSTRUCT"), "fml.modloading.failedtoloadmodclass", e));
                }
            }
            catch (final NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                this.logger.fatal(Logging.LOADING,"Unable to load PluginModContainer, excuse me?", e);
                final Class<RuntimeException> mle = (Class<RuntimeException>)LamdbaExceptionUtils.uncheck(()->Class.forName("net.minecraftforge.fml.ModLoadingException", true, Thread.currentThread().getContextClassLoader()));
                final Class<ModLoadingStage> mls = (Class<ModLoadingStage>) LamdbaExceptionUtils.uncheck(()->Class.forName("net.minecraftforge.fml.ModLoadingStage", true, Thread.currentThread().getContextClassLoader()));
                throw LamdbaExceptionUtils.uncheck(()->LamdbaExceptionUtils.uncheck(()->mle.getConstructor(IModInfo.class, mls, String.class, Throwable.class)).newInstance(info, Enum.valueOf(mls, "CONSTRUCT"), "fml.modloading.failedtoloadmodclass", e));
            }
        }

        public String getPlugin() {
            return this.plugin;
        }
    }
}
