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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.common.text.format.SpongeTextColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

public final class LegacyTexts {

    private static final TextFormatting[] formatting = TextFormatting.values();
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
    public static TextFormatting getFormat(char format) {
        int pos = findFormat(format);
        return pos != -1 ? formatting[pos] : null;
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
            TextFormatting format = getFormat(input.charAt(next + 1));
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

    private static boolean applyStyle(Text.Builder builder, TextFormatting formatting) {
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
            default:
                if (builder.getColor() == TextColors.NONE) {
                    builder.color(SpongeTextColor.of(formatting));
                }
                return true;
        }

        return false;
    }

    public static TextComponentString parseComponent(TextComponentString component, char code) {
        String text = component.getText();
        int next = text.lastIndexOf(code, text.length() - 2);

        List<ITextComponent> parsed = null;
        if (next >= 0) {
            parsed = new ArrayList<>();

            TextComponentString current = null;
            boolean reset = false;

            int pos = text.length();
            do {
                TextFormatting format = getFormat(text.charAt(next + 1));
                if (format != null) {
                    int from = next + 2;
                    if (from != pos) {
                        if (current != null) {
                            if (reset) {
                                parsed.add(current);
                                current.getStyle().setParentStyle(component.getStyle());
                                reset = false;
                                current = new TextComponentString("");
                            } else {
                                TextComponentString old = current;
                                current = new TextComponentString("");
                                current.appendSibling(old);
                            }
                        } else {
                            current = new TextComponentString("");
                        }

                        current.text = text.substring(from, pos);
                    } else if (current == null) {
                        current = new TextComponentString("");
                    }

                    reset |= applyStyle(current.getStyle(), format);
                    pos = next;
                }

                next = text.lastIndexOf(code, next - 1);
            } while (next != -1);

            if (current != null) {
                parsed.add(current);
                current.getStyle().setParentStyle(component.getStyle());
            }

            Collections.reverse(parsed);
            text = pos > 0 ? text.substring(0, pos) : "";
            if (component.getSiblings().isEmpty()) {
                TextComponentString newComponent = new TextComponentString(text);
                newComponent.getSiblings().addAll(parsed);
                newComponent.setStyle(component.getStyle());
                return newComponent;
            }
        } else if (component.getSiblings().isEmpty()) {
            return component;
        }

        TextComponentString newComponent = new TextComponentString(text);
        if (parsed != null) {
            newComponent.getSiblings().addAll(parsed);
        }

        newComponent.setStyle(component.getStyle());
        for (ITextComponent child : component.getSiblings()) {
            if (child instanceof TextComponentString) {
                child = parseComponent((TextComponentString) child, code);
            } else {
                child = child.createCopy();
            }
            newComponent.appendSibling(child);
        }

        return newComponent;
    }

    private static boolean applyStyle(Style style, TextFormatting formatting) {
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
