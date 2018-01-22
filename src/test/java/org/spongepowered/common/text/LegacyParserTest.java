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
package org.spongepowered.common.text;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.lwts.runner.LaunchWrapperTestRunner;

@RunWith(LaunchWrapperTestRunner.class)
public class LegacyParserTest {

    private static Text parse(String input) {
        return TextSerializers.FORMATTING_CODE.deserialize(input);
    }

    @Test
    public void testPlain() {
        assertEquals(Text.empty(), parse(""));
        assertEquals(Text.of("&&"), parse("&&"));
        assertEquals(Text.of("Sponge"), parse("Sponge"));
        assertEquals(Text.of("Sponge & Water"), parse("Sponge & Water"));
    }

    @Test
    public void testSimple() {
        assertEquals(Text.builder("Sponge").color(TextColors.GOLD).build(), parse("&6Sponge"));
    }

    @Test
    public void testMany() {
        assertEquals(Text.builder("Hello ").append(Text.builder("Sponge").color(TextColors.YELLOW).build()).build(),
                parse("Hello &eSponge"));
        assertEquals(Text.builder().append(
                Text.builder("Sponge").color(TextColors.YELLOW).style(TextStyles.BOLD).build(),
                Text.builder(" & ").color(TextColors.RESET).build(),
                Text.builder("Water").color(TextColors.AQUA).style(TextStyles.ITALIC).build()).build(),
                parse("&e&lSponge&r & &b&oWater"));
    }

    @Test
    public void testRedundantFormattingCodes() {
        assertEquals(Text.empty(), parse("&c&e&l&f"));
        assertEquals(Text.builder("Hello ").append(Text.builder("Sponge").color(TextColors.YELLOW).build()).build(),
                parse("Hello &a&f&e&eSponge"));
    }

}
