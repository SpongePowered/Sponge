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
package org.spongepowered.common.registry.builtin.sponge;

import net.minecraft.block.material.MaterialColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.map.color.MapColorType;
import org.spongepowered.common.map.color.SpongeMapColorType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class MapColorTypeStreamGenerator {
	private static final Map<Integer, MapColorType> COLOR_INDEX_MAP = new HashMap<>();
	private static final Logger logger = LogManager.getLogger(MapColorTypeStreamGenerator.class.getSimpleName());

	public static Stream<MapColorType> stream() {
		///////////////////////// ORDER MATTERS HERE ////////////////////
		return Stream.of(
				addToRegistry("air", 					MaterialColor.AIR),
				addToRegistry("grass", 					MaterialColor.GRASS),
				addToRegistry("sand", 					MaterialColor.SAND),
				addToRegistry("wool", 					MaterialColor.WOOL),
				addToRegistry("tnt", 					MaterialColor.TNT),
				addToRegistry("ice", 					MaterialColor.ICE),
				addToRegistry("iron", 					MaterialColor.IRON),
				addToRegistry("foliage", 				MaterialColor.FOLIAGE),
				addToRegistry("snow", 					MaterialColor.SNOW),
				addToRegistry("clay", 					MaterialColor.CLAY),
				addToRegistry("dirt",					MaterialColor.DIRT),
				addToRegistry("stone", 					MaterialColor.STONE),
				addToRegistry("water", 					MaterialColor.WATER),
				addToRegistry("wood", 					MaterialColor.WOOD),
				addToRegistry("quartz", 				MaterialColor.QUARTZ),
				addToRegistry("adobe", 					MaterialColor.ADOBE),
				addToRegistry("magenta", 				MaterialColor.MAGENTA),
				addToRegistry("light_blue",				MaterialColor.LIGHT_BLUE),
				addToRegistry("yellow", 				MaterialColor.YELLOW),
				addToRegistry("lime", 					MaterialColor.LIME),
				addToRegistry("pink",					MaterialColor.PINK),
				addToRegistry("gray", 					MaterialColor.GRAY),
				addToRegistry("light_gray",				MaterialColor.LIGHT_GRAY),
				addToRegistry("cyan", 					MaterialColor.CYAN),
				addToRegistry("purple", 				MaterialColor.PURPLE),
				addToRegistry("blue",	 				MaterialColor.BLUE),
				addToRegistry("brown", 					MaterialColor.BROWN),
				addToRegistry("green", 					MaterialColor.GREEN),
				addToRegistry("red", 					MaterialColor.RED),
				addToRegistry("black", 					MaterialColor.BLACK),
				addToRegistry("gold", 					MaterialColor.GOLD),
				addToRegistry("diamond", 				MaterialColor.DIAMOND),
				addToRegistry("lapis_lazuli", 			MaterialColor.LAPIS),
				addToRegistry("emerald", 				MaterialColor.EMERALD),
				addToRegistry("obsidian", 				MaterialColor.OBSIDIAN),
				addToRegistry("netherrack", 			MaterialColor.NETHERRACK),
				addToRegistry("white_terracotta", 		MaterialColor.WHITE_TERRACOTTA),
				addToRegistry("orange_terracotta", 		MaterialColor.ORANGE_TERRACOTTA),
				addToRegistry("magenta_terracotta", 	MaterialColor.MAGENTA_TERRACOTTA),
				addToRegistry("light_blue_terracotta", 	MaterialColor.LIGHT_BLUE_TERRACOTTA),
				addToRegistry("yellow_terracotta", 		MaterialColor.YELLOW_TERRACOTTA),
				addToRegistry("lime_terracotta", 		MaterialColor.LIME_TERRACOTTA),
				addToRegistry("pink_terracotta", 		MaterialColor.PINK_TERRACOTTA),
				addToRegistry("gray_terracotta", 		MaterialColor.GRAY_TERRACOTTA),
				addToRegistry("light_gray_terracotta", 	MaterialColor.LIGHT_GRAY_TERRACOTTA),
				addToRegistry("cyan_terracotta", 		MaterialColor.CYAN_TERRACOTTA),
				addToRegistry("purple_terracotta", 		MaterialColor.PURPLE_TERRACOTTA),
				addToRegistry("blue_terracotta", 		MaterialColor.BLUE_TERRACOTTA),
				addToRegistry("brown_terracotta", 		MaterialColor.BROWN_TERRACOTTA),
				addToRegistry("green_terracotta", 		MaterialColor.GREEN_TERRACOTTA),
				addToRegistry("red_terracotta", 		MaterialColor.RED_TERRACOTTA),
				addToRegistry("black_terracotta", 		MaterialColor.BLACK_TERRACOTTA)
		);
	}

	private static MapColorType addToRegistry(final String id, final MaterialColor color) {
		final SpongeMapColorType mapColorType = new SpongeMapColorType(ResourceKey.minecraft(id), color.colorIndex);
		MapColorTypeStreamGenerator.COLOR_INDEX_MAP.put(mapColorType.getColorIndex(), mapColorType);

		if (color.colorIndex != COLOR_INDEX_MAP.size() - 1) {
			logger.error("Missing an color registry, expected color index: {} next, but got {}. This is most likely a bug, but continuing to allow for further diagnosis", COLOR_INDEX_MAP.size() - 1, color.colorIndex);
		}

		return mapColorType;
	}

	/**
	 * Gets a {@link MapColorType} by its color index.
	 *
	 * @param index Index, by default between 0-51 inclusive
	 * @return {@link MapColorType} if a mapping exists for the given index.
	 */
	public static Optional<MapColorType> getByColorIndex(int index) {
		return Optional.ofNullable(MapColorTypeStreamGenerator.COLOR_INDEX_MAP.get(index));
	}
}