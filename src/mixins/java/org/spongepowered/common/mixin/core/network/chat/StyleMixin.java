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
package org.spongepowered.common.mixin.core.network.chat;

import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.accessor.network.chat.StyleAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.network.chat.StyleBridge;

@Mixin(Style.class)
public class StyleMixin implements StyleBridge {
    private net.kyori.adventure.text.format.Style bridge$adventure;

    @Override
    @SuppressWarnings("ConstantConditions")
    public net.kyori.adventure.text.format.Style bridge$asAdventure() {
        if (this.bridge$adventure == null) {
            final net.kyori.adventure.text.format.Style.Builder builder = net.kyori.adventure.text.format.Style.style();
            final Style $this = (Style) (Object) this;
            final StyleAccessor $access = (StyleAccessor) this;
            // font
            builder.font(SpongeAdventure.asAdventure($access.accessor$font()));
            // color
            builder.color(SpongeAdventure.asAdventure($this.getColor()));
            // decorations
            builder.decoration(TextDecoration.OBFUSCATED, TextDecoration.State.byBoolean($access.accessor$obfuscated()));
            builder.decoration(TextDecoration.BOLD, TextDecoration.State.byBoolean($access.accessor$bold()));
            builder.decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.byBoolean($access.accessor$strikethrough()));
            builder.decoration(TextDecoration.UNDERLINED, TextDecoration.State.byBoolean($access.accessor$underlined()));
            builder.decoration(TextDecoration.ITALIC, TextDecoration.State.byBoolean($access.accessor$italic()));
            // events
            final HoverEvent hoverEvent = $this.getHoverEvent();
            if (hoverEvent != null) {
                builder.hoverEvent(SpongeAdventure.asAdventure(hoverEvent));
            }
            final ClickEvent clickEvent = $this.getClickEvent();
            if (clickEvent != null) {
                builder.clickEvent(net.kyori.adventure.text.event.ClickEvent.clickEvent(SpongeAdventure.asAdventure(clickEvent.getAction()), clickEvent.getValue()));
            }
            // insertion
            builder.insertion($this.getInsertion());
            this.bridge$adventure = builder.build();
        }
        return this.bridge$adventure;
    }
}
