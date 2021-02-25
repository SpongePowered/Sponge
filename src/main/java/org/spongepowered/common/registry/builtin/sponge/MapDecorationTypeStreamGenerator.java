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

import net.minecraft.world.level.saveddata.maps.MapDecoration;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.map.decoration.MapDecorationType;
import org.spongepowered.common.map.decoration.SpongeMapDecorationBannerType;
import org.spongepowered.common.map.decoration.SpongeMapDecorationType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MapDecorationTypeStreamGenerator {
	private static final Map<MapDecoration.Type, MapDecorationType> MC_TYPE_MAPPER = new HashMap<>();

	public static Optional<MapDecorationType> getByMcType(MapDecoration.Type type) {
		return Optional.ofNullable(MapDecorationTypeStreamGenerator.MC_TYPE_MAPPER.get(type));
	}

	public static Stream<MapDecorationType> stream() {
		return Stream.of(
				addToRegistry(MapDecoration.Type.PLAYER),
				addToRegistry(MapDecoration.Type.FRAME),
				addToRegistry(MapDecoration.Type.RED_MARKER),
				addToRegistry(MapDecoration.Type.BLUE_MARKER),
				addToRegistry(MapDecoration.Type.TARGET_X),
				addToRegistry(MapDecoration.Type.TARGET_POINT),
				addToRegistry(MapDecoration.Type.PLAYER_OFF_MAP),
				addToRegistry(MapDecoration.Type.PLAYER_OFF_LIMITS),
				addToRegistry(MapDecoration.Type.MANSION),
				addToRegistry(MapDecoration.Type.MONUMENT),

				addToRegistry(MapDecoration.Type.BANNER_WHITE, 		DyeColors.WHITE),
				addToRegistry(MapDecoration.Type.BANNER_ORANGE, 	DyeColors.ORANGE),
				addToRegistry(MapDecoration.Type.BANNER_MAGENTA, 	DyeColors.MAGENTA),
				addToRegistry(MapDecoration.Type.BANNER_LIGHT_BLUE, DyeColors.LIGHT_BLUE),
				addToRegistry(MapDecoration.Type.BANNER_YELLOW, 	DyeColors.YELLOW),
				addToRegistry(MapDecoration.Type.BANNER_LIME, 		DyeColors.LIME),
				addToRegistry(MapDecoration.Type.BANNER_PINK, 		DyeColors.PINK),
				addToRegistry(MapDecoration.Type.BANNER_GRAY, 		DyeColors.GRAY),
				addToRegistry(MapDecoration.Type.BANNER_LIGHT_GRAY, DyeColors.LIGHT_GRAY),
				addToRegistry(MapDecoration.Type.BANNER_CYAN, 		DyeColors.CYAN),
				addToRegistry(MapDecoration.Type.BANNER_PURPLE, 	DyeColors.PURPLE),
				addToRegistry(MapDecoration.Type.BANNER_BLUE, 		DyeColors.BLUE),
				addToRegistry(MapDecoration.Type.BANNER_BROWN, 		DyeColors.BROWN),
				addToRegistry(MapDecoration.Type.BANNER_GREEN, 		DyeColors.GREEN),
				addToRegistry(MapDecoration.Type.BANNER_RED, 		DyeColors.RED),
				addToRegistry(MapDecoration.Type.BANNER_BLACK, 		DyeColors.BLACK),

				addToRegistry(MapDecoration.Type.RED_X)
		);
	}

	private static MapDecorationType addToRegistry(final MapDecoration.Type type) {
		SpongeMapDecorationType mapDecorationType = new SpongeMapDecorationType(type);
		addToRegistry(mapDecorationType);
		return mapDecorationType;
	}

	private static MapDecorationType addToRegistry(final MapDecoration.Type type, final Supplier<DyeColor> dyeColor) {
		SpongeMapDecorationType mapDecorationType = new SpongeMapDecorationBannerType(/*ResourceKey.minecraft(type.name().toLowerCase()), */type, dyeColor);
		addToRegistry(mapDecorationType);
		return mapDecorationType;
	}

	private static void addToRegistry(final SpongeMapDecorationType type) {
		MapDecorationTypeStreamGenerator.MC_TYPE_MAPPER.put(type.getType(), type);
	}
}
