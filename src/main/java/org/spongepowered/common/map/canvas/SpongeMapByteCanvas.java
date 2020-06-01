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
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.api.map.color.MapColor;
import org.spongepowered.api.map.color.MapColorType;
import org.spongepowered.api.map.color.MapColorTypes;
import org.spongepowered.common.map.color.SpongeMapColor;
import org.spongepowered.common.registry.type.map.MapColorRegistryModule;
import org.spongepowered.common.util.Constants;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkState;

// MapCanvas backed by a byte array
public class SpongeMapByteCanvas implements SpongeMapCanvas {
    // Main Canvas storage
    public byte[] canvas;

    SpongeMapByteCanvas() {
        this.canvas = new byte[Constants.Map.MAP_SIZE];
        //Arrays.fill(canvas, Byte.MIN_VALUE);
    }

    public SpongeMapByteCanvas(byte[] canvas) {
        this.canvas = canvas;
    }

    public void setPixel(int index, byte value) {
        canvas[index] = value;
    }

    void paintAll(MapColor mapColor) {
        Arrays.fill(canvas, ((SpongeMapColor)mapColor).getMCColor());
    }

    public void paint(int startX, int endX, int startY, int endY, MapColor mapColor) {
        checkState(isInRange(startX), "startX out of bounds");
        checkState(isInRange(endX), "endX out of bounds");
        checkState(isInRange(startY), "startY out of bounds");
        checkState(isInRange(endY), "endY out of bounds");

        byte color = ((SpongeMapColor)mapColor).getMCColor();
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                canvas[x + (y * Constants.Map.MAP_PIXELS)] = color;
            }
        }
    }

    /**
     * Checks if x or y can fit on the map
     * @param i int x or y
     * @return if this would not be out of bounds
     */
    private boolean isInRange(int i) {
        return 0 <= i && i <= Constants.Map.MAP_MAX_INDEX;
    }

    public void applyToMapData(MapData mapData) {
        mapData.colors = canvas;
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew().set(DataQuery.of("MapColors"), Bytes.asList(canvas));
    }

    @Override
    public MapColor getColor(int x, int y) {
        checkState(isInRange(x), "x is out of bounds");
        checkState(isInRange(y), "y is out of bounds");
        return getColor(y * Constants.Map.MAP_PIXELS + x);
    }

    public MapColor getColor(int pos) {
        byte color = canvas[pos];
        int intColor = color;
        // Make positive so colorIndex is positive
        if (color < 0) {
            intColor += 256; // Undo byte wrap around.
        }
        int shade = intColor % Constants.Map.MAP_SHADES;
        int colorIndex = (intColor - shade)/Constants.Map.MAP_SHADES;
        MapColorType mapColorType = MapColorRegistryModule.getByColorValue(colorIndex).orElseThrow(() -> new IllegalStateException("Tried to get a color from a map that didn't exist!"));
        return new SpongeMapColor(mapColorType, shade);
    }

    @Override
    public Image toImage() {
        BufferedImage image = new BufferedImage(Constants.Map.MAP_PIXELS, Constants.Map.MAP_PIXELS, BufferedImage.TYPE_INT_RGB);
        int pos = 0;
        for (int y = 0; y < Constants.Map.MAP_PIXELS; y++, pos += Constants.Map.MAP_PIXELS) {
            for (int x = 0; x < Constants.Map.MAP_PIXELS; x++, pos++) {
                image.setRGB(x, y, getColor(x,y).getColor().getRgb());
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
                MapColor foundColor = getColor(x,y);
                Color paintColor = foundColor.getType() == MapColorTypes.AIR ? color
                        : foundColor.getColor().asJavaColor();
                image.setRGB(x, y, paintColor.getRGB());
            }
        }
        return image;
    }

    @Override
    public Builder toBuilder(MapCanvas canvas) {
        return MapCanvas.builder();
    }
}
