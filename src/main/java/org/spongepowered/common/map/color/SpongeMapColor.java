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
package org.spongepowered.common.map.color;

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.map.color.MapColor;
import org.spongepowered.api.map.color.MapColorType;
import org.spongepowered.api.map.color.MapShade;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.util.Constants;

import java.util.Objects;

public class SpongeMapColor implements MapColor {
    private final MapColorType type;
    private final MapShade shade;
    private final byte mcColor;

    public SpongeMapColor(MapColorType type, MapShade shade) {
        this.type = type;
        this.shade = shade;
        this.mcColor = (byte) ((((SpongeMapColorType) this.type).getColorIndex() * Constants.Map.MAP_SHADES) + ((SpongeMapShade)shade).getShadeNum());
    }

    public byte getMCColor() {
        return this.mcColor;
    }

    @Override
    public MapShade getShade() {
        return this.shade;
    }

    @Override
    public MapColorType getType() {return this.type;}

    @Override
    public Color getColor() {
        Color color = getType().getColor();
        int r = addShade(color.getRed());
        int g = addShade(color.getGreen());
        int b = addShade(color.getBlue());
        return Color.ofRgb(r,g,b);
    }

    private int addShade(int color) {
        return Math.floorDiv(color * ((SpongeMapShade)this.shade).getMultiplier(), Constants.Map.SHADE_DIVIDER);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(Constants.Map.COLOR_INDEX, ((SpongeMapColorType) this.type).getColorIndex())
                .set(Constants.Map.SHADE_NUM, ((SpongeMapShade)this.shade).getShadeNum());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpongeMapColor that = (SpongeMapColor) o;
        return type.equals(that.type) &&
                shade.equals(that.shade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, shade);
    }
}
