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
package org.spongepowered.common.text.format;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.registry.MappedRegistry;

public final class SpongeTextColor implements TextColor {

    public static SpongeTextColor of(TextFormatting formatting) {
        final SimpleRegistry<TextColor> registry = SpongeImpl.getRegistry().getCatalogRegistry().getRegistry(TextColor.class);
        TextColor color = ((MappedRegistry<TextColor, TextFormatting>) registry).getReverseMapping(formatting);
        if (color == null) {
            color = TextColors.NONE.get();
        }

        return (SpongeTextColor) color;
    }

    public static TextFormatting of(TextColor color) {
        final SimpleRegistry<TextColor> registry = SpongeImpl.getRegistry().getCatalogRegistry().getRegistry(TextColor.class);
        TextFormatting formatting = ((MappedRegistry<TextColor, TextFormatting>) registry).getMapping(color);
        if (formatting == null) {
            formatting = TextFormatting.WHITE;
        }

        return formatting;
    }

    private final CatalogKey key;
    private final Color color;

    public SpongeTextColor(CatalogKey key, Color color) {
        this.key = key;
        this.color = checkNotNull(color, "color");
    }

    @Override
    public CatalogKey getKey() {
        return this.key;
    }

    @Override
    public Color getColor() {
        return this.color;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(SpongeTextColor.class)
            .add("key", this.key)
            .add("color", this.color)
            .toString();
    }
}
