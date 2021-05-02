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

import com.google.common.primitives.Bytes;

import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.map.color.MapColor;
import org.spongepowered.api.map.color.MapColorType;
import org.spongepowered.api.map.color.MapShade;
import org.spongepowered.api.map.decoration.orientation.MapDecorationOrientation;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.map.color.SpongeMapColor;

import net.minecraft.core.MappedRegistry;
import net.minecraft.nbt.CompoundTag;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class MapUtil {

    /**
     * Checks if a number fits on a minecraft map
     * @param i number to check
     * @return true if it is in bounds
     */
    public static boolean isInCanvasBounds(final int i) {
        return i >= 0 && i < Constants.Map.MAP_PIXELS;
    }

    public static boolean isInMapDecorationBounds(final int i) {
        return i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE;
    }

    public static Optional<MapColor> getMapColorFromPixelValue(final byte value) {
        final int intColor = Byte.toUnsignedInt(value);
        final int shade = intColor % Constants.Map.MAP_SHADES;
        final int colorIndex = (intColor - shade)/Constants.Map.MAP_SHADES;
        final Optional<MapColorType> mapColorType = MapUtil.getMapColorTypeByColorIndex(colorIndex);
        final MapShade mapShade = MapUtil.getMapShadeById(shade);
        if (!mapColorType.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(new SpongeMapColor(mapColorType.get(), mapShade));
    }

    public static byte[] getMapCanvasFromContainer(final DataView container) {
        final DataQuery canvasQuery = Constants.Map.MAP_CANVAS;
        final List<Byte> data = container.getByteList(canvasQuery)
                .orElseThrow(() -> new InvalidDataException(canvasQuery + " was not a byte list!"));
        if (data.size() != Constants.Map.MAP_SIZE) {
            throw new InvalidDataException(canvasQuery + "had incorrect length, expected: " + Constants.Map.MAP_SIZE + ", got: " + data.size());
        }
        final Set<Byte> validPixels = new HashSet<>();
        // Ensure the data is valid.
        for (Byte pixel : data) {
            if (validPixels.contains(pixel)) {
                continue;
            }
            MapUtil.getMapColorFromPixelValue(pixel)
                    .orElseThrow(() -> new InvalidDataException("Invalid pixel value: " + pixel));
            validPixels.add(pixel);
        }
        return Bytes.toArray(data);
    }

    // Minecraft's orientation system is weird, it goes positive or negative
    // depending on what way you start going, and so there are two values for every
    // rotation. This includes 0, which depending on which way you approach, could be 16 or 0.
    // To make this make sense when serializing, we run it through this method
    public static int normalizeDecorationOrientation(byte rotation) {
        return Math.floorMod(rotation, 16); // Different to java modulo, when rotation is negative.
    }

    public static Optional<MapColorType> getMapColorTypeByColorIndex(final int colorIndex) {
        return Optional.ofNullable(((MappedRegistry<MapColorType>) (Object) RegistryTypes.MAP_COLOR_TYPE.get()).byId(colorIndex));
    }

    public static MapShade getMapShadeById(final int id) {
        return ((MappedRegistry<MapShade>) (Object) RegistryTypes.MAP_SHADE.get()).byId(id);
    }

    public static void saveMapUUIDIndex(final CompoundTag tag, final Map<Integer, UUID> index) {
        for (final Map.Entry<Integer, UUID> entry : index.entrySet()) {
            tag.putUUID(String.valueOf(entry.getKey()), entry.getValue());
        }
    }

    public static MapDecorationOrientation getMapRotById(int intRot) {
        final MapDecorationOrientation orientation = ((MappedRegistry<MapDecorationOrientation>) (Object) RegistryTypes.MAP_DECORATION_ORIENTATION.get()).byId(intRot);
        if (orientation == null) {
            throw new InvalidDataException(intRot + " is not a valid number for a MapDecorationOrientation");
        }
        return orientation;
    }
}
