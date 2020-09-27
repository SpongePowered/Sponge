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

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.map.color.MapShade;
import org.spongepowered.api.map.color.MapShades;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.map.color.SpongeMapShade;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MapShadeRegistryModule implements CatalogRegistryModule<MapShade> {
	@RegisterCatalog(MapShades.class)
	private final Map<String, MapShade> mapShades = new HashMap<>();
	private final Map<Integer, MapShade> shadeNumMapShadeMap = new HashMap<>();

	@Override
	public Optional<MapShade> getById(String id) {
		return Optional.ofNullable(mapShades.get(id));
	}

	@Override
	public Collection<MapShade> getAll() {
		return ImmutableSet.copyOf(mapShades.values());
	}

	@Override
	public void registerDefaults() {
		this.register(new SpongeMapShade("darker", "Darker", 0, 180));
		this.register(new SpongeMapShade("dark", "Dark", 1, 220));
		this.register(new SpongeMapShade("base", "Base", 2, 255)); // Note multiplying then dividing by 255 causes no change, thus this is the base shade
		this.register(new SpongeMapShade("darkest", "Darkest", 3, 135));
	}

	private void register(MapShade mapShade) {
		if (this.mapShades.containsKey(mapShade.getId())
				|| this.mapShades.containsValue(mapShade)) {
			throw new IllegalStateException("Duplicate MapShade registered. Id: " + mapShade.getId());
		}
		if (this.shadeNumMapShadeMap.containsKey(((SpongeMapShade)mapShade).getShadeNum())) {
			throw new IllegalStateException("Duplicate ShadeNum registered for MapShade id: " + mapShade.getId());
		}
		this.mapShades.put(mapShade.getId(), mapShade);
		this.shadeNumMapShadeMap.put(((SpongeMapShade) mapShade).getShadeNum(), mapShade);
	}

	public Optional<MapShade> getByShadeNum(int shadeNum) {
		return Optional.ofNullable(this.shadeNumMapShadeMap.get(shadeNum));
	}

	private MapShadeRegistryModule() {}

	public static MapShadeRegistryModule getInstance() {
		return MapShadeRegistryModule.Holder.INSTANCE;
	}

	static final class Holder {
		static final MapShadeRegistryModule INSTANCE = new MapShadeRegistryModule();
	}
}
