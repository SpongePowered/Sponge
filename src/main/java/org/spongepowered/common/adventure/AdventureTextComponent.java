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
package org.spongepowered.common.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.renderer.ComponentRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.bridge.adventure.ComponentBridge;
import org.spongepowered.common.bridge.network.chat.BaseComponentBridge;
import org.spongepowered.common.util.LocaleCache;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class AdventureTextComponent implements net.minecraft.network.chat.Component, BaseComponentBridge {
    private net.minecraft.network.chat.@MonotonicNonNull Component converted;
    private @Nullable Locale deepConvertedLocalized;
    private final net.kyori.adventure.text.Component wrapped;
    private final @Nullable ComponentRenderer<Locale> renderer;
    private @Nullable Locale lastLocale;
    private @Nullable AdventureTextComponent lastRendered;

    public AdventureTextComponent(final net.kyori.adventure.text.Component wrapped, final @Nullable ComponentRenderer<Locale> renderer) {
        this.wrapped = wrapped;
        this.renderer = renderer;
    }

    public @Nullable ComponentRenderer<Locale> renderer() {
        return this.renderer;
    }

    public net.kyori.adventure.text.Component wrapped() {
        return this.wrapped;
    }

    public synchronized AdventureTextComponent rendered(final Locale locale) {
        if (Objects.equals(locale, this.lastLocale)) {
            return this.lastRendered;
        }
        this.lastLocale = locale;
        return this.lastRendered = this.renderer == null ? this : new AdventureTextComponent(this.renderer.render(this.wrapped, locale), null);
    }

    net.minecraft.network.chat.Component deepConverted() {
        net.minecraft.network.chat.Component converted = this.converted;
        if (converted == null || this.deepConvertedLocalized != null) {
            converted = this.converted = ((ComponentBridge) this.wrapped).bridge$asVanillaComponent();
            this.deepConvertedLocalized = null;
        }
        return converted;
    }

    @OnlyIn(Dist.CLIENT)
    net.minecraft.network.chat.Component deepConvertedLocalized() {
        net.minecraft.network.chat.Component converted = this.converted;
        final Locale target = LocaleCache.getLocale(Minecraft.getInstance().options.languageCode);
        if (converted == null || this.deepConvertedLocalized != target) {
            converted = this.converted = this.rendered(target).deepConverted();
            this.deepConvertedLocalized = target;
        }
        return converted;
    }

    public net.minecraft.network.chat.@Nullable Component deepConvertedIfPresent() {
        return this.converted;
    }

    @Override
    public Style getStyle() {
        return this.deepConverted().getStyle();
    }

    @Override
    public String getString() {
        return this.rendered(Locale.getDefault()).deepConverted().getString();
    }

    @Override
    public String getString(final int length) {
        return this.deepConverted().getString(length);
    }

    @Override
    public String getContents() {
        if (this.wrapped instanceof TextComponent) {
            return ((TextComponent) this.wrapped).content();
        } else {
            return this.deepConverted().getContents();
        }
    }

    @Override
    public List<net.minecraft.network.chat.Component> getSiblings() {
        return this.deepConverted().getSiblings();
    }

    @Override
    public MutableComponent plainCopy() {
        return this.deepConverted().plainCopy();
    }

    @Override
    public MutableComponent copy() {
        return this.deepConverted().copy();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public FormattedCharSequence getVisualOrderText() {
        return this.deepConvertedLocalized().getVisualOrderText();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <T> Optional<T> visit(final StyledContentConsumer<T> visitor, final Style style) {
        return this.deepConvertedLocalized().visit(visitor, style);
    }

    @Override
    public <T> Optional<T> visit(final ContentConsumer<T> visitor) {
        return this.deepConverted().visit(visitor);
    }

    @Override
    public Component bridge$asAdventureComponent() {
        return this.wrapped;
    }

    @Override
    public @Nullable Component bridge$adventureComponentIfPresent() {
        return this.bridge$asAdventureComponent();
    }
}
