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
package org.spongepowered.common.registry.builtin.stream;

import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.text.format.SpongeTextStyleType;

import java.util.stream.Stream;

public final class TextStyleTypeStreamGenerator {

    private TextStyleTypeStreamGenerator() {
    }

    public static Stream<Tuple<TextStyle.Type, TextFormatting>> stream() {
        return Stream.of(
            Tuple.of(new SpongeTextStyleType(CatalogKey.minecraft("bold"), TextFormatting.BOLD), TextFormatting.BOLD),
            Tuple.of(new SpongeTextStyleType(CatalogKey.minecraft("italic"), TextFormatting.ITALIC), TextFormatting.ITALIC),
            Tuple.of(new SpongeTextStyleType(CatalogKey.minecraft("obfuscated"), TextFormatting.OBFUSCATED), TextFormatting.OBFUSCATED),
            Tuple.of(new SpongeTextStyleType(CatalogKey.minecraft("reset"), TextFormatting.RESET), TextFormatting.RESET),
            Tuple.of(new SpongeTextStyleType(CatalogKey.minecraft("strikethrough"), TextFormatting.STRIKETHROUGH), TextFormatting.STRIKETHROUGH),
            Tuple.of(new SpongeTextStyleType(CatalogKey.minecraft("underline"), TextFormatting.UNDERLINE), TextFormatting.UNDERLINE)
        );
    }
}
