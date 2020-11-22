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
package org.spongepowered.common.data.provider.map;

import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.storage.MapDataBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.map.canvas.SpongeMapByteCanvas;
import org.spongepowered.common.map.canvas.SpongeMapCanvas;
import org.spongepowered.math.vector.Vector2i;

public class MapInfoData {

	private MapInfoData() {
	}

	// @formatter:off
	public static void register(final DataProviderRegistrator registrator) {
		registrator.asMutable(MapData.class)
				.create(Keys.MAP_CANVAS)
					.get(mapData -> new SpongeMapByteCanvas(mapData.colors))
					.set((mapData, mapCanvas) -> ((SpongeMapCanvas)mapCanvas).applyToMapData(mapData))
				.create(Keys.MAP_DECORATIONS)
					.get(mapData -> (((MapDataBridge) mapData).bridge$getDecorations()))
					.set((mapData, mapDecorations) -> ((MapDataBridge) mapData).bridge$setDecorations(mapDecorations))
				.create(Keys.MAP_LOCATION)
					.get(mapData -> Vector2i.from(mapData.xCenter, mapData.zCenter))
					.set((mapData, vector2i) -> {
						mapData.calculateMapCenter(vector2i.getX(), vector2i.getY(), mapData.scale);
						mapData.markDirty();
					})
				.create(Keys.MAP_LOCKED)
					.get(mapData -> mapData.locked)
					.set((mapData, locked) -> mapData.locked = locked)
				.create(Keys.MAP_SCALE)
					.get(mapData -> (int) mapData.scale)
					.set((mapData, scale) -> {
						mapData.scale = scale.byteValue();
						mapData.calculateMapCenter(mapData.xCenter, mapData.zCenter, mapData.scale);
					})
				.create(Keys.MAP_TRACKS_PLAYERS)
					.get(mapData -> mapData.trackingPosition)
					.set((mapData, tracksPlayers) -> mapData.trackingPosition = tracksPlayers)
				.create(Keys.MAP_UNLIMITED_TRACKING)
					.get(mapData -> mapData.unlimitedTracking)
					.set((mapData, unlimitedTracking) -> mapData.unlimitedTracking = unlimitedTracking)
				.create(Keys.MAP_WORLD)
					.get(mapData -> Sponge.getServer().getWorldManager().getWorld(((ResourceKeyBridge)mapData.dimension).bridge$getKey())
							.orElseThrow(() -> new IllegalStateException("MapData with id: " + ((MapDataBridge)mapData).bridge$getMapId())))
					.set((mapData, world) -> mapData.dimension = ((ServerWorld)world).getDimension().getType()) // TODO:
				.build(MapData.class);
	}
	// @formatter:on
}
