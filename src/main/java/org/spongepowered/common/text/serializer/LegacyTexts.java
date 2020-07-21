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

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.common.accessor.util.text.StringTextComponentAccessor;
import org.spongepowered.common.accessor.util.text.StyleAccessor;
import org.spongepowered.common.accessor.util.text.TextFormattingAccessor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public final class LegacyTexts {

    private static final int FORMATTING_CODE_LENGTH = 2;

    private static final TextFormatting[] formatting = TextFormatting.values();
    private static final String LOOKUP;

    private LegacyTexts() {
    }

    static {
        final char[] lookup = new char[formatting.length];

        for (int i = 0; i < formatting.length; i++) {
            lookup[i] = ((TextFormattingAccessor) (Object) formatting[i]).accessor$getFormattingCode();
        }

        LOOKUP = new String(lookup);
    }

    public static int getFormattingCount() {
        return formatting.length;
    }

    public static int findFormat(final char format) {
        int pos = LOOKUP.indexOf(format);
        if (pos == -1) {
            pos = LOOKUP.indexOf(Character.toLowerCase(format));
        }

        return pos;
    }

    public static boolean isFormat(final char format) {
        return findFormat(format) != -1;
    }

    @Nullable
    public static TextFormatting parseFormat(final char format) {
        final int pos = findFormat(format);
        return pos != -1 ? formatting[pos] : null;
    }

    @SuppressWarnings("ConstantConditions")
    public static StringTextComponent parseComponent(final StringTextComponent component, final char code) {
        String text = component.getText();
        int next = text.lastIndexOf(code, text.length() - 2);

        List<ITextComponent> parsed = null;
        if (next >= 0) {
            parsed = new ArrayList<>();

            StringTextComponent current = null;
            boolean reset = false;

            int pos = text.length();
            do {
                final TextFormatting format = parseFormat(text.charAt(next + 1));
                if (format != null) {
                    final int from = next + 2;
                    if (from != pos) {
                        if (current != null) {
                            if (reset) {
                                parsed.add(current);
                                setParentIfNotSame(current.getStyle(), component.getStyle());
                                reset = false;
                                current = new StringTextComponent("");
                            } else {
                                final StringTextComponent old = current;
                                current = new StringTextComponent("");
                                current.appendSibling(old);
                            }
                        } else {
                            current = new StringTextComponent("");
                        }

                        ((StringTextComponentAccessor) current).accessor$setText(text.substring(from, pos));
                    } else if (current == null) {
                        current = new StringTextComponent("");
                    }

                    reset |= applyStyle(current.getStyle(), format);
                    pos = next;
                }

                next = text.lastIndexOf(code, next - 1);
            } while (next != -1);

            if (current != null) {
                parsed.add(current);
                setParentIfNotSame(current.getStyle(), component.getStyle());
            }

            Collections.reverse(parsed);
            text = pos > 0 ? text.substring(0, pos) : "";
            if (component.getSiblings().isEmpty()) {
                final StringTextComponent newComponent = new StringTextComponent(text);
                newComponent.getSiblings().addAll(parsed);
                newComponent.setStyle(component.getStyle());
                return newComponent;
            }
        } else if (component.getSiblings().isEmpty()) {
            return component;
        }

        final StringTextComponent newComponent = new StringTextComponent(text);
        if (parsed != null) {
            newComponent.getSiblings().addAll(parsed);
        }

        newComponent.setStyle(component.getStyle());
        for (ITextComponent child : component.getSiblings()) {
            if (child instanceof StringTextComponent) {
                child = parseComponent((StringTextComponent) child, code);
            } else {
                child = child.shallowCopy();
            }
            newComponent.appendSibling(child);
        }

        return newComponent;
    }

    private static void setParentIfNotSame(final Style child, final Style parent) {
        if (parent != child) {
            child.setParentStyle(parent);
        }
    }

    private static boolean applyStyle(final Style style, final TextFormatting formatting) {
        switch (formatting) {
            case BOLD:
                ((StyleAccessor) style).accessor$setBold(true);
                break;
            case ITALIC:
                ((StyleAccessor) style).accessor$setItalic(true);
                break;
            case UNDERLINE:
                ((StyleAccessor) style).accessor$setUnderlined(true);
                break;
            case STRIKETHROUGH:
                ((StyleAccessor) style).accessor$setStrikethrough(true);
                break;
            case OBFUSCATED:
                ((StyleAccessor) style).accessor$setObfuscated(true);
                break;
            default:
                if (((StyleAccessor) style).accessor$getColor() == null) {
                    ((StyleAccessor) style).accessor$setColor(formatting);
                }
                return true;
        }

        return false;
    }

    public static String replace(final String text, final char from, final char to) {
        int pos = text.indexOf(from);
        final int last = text.length() - 1;
        if (pos == -1 || pos == last) {
            return text;
        }

        final char[] result = text.toCharArray();
        for (; pos < last; pos++) {
            if (result[pos] == from && isFormat(result[pos + 1])) {
                result[pos] = to;
            }
        }

        return new String(result);
    }

}
