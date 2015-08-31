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
package org.spongepowered.common.mixin.api.text;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.ShiftClickAction;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.text.IMixinChatComponent;
import org.spongepowered.common.interfaces.text.IMixinText;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.action.SpongeClickAction;
import org.spongepowered.common.text.action.SpongeHoverAction;
import org.spongepowered.common.text.format.SpongeTextColor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Mixin(value = Text.class, remap = false)
public abstract class MixinText implements IMixinText {

    @Shadow protected TextFormat format;
    @Shadow protected ImmutableList<Text> children;
    @Shadow protected Optional<ClickAction<?>> clickAction;
    @Shadow protected Optional<HoverAction<?>> hoverAction;
    @Shadow protected Optional<ShiftClickAction<?>> shiftClickAction;

    private Map<Locale, IChatComponent> localizedComponents;
    private String json;

    protected ChatComponentStyle createComponent(Locale locale) {
        throw new UnsupportedOperationException();
    }

    private IChatComponent initializeComponent(Locale locale) {
        if (this.localizedComponents == null) {
            this.localizedComponents = Collections.synchronizedMap(new HashMap<Locale, IChatComponent>());
        }
        IChatComponent component = this.localizedComponents.get(locale);
        if (component == null) {
            component = createComponent(locale);
            ChatStyle style = component.getChatStyle();

            if (this.format.getColor() != TextColors.NONE) {
                style.setColor(((SpongeTextColor) this.format.getColor()).getHandle());
            }

            if (!this.format.getStyle().isEmpty()) {
                style.setBold(this.format.getStyle().isBold().orElse(null));
                style.setItalic(this.format.getStyle().isItalic().orElse(null));
                style.setUnderlined(this.format.getStyle().hasUnderline().orElse(null));
                style.setStrikethrough(this.format.getStyle().hasStrikethrough().orElse(null));
                style.setObfuscated(this.format.getStyle().isObfuscated().orElse(null));
            }

            if (this.clickAction.isPresent()) {
                style.setChatClickEvent(SpongeClickAction.getHandle(this.clickAction.get()));
            }

            if (this.hoverAction.isPresent()) {
                style.setChatHoverEvent(SpongeHoverAction.getHandle(this.hoverAction.get(), locale));
            }

            if (this.shiftClickAction.isPresent()) {
                ShiftClickAction.InsertText insertion = (ShiftClickAction.InsertText) this.shiftClickAction.get();
                style.setInsertion(insertion.getResult());
            }

            for (Text child : this.children) {
                component.appendSibling(((IMixinText) child).toComponent(locale));
            }
            this.localizedComponents.put(locale, component);
        }
        return component;
    }

    private IChatComponent getHandle(Locale locale) {
        return initializeComponent(locale);
    }

    @Override
    public IChatComponent toComponent(Locale locale) {
        return getHandle(locale).createCopy(); // Mutable instances are not nice :(
    }

    @Override
    public String toPlain(Locale locale) {
        return ((IMixinChatComponent) getHandle(locale)).toPlain();
    }

    @Override
    public String toJson(Locale locale) {
        if (this.json == null) {
            this.json = IChatComponent.Serializer.componentToJson(getHandle(locale));
        }

        return this.json;
    }

    @Override
    public String getLegacyFormatting() {
        return ((IMixinChatComponent) getHandle(SpongeTexts.getDefaultLocale())).getLegacyFormatting();
    }

    @Override
    public String toLegacy(char code, Locale locale) {
        return ((IMixinChatComponent) getHandle(locale)).toLegacy(code);
    }

}
