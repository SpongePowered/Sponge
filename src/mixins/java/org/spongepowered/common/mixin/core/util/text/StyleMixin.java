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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.accessor.util.text.StyleAccessor;
import org.spongepowered.common.adventure.NbtLegacyHoverEventSerializer;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.util.text.StyleBridge;

import java.io.IOException;

@Mixin(Style.class)
public class StyleMixin implements StyleBridge {
    private net.kyori.adventure.text.format.Style bridge$adventure;

    @Override
    @SuppressWarnings("ConstantConditions")
    public net.kyori.adventure.text.format.Style bridge$asAdventure() {
        if (this.bridge$adventure == null) {
            final net.kyori.adventure.text.format.Style.Builder builder = net.kyori.adventure.text.format.Style.builder();
            final Style $this = (Style) (Object) this;
            final StyleAccessor $access = (StyleAccessor) this;
            // font
            // TODO(adventure): 1.16
            // color
            builder.color(SpongeAdventure.asAdventureNamed($this.getColor()));
            // decorations
            builder.decoration(TextDecoration.OBFUSCATED, TextDecoration.State.byBoolean($access.accessor$getObfuscated()));
            builder.decoration(TextDecoration.BOLD, TextDecoration.State.byBoolean($access.accessor$getBold()));
            builder.decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.byBoolean($access.accessor$getStrikethrough()));
            builder.decoration(TextDecoration.UNDERLINED, TextDecoration.State.byBoolean($access.accessor$getUnderlined()));
            builder.decoration(TextDecoration.ITALIC, TextDecoration.State.byBoolean($access.accessor$getItalic()));
            // events
            final HoverEvent hoverEvent = $this.getHoverEvent();
            if (hoverEvent != null) {
                final net.kyori.adventure.text.event.HoverEvent.Action<?> action = SpongeAdventure.asAdventure(hoverEvent.getAction());
                final Component value = SpongeAdventure.asAdventure(hoverEvent.getValue());
                try {
                    if (action == net.kyori.adventure.text.event.HoverEvent.Action.SHOW_TEXT) {
                        builder.hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(SpongeAdventure.asAdventure(hoverEvent.getValue())));
                    } else if (action == net.kyori.adventure.text.event.HoverEvent.Action.SHOW_ITEM) {
                        builder.hoverEvent(net.kyori.adventure.text.event.HoverEvent.showItem(NbtLegacyHoverEventSerializer.INSTANCE.deserializeShowItem(value)));
                    } else if (action == net.kyori.adventure.text.event.HoverEvent.Action.SHOW_ENTITY) {
                        builder.hoverEvent(net.kyori.adventure.text.event.HoverEvent.showEntity(NbtLegacyHoverEventSerializer.INSTANCE.deserializeShowEntity(value, SpongeAdventure.GSON::deserialize)));
                    }
                } catch (final IOException e) {
                    // can't deal
                }
            }
            final ClickEvent clickEvent = $this.getClickEvent();
            if (clickEvent != null) {
                builder.clickEvent(net.kyori.adventure.text.event.ClickEvent.of(SpongeAdventure.asAdventure(clickEvent.getAction()), clickEvent.getValue()));
            }
            // insertion
            builder.insertion($this.getInsertion());
            this.bridge$adventure = builder.build();
        }
        return this.bridge$adventure;
    }

    @Inject(method = "setParentStyle", at = @At("HEAD"))
    private void preventParentCycles(final Style newParent, final CallbackInfoReturnable<Style> ci) {
        if (newParent == (Style) (Object) this) {
            throw new IllegalArgumentException("Cannot set style " + this + " as a parent of itself!");
        }
    }
}
