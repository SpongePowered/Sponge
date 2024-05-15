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
package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.api.world.DefaultWorldKeys;
import org.spongepowered.common.bridge.world.storage.MapItemSavedDataBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;


public final class MapInfoItemStackData {

    private MapInfoItemStackData() {
    }

    // @formatter:off
	public static void register(final DataProviderRegistrator registrator) {
		registrator
				.asMutable(ItemStack.class)
					.create(Keys.MAP_INFO)
						.supports(item -> item.getItem() instanceof MapItem)
						.get(itemStack -> {
							final MapId mapId = itemStack.get(DataComponents.MAP_ID);
							if (mapId == null) {
								return null;
							}
							final Level level = (Level)Sponge.server().worldManager().world(DefaultWorldKeys.DEFAULT).get();
							return (MapInfo) level.getMapData(mapId);
						}) // Nullable
						.set((itemStack, mapInfo) -> itemStack.set(DataComponents.MAP_ID,
								new MapId(((MapItemSavedDataBridge)mapInfo).bridge$getMapId())));
	}
	// @formatter:on
}
