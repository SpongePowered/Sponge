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
package org.spongepowered.common.registry.type.text;

import org.spongepowered.api.text.translation.locale.Locales;
import org.spongepowered.common.registry.RegistryModule;
import org.spongepowered.common.util.LanguageUtil;

import java.lang.reflect.Field;
import java.util.Locale;

public class LocaleRegistryModule implements RegistryModule {

    // TODO: Cleanup this mess

    @Override
    public void registerDefaults() {
        Field[] locales = Locales.class.getFields();
        for (Field field : locales) {
            int pos = field.getName().indexOf('_');
            if (pos < 0) {
                continue;
            }

            char[] c = field.getName().toCharArray();
            for (int i = 0; i < pos; i++) {
                c[i] = Character.toLowerCase(c[i]);
            }

            String code = new String(c);
            try {
                LanguageUtil.LOCALE_CACHE.put(code, (Locale) field.get(null));
            } catch (IllegalAccessException ignored) {
            }
        }

    }

}
