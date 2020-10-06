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
package org.spongepowered.common.registry.type.map;

import org.spongepowered.api.map.decoration.orientation.MapDecorationOrientation;
import org.spongepowered.api.map.decoration.orientation.MapDecorationOrientations;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.map.decoration.orientation.SpongeMapDecorationOrientation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MapDecorationOrientationRegistryModule implements CatalogRegistryModule<MapDecorationOrientation>  {
	@RegisterCatalog(MapDecorationOrientations.class)
	private final Map<String, MapDecorationOrientation> orientationMappings = new HashMap<>();

	private final Map<Integer, MapDecorationOrientation> intOrientationMappings = new HashMap<>();

	@Override
	public Optional<MapDecorationOrientation> getById(String id) {
		return Optional.of(this.orientationMappings.get(id));
	}

	@Override
	public Collection<MapDecorationOrientation> getAll() {
		return this.orientationMappings.values();
	}

	public Optional<MapDecorationOrientation> getByOrientationId(int id) {
		return Optional.of(intOrientationMappings.get(id));
	}

	@Override
	public void registerDefaults() {
		this.register(new SpongeMapDecorationOrientation("south", 			"South", 			0));
		this.register(new SpongeMapDecorationOrientation("south_southwest", "South Southwest", 	1));
		this.register(new SpongeMapDecorationOrientation("southwest", 		"Southwest", 		2));
		this.register(new SpongeMapDecorationOrientation("west_southwest", 	"West Southwest", 	3));
		this.register(new SpongeMapDecorationOrientation("west", 			"West", 				4));
		this.register(new SpongeMapDecorationOrientation("west_northwest", 	"West Northwest", 	5));
		this.register(new SpongeMapDecorationOrientation("northwest", 		"Northwest", 		6));
		this.register(new SpongeMapDecorationOrientation("north_northwest", "North Northwest", 	7));
		this.register(new SpongeMapDecorationOrientation("north", 			"North",				8));
		this.register(new SpongeMapDecorationOrientation("north_northeast", "North Northeast",	9));
		this.register(new SpongeMapDecorationOrientation("northeast", 		"Northeast", 		10));
		this.register(new SpongeMapDecorationOrientation("east_northeast", 	"East Northeast", 	11));
		this.register(new SpongeMapDecorationOrientation("east", 			"East", 				12));
		this.register(new SpongeMapDecorationOrientation("east_southeast", 	"East Southeast", 	13));
		this.register(new SpongeMapDecorationOrientation("southeast", 		"Southeast", 		14));
		this.register(new SpongeMapDecorationOrientation("south_southeast", "South Southeast", 	15));
	}

	private void register(SpongeMapDecorationOrientation orientation) {
		if (orientationMappings.containsKey(orientation.getId())) {
			throw new IllegalStateException("More than one MapDecorationOrientation registered with the same id!");
		}
		if (intOrientationMappings.containsKey(orientation.getOrientationNumber())) {
			throw new IllegalStateException("More than one MapDecorationOrientation registered with the same orientation id");
		}

		orientationMappings.put(orientation.getId(), orientation);
		intOrientationMappings.put(orientation.getOrientationNumber(), orientation);
	}

	private MapDecorationOrientationRegistryModule() {}

	public static MapDecorationOrientationRegistryModule getInstance() {
		return MapDecorationOrientationRegistryModule.Holder.INSTANCE;
	}

	static final class Holder {
		static final MapDecorationOrientationRegistryModule INSTANCE = new MapDecorationOrientationRegistryModule();
	}
}
