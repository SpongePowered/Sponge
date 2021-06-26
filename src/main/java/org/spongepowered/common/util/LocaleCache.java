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
package org.spongepowered.common.util;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.LocaleUtils;
import org.spongepowered.common.SpongeCommon;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public final class LocaleCache {

    private static final Map<String, Locale> LOCALE_CACHE = Maps.newHashMap();
    private static final Function<String, Locale> LOCALE_FUNCTION = new Function<String, Locale>() {
        @Override
        public Locale apply(String key) {
            try {
                return LocaleUtils.toLocale(key);
            } catch (final IllegalArgumentException ignored) {
                // Ignore the first exception and try again, this time using a "fixed" key.
                final String fixedKey = this.fixLocaleKey(key);
                try {
                    return LocaleUtils.toLocale(fixedKey);
                } catch (final IllegalArgumentException e) {
                    SpongeCommon.logger().error("Could not transform '{}' or '{}' into a Locale", key, fixedKey);
                    throw e;
                }
            }
        }

        private String fixLocaleKey(final String key) {
            final String[] parts = key.split("_");
            if (parts.length == 2) {
                return parts[0] + '_' + parts[1].toUpperCase();
            }
            return key;
        }
    };

    /**
     * Gets a locale from the cache.
     *
     * @param tag The locale tag
     * @return The locale
     */
    public static Locale getLocale(final String tag) {
        return LocaleCache.LOCALE_CACHE.computeIfAbsent(tag, LocaleCache.LOCALE_FUNCTION);
    }

    /**
     * Preload a locale into the cache.
     *
     * @param locale The locale to preload
     */
    public static void preload(final Locale locale) {
        final String tag = locale.toString();
        LocaleCache.LOCALE_CACHE.put(tag, locale);
        LocaleCache.LOCALE_CACHE.put(tag.toLowerCase(), locale);
    }

    private LocaleCache() {
    }
}
