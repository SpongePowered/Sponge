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
package org.spongepowered.common.map.canvas;

import com.google.common.primitives.Bytes;
import net.minecraft.world.storage.MapData;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.api.map.color.MapColor;
import org.spongepowered.api.map.color.MapColorType;
import org.spongepowered.api.map.color.MapColorTypes;
import org.spongepowered.api.map.color.MapShade;
import org.spongepowered.common.map.MapUtil;
import org.spongepowered.common.map.color.SpongeMapColor;
import org.spongepowered.common.registry.type.map.MapColorRegistryModule;
import org.spongepowered.common.registry.type.map.MapShadeRegistryModule;
import org.spongepowered.common.util.Constants;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;

import static com.google.common.base.Preconditions.checkState;

// MapCanvas backed by a byte array
public class SpongeMapByteCanvas implements SpongeMapCanvas {
    // Main Canvas storage
    public byte[] canvas;

    public SpongeMapByteCanvas(byte[] canvas) {
        this.canvas = canvas;
    }

    public void applyToMapData(MapData mapData) {
        mapData.colors = canvas;
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew().set(Keys.MAP_CANVAS.getQuery(), Bytes.asList(canvas));
    }

    @Override
    public MapColor getColor(int x, int y) {
        checkState(MapUtil.isInCanvasBounds(x), "x is out of bounds");
        checkState(MapUtil.isInCanvasBounds(y), "y is out of bounds");
        return getColor(y * Constants.Map.MAP_PIXELS + x);
    }

    public MapColor getColor(int pos) {
        return MapUtil.getMapColorFromPixelValue(this.canvas[pos])
                .orElseThrow(() -> new IllegalStateException("Tried to get a color that didn't exist! pixel value: " + this.canvas[pos]));
    }

    @Override
    public Image toImage() {
        BufferedImage image = new BufferedImage(Constants.Map.MAP_PIXELS, Constants.Map.MAP_PIXELS, BufferedImage.TYPE_INT_RGB);
        int pos = 0;
        for (int y = 0; y < Constants.Map.MAP_PIXELS; y++, pos += Constants.Map.MAP_PIXELS) {
            for (int x = 0; x < Constants.Map.MAP_PIXELS; x++, pos++) {
                image.setRGB(x, y, this.getColor(x,y).getColor().getRgb());
            }
        }
        return image;
    }

    @Override
    public Image toImage(Color color) {
        BufferedImage image = new BufferedImage(Constants.Map.MAP_PIXELS, Constants.Map.MAP_PIXELS, BufferedImage.TYPE_INT_ARGB);
        int pos = 0;
        for (int y = 0; y < Constants.Map.MAP_PIXELS; y++, pos += Constants.Map.MAP_PIXELS) {
            for (int x = 0; x < Constants.Map.MAP_PIXELS; x++, pos++) {
                MapColor foundColor = this.getColor(x,y);
                Color paintColor = foundColor.getType() == MapColorTypes.AIR ? color
                        : foundColor.getColor().asJavaColor();
                image.setRGB(x, y, paintColor.getRGB());
            }
        }
        return image;
    }

    @Override
    public Builder toBuilder() {
        return MapCanvas.builder().from(this);
    }
}
