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
package org.spongepowered.common.util;

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.commons.lang3.Validate;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.util.Color;

import java.util.Optional;

public final class SpongeColor implements Color {

    private final int rgb;
    private final int red;
    private final int green;
    private final int blue;

    private SpongeColor(int red, int green, int blue) {
        this.rgb = red << 16 | green << 8 | blue;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public int getRgb() {
        return this.rgb;
    }

    @Override
    public int getRed() {
        return this.red;
    }

    @Override
    public int getGreen() {
        return this.green;
    }

    @Override
    public int getBlue() {
        return this.blue;
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, this.getContentVersion())
                .set(Queries.COLOR_RED, this.getRed())
                .set(Queries.COLOR_GREEN, this.getGreen())
                .set(Queries.COLOR_BLUE, this.getBlue());
    }

    @Override
    public int hashCode() {
        return rgb;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj != null && getClass() == obj.getClass() && hashCode() == obj.hashCode();
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("rgb", this.rgb)
                .add("red", this.red)
                .add("green", this.green)
                .add("blue", this.blue)
                .toString();
    }

    public static final class Builder extends AbstractDataBuilder<Color> implements Color.Builder {

        private int red = -1;
        private int green = -1;
        private int blue = -1;

        public Builder() {
            super(Color.class, 1);
        }

        @Override
        public Builder rgb(int rgb) {
            return rgb(rgb >> 16, rgb >> 8, rgb);
        }

        @Override
        public Builder rgb(int red, int green, int blue) {
            this.red = red & 0xFF;
            this.green = green & 0xFF;
            this.blue = blue & 0xFF;
            return this;
        }

        @Override
        public Builder mix(Color... colors) {
            Validate.noNullElements(colors, "No null colors allowed!");
            checkArgument(colors.length > 0, "Cannot have an empty color array!");
            if (colors.length == 1) {
                return from(colors[0]);
            }
            reset();
            for (Color color : colors) {
                red += color.getRed();
                green += color.getGreen();
                blue += color.getBlue();
            }
            return rgb(Math.round((float) red / colors.length), Math.round((float) green / colors.length), Math.round((float) blue / colors.length));
        }

        @Override
        public Builder from(Color value) {
            return rgb(value.getRed(), value.getGreen(), value.getBlue());
        }

        @Override
        public Builder reset() {
            this.red = -1;
            this.green = -1;
            this.blue = -1;
            return this;
        }

        @Override
        public Color build() {
            if (red == -1 || green == -1 || blue == -1) {
                throw new IllegalStateException("No color has been defined.");
            }
            return new SpongeColor(red, green, blue);
        }

        @Override
        protected Optional<Color> buildContent(DataView container) throws InvalidDataException {
            if (!container.contains(Queries.COLOR_RED, Queries.COLOR_GREEN, Queries.COLOR_BLUE)) {
                return Optional.empty();
            }
            try {
                final int red = container.getInt(Queries.COLOR_RED).get();
                final int green = container.getInt(Queries.COLOR_GREEN).get();
                final int blue = container.getInt(Queries.COLOR_BLUE).get();
                return Optional.of(Color.of(red, green, blue));
            } catch (Exception e) {
                throw new InvalidDataException("Could not parse some data.", e);
            }
        }

    }

}
