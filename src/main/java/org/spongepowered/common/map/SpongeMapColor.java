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
import org.spongepowered.api.map.MapColor;
import org.spongepowered.api.map.MapColorType;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.util.Constants;

public class SpongeMapColor implements MapColor {
    private MapColorType type;
    private int shade;

    public SpongeMapColor(MapColorType type) {
        this(type, 0);
    }

    public SpongeMapColor(MapColorType type, int shade) {
        this.type = type;
        this.shade = shade;
    }

    public byte getMCColor() {
        return (byte) ((((SpongeMapColorType)type).getBaseColor() * Constants.ItemStack.MAP_SHADES) + shade);
    }

    public int getShade() {
        return shade;
    }

    @Override
    public MapColorType getType() {return type;}

    @Override
    public Color getColor() {
        Color color = getType().getColor();
        int r = addShade(color.getRed());
        int g = addShade(color.getGreen());
        int b = addShade(color.getBlue());
        return Color.ofRgb(r,g,b);
    }

    private int addShade(int color) {
        return Math.floorDiv(color * Constants.ItemStack.SHADE_MULTIPLIER[shade], Constants.ItemStack.SHADE_DIVIDER);
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(DataQuery.of("colorIndex"), ((SpongeMapColorType)type).getBaseColor())
                .set(DataQuery.of("shade"), shade);
    }
}
