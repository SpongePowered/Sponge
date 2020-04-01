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
package org.spongepowered.common.mixin.invalid.core.util.text;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.util.text.TextComponentBridge;
import org.spongepowered.common.bridge.util.text.event.ClickEventBridge;
import org.spongepowered.common.bridge.util.text.event.HoverEventBridge;
import org.spongepowered.common.text.ResolvedChatStyle;

@Mixin(TextComponent.class)
public abstract class TextComponentMixin implements TextComponentBridge, ITextComponent {


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

    @SuppressWarnings("ConstantConditions")
    private static void apply(final StringBuilder builder, final char code, final TextFormatting formatting) {
        builder.append(code).append(((TextFormattingBridge) (Object) formatting).bridge$getFormattingCode());
    }


    @Override
    public Text bridge$toText() {
        final Text.Builder builder = this.impl$createBuilder();

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
            builder.append(((TextComponentBridge) child).bridge$toText());
        }

        return builder.build();
    }

}
