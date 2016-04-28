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

import org.spongepowered.api.locale.bundle.MultiSourceResourceBundleDictionary;
import org.spongepowered.common.SpongeImpl;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.PropertyResourceBundle;

public class SpongeGameDictionary extends MultiSourceResourceBundleDictionary {

    public SpongeGameDictionary(Object subject) {
        super(subject, SpongeImpl.getGlobalConfig().getConfig().getGeneral().getLocale(), SpongeImpl.ECOSYSTEM_NAME);
        Path translations = SpongeImpl.getSpongeConfigDir().resolve("translations");
        this.getResolver().add(locale -> {
            Path path = translations.resolve(SpongeImpl.ECOSYSTEM_NAME + '_' + locale.toString());
            if (Files.isReadable(path)) {
                try (FileInputStream fis = new FileInputStream(path.toFile())) {
                    return new PropertyResourceBundle(fis);
                } catch (IOException e) {
                    return null;
                }
            }
            return null;
        });
    }
}
