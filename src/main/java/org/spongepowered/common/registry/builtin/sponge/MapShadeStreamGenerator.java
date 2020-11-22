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
import org.apache.logging.log4j.LogManager;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.map.color.MapShade;
import org.spongepowered.api.map.decoration.MapDecorationType;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.map.color.SpongeMapShade;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class MapShadeStreamGenerator {
	private static final Map<Integer, MapShade> REGISTRY_MAP = new HashMap<>();

	public static Stream<MapShade> stream() {
		return Stream.of(
				addToRegistry(new SpongeMapShade(ResourceKey.sponge("darker"), 	0, 180)),
				addToRegistry(new SpongeMapShade(ResourceKey.sponge("dark"), 	1, 220)),
				addToRegistry(new SpongeMapShade(ResourceKey.sponge("base"), 	2, 255)),
				addToRegistry(new SpongeMapShade(ResourceKey.sponge("darkest"), 3, 135))
		);
	}

	private static MapShade addToRegistry(SpongeMapShade shade) {
		if (MapShadeStreamGenerator.REGISTRY_MAP.containsKey(shade.getShadeNum())) {
			throw new IllegalArgumentException("Shade with shade num: " + shade.getShadeNum() + " already registered!");
		}
		MapShadeStreamGenerator.REGISTRY_MAP.put(shade.getShadeNum(), shade);
		return shade;
	}

	/**
	 * Gets a map shade by a given color index
	 * @param index Index, by default 0-3 inclusive
	 * @return {@link MapShade} if it exists
	 */
	public static Optional<MapShade> getByShadeNum(int index) {
		return Optional.ofNullable(MapShadeStreamGenerator.REGISTRY_MAP.get(index));
	}
}
