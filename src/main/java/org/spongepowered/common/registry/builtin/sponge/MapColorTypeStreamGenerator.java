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

public final class MapColorTypeStreamGenerator {
	private static final Map<Integer, MapColorType> COLOR_INDEX_MAP = new HashMap<>();
	private static final Logger logger = LogManager.getLogger(MapColorTypeStreamGenerator.class.getSimpleName());

	private MapColorTypeStreamGenerator() {
	}

	public static Stream<MapColorType> stream() {
		///////////////////////// ORDER MATTERS HERE ////////////////////
		return Stream.of(
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("air"), 					MaterialColor.AIR),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("grass"), 				MaterialColor.GRASS),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("sand"), 					MaterialColor.SAND),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("wool"), 					MaterialColor.WOOL),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("tnt"), 					MaterialColor.TNT),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("ice"), 					MaterialColor.ICE),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("iron"), 					MaterialColor.IRON),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("foliage"), 				MaterialColor.FOLIAGE),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("snow"), 					MaterialColor.SNOW),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("clay"), 					MaterialColor.CLAY),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("dirt"),					MaterialColor.DIRT),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("stone"), 				MaterialColor.STONE),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("water"), 				MaterialColor.WATER),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("wood"), 					MaterialColor.WOOD),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("quartz)"), 				MaterialColor.QUARTZ),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("adobe"), 				MaterialColor.ADOBE),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("magenta"), 				MaterialColor.MAGENTA),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("light_blue"),			MaterialColor.LIGHT_BLUE),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("yellow"), 				MaterialColor.YELLOW),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("lime"), 					MaterialColor.LIME),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("pink"),					MaterialColor.PINK),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("gray"), 					MaterialColor.GRAY),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("light_gray"),			MaterialColor.LIGHT_GRAY),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("cyan"), 					MaterialColor.CYAN),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("purple"), 				MaterialColor.PURPLE),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("blue"),	 				MaterialColor.BLUE),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("brown"), 				MaterialColor.BROWN),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("green"), 				MaterialColor.GREEN),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("red"), 					MaterialColor.RED),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("black"), 				MaterialColor.BLACK),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("gold"), 					MaterialColor.GOLD),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("diamond"), 				MaterialColor.DIAMOND),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("lapis_lazuli"), 			MaterialColor.LAPIS),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("emerald"), 				MaterialColor.EMERALD),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("obsidian"), 				MaterialColor.OBSIDIAN),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("netherrack"), 			MaterialColor.NETHERRACK),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("white_terracotta"), 		MaterialColor.WHITE_TERRACOTTA),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("orange_terracotta"), 	MaterialColor.ORANGE_TERRACOTTA),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("magenta_terracotta"), 	MaterialColor.MAGENTA_TERRACOTTA),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("light_blue_terracotta"), MaterialColor.LIGHT_BLUE_TERRACOTTA),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("yellow_terracotta"), 	MaterialColor.YELLOW_TERRACOTTA),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("lime_terracotta"), 		MaterialColor.LIME_TERRACOTTA),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("pink_terracotta"), 		MaterialColor.PINK_TERRACOTTA),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("gray_terracotta"), 		MaterialColor.GRAY_TERRACOTTA),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("light_gray_terracotta"), MaterialColor.LIGHT_GRAY_TERRACOTTA),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("cyan_terracotta"), 		MaterialColor.CYAN_TERRACOTTA),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("purple_terracotta"), 	MaterialColor.PURPLE_TERRACOTTA),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("blue_terracotta"), 		MaterialColor.BLUE_TERRACOTTA),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("brown_terracotta"), 		MaterialColor.BROWN_TERRACOTTA),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("green_terracotta"), 		MaterialColor.GREEN_TERRACOTTA),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("red_terracotta"), 		MaterialColor.RED_TERRACOTTA),
				MapColorTypeStreamGenerator.newMapColorType(ResourceKey.minecraft("black_terracotta"), 		MaterialColor.BLACK_TERRACOTTA)
		);
	}

	private static MapColorType newMapColorType(final ResourceKey key, final MaterialColor color) {
		final SpongeMapColorType mapColorType = new SpongeMapColorType(key, color.colorIndex);
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