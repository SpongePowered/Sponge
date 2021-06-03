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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.api.map.color.MapColor;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.util.MapUtil;
import org.spongepowered.common.map.color.SpongeMapColor;
import org.spongepowered.common.util.Constants;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class SpongeMapCanvasBuilder implements MapCanvas.Builder {
    // If its being used to build from a DataView, or is blank
    // We don't want to create a big array.
    private byte @Nullable [] canvas = null;

    private byte[] getCanvas() {
        if (this.canvas == null) {
            this.canvas = new byte[Constants.Map.MAP_SIZE];
        }
        return this.canvas;
    }

    @Override
    public MapCanvas.Builder paintAll(final MapColor color) {
        Arrays.fill(this.getCanvas(), ((SpongeMapColor) color).getMCColor());
        return this;
    }

    @Override
    public MapCanvas.Builder paint(final int startX, final int startY, final int endX, final int endY, final MapColor mapColor) {
        if (!MapUtil.isInCanvasBounds(startX)) {
            throw new IllegalStateException("startX out of bounds");
        }
        if (!MapUtil.isInCanvasBounds(startY)) {
            throw new IllegalStateException("startY out of bounds");
        }
        if (!MapUtil.isInCanvasBounds(endX)) {
            throw new IllegalStateException("endX out of bounds");
        }
        if (!MapUtil.isInCanvasBounds(endY)) {
            throw new IllegalStateException("endY out of bounds");
        }

        final byte[] canvas = this.getCanvas();
        final byte color = ((SpongeMapColor) mapColor).getMCColor();
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                canvas[x + (y * Constants.Map.MAP_PIXELS)] = color;
            }
        }
        return this;
    }

    @Override
    public MapCanvas.Builder from(final MapCanvas canvas) {
        if (canvas instanceof SpongeEmptyCanvas) {
            return MapCanvas.builder();
        }
        this.canvas = ((SpongeMapByteCanvas) canvas).canvas.clone();
        return this;
    }

    @Override
    public MapCanvas.Builder reset() {
        this.canvas = null;
        return this;
    }

    @Override
    public MapCanvas.Builder fromImage(final Image image) {
        Objects.requireNonNull(image, "image cannot be null");
        if (image.getWidth(null) != Constants.Map.MAP_PIXELS || image.getHeight(null) != Constants.Map.MAP_PIXELS) {
            throw new IllegalArgumentException("image size was invalid!");
        }
        final BufferedImage bufferedImage = this.createBufferedImage(image);
        final int[] pixels = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
        // Get the color palette we are working with
        final Map<Integer, SpongeMapColor> palette = new HashMap<>();
        Sponge.game().registries().registry(RegistryTypes.MAP_COLOR_TYPE).stream().forEach(color -> {
            Sponge.game().registries().registry(RegistryTypes.MAP_SHADE).stream().forEach(shade -> {
                final SpongeMapColor spongeMapColor = new SpongeMapColor(color, shade);
                palette.put(spongeMapColor.color().rgb(), spongeMapColor);
            });
        });
        final byte[] canvas = this.getCanvas();
        for (int i = 0; i < pixels.length; i++) {
            final SpongeMapColor color = palette.get(pixels[i]);
            if (color == null) {
                throw new IllegalArgumentException("Can not find a matching color for rgb value: " + Integer.toHexString(pixels[i]) + ". The MapCanvas will have painted all pixels up to this point.");
            }
            canvas[i] = color.getMCColor();
        }
        return this;
    }

    @Override
    public MapCanvas.Builder fromContainer(final DataView container) {
        Objects.requireNonNull(container, "container cannot be null");
        if (!container.contains(Constants.Map.MAP_CANVAS)) {
            return this;
        }
        this.reset();
        this.canvas = MapUtil.getMapCanvasFromContainer(container);
        return this;
    }

    @Override
    public MapCanvas build() {
        if (this.canvas == null) {
            return SpongeEmptyCanvas.INSTANCE;
        }
        return new SpongeMapByteCanvas(this.canvas.clone());
    }

    private BufferedImage createBufferedImage(final Image image) {
        if (image instanceof BufferedImage) {
            final BufferedImage bufferedImage = (BufferedImage) image;
            // If its not TYPE_INT_RGB, we need to convert anyway.
            if (bufferedImage.getType() == BufferedImage.TYPE_INT_RGB) {
                return bufferedImage;
            }
        }

        final BufferedImage bufferedImage = new BufferedImage(
                image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        final Graphics2D bGr = bufferedImage.createGraphics();
        bGr.drawImage(image, 0, 0, null);
        bGr.dispose();
        return bufferedImage;
    }

}
