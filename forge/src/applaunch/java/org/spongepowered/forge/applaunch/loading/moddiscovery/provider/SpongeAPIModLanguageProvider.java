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
package org.spongepowered.forge.applaunch.loading.moddiscovery.provider;

import net.minecraftforge.forgespi.language.ILifecycleEvent;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class SpongeAPIModLanguageProvider implements IModLanguageProvider {

    private static final String NAME = "spongeapi";

    @Override
    public String name() {
        return SpongeAPIModLanguageProvider.NAME;
    }

    @Override
    public Consumer<ModFileScanData> getFileVisitor() {
        return sd -> sd.addLanguageLoader(Collections.singletonMap(SpongeAPIModLanguageProvider.NAME,
                new DummySpongeModLoader(SpongeAPIModLanguageProvider.NAME)));
    }

    @Override
    public <R extends ILifecycleEvent<R>> void consumeLifecycleEvent(final Supplier<R> consumeEvent) {

    }
}
