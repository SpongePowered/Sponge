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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.map.color.MapShade;
import org.spongepowered.api.map.color.MapShades;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.map.SpongeMapShade;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class MapShadeRegistryModule implements CatalogRegistryModule<MapShade> {

    public static MapShadeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(MapShades.class)
    private static final Map<String, MapShade> mapShadeMappings = new HashMap<>();
    private static final MapShade[] indexMappings = new MapShade[4];

    @Override
    public void registerDefaults() {
        registerMapping("darker", 0, 180);
        registerMapping("dark", 1, 220);
        registerMapping("base", 2, 255);
        registerMapping("darkest", 3, 135);
    }

    private void registerMapping(String id, int index, int mulFactor) {
        SpongeMapShade shade =  new SpongeMapShade(id, index, mulFactor);
        mapShadeMappings.put(id, shade);
        indexMappings[index] = shade;
    }

    public MapShade fromIndex(int index) {
        return indexMappings[index];
    }

    @Override
    public Optional<MapShade> getById(String id) {
        checkNotNull(id, "id");
        return Optional.ofNullable(mapShadeMappings.get(id.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<MapShade> getAll() {
        return ImmutableList.copyOf(mapShadeMappings.values());
    }

    MapShadeRegistryModule() { }

    private static final class Holder {
        static final MapShadeRegistryModule INSTANCE = new MapShadeRegistryModule();
    }
}
