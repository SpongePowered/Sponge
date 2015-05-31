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

import static org.spongepowered.common.text.SpongeTexts.COLOR_CHAR;
import static org.spongepowered.common.text.SpongeTexts.getDefaultLocale;

import com.google.gson.JsonSyntaxException;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextFactory;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.common.interfaces.text.IMixinChatComponent;
import org.spongepowered.common.interfaces.text.IMixinText;
import org.spongepowered.common.text.xml.TextXmlParser;
import org.spongepowered.common.text.xml.TextXmlPrinter;

import java.util.Locale;

@NonnullByDefault
public class SpongeTextFactory implements TextFactory {

    @Override
    public Text parseJson(String json) throws IllegalArgumentException {
        try {
            return ((IMixinChatComponent) IChatComponent.Serializer.jsonToComponent(json)).toText();
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Failed to parse JSON", e);
        }
    }

    @Override
    public Text parseJsonLenient(String json) throws IllegalArgumentException {
        return parseJson(json); // TODO
    }

    @Override
    public String toPlain(Text text) {
        return toPlain(text, getDefaultLocale());
    }

    @Override
    public String toPlain(Text text, Locale locale) {
        return ((IMixinText) text).toPlain(locale);
    }

    @Override
    public String toJson(Text text) {
        return toJson(text, getDefaultLocale());
    }

    @Override
    public String toJson(Text text, Locale locale) {
        return ((IMixinText) text).toJson(locale);
    }

    @Override
    public Text parseXml(String input) {
        try {
            return TextXmlParser.parse(input);
        } catch (Exception e) { // TODO: Exception handling?
            return Texts.of(input);
        }
    }

    @Override
    public String toXml(Text input, Locale locale) {
        return TextXmlPrinter.toHtml(input, locale);
    }

    @Override
    public char getLegacyChar() {
        return COLOR_CHAR;
    }

    @Override
    public Text.Literal parseLegacyMessage(String text, char code) {
        return LegacyTexts.parse(text, code);
    }

    @Override
    public String stripLegacyCodes(String text, char code) {
        return LegacyTexts.strip(text, code);
    }

    @Override
    public String replaceLegacyCodes(String text, char from, char to) {
        return LegacyTexts.replace(text, from, to);
    }

    @Override
    public String toLegacy(Text text, char code) {
        return toLegacy(text, code, getDefaultLocale());
    }

    @Override
    public String toLegacy(Text text, char code, Locale locale) {
        return ((IMixinText) text).toLegacy(code, locale);
    }


}
