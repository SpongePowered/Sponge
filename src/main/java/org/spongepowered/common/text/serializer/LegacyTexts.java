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
package org.spongepowered.common.text.serializer;

import com.google.common.collect.Lists;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.common.interfaces.text.IMixinText;
import org.spongepowered.common.text.format.SpongeTextColor;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

public final class LegacyTexts {

    private static final EnumChatFormatting[] formatting = EnumChatFormatting.values();
    private static final String LOOKUP;

    private LegacyTexts() {
    }

    static {
        char[] lookup = new char[formatting.length];

        for (int i = 0; i < formatting.length; i++) {
            lookup[i] = formatting[i].formattingCode;
        }

        LOOKUP = new String(lookup);
    }

    public static int getFormattingCount() {
        return formatting.length;
    }

    public static int findFormat(char format) {
        int pos = LOOKUP.indexOf(format);
        if (pos == -1) {
            pos = LOOKUP.indexOf(Character.toLowerCase(format));
        }

        return pos;
    }

    public static boolean isFormat(char format) {
        return findFormat(format) != -1;
    }

    @Nullable
    public static EnumChatFormatting getFormat(char format) {
        int pos = findFormat(format);
        return pos != -1 ? formatting[pos] : null;
    }

    public static String serialize(Text text, char code) {
        return ((IMixinText) text).toLegacy(code);
    }

    public static Text parse(String input, char code) {
        int next = input.lastIndexOf(code, input.length() - 2);
        if (next == -1) {
            return Text.of(input);
        }

        List<Text> parts = Lists.newArrayList();

        LiteralText.Builder current = null;
        boolean reset = false;

        int pos = input.length();
        do {
            EnumChatFormatting format = getFormat(input.charAt(next + 1));
            if (format != null) {
                int from = next + 2;
                if (from != pos) {
                    if (current != null) {
                        if (reset) {
                            parts.add(current.build());
                            reset = false;
                            current = Text.builder("");
                        } else {
                            current = Text.builder("").append(current.build());
                        }
                    } else {
                        current = Text.builder("");
                    }

                    current.content(input.substring(from, pos));
                } else if (current == null) {
                    current = Text.builder("");
                }

                reset |= applyStyle(current, format);
                pos = next;
            }

            next = input.lastIndexOf(code, next - 1);
        } while (next != -1);

        if (current != null) {
            parts.add(current.build());
        }

        Collections.reverse(parts);
        return Text.builder(pos > 0 ? input.substring(0, pos) : "").append(parts).build();
    }

    private static boolean applyStyle(Text.Builder builder, EnumChatFormatting formatting) {
        switch (formatting) {
            case BOLD:
                builder.style(TextStyles.BOLD);
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

    public static ChatComponentText parseComponent(String input, char code) {
        return parseComponent(new ChatComponentText(input), code);
    }

    public static ChatComponentText parseComponent(ChatComponentText component, char code) {
        final String input = component.text;
        int next = input.lastIndexOf(code, input.length() - 2);
        if (next == -1) {
            return component;
        }

        List<IChatComponent> parts = Lists.newArrayList();

        ChatComponentText current = null;
        boolean reset = false;

        int pos = input.length();
        do {
            EnumChatFormatting format = getFormat(input.charAt(next + 1));
            if (format != null) {
                int from = next + 2;
                if (from != pos) {
                    if (current != null) {
                        if (reset) {
                            parts.add(current);
                            current.getChatStyle().setParentStyle(component.getChatStyle());
                            reset = false;
                            current = new ChatComponentText("");
                        } else {
                            ChatComponentText old = current;
                            current = new ChatComponentText("");
                            current.appendSibling(old);
                        }
                    } else {
                        current = new ChatComponentText("");
                    }

                    current.text = input.substring(from, pos);
                } else if (current == null) {
                    current = new ChatComponentText("");
                }

                reset |= applyStyle(current.getChatStyle(), format);
                pos = next;
            }

            next = input.lastIndexOf(code, next - 1);
        } while (next != -1);

        if (current != null) {
            parts.add(current);
        }

        Collections.reverse(parts);
        component.text = pos > 0 ? input.substring(0, pos) : "";
        component.getSiblings().addAll(parts);
        return component;
    }

    private static boolean applyStyle(ChatStyle style, EnumChatFormatting formatting) {
        switch (formatting) {
            case BOLD:
                style.bold = true;
                break;
            case ITALIC:
                style.italic = true;
                break;
            case UNDERLINE:
                style.underlined = true;
                break;
            case STRIKETHROUGH:
                style.strikethrough = true;
                break;
            case OBFUSCATED:
                style.obfuscated = true;
                break;
            case RESET:
                return true;
            default:
                if (style.color == null) {
                    style.color = formatting;
                }
                return true;
        }

        return false;
    }

    public static String replace(String text, char from, char to) {
        int pos = text.indexOf(from);
        int last = text.length() - 1;
        if (pos == -1 || pos == last) {
            return text;
        }

        char[] result = text.toCharArray();
        for (; pos < last; pos++) {
            if (result[pos] == from && isFormat(result[pos + 1])) {
                result[pos] = to;
            }
        }

        return new String(result);
    }

    public static String strip(String text, char code) {
        return strip(text, code, false, false);
    }

    public static String stripAll(String text, char code) {
        return strip(text, code, true, false);
    }

    public static String stripChars(String text, char code) {
        return strip(text, code, false, true);
    }

    private static String strip(String text, char code, boolean all, boolean keepFormat) {
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
                pos = next += keepFormat ? 1 : 2; // Skip formatting
            } else if (all) {
                pos = next += 1; // Skip code only
            } else {
                next++;
            }

            next = text.indexOf(code, next);
        } while (next != -1 && next < last);

        return result.append(text, pos, text.length()).toString();
    }

}
