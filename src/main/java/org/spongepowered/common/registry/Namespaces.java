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
package org.spongepowered.common.registry;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.CatalogType;
import org.spongepowered.common.SpongeImpl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class Namespaces {

    public static final String MINECRAFT = SpongeImpl.GAME_ID;
    public static final String SPONGE = SpongeImpl.ECOSYSTEM_ID;

    private Namespaces() {
    }

    public static String namespacedId(final String id) {
        return namespacedId(MINECRAFT, id);
    }

    public static String namespacedId(final String namespace, final String id) {
        checkNotNull(id, "id");
        if (id.indexOf(':') == -1) {
            return toLowerCase(namespace) + ':' + toLowerCase(id);
        }
        return toLowerCase(id);
    }

    // Replace the minecraft and sponge namespace for fields.
    public static <C extends CatalogType> Map<String, C> prepareForField(final Map<String, ? extends C> in) {
        final Map<String, C> map = new HashMap<>(in.size());
        for (Map.Entry<String, ? extends C> entry : in.entrySet()) {
            map.put(prepareForField(entry.getKey()), entry.getValue());
        }
        return map;
    }

    // Replace the minecraft and sponge namespace for fields.
    public static String prepareForField(final String id) {
        return id
                .replace(MINECRAFT + ':', "")
                .replace(SPONGE + ':', "");
    }

    static String toLowerCase(final String string) {
        return string.toLowerCase(Locale.ENGLISH);
    }
}
