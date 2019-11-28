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
package org.spongepowered.common.mixin.core.util.text;

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
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.util.text.ITextComponentBridge;
import org.spongepowered.common.bridge.util.text.StyleBridge;
import org.spongepowered.common.bridge.util.text.TextFormattingBridge;
import org.spongepowered.common.bridge.util.text.event.ClickEventBridge;
import org.spongepowered.common.bridge.util.text.event.HoverEventBridge;
import org.spongepowered.common.text.ResolvedChatStyle;
import org.spongepowered.common.text.TextComponentIterable;
import org.spongepowered.common.text.format.SpongeTextColor;

import java.util.Iterator;
import java.util.List;

@Mixin(TextComponent.class)
public abstract class TextComponentBaseMixin implements ITextComponentBridge, ITextComponent {

    @Shadow private Style style;
    @Shadow protected List<ITextComponent> siblings;

    protected Text.Builder impl$createBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<ITextComponent> bridge$childrenIterator() {
        return getSiblings().iterator();
    }

    @Override
    public Iterable<ITextComponent> bridge$withChildren() {
        return new TextComponentIterable(this, true);
    }

    @Override
    public String bridge$toPlain() {
        final StringBuilder builder = new StringBuilder();

        for (final ITextComponent component : bridge$withChildren()) {
            builder.append(component.getUnformattedComponentText());
        }

        return builder.toString();
    }

    private StringBuilder getLegacyFormattingBuilder() {
        final StringBuilder builder = new StringBuilder();

        final Style style = getStyle();
        apply(builder, COLOR_CHAR, defaultIfNull(style.getColor(), RESET));
        apply(builder, COLOR_CHAR, BOLD, style.getBold());
        apply(builder, COLOR_CHAR, ITALIC, style.getItalic());
        apply(builder, COLOR_CHAR, UNDERLINE, style.getUnderlined());
        apply(builder, COLOR_CHAR, STRIKETHROUGH, style.getStrikethrough());
        apply(builder, COLOR_CHAR, OBFUSCATED, style.getObfuscated());

        return builder;
    }

    @Override
    public String bridge$getLegacyFormatting() {
        return getLegacyFormattingBuilder().toString();
    }

    @Override
    public String bridge$toLegacy(final char code) {
        final StringBuilder builder = new StringBuilder();

        ResolvedChatStyle current = null;
        Style previous = null;

        for (final ITextComponent component : bridge$withChildren()) {
            final Style newStyle = component.getStyle();
            final ResolvedChatStyle style = resolve(current, previous, newStyle);
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
    public String bridge$toLegacySingle(final char code) {
        return getLegacyFormattingBuilder()
                .append(getUnformattedComponentText())
                .toString();
    }

    private static ResolvedChatStyle resolve(final ResolvedChatStyle current, final Style previous, final Style style) {
        final StyleBridge bridge = (StyleBridge) style;
        if (current != null && bridge.bridge$getParentStyle() == previous) {
            return new ResolvedChatStyle(
                    defaultIfNull(bridge.bridge$getColor(), current.color),
                    firstNonNull(bridge.bridge$getBold(), current.bold),
                    firstNonNull(bridge.bridge$getItalic(), current.italic),
                    firstNonNull(bridge.bridge$getUnderlined(), current.underlined),
                    firstNonNull(bridge.bridge$getStrikethrough(), current.strikethrough),
                    firstNonNull(bridge.bridge$getObfuscated(), current.obfuscated)
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

    private static boolean firstNonNull(final Boolean b1, final boolean b2) {
        return b1 != null ? b1 : b2;
    }

    @SuppressWarnings("ConstantConditions")
    private static void apply(final StringBuilder builder, final char code, final TextFormatting formatting) {
        builder.append(code).append(((TextFormattingBridge) (Object) formatting).bridge$getFormattingCode());
    }

    private static void apply(final StringBuilder builder, final char code, final TextFormatting formatting, final boolean state) {
        if (state) {
            apply(builder, code, formatting);
        }
    }

    @Override
    public Text bridge$toText() {
        final Text.Builder builder = impl$createBuilder();

        final StyleBridge style = (StyleBridge) this.style;
        if (style != null) {
            if (style.bridge$getColor() != null) {
                builder.color(SpongeTextColor.of(style.bridge$getColor()));
            }

            builder.style(new TextStyle(style.bridge$getBold(), style.bridge$getItalic(), style.bridge$getUnderlined(), style.bridge$getStrikethrough(),
                    style.bridge$getObfuscated()));

            if (style.bridge$getClickEvent() != null) {
                builder.onClick(((ClickEventBridge) style.bridge$getClickEvent()).bridge$getHandle());
            }
            if (style.bridge$getHoverEvent() != null) {
                builder.onHover(((HoverEventBridge) style.bridge$getHoverEvent()).bridge$getHandle());
            }
            if (style.bridge$getInsertion() != null) {
                builder.onShiftClick(TextActions.insertText(style.bridge$getInsertion()));
            }
        }

        for (final ITextComponent child : this.siblings) {
            builder.append(((ITextComponentBridge) child).bridge$toText());
        }

        return builder.build();
    }

}
