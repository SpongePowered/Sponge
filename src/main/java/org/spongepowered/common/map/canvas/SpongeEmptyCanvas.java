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

import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.api.map.color.MapColor;
import org.spongepowered.api.map.color.MapColorTypes;
import org.spongepowered.common.util.Constants;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;

// This class basically means that we don't have to create tons of huge byte arrays
// While doing stuff like create SpongeValues for keys.
public final class SpongeEmptyCanvas implements SpongeMapCanvas {

    // Only allow construction from within this class
    private SpongeEmptyCanvas() {}
    public static final SpongeEmptyCanvas INSTANCE = new SpongeEmptyCanvas();

    @Override
    public MapColor color(final int x, final int y) {
        return MapColor.of(MapColorTypes.NONE.get());
    }

    @Override
    public Image toImage() {
        return new BufferedImage(Constants.Map.MAP_PIXELS, Constants.Map.MAP_PIXELS, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public Image toImage(final Color color) {
        final BufferedImage image = new BufferedImage(Constants.Map.MAP_PIXELS, Constants.Map.MAP_PIXELS, BufferedImage.TYPE_INT_RGB);
        final Graphics graphics = image.getGraphics();
        graphics.setColor(color);
        graphics.drawRect(0,0, image.getWidth(), image.getHeight());
        return image;
    }

    @Override
    public Builder toBuilder() {
        return MapCanvas.builder();
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew().set(Constants.Map.MAP_CANVAS,
                Collections.singletonList(new byte[Constants.Map.MAP_SIZE]));
    }

    @Override
    public void applyToMapData(final MapItemSavedData mapData) {
        Arrays.fill(mapData.colors, (byte) 0);
    }
}
