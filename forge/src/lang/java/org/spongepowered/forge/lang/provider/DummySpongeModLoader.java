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

import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class DummySpongeModLoader implements IModLanguageProvider.IModLanguageLoader {
    private static final Logger LOGGER = LogManager.getLogger();

    private final String dummyInstance;

    public DummySpongeModLoader(final String dummyInstance) {
        this.dummyInstance = dummyInstance;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T loadMod(final IModInfo info, final ModFileScanData modFileScanData, final ModuleLayer moduleLayer) {
        try {
            final Class<?> mcModClass = Class.forName("org.spongepowered.forge.launch.plugin.DummySpongeModContainer", true, Thread.currentThread().getContextClassLoader());
            return (T)mcModClass.getConstructor(String.class, IModInfo.class).newInstance(this.dummyInstance, info);
        } catch (final Exception ex) {
            LOGGER.fatal("Unable to load DummySpongeModContainer!", ex);
            throw new RuntimeException(ex);
        }
    }
}
