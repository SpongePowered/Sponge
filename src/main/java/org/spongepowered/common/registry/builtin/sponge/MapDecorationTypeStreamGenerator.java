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

import net.minecraft.world.storage.MapDecoration;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.map.decoration.MapDecorationType;
import org.spongepowered.api.util.Tuple;
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

	public static Stream<Tuple<MapDecorationType, MapDecoration.Type>> stream() {
		return Stream.of(
				makeTupleAndAddToRegistry(MapDecoration.Type.PLAYER),
				makeTupleAndAddToRegistry(MapDecoration.Type.FRAME),
				makeTupleAndAddToRegistry(MapDecoration.Type.RED_MARKER),
				makeTupleAndAddToRegistry(MapDecoration.Type.BLUE_MARKER),
				makeTupleAndAddToRegistry(MapDecoration.Type.TARGET_X),
				makeTupleAndAddToRegistry(MapDecoration.Type.TARGET_POINT),
				makeTupleAndAddToRegistry(MapDecoration.Type.PLAYER_OFF_MAP),
				makeTupleAndAddToRegistry(MapDecoration.Type.PLAYER_OFF_LIMITS),
				makeTupleAndAddToRegistry(MapDecoration.Type.MANSION),
				makeTupleAndAddToRegistry(MapDecoration.Type.MONUMENT),

				makeTupleAndAddToRegistry(MapDecoration.Type.BANNER_WHITE, 		DyeColors.WHITE),
				makeTupleAndAddToRegistry(MapDecoration.Type.BANNER_ORANGE, 	DyeColors.ORANGE),
				makeTupleAndAddToRegistry(MapDecoration.Type.BANNER_MAGENTA, 	DyeColors.MAGENTA),
				makeTupleAndAddToRegistry(MapDecoration.Type.BANNER_LIGHT_BLUE, DyeColors.LIGHT_BLUE),
				makeTupleAndAddToRegistry(MapDecoration.Type.BANNER_YELLOW, 	DyeColors.YELLOW),
				makeTupleAndAddToRegistry(MapDecoration.Type.BANNER_LIME, 		DyeColors.LIME),
				makeTupleAndAddToRegistry(MapDecoration.Type.BANNER_PINK, 		DyeColors.PINK),
				makeTupleAndAddToRegistry(MapDecoration.Type.BANNER_GRAY, 		DyeColors.GRAY),
				makeTupleAndAddToRegistry(MapDecoration.Type.BANNER_LIGHT_GRAY, DyeColors.LIGHT_GRAY),
				makeTupleAndAddToRegistry(MapDecoration.Type.BANNER_CYAN, 		DyeColors.CYAN),
				makeTupleAndAddToRegistry(MapDecoration.Type.BANNER_PURPLE, 	DyeColors.PURPLE),
				makeTupleAndAddToRegistry(MapDecoration.Type.BANNER_BLUE, 		DyeColors.BLUE),
				makeTupleAndAddToRegistry(MapDecoration.Type.BANNER_BROWN, 		DyeColors.BROWN),
				makeTupleAndAddToRegistry(MapDecoration.Type.BANNER_GREEN, 		DyeColors.GREEN),
				makeTupleAndAddToRegistry(MapDecoration.Type.BANNER_RED, 		DyeColors.RED),
				makeTupleAndAddToRegistry(MapDecoration.Type.BANNER_BLACK, 		DyeColors.BLACK),

				makeTupleAndAddToRegistry(MapDecoration.Type.RED_X)
		);
	}

	private static Tuple<MapDecorationType, MapDecoration.Type> makeTupleAndAddToRegistry(final MapDecoration.Type type) {
		SpongeMapDecorationType mapDecorationType = new SpongeMapDecorationType(ResourceKey.minecraft(type.name().toLowerCase()), type);
		addToRegistry(mapDecorationType);
		return Tuple.of(mapDecorationType, type);
	}

	private static Tuple<MapDecorationType, MapDecoration.Type> makeTupleAndAddToRegistry(final MapDecoration.Type type, final Supplier<DyeColor> dyeColor) {
		SpongeMapDecorationType mapDecorationType = new SpongeMapDecorationBannerType(ResourceKey.minecraft(type.name().toLowerCase()), type, dyeColor);
		addToRegistry(mapDecorationType);
		return Tuple.of(mapDecorationType, type);
	}

	private static void addToRegistry(final SpongeMapDecorationType type) {
		MapDecorationTypeStreamGenerator.MC_TYPE_MAPPER.put(type.getType(), type);
	}
}
