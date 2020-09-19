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

import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.MapData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.mutable.MapInfoData;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.action.CreateMapEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.api.map.decoration.MapDecoration;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.storage.MapDecorationBridge;
import org.spongepowered.common.bridge.world.storage.MapStorageBridge;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.WorldManager;

import java.io.IOException;
import java.util.Optional;

public final class MapUtil {
    /**
     * Returns MapInfo of newly created map, if the event was not cancelled.
     * @param mapInfoData MapInfoData for the data in the MapData
     * @param cause Cause of the event
     * @return MapInfo if event was not cancelled
     */
    public static Optional<MapInfo> fireCreateMapEvent(final MapInfoData mapInfoData, final Cause cause) {
        final int id = Sponge.getServer().getMapStorage()
                .map(mapStorage -> (MapStorageBridge)mapStorage)
                .flatMap(MapStorageBridge::bridge$getHighestMapId)
                .map(i -> i + 1)
                .orElse(0);

        final net.minecraft.world.World defaultWorld = WorldManager.getWorld(Sponge.getServer().getDefaultWorldName()).get();

        final String s = Constants.Map.MAP_PREFIX + id;
        final MapData mapData = new MapData(s);

        final MapInfo mapInfo = (MapInfo) mapData;
        mapInfo.offer(mapInfoData);

        final CreateMapEvent event = SpongeEventFactory.createCreateMapEvent(cause, mapInfo);
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            return Optional.empty();
        }

        // Call getUniqueDataId to advance the map ids.
        final int mcId = defaultWorld.getUniqueDataId("map");
        if (id != mcId) {
            // Short has overflown.
            SpongeImpl.getLogger().warn("Map size corruption, vanilla only allows " + Short.MAX_VALUE + "! Expected next number was not equal to the true next number.");
            SpongeImpl.getLogger().warn("Expected: " + id + ". Got: " + mcId);
            SpongeImpl.getLogger().warn("Automatically cancelling map creation");
            ((MapStorageBridge)Sponge.getServer().getMapStorage().get()).bridge$setHighestMapId((short) (id - 1));
            return Optional.empty();
        }
        defaultWorld.setData(s, mapData);
        return Optional.of(mapInfo);
    }

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

    public static net.minecraft.world.storage.MapDecoration mapDecorationFromNBT(final NBTTagCompound nbt) {
        final net.minecraft.world.storage.MapDecoration mapDecoration = new net.minecraft.world.storage.MapDecoration(
                net.minecraft.world.storage.MapDecoration.Type.byIcon(nbt.getByte(Constants.Map.DECORATION_TYPE.asString(""))),
                (byte)nbt.getDouble(Constants.Map.DECORATION_X.asString("")),
                (byte)nbt.getDouble(Constants.Map.DECORATION_Y.asString("")),
                (byte)nbt.getDouble(Constants.Map.DECORATION_ROTATION.asString(""))
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
        final MapDecoration mapDecoration = (MapDecoration) new net.minecraft.world.storage.MapDecoration(
                net.minecraft.world.storage.MapDecoration.Type.byIcon(container.getByte(Constants.Map.DECORATION_TYPE).get()),
                container.getByte(Constants.Map.DECORATION_X).get(),
                container.getByte(Constants.Map.DECORATION_Y).get(),
                container.getByte(Constants.Map.DECORATION_ROTATION).get()
        );
        ((MapDecorationBridge)mapDecoration).bridge$setKey(container.getString(Constants.Map.DECORATION_ID).get());
        return Optional.of(mapDecoration);
    }

    public static NBTTagCompound mapDecorationToNBT(final MapDecoration decoration) {
        try {
            return net.minecraft.nbt.JsonToNBT.getTagFromJson(DataFormats.JSON.write(decoration.toContainer()));
        } catch (final NBTException | IOException e) {
            // I don't see this ever happening but lets put logging in anyway
            throw new IllegalStateException("Error converting DataView to MC NBT", e);
        }
    }
}
