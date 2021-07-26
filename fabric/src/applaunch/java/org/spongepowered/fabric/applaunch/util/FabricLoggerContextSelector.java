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
package org.spongepowered.fabric.applaunch.util;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.async.AsyncLoggerContextSelector;
import org.apache.logging.log4j.core.selector.ContextSelector;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.URI;

/**
 * Sponge's hooks into Log4J to customize rendering.
 *
 * <p>Because Log4j needs to be used both inside and outside the transforming
 * class loader, we hook into Log4J at the context selector level. This lets us
 * deliver a different context within the transformed environment than
 * outside it.</p>
 */
public class FabricLoggerContextSelector extends AsyncLoggerContextSelector implements ContextSelector {

    @Override
    public LoggerContext getContext(final String fqcn, final @Nullable ClassLoader loader, final boolean currentContext, final URI configLocation) {
        // TODO: add custom log4j hook, use sponge logger only if it is loaded by loader
        return super.getContext(fqcn, loader, currentContext, configLocation);
    }
}
