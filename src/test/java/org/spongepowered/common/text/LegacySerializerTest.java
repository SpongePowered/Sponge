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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.spongepowered.common.text.SpongeTexts.COLOR_CHAR;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.lwts.runner.LaunchWrapperTestRunner;

@RunWith(LaunchWrapperTestRunner.class)
public class LegacySerializerTest {

    @Test
    public void testPlainText() {
        assertThat(SpongeTexts.toLegacy(new TextComponentString("test")), is("test"));
    }

    @Test
    public void testTranslatableText() {
        assertThat(SpongeTexts.toLegacy(new TextComponentTranslation("test")), is("test"));
    }

    @Test
    public void testColoredText() {
        ITextComponent component = new TextComponentString("test");
        component.getStyle().setColor(TextFormatting.RED);
        assertThat(SpongeTexts.toLegacy(component), is(COLOR_CHAR + "ctest"));
    }

    @Test
    public void testNestedText() {
        ITextComponent component = new TextComponentString("first");
        component.getStyle().setColor(TextFormatting.RED);

        component.appendSibling(new TextComponentString("second"));

        TextComponentString component2 = new TextComponentString("third");
        component2.getStyle().setColor(TextFormatting.BLUE);
        component.appendSibling(component2);

        assertThat(SpongeTexts.toLegacy(component), is(COLOR_CHAR + "cfirstsecond" + COLOR_CHAR + "9third"));
    }

    @Test
    public void testEmptyTranslatableText() {
        assertThat(SpongeTexts.toLegacy(new TextComponentString("blah").appendSibling(new TextComponentTranslation(""))), is("blah"));
    }

}
