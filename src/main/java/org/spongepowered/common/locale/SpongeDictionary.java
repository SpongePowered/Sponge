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
package org.spongepowered.common.locale;

import org.spongepowered.api.locale.Localized;
import org.spongepowered.api.locale.dictionary.ConfigDictionarySingle;
import org.spongepowered.api.locale.dictionary.ResourceDictionary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

public class SpongeDictionary<T extends Localized> extends ConfigDictionarySingle<T> implements ResourceDictionary<T> {

    private final File file;

    public SpongeDictionary(T subject, Locale defaultLocale, Path parent, Path path) {
        super(subject, defaultLocale);
        this.file = new File(parent.toString(), path.toString());
    }

    @Override
    public Path getPath(Locale locale) {
        if (this.file.exists()) {
            return this.file.toPath();
        }
        return this.file.toPath().getFileName();
    }

    @Override
    public InputStream getSource(Locale locale) throws IOException {
        if (this.file.exists()) {
            return Files.newInputStream(file.toPath(), StandardOpenOption.READ);
        }
        return ResourceDictionary.super.getSource(locale);
    }
}
