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

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.renderer.ComponentRenderer;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.util.FormattedCharSequence;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.bridge.adventure.ComponentBridge;
import org.spongepowered.common.util.LocaleCache;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class AdventureTextComponent implements net.minecraft.network.chat.Component {
    private net.minecraft.network.chat.@MonotonicNonNull Component converted;
    private @Nullable Locale deepConvertedLocalized;
    private final net.kyori.adventure.text.Component wrapped;
    private final @Nullable ComponentRenderer<SpongeRendererContext> renderer;
    private @Nullable Locale lastLocale;
    private @Nullable AdventureTextComponent lastRendered;

    public AdventureTextComponent(final net.kyori.adventure.text.Component wrapped, final @Nullable ComponentRenderer<SpongeRendererContext> renderer) {
        this.wrapped = wrapped;
        this.renderer = renderer;
    }

    public @Nullable ComponentRenderer<SpongeRendererContext> renderer() {
        return this.renderer;
    }

    public net.kyori.adventure.text.Component wrapped() {
        return this.wrapped;
    }

    public synchronized AdventureTextComponent rendered(final Locale locale, final @Nullable Audience audience) {
        if (this.renderer == null) {
            return this;
        }
        final SpongeRendererContext context = new SpongeRendererContext(locale, audience);
        final Component component = this.renderer.render(this.wrapped, context);
        if (context.containsVirtualComponents()) {
            return new AdventureTextComponent(component, null);
        }
        if (Objects.equals(locale, this.lastLocale)) {
            return this.lastRendered;
        }
        this.lastLocale = locale;
        return this.lastRendered = new AdventureTextComponent(component, null);
    }

    net.minecraft.network.chat.Component deepConverted() {
        net.minecraft.network.chat.Component converted = this.converted;
        if (converted == null || this.deepConvertedLocalized != null) {
            converted = this.converted = ((ComponentBridge) this.wrapped).bridge$asVanillaComponent();
            this.deepConvertedLocalized = null;
        }
        return converted;
    }

    net.minecraft.network.chat.Component deepConvertedLocalized() {
        // Client-only: resolve locale from Minecraft settings.
        // On the server, fall back to non-localized conversion.
        final Locale target = AdventureTextComponent.clientLocale();
        if (target == null) {
            return this.deepConverted();
        }
        net.minecraft.network.chat.Component converted = this.converted;
        if (converted == null || this.deepConvertedLocalized != target) {
            converted = this.converted = this.rendered(target, null).deepConverted();
            this.deepConvertedLocalized = target;
        }
        return converted;
    }

    private static @Nullable Locale clientLocale() {
        try {
            return LocaleCache.getLocale(net.minecraft.client.Minecraft.getInstance().options.languageCode);
        } catch (final NoClassDefFoundError | NullPointerException e) {
            return null;
        }
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
        return this.rendered(Locale.getDefault(), null).deepConverted().getString();
    }

    @Override
    public String getString(final int length) {
        return this.deepConverted().getString(length);
    }

    @Override
    public ComponentContents getContents() {
        if (this.wrapped instanceof final TextComponent tc) {
            return PlainTextContents.create(tc.content());
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

    @Override
    public FormattedCharSequence getVisualOrderText() {
        return this.deepConvertedLocalized().getVisualOrderText();
    }

    @Override
    public <T> Optional<T> visitUnbounded(final StyledContentConsumer<T> visitor, final Style style) {
        return this.deepConvertedLocalized().visitUnbounded(visitor, style);
    }

    @Override
    public <T> Optional<T> visitUnbounded(final ContentConsumer<T> visitor) {
        return this.deepConverted().visitUnbounded(visitor);
    }

}
