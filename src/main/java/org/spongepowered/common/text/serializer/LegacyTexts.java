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
import org.spongepowered.common.bridge.util.text.TextFormattingBridge;
import org.spongepowered.common.mixin.core.util.text.StyleAccessor;
import org.spongepowered.common.mixin.core.util.text.TextComponentStringAccessor;
import org.spongepowered.common.text.format.SpongeTextColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

public final class LegacyTexts {

    private static final int FORMATTING_CODE_LENGTH = 2;

    private static final TextFormatting[] formatting = TextFormatting.values();
    private static final String LOOKUP;

    private LegacyTexts() {
    }

    static {
        final char[] lookup = new char[formatting.length];

        for (int i = 0; i < formatting.length; i++) {
            lookup[i] = ((TextFormattingBridge) (Object) formatting[i]).bridge$getFormattingCode();
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

    /**
     * This method parses an input string with formatting codes into a
     * {@link Text} object.
     *
     * <p>This implementation parses the input string in reverse direction
     * to avoid recursion. It returns a {@link Text} which is equivalent
     * to the input string when rendered on the client.</p>
     *
     * <p>Note: The implementation does not attempt to preserve redundant
     * formatting codes (e.g. two consecutive color codes). Only relevant
     * formatting codes are represented in the output.</p>
     *
     * @param input The input string
     * @param code The formatting sign (e.g. {@code &})
     * @return The parsed text
     */
    public static Text parse(final String input, final char code) {
        int pos = input.length();
        if (pos < FORMATTING_CODE_LENGTH) {
            // Not enough characters to form a formatting code => plain text
            return Text.of(input);
        }

        // Find the first (potential) formatting code
        int next = input.lastIndexOf(code, pos - FORMATTING_CODE_LENGTH);
        if (next == -1) {
            // No potential formatting code found => plain text
            return Text.of(input);
        }

        LiteralText.Builder current = null;
        boolean reset = false;
        final List<Text> parts = Lists.newArrayList();

        do {
            // Parse the formatting code
            final TextFormatting format = parseFormat(input.charAt(next + 1));
            if (format != null) {
                final int from = next + FORMATTING_CODE_LENGTH;
                if (from != pos) {
                    // The plain text between the current and last formatting code
                    final String content = input.substring(from, pos);

                    if (current != null) {
                        if (reset) {
                            // Color codes reset the text style so we avoid inheritance
                            // by adding directly to the root text
                            parts.add(current.build());
                            reset = false;
                            current = Text.builder(content);
                        } else {
                            // Inherit color/style
                            current = Text.builder(content).append(current.build());
                        }
                    } else {
                        current = Text.builder(content);
                    }
                }

                // current == null => style does not apply to any content
                if (current != null) {
                    reset |= applyStyle(current, format);
                }

                // Mark the current position
                pos = next;
            }

            // Search for next formatting code
            next = input.lastIndexOf(code, next - 1);
        } while (next != -1);

        if (current == null) {
            // No formatted text found
            if (pos == 0) {
                // Text contains only (redundant) formatting codes => empty text
                return Text.EMPTY;
            } else {
                // No valid formatting code found => plain text
                return Text.of(input);
            }
        }

        // Return simple text if there is only one text style in the input string
        if (pos == 0 && parts.isEmpty()) {
            return current.build();
        }

        // Build the resulting text
        parts.add(current.build());
        Collections.reverse(parts);
        return Text.builder(pos > 0 ? input.substring(0, pos) : "").append(parts).build();
    }

    private static boolean applyStyle(final Text.Builder builder, final TextFormatting formatting) {
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

    @SuppressWarnings("ConstantConditions")
    public static TextComponentString parseComponent(final TextComponentString component, final char code) {
        String text = component.func_150265_g();
        int next = text.lastIndexOf(code, text.length() - 2);

        List<ITextComponent> parsed = null;
        if (next >= 0) {
            parsed = new ArrayList<>();

            TextComponentString current = null;
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
                                current.func_150256_b().func_150221_a(component.func_150256_b());
                                reset = false;
                                current = new TextComponentString("");
                            } else {
                                final TextComponentString old = current;
                                current = new TextComponentString("");
                                current.func_150257_a(old);
                            }
                        } else {
                            current = new TextComponentString("");
                        }

                        ((TextComponentStringAccessor) current).accessor$setText(text.substring(from, pos));
                    } else if (current == null) {
                        current = new TextComponentString("");
                    }

                    reset |= applyStyle(current.func_150256_b(), format);
                    pos = next;
                }

                next = text.lastIndexOf(code, next - 1);
            } while (next != -1);

            if (current != null) {
                parsed.add(current);
                current.func_150256_b().func_150221_a(component.func_150256_b());
            }

            Collections.reverse(parsed);
            text = pos > 0 ? text.substring(0, pos) : "";
            if (component.func_150253_a().isEmpty()) {
                final TextComponentString newComponent = new TextComponentString(text);
                newComponent.func_150253_a().addAll(parsed);
                newComponent.func_150255_a(component.func_150256_b());
                return newComponent;
            }
        } else if (component.func_150253_a().isEmpty()) {
            return component;
        }

        final TextComponentString newComponent = new TextComponentString(text);
        if (parsed != null) {
            newComponent.func_150253_a().addAll(parsed);
        }

        newComponent.func_150255_a(component.func_150256_b());
        for (ITextComponent child : component.func_150253_a()) {
            if (child instanceof TextComponentString) {
                child = parseComponent((TextComponentString) child, code);
            } else {
                child = child.func_150259_f();
            }
            newComponent.func_150257_a(child);
        }

        return newComponent;
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

    public static String strip(final String text, final char code) {
        return strip(text, code, false, false);
    }

    public static String stripAll(final String text, final char code) {
        return strip(text, code, true, false);
    }

    public static String stripChars(final String text, final char code) {
        return strip(text, code, false, true);
    }

    private static String strip(final String text, final char code, final boolean all, final boolean keepFormat) {
        int next = text.indexOf(code);
        final int last = text.length() - 1;
        if (next == -1 || next == last) {
            return text;
        }

        final StringBuilder result = new StringBuilder(text.length());

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
