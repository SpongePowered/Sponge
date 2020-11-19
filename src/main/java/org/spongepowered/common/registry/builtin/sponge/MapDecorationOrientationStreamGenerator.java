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

	public static Stream<Tuple<MapDecorationOrientation, Integer>> stream() {
		return Stream.of(
				makeTupleAndRegister("south",				0),
				makeTupleAndRegister("south_southwest",		1),
				makeTupleAndRegister("southwest",			2),
				makeTupleAndRegister("west_southwest",		3),
				makeTupleAndRegister("west",				4),
				makeTupleAndRegister("west_northwest",		5),
				makeTupleAndRegister("northwest",			6),
				makeTupleAndRegister("north_northwest",		7),
				makeTupleAndRegister("north",				8),
				makeTupleAndRegister("north_northeast",		9),
				makeTupleAndRegister("northeast",			10),
				makeTupleAndRegister("east_northeast",		11),
				makeTupleAndRegister("east",				12),
				makeTupleAndRegister("east_southeast",		13),
				makeTupleAndRegister("southeast",			14),
				makeTupleAndRegister("south_southeast",		15)
		);
	}

	private static Tuple<MapDecorationOrientation, Integer> makeTupleAndRegister(final String id, final int orientationNumber) {
		MapDecorationOrientation orientation = new SpongeMapDecorationOrientation(ResourceKey.sponge(id), orientationNumber);
		MapDecorationOrientationStreamGenerator.ORIENTATION_MAP.put(orientationNumber, orientation);

		return Tuple.of(orientation, orientationNumber);
	}
}
