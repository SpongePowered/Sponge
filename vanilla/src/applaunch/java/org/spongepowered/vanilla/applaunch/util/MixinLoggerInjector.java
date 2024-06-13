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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.CompositeFilter;
import org.apache.logging.log4j.core.filter.DenyAllFilter;
import org.apache.logging.log4j.core.filter.RegexFilter;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.Queue;

/**
 * Small utility to inject validation into the mixin logger.
 *
 * <p>Needs to be on app classloader because Mixin is.</p>
 */
public final class MixinLoggerInjector {

    private MixinLoggerInjector() {
    }

    private static RegexFilter pattern(final String pattern) {
        try {
            return RegexFilter.createFilter(pattern, new String[0], false, Filter.Result.ACCEPT, Filter.Result.NEUTRAL);
        } catch (final IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Queue<String> captureLogger() {
        if (!MixinEnvironment.getDefaultEnvironment().getOption(MixinEnvironment.Option.DEBUG_VERBOSE)) {
            throw new IllegalStateException("Mixin capture will not function appropriately unless -Dmixin.debug.verbose is enabled");
        }

        final Logger mixinLogger = (Logger) LogManager.getLogger("mixin");
        final CompositeFilter messageFilter = CompositeFilter.createFilters(new Filter[] {
            // regex patterns
            MixinLoggerInjector.pattern(".*for final field [^@]+@Mutable.*"),
            // MixinLoggerInjector.pattern("^NESTING not supported.*"), // todo: if we need to
            DenyAllFilter.newBuilder().build(),
        });
        final CaptureAppender appender = CaptureAppender.builder()
            .setName("IntegrationTest-Capture")
            .setFilter(messageFilter)
            .build();
        appender.start();
        mixinLogger.addAppender(appender);
        return appender.messages();
    }

}
