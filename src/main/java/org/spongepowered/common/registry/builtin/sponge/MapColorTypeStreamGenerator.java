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
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.map.color.MapColorType;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.map.color.SpongeMapColorType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class MapColorTypeStreamGenerator {
	private static final Map<Integer, MapColorType> COLOR_INDEX_MAP = new HashMap<>();

	public static Stream<Tuple<MapColorType, MaterialColor>> stream() {
		return Stream.of(
				makeTupleAndAddToRegistry("air", 					MaterialColor.AIR),
				makeTupleAndAddToRegistry("grass", 					MaterialColor.GRASS),
				makeTupleAndAddToRegistry("sand", 					MaterialColor.SAND),
				makeTupleAndAddToRegistry("wool", 					MaterialColor.WOOL),
				makeTupleAndAddToRegistry("tnt", 					MaterialColor.TNT),
				makeTupleAndAddToRegistry("ice", 					MaterialColor.ICE),
				makeTupleAndAddToRegistry("iron", 					MaterialColor.IRON),
				makeTupleAndAddToRegistry("foliage", 				MaterialColor.FOLIAGE),
				makeTupleAndAddToRegistry("snow", 					MaterialColor.SNOW),
				makeTupleAndAddToRegistry("clay", 					MaterialColor.CLAY),
				makeTupleAndAddToRegistry("stone", 					MaterialColor.STONE),
				makeTupleAndAddToRegistry("water", 					MaterialColor.WATER),
				makeTupleAndAddToRegistry("wood", 					MaterialColor.WOOD),
				makeTupleAndAddToRegistry("quartz", 				MaterialColor.QUARTZ),
				makeTupleAndAddToRegistry("adobe", 					MaterialColor.ADOBE),
				makeTupleAndAddToRegistry("magenta", 				MaterialColor.MAGENTA),
				makeTupleAndAddToRegistry("light_blue",				MaterialColor.LIGHT_BLUE),
				makeTupleAndAddToRegistry("yellow", 				MaterialColor.YELLOW),
				makeTupleAndAddToRegistry("pink", 					MaterialColor.LIME),
				makeTupleAndAddToRegistry("gray", 					MaterialColor.GRAY),
				makeTupleAndAddToRegistry("light_gray",				MaterialColor.LIGHT_GRAY),
				makeTupleAndAddToRegistry("cyan", 					MaterialColor.CYAN),
				makeTupleAndAddToRegistry("purple", 				MaterialColor.PURPLE),
				makeTupleAndAddToRegistry("blue",	 				MaterialColor.BLUE),
				makeTupleAndAddToRegistry("brown", 					MaterialColor.BROWN),
				makeTupleAndAddToRegistry("green", 					MaterialColor.GREEN),
				makeTupleAndAddToRegistry("red", 					MaterialColor.RED),
				makeTupleAndAddToRegistry("black", 					MaterialColor.BLACK),
				makeTupleAndAddToRegistry("gold", 					MaterialColor.GOLD),
				makeTupleAndAddToRegistry("diamond", 				MaterialColor.DIAMOND),
				makeTupleAndAddToRegistry("lapis_lazuli", 			MaterialColor.LAPIS),
				makeTupleAndAddToRegistry("emerald", 				MaterialColor.EMERALD),
				makeTupleAndAddToRegistry("obsidian", 				MaterialColor.OBSIDIAN),
				makeTupleAndAddToRegistry("netherrack", 			MaterialColor.NETHERRACK),
				makeTupleAndAddToRegistry("white_terracotta", 		MaterialColor.WHITE_TERRACOTTA),
				makeTupleAndAddToRegistry("orange_terracotta", 		MaterialColor.ORANGE_TERRACOTTA),
				makeTupleAndAddToRegistry("magenta_terracotta", 	MaterialColor.MAGENTA_TERRACOTTA),
				makeTupleAndAddToRegistry("light_blue_terracotta", 	MaterialColor.LIGHT_BLUE_TERRACOTTA),
				makeTupleAndAddToRegistry("yellow_terracotta", 		MaterialColor.YELLOW_TERRACOTTA),
				makeTupleAndAddToRegistry("lime_terracotta", 		MaterialColor.LIME_TERRACOTTA),
				makeTupleAndAddToRegistry("pink_terracotta", 		MaterialColor.PINK_TERRACOTTA),
				makeTupleAndAddToRegistry("gray_terracotta", 		MaterialColor.GRAY_TERRACOTTA),
				makeTupleAndAddToRegistry("light_gray_terracotta", 	MaterialColor.LIGHT_GRAY_TERRACOTTA),
				makeTupleAndAddToRegistry("cyan_terracotta", 		MaterialColor.CYAN_TERRACOTTA),
				makeTupleAndAddToRegistry("purple_terracotta", 		MaterialColor.PURPLE_TERRACOTTA),
				makeTupleAndAddToRegistry("blue_terracotta", 		MaterialColor.BLUE_TERRACOTTA),
				makeTupleAndAddToRegistry("brown_terracotta", 		MaterialColor.BROWN_TERRACOTTA),
				makeTupleAndAddToRegistry("green_terracotta", 		MaterialColor.GREEN_TERRACOTTA),
				makeTupleAndAddToRegistry("red_terracotta", 		MaterialColor.RED_TERRACOTTA),
				makeTupleAndAddToRegistry("black_terracotta", 		MaterialColor.BLACK_TERRACOTTA)
		);
	}

	private static Tuple<MapColorType, MaterialColor> makeTupleAndAddToRegistry(final String id, final MaterialColor color) {
		final SpongeMapColorType mapColorType = new SpongeMapColorType(ResourceKey.minecraft(id), color.colorIndex);
		MapColorTypeStreamGenerator.COLOR_INDEX_MAP.put(mapColorType.getColorIndex(), mapColorType);

		return Tuple.of(mapColorType, color);
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