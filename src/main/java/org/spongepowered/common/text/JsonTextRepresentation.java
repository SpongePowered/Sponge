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

import static org.spongepowered.common.util.SpongeCommonTranslationHelper.t;

import com.google.gson.JsonSyntaxException;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentation;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.TextMessageException;
import org.spongepowered.common.interfaces.text.IMixinChatComponent;
import org.spongepowered.common.interfaces.text.IMixinText;

import java.util.Locale;

/**
 * TextSerializer implementation for the json format.
 */
public class JsonTextRepresentation implements TextRepresentation {
    public static final JsonTextRepresentation INSTANCE = new JsonTextRepresentation();

    private JsonTextRepresentation() {}

    @Override
    public String to(Text text) {
        return to(text, SpongeTexts.getDefaultLocale());
    }

    @Override
    public String to(Text text, Locale locale) {
        return ((IMixinText) text).toJson(locale);
    }

    @Override
    public Text from(String input) throws TextMessageException {
        try {
            return ((IMixinChatComponent) IChatComponent.Serializer.jsonToComponent(input)).toText();
        } catch (JsonSyntaxException e) {
            throw new TextMessageException(t("Failed to parse JSON"), e);
        }
    }

    @Override
    public Text fromUnchecked(String input) {
        try {
            return from(input);
        } catch (TextMessageException e) {
            return Texts.of(input);
        }
    }
}
