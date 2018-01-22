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
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.common.text.serializer.config.TextFormatConfigSerializer;

import java.util.Objects;

public final class TextFormatImpl implements TextFormat {

    static {
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(TextFormat.class), new TextFormatConfigSerializer());
    }

    /**
     * An empty {@link TextFormat} with no {@link TextColor} and no {@link TextStyle}.
     */
    public static final TextFormat NONE = new TextFormatImpl(TextColors.NONE, TextStyles.NONE);
    private final TextColor color;
    private final TextStyle style;

    public TextFormatImpl(final TextColor color, final TextStyle style) {
        this.color = checkNotNull(color, "color");
        this.style = checkNotNull(style, "style");
    }

    @Override
    public TextColor getColor() {
        return this.color;
    }

    @Override
    public TextStyle getStyle() {
        return this.style;
    }

    @Override
    public TextFormat color(final TextColor color) {
        return new TextFormatImpl(color, this.style);
    }

    @Override
    public TextFormat style(final TextStyle style) {
        return new TextFormatImpl(this.color, style);
    }

    @Override
    public TextFormat merge(final TextFormat format) {
        TextColor color = format.getColor();
        // If the given format's color is NONE use this ones
        if (color == TextColors.NONE) {
            color = this.color;
            // If the given format's color is RESET use NONE
        } else if (color == TextColors.RESET) {
            color = TextColors.NONE;
        }
        return new TextFormatImpl(color, this.style.and(format.getStyle()));
    }

    @Override
    public boolean isEmpty() {
        return this.color == TextColors.NONE && this.style.isEmpty();
    }

    @Override
    public void applyTo(final Text.Builder builder) {
        builder.format(this);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TextFormatImpl)) {
            return false;
        }

        final TextFormatImpl that = (TextFormatImpl) o;
        return this.color.equals(that.color) && this.style.equals(that.style);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.color, this.style);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("color", this.color)
                .add("style", this.style)
                .toString();
    }
}
