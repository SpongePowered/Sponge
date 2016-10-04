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
import com.google.common.annotations.VisibleForTesting;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.loader.HeaderMode;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.common.interfaces.text.IMixinTextComponent;
import org.spongepowered.common.text.SpongeTexts;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.PrimitiveIterator;

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
                .setHeaderMode(HeaderMode.NONE)
                .build();
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
            for (int i = 0; i < unicodeCharWidths.length; ++i) {
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

    /**
     *
     * @param text
     * @return the number of lines that this text flows into.
     */
    public int getLines(Text text) {
        //TODO: this needs fixing as well.
        return (int) Math.ceil((double) this.getWidth(text) / LINE_WIDTH);
    }

    @VisibleForTesting
    int getWidth(int codePoint, boolean isBold) {
        int nonUnicodeIdx = NON_UNICODE_CHARS.indexOf(codePoint);
        int width;
        if (codePoint == 32) {
            width = 4;
        }
        else if (codePoint > 0 && nonUnicodeIdx != -1) {
            width = NON_UNICODE_CHAR_WIDTHS[nonUnicodeIdx];
        } else if (UNICODE_CHAR_WIDTHS[codePoint] != 0) {
            //from 1.9 & 255 to avoid strange signed int math ruining things.
            //https://bugs.mojang.com/browse/MC-7181
            final int temp = UNICODE_CHAR_WIDTHS[codePoint] & 255;
            // Split into high and low nibbles.
            //bit digits
            //87654321 >>> 4 = 00008765
            int startColumn = temp >>> 4;
            //87654321 & 00001111 = 00004321
            int endColumn = temp & 15;

            width = (endColumn + 1) - startColumn;
            //Why does this scaling happen?
            //I believe it makes unicode fonts skinnier to better match the character widths of the default Minecraft
            // font however there is a int math vs float math bug in the Minecraft FontRenderer.
            //The float math is adjusted for rendering, they attempt to do the same thing for calculating string widths
            //using integer math, this has potential rounding errors, but we should copy it and use ints as well.
            width = (width / 2)+1;
        } else {
            width = 0;
        }
        //if bolded width gets 1 added.
        if(isBold && width > 0) width = width + 1;

        return width;
    }

    /**
     * compute the width of a given text
     * @param text
     * @return the number of character pixels/columns the line takes up
     */
    @VisibleForTesting
    int getWidth(Text text) {
        ITextComponent component = SpongeTexts.toComponent(text);
        Iterable<ITextComponent> children = ((IMixinTextComponent) component).withChildren();
        int total = 0;

        for(ITextComponent child : children) {
            PrimitiveIterator.OfInt i_it;
            if(child instanceof TextComponentString || child instanceof TextComponentTranslation) {
                i_it = child.getUnformattedComponentText().codePoints().iterator();
            } else {
                continue;
            }

            boolean bold = child
                    .getStyle()
                    .getBold();

            Integer cp;
            while(i_it.hasNext()){
                cp = i_it.next();
                int width = getWidth(cp, bold);
                total += width;
            }
        }

        return total;
    }

    /**
     * Center a text in the middle of the chat box
     * @param text or 0 width text for no heading
     * @param padding a >1 width padding character
     * @return the centered text, or if too big, the original text.
     */
    //TODO: Probably should completely rewrite this to not compute padding, but loop until the padding is done, unless
    //we can get accurate computation of padding ahead of time.
    public Text center(Text text, Text padding) {
        int inputLength = this.getWidth(text);
        //Minecraft breaks lines when the next character would be > then LINE_WIDTH, this seems most graceful way to fail
        if (inputLength >= LINE_WIDTH) {
            return text;
        }
        Text styledSpace = this.withStyle(LiteralText.of(" "), text);
        Text textWithSpaces = this.addSpaces(styledSpace, text);

        //Minecraft breaks lines when the next character would be > then LINE_WIDTH
        boolean addSpaces = getWidth(textWithSpaces) <= LINE_WIDTH;

        //TODO: suspect, why are we changing the style of the padding, they may want different styles on the padding.
        int paddingLength = this.getWidth(this.withStyle(padding, text));
        final Text.Builder output =  Text.builder();

        //Using 0 width unicode symbols as padding throws us into an unending loop, replace them with the default padding
        if(paddingLength < 1) {
            Text defaultPadding = Text.of("=");
            padding = defaultPadding;
            paddingLength = this.getWidth(this.withStyle(defaultPadding, text));
        }

        Text styledPadding = this.withStyle(padding, text);

        //if we only need padding
        if (inputLength == 0) {
            this.addPadding(padding, output, GenericMath.floor((double) LINE_WIDTH / paddingLength));
        } else {
            if(addSpaces) {
                text = textWithSpaces;
                inputLength = this.getWidth(textWithSpaces);
            }

            int paddingNecessary = LINE_WIDTH - inputLength;

            int paddingCount = GenericMath.floor(paddingNecessary / paddingLength);
            //pick a halfway point
            int beforePadding = GenericMath.floor(paddingCount / 2.0);
            //Do not use ceil, this prevents floating point errors.
            int afterPadding = paddingCount-beforePadding;

            this.addPadding(styledPadding, output, beforePadding);
            output.append(text);
            this.addPadding(styledPadding, output, afterPadding);
        }

        return this.finalizeBuilder(text, output);
    }

    private Text withStyle(Text text, Text styled) {
        return text.toBuilder().color(styled.getColor()).style(styled.getStyle()).build();
    }

    private Text finalizeBuilder(Text text, Text.Builder build) {
        build.color(text.getColor())
            .style(text.getStyle());
        return build.build();
    }

    private Text addSpaces(Text spaces, Text text) {
        Text.Builder build = Text.builder().color(text.getColor()).style(text.getStyle());
        build.append(spaces);
        build.append(text);
        build.append(spaces);
        return build.build();
    }

    private void addPadding(Text padding, Text.Builder build, int count) {
        if (count > 0) {
            build.append(Collections.nCopies(count, padding));
        }
    }
}
