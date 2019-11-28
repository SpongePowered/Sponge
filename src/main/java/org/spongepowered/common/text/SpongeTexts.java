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

import com.google.common.collect.Lists;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextParseException;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.bridge.util.text.ITextComponentBridge;
import org.spongepowered.common.bridge.api.text.TextBridge;

import java.util.List;

public final class SpongeTexts {

    public static final char COLOR_CHAR = '\u00A7';

    private SpongeTexts() {
    }

    public static Text[] splitChatMessage(ITextComponent component) {
        if (!(component instanceof TranslationTextComponent)){
            return new Text[] { null, null };
        }
        Text source = null;
        Text body = null;
        for (Object arg : ((TranslationTextComponent) (component)).getFormatArgs()) {
            if (source == null) {
                if (arg instanceof ITextComponent) {
                    source = SpongeTexts.toText((ITextComponent) arg);
                } else {
                    source = Text.of(arg.toString());
                }
            } else {
                Text text;
                if (arg instanceof ITextComponent) {
                    text = SpongeTexts.toText((ITextComponent) arg);
                } else {
                    text = Text.of(arg.toString());
                }
                if (body == null) {
                    body = text;
                } else {
                    body = body.concat(text);
                }
            }
        }
        return new Text[] {source, body};
    }

    public static ITextComponent toComponent(Text text) {
        return ((TextBridge) text).bridge$toComponent();
    }

    public static Text toText(ITextComponent component) {
        return ((ITextComponentBridge) component).bridge$toText();
    }

    public static String toPlain(ITextComponent component) {
        return ((ITextComponentBridge) component).bridge$toPlain();
    }

    @SuppressWarnings("deprecation")
    public static Text fromLegacy(String legacy) {
        return TextSerializers.LEGACY_FORMATTING_CODE.deserialize(legacy);
    }

    @SuppressWarnings("deprecation")
    public static String toLegacy(Text text) {
        return TextSerializers.LEGACY_FORMATTING_CODE.serialize(text);
    }

    public static String toLegacy(ITextComponent component) {
        return ((ITextComponentBridge) component).bridge$toLegacy(COLOR_CHAR);
    }

    public static ITextComponent fixActionBarFormatting(ITextComponent component) {
        if (!component.getSiblings().isEmpty()) {
            List<ITextComponent> children = component.getSiblings();
            for (int i = 0; i < children.size(); i++) {
                children.set(i, fixActionBarFormatting(children.get(i)));
            }
        }

        StringTextComponent result = new StringTextComponent(((ITextComponentBridge) component).bridge$getLegacyFormatting());
        result.appendSibling(component);
        return result;
    }

    public static List<String> asJson(List<Text> list) {
        List<String> json = Lists.newArrayList();
        for (Text line : list) {
            json.add(TextSerializers.JSON.serialize(line));
        }
        return json;
    }

    public static ListNBT asJsonNBT(List<Text> list) {
        final ListNBT legacy = new ListNBT();
        for (Text line : list) {
            legacy.appendTag(new StringNBT(TextSerializers.JSON.serialize(line)));
        }
        return legacy;
    }

    public static List<Text> fromJson(List<String> json) {
        List<Text> list = Lists.newArrayList();
        for (String line : json) {
           list.add(TextSerializers.JSON.deserialize(line));
        }
        return list;
    }

    public static List<Text> fromNbtJson(ListNBT legacy) throws TextParseException {
        List<Text> list = Lists.newArrayList();
        for (int i = 0; i < legacy.tagCount(); i++) {
            list.add(TextSerializers.JSON.deserialize(legacy.getString(i)));
        }
        return list;
    }

    public static List<Text> fromNbtLegacy(ListNBT legacy) {
        List<Text> list = Lists.newArrayList();
        for (int i = 0; i < legacy.tagCount(); i++) {
            list.add(SpongeTexts.fromLegacy(legacy.getString(i)));
        }
        return list;
    }

    public static ListNBT asLegacy(List<Text> list) {
        final ListNBT legacy = new ListNBT();
        for (Text line : list) {
            legacy.appendTag(new StringNBT(toLegacy(line)));
        }
        return legacy;
    }
}
