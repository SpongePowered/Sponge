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
package org.spongepowered.vanilla.applaunch.util;

import cpw.mods.modlauncher.TransformingClassLoader;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.impl.ContextAnchor;
import org.apache.logging.log4j.core.selector.ClassLoaderContextSelector;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

/**
 * Sponge's hooks into Log4J to customize rendering.
 *
 * <p>Because Log4j needs to be used both inside and outside the transforming
 * class loader, we hook into Log4J at the context selector level. This lets us
 * deliver a different context within the transformed environment than
 * outside it.</p>
 */
public class VanillaLoggerContextSelector extends ClassLoaderContextSelector {

    private static final String CONTEXT_PROVIDER_CLASS = "org.spongepowered.vanilla.chat.VanillaLoggerContextProvider";

    @Override
    public LoggerContext getContext(final String fqcn, final @Nullable ClassLoader loader, final boolean currentContext, final URI configLocation) {
        if (!currentContext && loader instanceof TransformingClassLoader) {
            // Return custom context
            return this.provideTransformingContext(loader, configLocation);
        } else if (loader == null) {
            final Class<?> clazz = StackLocatorUtil.getCallerClass(fqcn);
            if (clazz != null && clazz.getClassLoader() instanceof TransformingClassLoader) {
                // return custom context
                return this.provideTransformingContext(clazz.getClassLoader(), configLocation);
            }
            // identical to superclass
            final LoggerContext lc = ContextAnchor.THREAD_CONTEXT.get();
            if (lc != null) {
                return lc;
            }
            return this.getDefault();
        }

        return super.getContext(fqcn, loader, currentContext, configLocation);
    }

    protected LoggerContext provideTransformingContext(final ClassLoader loader, final URI configLocation) {
        // Mostly copied from ClassLoaderContextSelector.locateContext, with modifications to not traverse parent loaders
        final String name = this.toContextMapKey(loader);
        AtomicReference<WeakReference<LoggerContext>> ref = ClassLoaderContextSelector.CONTEXT_MAP.get(name);
        if (ref == null) {
            LoggerContext ctx = this.createSpongeContext(loader, name, configLocation);
            final AtomicReference<WeakReference<LoggerContext>> r = new AtomicReference<>();
            r.set(new WeakReference<>(ctx));
            ClassLoaderContextSelector.CONTEXT_MAP.putIfAbsent(name, r);
            ctx = ClassLoaderContextSelector.CONTEXT_MAP.get(name).get().get();
            return ctx;
        }
        final WeakReference<LoggerContext> weakRef = ref.get();
        LoggerContext ctx = weakRef.get();
        if (ctx != null) {
            if (ctx.getConfigLocation() == null && configLocation != null) {
                ClassLoaderContextSelector.LOGGER.debug("Setting configuration to {}", configLocation);
                ctx.setConfigLocation(configLocation);
            } else if (ctx.getConfigLocation() != null && configLocation != null
                && !ctx.getConfigLocation().equals(configLocation)) {
                ClassLoaderContextSelector.LOGGER.warn("locateContext called with URI {}. Existing LoggerContext has URI {}", configLocation,
                    ctx.getConfigLocation());
            }
            return ctx;
        }
        ctx = this.createSpongeContext(loader, name, configLocation);
        ref.compareAndSet(weakRef, new WeakReference<>(ctx));
        return ctx;

    }

    @SuppressWarnings("unchecked")
    protected LoggerContext createSpongeContext(final ClassLoader transformingLoader, final String name, final URI configLocation) {
            BiFunction<String, URI, LoggerContext> transformingContextProvider;
        try {
            final Class<?> clazz = Class.forName(VanillaLoggerContextSelector.CONTEXT_PROVIDER_CLASS, true, transformingLoader);
            transformingContextProvider = (BiFunction<String, URI, LoggerContext>) clazz.getConstructor().newInstance();
        } catch (final ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            ClassLoaderContextSelector.LOGGER.error("Failed to create context provider instance", ex);
            throw new IllegalStateException(ex);
        }
        return transformingContextProvider.apply(name, configLocation);
    }
}
