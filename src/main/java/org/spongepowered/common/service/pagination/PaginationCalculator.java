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
package org.spongepowered.common.service.pagination;

import com.flowpowered.math.GenericMath;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.command.CommandMessageFormatting;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextStyles;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Pagination calculator for players.
 */
public class PaginationCalculator {
    private static final String NON_UNICODE_CHARS;
    private static final int[] NON_UNICODE_CHAR_WIDTHS;
    private static final byte[] UNICODE_CHAR_WIDTHS;
    private static final int LINE_WIDTH = 320;

    private final int linesPerPage;

    public PaginationCalculator(int linesPerPage) {
        this.linesPerPage = linesPerPage;
    }

    static {
        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
                .setURL(PaginationCalculator.class.getResource("font-sizes.json"))
                .setPreservesHeader(false).build();
        try {
            ConfigurationNode node = loader.load();
            NON_UNICODE_CHARS = node.getNode("non-unicode").getString();
            List<? extends ConfigurationNode> charWidths = node.getNode("char-widths").getChildrenList();
            int[] nonUnicodeCharWidths = new int[charWidths.size()];
            for (int i = 0; i < nonUnicodeCharWidths.length; ++i) {
                nonUnicodeCharWidths[i] = charWidths.get(i).getInt();
            }
            NON_UNICODE_CHAR_WIDTHS = nonUnicodeCharWidths;


            List<? extends ConfigurationNode> glyphWidths = node.getNode("glyph-widths").getChildrenList();
            byte[] unicodeCharWidths = new byte[glyphWidths.size()];
            for (int i = 0; i < nonUnicodeCharWidths.length; ++i) {
                unicodeCharWidths[i] = (byte) glyphWidths.get(i).getInt();
            }
            UNICODE_CHAR_WIDTHS = unicodeCharWidths;
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public int getLinesPerPage(MessageReceiver source) {
        return this.linesPerPage;
    }

    public int getLines(MessageReceiver source, Text text) {
        return (int) Math.ceil((double) getLength(source, text) / LINE_WIDTH);
    }

    private double getWidth(int codePoint, boolean isBold) {
        int nonUnicodeIdx = NON_UNICODE_CHARS.indexOf(codePoint);
        double width;
        if (nonUnicodeIdx != -1) {
            width = NON_UNICODE_CHAR_WIDTHS[nonUnicodeIdx];
            if (isBold) {
                width += 1;
            }
        } else {
            // MC unicode -- what does this even do? but it's client-only so we can't use it directly :/
            int j = UNICODE_CHAR_WIDTHS[codePoint] >>> 4;
            int k = UNICODE_CHAR_WIDTHS[codePoint] & 15;

            if (k > 7) {
                k = 15;
                j = 0;
            }
            width = ((k + 1) - j) / 2 + 1;
            if (isBold) {
                width += 0.5;
            }
        }
        return width;
    }

    private int getLength(MessageReceiver source, Text text) {
        double columnCount = 0d;
        for (Text child : text.withChildren()) {
            final String txt;
            if (child instanceof LiteralText) {
                txt = ((LiteralText) child).getContent();
            } else if (child instanceof TranslatableText) {
                txt = child.toPlain();
            } else {
                continue;
            }
            boolean isBold = child.getStyle().contains(TextStyles.BOLD);
            for (int i = 0; i < txt.length(); ++i) {
                columnCount += getWidth(txt.codePointAt(i), isBold);
            }
        }
        return (int) Math.ceil(columnCount);

    }

    public Text center(MessageReceiver source, Text text, Text padding) {
        int length = getLength(source, text);
        if (length >= LINE_WIDTH) {
            return text;
        }
        int paddingLength = getLength(source, padding.toBuilder().style(text.getStyle()).build());
        double paddingNecessary = LINE_WIDTH - length;

        Text.Builder build =  Text.builder();
        if (length == 0) {
            build.append(Collections.nCopies(GenericMath.floor((double) LINE_WIDTH / paddingLength), padding));
        } else {
            paddingNecessary -= getWidth(' ', text.getStyle().contains(TextStyles.BOLD)) * 2;
            int paddingCount = GenericMath.floor(paddingNecessary / paddingLength);
            int beforePadding = GenericMath.floor(paddingCount / 2.0);
            int afterPadding = (int) Math.ceil(paddingCount / 2.0);
            if (beforePadding > 0) {
                if (beforePadding > 1) {
                    build.append(Collections.nCopies(beforePadding, padding));
                }
                build.append(CommandMessageFormatting.SPACE_TEXT);
            }
            build.append(text);
            if (afterPadding > 0) {
                build.append(CommandMessageFormatting.SPACE_TEXT);
                if (afterPadding > 1) {
                    build.append(Collections.nCopies(afterPadding, padding));
                }
            }
        }

        build.color(text.getColor())
            .style(text.getStyle());
        return build.build();
    }
}
