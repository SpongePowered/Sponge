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
package org.spongepowered.forge.launch.loading.language;

import static net.minecraftforge.fml.loading.LogMarkers.LOADING;

import net.minecraftforge.forgespi.language.ILifecycleEvent;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SpongeModLanguageProvider implements IModLanguageProvider {

    private static final Logger log = LogManager.getLogger();

    private final static String NAME = "sponge";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Consumer<ModFileScanData> getFileVisitor() {
        return sd -> {
            final Map<String, IModLanguageLoader> mods = new HashMap<>();

            for (final IModFileInfo info : sd.getIModInfoData()) {
                for (final IModInfo mod : info.getMods()) {
                    mods.put(mod.getModId(), new Loader());
                }
            }

            sd.addLanguageLoader(mods);
        };
    }

    @Override
    public <R extends ILifecycleEvent<R>> void consumeLifecycleEvent(final Supplier<R> consumeEvent) {
    }

    private static class Loader implements IModLanguageLoader {

        @Override
        public <T> T loadMod(final IModInfo info, final ClassLoader modClassLoader, final ModFileScanData modFileScanResults) {
            try {
                final Class<?> spongeContainer = Class.forName(
                        "org.spongepowered.forge.launch.SpongeModContainer",
                        true,
                        Thread.currentThread().getContextClassLoader()
                );

                log.info(LOADING, "Loading SpongeModContainer from classloader {} - got {}",
                        Thread.currentThread().getContextClassLoader(),
                        spongeContainer.getClassLoader()
                );

                return (T) spongeContainer.getConstructor(IModInfo.class, ClassLoader.class)
                        .newInstance(info, modClassLoader);
            }
            catch (final Throwable ex) {
                log.fatal(LOADING, "Unable to load SpongeModContainer", ex);
                throw new RuntimeException(ex);
            }
        }

    }

}
