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

import net.minecraftforge.fml.Logging;
import net.minecraftforge.fml.ModLoadingException;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.forgespi.language.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public final class SpongeLanguageProvider implements IModLanguageProvider {

    @Override
    public String name() {
        return "sponge";
    }

    @Override
    public Consumer<ModFileScanData> getFileVisitor() {
        return scanData -> {
            final Map<String, IModLanguageLoader> targets = (Map<String, IModLanguageLoader>) scanData.getTargets();
            for (final IModFileInfo fileInfo : scanData.getIModInfoData()) {
                for (final IModInfo modInfo : fileInfo.getMods()) {
                    targets.put(modInfo.getModId(), SpongeLanguageLoader.INSTANCE);
                }
            }
        };
    }

    @Override
    public <R extends ILifecycleEvent<R>> void consumeLifecycleEvent(final Supplier<R> consumeEvent) {}

    private static final class SpongeLanguageLoader implements IModLanguageProvider.IModLanguageLoader {
        private static final Logger LOGGER = LogManager.getLogger();
        private static final SpongeLanguageLoader INSTANCE = new SpongeLanguageLoader();

        @Override
        public <T> T loadMod(final IModInfo info, final ModFileScanData scanData, final ModuleLayer layer) {
            try {
                final Module module = layer.findModule("spongeforge").orElseThrow(() -> new NoSuchElementException("Module spongeforge"));
                final Class<?> containerClass = Class.forName(module, "org.spongepowered.forge.launch.plugin.PluginModContainer");
                final Constructor<?> constructor = containerClass.getConstructor(IModInfo.class, ModFileScanData.class, ModuleLayer.class);
                return (T) constructor.newInstance(info, scanData, layer);
            } catch (final InvocationTargetException e) {
                SpongeLanguageLoader.LOGGER.fatal(Logging.LOADING, "Failed to build plugin", e);
                throw new ModLoadingException(info, ModLoadingStage.CONSTRUCT, "Failed to build plugin", e);
            } catch (final Exception e) {
                SpongeLanguageLoader.LOGGER.fatal(Logging.LOADING, "Unable to load PluginModContainer", e);
                throw new ModLoadingException(info, ModLoadingStage.CONSTRUCT, "Unable to load PluginModContainer", e);
            }
        }
    }
}
