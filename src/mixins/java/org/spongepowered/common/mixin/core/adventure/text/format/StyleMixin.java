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
package org.spongepowered.common.mixin.core.adventure.text.format;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.adventure.NbtLegacyHoverEventSerializer;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.adventure.StyleBridge;

import static org.spongepowered.common.adventure.SpongeAdventure.asVanilla;

import java.io.IOException;

@Mixin(net.kyori.adventure.text.format.Style.class)
public class StyleMixin implements StyleBridge {
    private Style bridge$vanilla;

    @Override
    @SuppressWarnings("ConstantConditions")
    public Style bridge$asVanilla() {
        if ((Object) this == net.kyori.adventure.text.format.Style.empty()) {
            return new Style();
        }

        if (this.bridge$vanilla == null) {
            final net.kyori.adventure.text.format.Style $this = (net.kyori.adventure.text.format.Style) (Object) this;
            this.bridge$vanilla = new Style();
            // font
            // TODO(adventure): 1.16
            // color
            final TextColor color = $this.color();
            this.bridge$vanilla.setColor(color == null ? null : asVanilla(NamedTextColor.nearestTo(color)));
            // decorations
            this.bridge$vanilla.setObfuscated(SpongeAdventure.asVanilla($this.decoration(TextDecoration.OBFUSCATED)));
            this.bridge$vanilla.setBold(SpongeAdventure.asVanilla($this.decoration(TextDecoration.BOLD)));
            this.bridge$vanilla.setStrikethrough(SpongeAdventure.asVanilla($this.decoration(TextDecoration.STRIKETHROUGH)));
            this.bridge$vanilla.setUnderlined(SpongeAdventure.asVanilla($this.decoration(TextDecoration.UNDERLINED)));
            this.bridge$vanilla.setItalic(SpongeAdventure.asVanilla($this.decoration(TextDecoration.ITALIC)));
            // events
            final net.kyori.adventure.text.event.HoverEvent<?> hoverEvent = $this.hoverEvent();
            if (hoverEvent != null) {
                final Object oldValue = hoverEvent.value();
                final ITextComponent value;
                if (oldValue instanceof Component) {
                    value = SpongeAdventure.asVanilla((Component) oldValue);
                } else if (oldValue instanceof net.kyori.adventure.text.event.HoverEvent.ShowItem) {
                    try {
                        value = SpongeAdventure.asVanilla(NbtLegacyHoverEventSerializer.INSTANCE.serializeShowItem((net.kyori.adventure.text.event.HoverEvent.ShowItem) oldValue));
                    } catch (IOException e) {
                        throw new IllegalArgumentException();
                    }
                } else if (oldValue instanceof net.kyori.adventure.text.event.HoverEvent.ShowEntity) {
                    value = SpongeAdventure.asVanilla(NbtLegacyHoverEventSerializer.INSTANCE.serializeShowEntity((net.kyori.adventure.text.event.HoverEvent.ShowEntity) oldValue, SpongeAdventure.GSON::serialize));
                } else {
                    throw new IllegalArgumentException();
                }
                this.bridge$vanilla.setHoverEvent(new HoverEvent(SpongeAdventure.asVanilla(hoverEvent.action()), value));
            }
            final net.kyori.adventure.text.event.ClickEvent clickEvent = $this.clickEvent();
            if (clickEvent != null) {
                this.bridge$vanilla.setClickEvent(new ClickEvent(SpongeAdventure.asVanilla(clickEvent.action()), clickEvent.value()));
            }
            // insertion
            this.bridge$vanilla.setInsertion($this.insertion());
        }
        return this.bridge$vanilla;
    }
}
