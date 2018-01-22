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
package org.spongepowered.common.mixin.core.text;

import static net.minecraft.util.text.TextFormatting.BOLD;
import static net.minecraft.util.text.TextFormatting.ITALIC;
import static net.minecraft.util.text.TextFormatting.OBFUSCATED;
import static net.minecraft.util.text.TextFormatting.RESET;
import static net.minecraft.util.text.TextFormatting.STRIKETHROUGH;
import static net.minecraft.util.text.TextFormatting.UNDERLINE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.spongepowered.common.text.SpongeTexts.COLOR_CHAR;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.text.IMixinTextComponent;
import org.spongepowered.common.interfaces.text.IMixinClickEvent;
import org.spongepowered.common.interfaces.text.IMixinHoverEvent;
import org.spongepowered.common.text.ResolvedChatStyle;
import org.spongepowered.common.text.TextComponentIterable;
import org.spongepowered.common.text.format.SpongeTextColor;
import org.spongepowered.common.text.format.TextStyleImpl;

import java.util.Iterator;
import java.util.List;

@Mixin(TextComponentBase.class)
public abstract class MixinTextComponentBase implements IMixinTextComponent {

    @Shadow private Style style;
    @Shadow protected List<ITextComponent> siblings;

    protected Text.Builder createBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<ITextComponent> childrenIterator() {
        return getSiblings().iterator();
    }

    @Override
    public Iterable<ITextComponent> withChildren() {
        return new TextComponentIterable(this, true);
    }

    @Override
    public String toPlain() {
        StringBuilder builder = new StringBuilder();

        for (ITextComponent component : withChildren()) {
            builder.append(component.getUnformattedComponentText());
        }

        return builder.toString();
    }

    private StringBuilder getLegacyFormattingBuilder() {
        StringBuilder builder = new StringBuilder();

        Style style = getStyle();
        apply(builder, COLOR_CHAR, defaultIfNull(style.getColor(), RESET));
        apply(builder, COLOR_CHAR, BOLD, style.getBold());
        apply(builder, COLOR_CHAR, ITALIC, style.getItalic());
        apply(builder, COLOR_CHAR, UNDERLINE, style.getUnderlined());
        apply(builder, COLOR_CHAR, STRIKETHROUGH, style.getStrikethrough());
        apply(builder, COLOR_CHAR, OBFUSCATED, style.getObfuscated());

        return builder;
    }

    @Override
    public String getLegacyFormatting() {
        return getLegacyFormattingBuilder().toString();
    }

    @Override
    public String toLegacy(char code) {
        StringBuilder builder = new StringBuilder();

        ResolvedChatStyle current = null;
        Style previous = null;

        for (ITextComponent component : withChildren()) {
            Style newStyle = component.getStyle();
            ResolvedChatStyle style = resolve(current, previous, newStyle);
            previous = newStyle;

            if (current == null
                    || (current.color != style.color)
                    || (current.bold && !style.bold)
                    || (current.italic && !style.italic)
                    || (current.underlined && !style.underlined)
                    || (current.strikethrough && !style.strikethrough)
                    || (current.obfuscated && !style.obfuscated)) {

                if (style.color != null) {
                    apply(builder, code, style.color);
                } else if (current != null) {
                    apply(builder, code, RESET);
                }

                apply(builder, code, BOLD, style.bold);
                apply(builder, code, ITALIC, style.italic);
                apply(builder, code, UNDERLINE, style.underlined);
                apply(builder, code, STRIKETHROUGH, style.strikethrough);
                apply(builder, code, OBFUSCATED, style.obfuscated);
            } else {
                apply(builder, code, BOLD, current.bold != style.bold);
                apply(builder, code, ITALIC, current.italic != style.italic);
                apply(builder, code, UNDERLINE, current.underlined != style.underlined);
                apply(builder, code, STRIKETHROUGH, current.strikethrough != style.strikethrough);
                apply(builder, code, OBFUSCATED, current.obfuscated != style.obfuscated);
            }

            current = style;
            builder.append(component.getUnformattedComponentText());
        }

        return builder.toString();
    }

    @Override
    public String toLegacySingle(char code) {
        return getLegacyFormattingBuilder()
                .append(getUnformattedComponentText())
                .toString();
    }

    private static ResolvedChatStyle resolve(ResolvedChatStyle current, Style previous, Style style) {
        if (current != null && style.parentStyle == previous) {
            return new ResolvedChatStyle(
                    defaultIfNull(style.color, current.color),
                    firstNonNull(style.bold, current.bold),
                    firstNonNull(style.italic, current.italic),
                    firstNonNull(style.underlined, current.underlined),
                    firstNonNull(style.strikethrough, current.strikethrough),
                    firstNonNull(style.obfuscated, current.obfuscated)
            );
        }
        return new ResolvedChatStyle(
                style.getColor(),
                style.getBold(),
                style.getItalic(),
                style.getUnderlined(),
                style.getStrikethrough(),
                style.getObfuscated()
        );
    }

    private static boolean firstNonNull(Boolean b1, boolean b2) {
        return b1 != null ? b1 : b2;
    }

    private static void apply(StringBuilder builder, char code, TextFormatting formatting) {
        builder.append(code).append(formatting.formattingCode);
    }

    private static void apply(StringBuilder builder, char code, TextFormatting formatting, boolean state) {
        if (state) {
            apply(builder, code, formatting);
        }
    }

    @Override
    public Text toText() {
        Text.Builder builder = createBuilder();

        if (this.style != null) {
            if (this.style.color != null) {
                builder.color(SpongeTextColor.of(this.style.color));
            }

            builder.style(new TextStyleImpl(this.style.bold, this.style.italic, this.style.underlined, this.style.strikethrough, this.style.obfuscated));

            if (this.style.clickEvent != null) {
                builder.onClick(((IMixinClickEvent) this.style.clickEvent).getHandle());
            }
            if (this.style.hoverEvent != null) {
                builder.onHover(((IMixinHoverEvent) this.style.hoverEvent).getHandle());
            }
            if (this.style.insertion != null) {
                builder.onShiftClick(TextActions.insertText(this.style.insertion));
            }
        }

        for (ITextComponent child : this.siblings) {
            builder.append(((IMixinTextComponent) child).toText());
        }

        return builder.build();
    }

}
