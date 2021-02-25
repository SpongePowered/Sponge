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
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.map.color.MapColor;
import org.spongepowered.api.map.color.MapColorType;
import org.spongepowered.api.map.color.MapShade;
import org.spongepowered.api.map.color.MapShades;
import org.spongepowered.api.map.decoration.MapDecoration;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.bridge.world.storage.MapDecorationBridge;
import org.spongepowered.common.map.color.SpongeMapColor;
import org.spongepowered.common.map.color.SpongeMapColorType;
import org.spongepowered.common.registry.builtin.sponge.MapShadeStreamGenerator;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    public static net.minecraft.world.level.saveddata.maps.MapDecoration mapDecorationFromNBT(final CompoundTag nbt) {
        final net.minecraft.world.level.saveddata.maps.MapDecoration mapDecoration = new net.minecraft.world.level.saveddata.maps.MapDecoration(
                net.minecraft.world.level.saveddata.maps.MapDecoration.Type.byIcon(nbt.getByte(Constants.Map.DECORATION_TYPE.asString(""))),
                (byte)nbt.getDouble(Constants.Map.DECORATION_X.asString("")),
                (byte)nbt.getDouble(Constants.Map.DECORATION_Y.asString("")),
                (byte)nbt.getDouble(Constants.Map.DECORATION_ROTATION.asString("")),
                nbt.contains("Name") ? TextComponent.Serializer.fromJson(nbt.getString("Name")) : null
        );
        ((MapDecorationBridge)mapDecoration).bridge$setKey(nbt.getString(Constants.Map.DECORATION_ID.asString("")));
        return mapDecoration;
    }

    public static Optional<MapDecoration> mapDecorationFromContainer(final DataView container) {
        if (!container.contains(
                Constants.Map.DECORATION_TYPE,
                Constants.Map.DECORATION_ID,
                Constants.Map.DECORATION_X,
                Constants.Map.DECORATION_Y,
                Constants.Map.DECORATION_ROTATION
        )) {
            return Optional.empty();
        }
        final MapDecoration mapDecoration = (MapDecoration) new net.minecraft.world.level.saveddata.maps.MapDecoration(
                net.minecraft.world.level.saveddata.maps.MapDecoration.Type.byIcon(container.getByte(Constants.Map.DECORATION_TYPE).get()),
                container.getByte(Constants.Map.DECORATION_X).get(),
                container.getByte(Constants.Map.DECORATION_Y).get(),
                container.getByte(Constants.Map.DECORATION_ROTATION).get(),
                container.getString(DataQuery.of("Name")).map(TextComponent.Serializer::fromJson).orElse(null)
        );
        ((MapDecorationBridge)mapDecoration).bridge$setKey(container.getString(Constants.Map.DECORATION_ID).get());
        return Optional.of(mapDecoration);
    }

    public static CompoundTag mapDecorationToNBT(final MapDecoration decoration) {
        try {
            return TagParser.parseTag(DataFormats.JSON.get().write(decoration.toContainer()));
        } catch (final IOException | CommandSyntaxException e) {
            // I don't see this ever happening but lets put logging in anyway
            throw new IllegalStateException("Error converting DataView to MC NBT", e);
        }
    }

    public static Optional<MapColor> getMapColorFromPixelValue(byte value) {
        int intColor = Byte.toUnsignedInt(value);
        int shade = intColor % Constants.Map.MAP_SHADES;
        int colorIndex = (intColor - shade)/Constants.Map.MAP_SHADES;
        Optional<MapColorType> mapColorType = MapUtil.getMapColorTypeByColorIndex(colorIndex);
        MapShade mapShade = MapUtil.getMapShadeById(shade);
        if (!mapColorType.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(new SpongeMapColor(mapColorType.get(), mapShade));
    }

    public static byte[] getMapCanvasFromContainer(DataView container) {
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
    public static int getMapDecorationOrientation(byte rotation) {
        return Math.floorMod(rotation, 16); // Different to java modulo, when rotation is negative.
    }

    public static Optional<MapColorType> getMapColorTypeByColorIndex(int colorIndex) {
        return Sponge.getGame().registries().registry(RegistryTypes.MAP_COLOR_TYPE).stream()
                .filter(type -> colorIndex == ((SpongeMapColorType) type).getColorIndex())
                .findAny();
    }

    public static MapShade getMapShadeById(int id) {
        switch (id) {
            case 0: return MapShades.BASE.get();
            case 1: return MapShades.DARK.get();
            case 2: return MapShades.DARKER.get();
            case 3: return MapShades.DARKEST.get();
            default: throw new IllegalStateException("Map shade with id " + id + " does not exist! should be 0-3");
        }
    }
}
