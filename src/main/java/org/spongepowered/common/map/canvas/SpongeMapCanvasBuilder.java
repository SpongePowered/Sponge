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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.api.map.color.MapColor;
import org.spongepowered.api.map.color.MapColorType;
import org.spongepowered.common.map.color.SpongeMapColor;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nullable;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class SpongeMapCanvasBuilder extends AbstractDataBuilder<MapCanvas> implements MapCanvas.Builder {
    // If its being used to build from a DataView, or is blank
    // We don't want to create a big array.
    @Nullable
    private SpongeMapByteCanvas canvas = null;

    public SpongeMapCanvasBuilder() {
        super(MapCanvas.class, 1);
    }

    @Override
    public MapCanvas.Builder paintAll(MapColor color) {
        if (this.canvas == null) {
            this.canvas = new SpongeMapByteCanvas();
        }
        this.canvas.paintAll(color);
        return this;
    }

    @Override
    public MapCanvas.Builder paint(int startX, int startY, int endX, int endY, MapColor mapColor) {
        if (this.canvas == null) {
            this.canvas = new SpongeMapByteCanvas();
        }
        this.canvas.paint(startX, endX, startY, endY, mapColor);
        return this;
    }

    @Override
    public MapCanvas.Builder from(MapCanvas canvas) {
        if (canvas instanceof SpongeEmptyCanvas) {
            return MapCanvas.builder();
        }
        this.canvas = (SpongeMapByteCanvas)canvas;
        return this;
    }

    @Override
    public MapCanvas.Builder fromImage(Image image) {
        Objects.requireNonNull(image, "image cannot be null");
        if (image.getWidth(null) != 128 || image.getHeight(null) != 128) {
            throw new IllegalStateException("image size was invalid!");
        }
        BufferedImage bufferedImage = null;
        boolean shouldConvert = true;
        if (image instanceof BufferedImage) {
            bufferedImage = (BufferedImage)image;
            // If its not TYPE_INT_RGB, convert it.
            shouldConvert = bufferedImage.getType() != BufferedImage.TYPE_INT_RGB;
        }
        if (shouldConvert) {
            bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics2D bGr = bufferedImage.createGraphics();
            bGr.drawImage(image, 0, 0, null);
            bGr.dispose();
        }
        int[] pixels = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
        // Get the color palette we are working with
        Map<Integer, SpongeMapColor> palette = new HashMap<>();
        for (MapColorType type : Sponge.getRegistry().getAllOf(MapColorType.class)) {
            // Put each shade in also.
            for (int i = 0; i < Constants.Map.MAP_SHADES; i++) {
                SpongeMapColor spongeMapColor = new SpongeMapColor(type, i);
                palette.put(spongeMapColor.getColor().getRgb(), spongeMapColor);
            }
        }
        if (this.canvas == null) {
            this.canvas = new SpongeMapByteCanvas();
        }
        for (int i = 0; i < pixels.length; i++) {
            SpongeMapColor color = palette.get(pixels[i]);
            if (color == null) {
                throw new IllegalStateException("Can not find a matching color for rgb value: " + pixels[i] + ". The MapCanvas will have painted all pixels up to this point.");
            }
            this.canvas.setPixel(i, color.getMCColor());
        }
        return this;
    }

    @Override
    protected Optional<MapCanvas> buildContent(DataView container) throws InvalidDataException {
        if (!container.contains(DataQuery.of("MapColors"))) {
            return Optional.empty();
        }
        SpongeMapCanvas mapCanvas = new SpongeMapByteCanvas(
                Bytes.toArray(container.getByteList(DataQuery.of("MapColors")).get())
        );
        return Optional.of(mapCanvas);
    }

    @Override
    public MapCanvas build() {
        if (this.canvas == null) {
            return SpongeEmptyCanvas.INSTANCE;
        }
        return this.canvas;
    }
}
