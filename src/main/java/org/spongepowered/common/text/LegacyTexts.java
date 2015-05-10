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
import net.minecraft.util.EnumChatFormatting;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.common.text.format.SpongeTextColor;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

public final class LegacyTexts {

    private LegacyTexts() {
    }

    private static final EnumChatFormatting[] formatting = EnumChatFormatting.values();
    private static final String LOOKUP;

    static {
        char[] lookup = new char[formatting.length];

        for (int i = 0; i < formatting.length; i++) {
            lookup[i] = formatting[i].formattingCode;
        }

        LOOKUP = new String(lookup);
    }

    private static int findFormat(char format) {
        int pos = LOOKUP.indexOf(format);
        if (pos == -1) {
            pos = LOOKUP.indexOf(Character.toLowerCase(format));
        }

        return pos;
    }

    private static boolean isFormat(char format) {
        return findFormat(format) != -1;
    }

    @Nullable
    private static EnumChatFormatting getFormat(char format) {
        int pos = findFormat(format);
        return pos != -1 ? formatting[pos] : null;
    }

    public static String replace(String text, char from, char to) {
        int pos = text.indexOf(from);
        int last = text.length() - 1;
        if (pos == -1 || pos == last) {
            return text;
        }

        char[] result = text.toCharArray();
        for (; pos < last; pos++) {
            if (isFormat(result[pos + 1])) {
                result[pos] = to;
            }
        }

        return new String(result);
    }

    public static String strip(String text, char code) {
        return strip(text, code, false);
    }

    public static String strip(String text, char code, boolean all) {
        int next = text.indexOf(code);
        int last = text.length() - 1;
        if (next == -1 || next == last) {
            return text;
        }

        StringBuilder result = new StringBuilder(text.length());

        int pos = 0;
        do {
            if (pos != next) {
                result.append(text, pos, next);
            }

            pos = next;

            if (isFormat(text.charAt(next + 1))) {
                pos = next += 2; // Skip formatting
            } else if (all) {
                pos = next += 1; // Skip code only
            } else {
                next++;
            }

            next = text.indexOf(code, next);
        } while (next != -1 && next < last);

        return result.append(text, pos, text.length()).toString();
    }

    public static Text.Literal parse(String text, char code) {
        int next = text.lastIndexOf(code, text.length() - 2);
        if (next == -1) {
            return Texts.of(text);
        }

        List<Text> parts = Lists.newArrayList();

        TextBuilder.Literal current = null;
        boolean reset = false;

        int pos = text.length();
        do {
            EnumChatFormatting format = getFormat(text.charAt(next + 1));
            if (format != null) {
                int from = next + 2;
                if (from != pos) {
                    if (current != null) {
                        if (reset) {
                            parts.add(current.build());
                            reset = false;
                            current = Texts.builder("");
                        } else {
                            current = Texts.builder("").append(current.build());
                        }
                    } else {
                        current = Texts.builder("");
                    }

                    current.content(text.substring(from, pos));
                }

                if (current != null) {
                    reset |= applyStyle(current, format);
                }
            }

            pos = next;
            next = text.lastIndexOf(code, next - 1);
        } while (next != -1);

        if (current != null) {
            parts.add(current.build());
        }

        Collections.reverse(parts);
        return Texts.builder("").append(parts).build();
    }

    private static boolean applyStyle(TextBuilder builder, EnumChatFormatting formatting) {
        switch (formatting) {
            case BOLD:
                builder.style(TextStyles.BOLD).style(TextStyles.BOLD);
                break;
            case ITALIC:
                builder.style(TextStyles.ITALIC);
                break;
            case UNDERLINE:
                builder.style(TextStyles.UNDERLINE);
                break;
            case STRIKETHROUGH:
                builder.style(TextStyles.STRIKETHROUGH);
                break;
            case OBFUSCATED:
                builder.style(TextStyles.OBFUSCATED);
                break;
            case RESET:
                return true;
            default:
                if (builder.getColor() == TextColors.NONE) {
                    builder.color(SpongeTextColor.of(formatting));
                }
                return true;
        }

        return false;
    }

}
