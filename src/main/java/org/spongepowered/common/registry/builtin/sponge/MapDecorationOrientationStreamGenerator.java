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

import org.apache.logging.log4j.LogManager;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.map.decoration.orientation.MapDecorationOrientation;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.map.decoration.orientation.SpongeMapDecorationOrientation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class MapDecorationOrientationStreamGenerator {
	private static final Map<Integer, MapDecorationOrientation> ORIENTATION_MAP = new HashMap<>();

	public static Optional<MapDecorationOrientation> getByInt(final int rot) {
		return Optional.ofNullable(MapDecorationOrientationStreamGenerator.ORIENTATION_MAP.get(rot));
	}

	public static Stream<MapDecorationOrientation> stream() {
		return Stream.of(
				register("south",				0),
				register("south_southwest",		1),
				register("southwest",			2),
				register("west_southwest",		3),
				register("west",				4),
				register("west_northwest",		5),
				register("northwest",			6),
				register("north_northwest",		7),
				register("north",				8),
				register("north_northeast",		9),
				register("northeast",			10),
				register("east_northeast",		11),
				register("east",				12),
				register("east_southeast",		13),
				register("southeast",			14),
				register("south_southeast",		15)
		);
	}

	private static MapDecorationOrientation register(final String id, final int orientationNumber) {
		MapDecorationOrientation orientation = new SpongeMapDecorationOrientation(orientationNumber);
		MapDecorationOrientationStreamGenerator.ORIENTATION_MAP.put(orientationNumber, orientation);

		return orientation;
	}
}
