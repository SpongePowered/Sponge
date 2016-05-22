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
package org.spongepowered.common.map;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.map.color.MapColor;
import org.spongepowered.api.map.color.MapShade;
import org.spongepowered.api.util.Color;

import javax.annotation.Nonnull;

public class SpongeMapColor implements MapColor {

    private final Base baseColor;
    private final MapShade shade;

    private Color shadedColor = null;

    public SpongeMapColor(Base baseColor, MapShade shade) {
        this.baseColor = baseColor;
        this.shade = shade;
    }

    @Override public Color getColor() {
        // lazy initialize
        if (this.shadedColor == null) {
            this.shadedColor = shadeColor(((net.minecraft.block.material.MapColor)baseColor).colorValue, shade);
        }
        return this.shadedColor;
    }

    @Override public MapColor shade(MapShade newShading) {
        return this.baseColor.shade(newShading);
    }

    @Override public MapShade getShade() {
        return this.shade;
    }

    @Override public Base base() {
        return this.baseColor;
    }

    @Override public int getContentVersion() {
        return 1;
    }

    @Override public DataContainer toContainer() {
        final DataContainer container = new MemoryDataContainer();
        container.set(Queries.CONTENT_VERSION, this.getContentVersion())
                .set(DataQuery.of("BaseColor"), this.baseColor.toContainer())
                .set(DataQuery.of("Shade"), this.shade.getId());
        return container;
    }

    @Nonnull
    public static final Color shadeColor(int color, MapShade shade) {
        Color spongeColor = Color.ofRgb(color);
        int mulFactor = shade.getMultiplication();
        return spongeColor.withRed((spongeColor.getRed() * mulFactor) & 255)
                .withGreen((spongeColor.getGreen() * mulFactor) & 255)
                .withBlue((spongeColor.getBlue() * mulFactor) & 255);

    }
}
