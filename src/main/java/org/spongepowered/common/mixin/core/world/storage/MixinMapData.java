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
package org.spongepowered.common.mixin.core.world.storage;

import com.flowpowered.math.vector.Vector2i;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapData;
import org.spongepowered.api.map.MapView;
import org.spongepowered.api.map.color.MapColor;
import org.spongepowered.api.map.color.MapColors;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@NonnullByDefault
@Mixin(MapData.class)
public abstract class MixinMapData extends WorldSavedData implements MapView {

    @Shadow public byte[] colors;
    @Shadow public abstract void updateMapData(int x, int y);

    public MixinMapData(String name) {
        super(name);
    }

    @Override
    public Vector2i getSize() {
        return Vector2i.from(128, 128);
    }

    @Override
    public BufferedImage toImage() {
        BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < 128; i++) {
            for (int j = 0; j < 128; j++) {
                data[j + (i*128)] = 0; // TODO: Use getPixel when it's implemented to get color
            }
        }
        return image;
    }

    @Override
    public void drawImage(int x, int y, BufferedImage image) {
        checkNotNull(image, "image");
        checkArgument(x >= 0 && x < 128, "x >= 0 && x < 128");
        checkArgument(y >= 0 && y < 128, "y >= 0 && y < 128");
        // int and byte are the only types observed as of yet.
        DataBuffer buffer = image.getData().getDataBuffer();
        System.out.println(buffer.getClass().getTypeName());
        System.out.println(image.getType());
        if (buffer instanceof DataBufferByte) {
            // TODO: Finish this implementation
            DataBufferByte byteBuffer = (DataBufferByte) buffer;
            byte[] imageData = byteBuffer.getData();
            // TODO: We will need to know what types people are using because
            // there are way too many assume 3 byte BGR for now
            for (int i = 0; i < Math.min(128-y, image.getHeight()); i++) {
                for (int j = 0; j < Math.min(128-x, image.getHeight()); j++) {
                    int baseIndex = (i*image.getHeight()) + j;
                    byte blue = imageData[baseIndex];
                    byte green = imageData[baseIndex+1];
                    byte red = imageData[baseIndex+2];
                    Color color = Color.ofRgb(red, green, blue);
                    // Use the unweighted RGB model to convert
                    // TODO: Is this the best default?
                    MapColor converted = MapColors.of(color);
                    // XXX: This is way broken, should actually expose the multiplied ID in the
                    // SpongeMapColor
                    colors[(i*128)+j] = (byte) ((net.minecraft.block.material.MapColor) converted).colorIndex;
                }
            }
            markDirty();
            updateMapData(0, 0);
            updateMapData(127, 127);

        } else if (buffer instanceof DataBufferInt) {

        } else {
            // TODO: Figure out how to throw an "alert sponge" exception
        }
    }

    @Override
    public String getId() {
        return mapName;
    }
}
