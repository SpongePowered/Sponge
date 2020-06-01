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
import org.spongepowered.api.map.color.MapColorType;
import org.spongepowered.api.map.color.MapColorTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.map.color.SpongeMapColorType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class MapColorRegistryModule implements CatalogRegistryModule<MapColorType> {

    @RegisterCatalog(MapColorTypes.class)
    private final Map<String, MapColorType> mapColorMappings = new HashMap<>();
    // For reference via colorIndex
    private final List<MapColorType> mapColors = new ArrayList<>();

    public static MapColorRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public Optional<MapColorType> getById(String id) {
        return Optional.ofNullable(mapColorMappings.get(id));
    }

    @Override
    public Collection<MapColorType> getAll() {
        return ImmutableSet.copyOf(mapColorMappings.values());
    }

    @Override
    public void registerDefaults() {
        // Minecraft's map colors
        this.register(new SpongeMapColorType("air", "Air", 0));
        this.register(new SpongeMapColorType("grass", "Grass", 1));
        this.register(new SpongeMapColorType("sand", "Sand", 2));
        this.register(new SpongeMapColorType("wool", "Wool", 3));
        this.register(new SpongeMapColorType("tnt", "TNT", 4));
        this.register(new SpongeMapColorType("ice", "Ice", 5));
        this.register(new SpongeMapColorType("iron", "Iron", 6));
        this.register(new SpongeMapColorType("foliage", "foliage", 7));
        this.register(new SpongeMapColorType("snow", "Snow", 8));
        this.register(new SpongeMapColorType("clay", "Clay", 9));
        this.register(new SpongeMapColorType("dirt", "Dirt", 10));
        this.register(new SpongeMapColorType("stone", "Stone", 11));
        this.register(new SpongeMapColorType("water", "Water", 12));
        this.register(new SpongeMapColorType("wood", "Wood", 13));
        this.register(new SpongeMapColorType("quartz", "Quartz", 14));
        this.register(new SpongeMapColorType("adobe", "Adobe", 15));
        this.register(new SpongeMapColorType("magenta", "Magenta", 16));
        this.register(new SpongeMapColorType("light_blue", "Light Blue", 17));
        this.register(new SpongeMapColorType("yellow", "Yellow", 18));
        this.register(new SpongeMapColorType("lime", "Lime", 19));
        this.register(new SpongeMapColorType("pink", "Pink", 20));
        this.register(new SpongeMapColorType("gray", "Gray", 21));
        this.register(new SpongeMapColorType("silver", "Silver", 22));
        this.register(new SpongeMapColorType("cyan", "Cyan", 23));
        this.register(new SpongeMapColorType("purple", "Purple", 24));
        this.register(new SpongeMapColorType("blue", "Blue", 25));
        this.register(new SpongeMapColorType("brown", "Brown", 26));
        this.register(new SpongeMapColorType("green", "Green", 27));
        this.register(new SpongeMapColorType("red", "Red", 28));
        this.register(new SpongeMapColorType("black", "Black", 29));
        this.register(new SpongeMapColorType("gold", "Gold", 30));
        this.register(new SpongeMapColorType("diamond", "Diamond", 31));
        this.register(new SpongeMapColorType("lapis", "Lapis", 32));
        this.register(new SpongeMapColorType("emerald", "Emerald", 33));
        this.register(new SpongeMapColorType("obsidian", "Obsidian", 34));
        this.register(new SpongeMapColorType("netherrack", "Netherrack", 35));
        this.register(new SpongeMapColorType("white_stained_hardened_clay", "White Stained Hardened Clay", 36));
        this.register(new SpongeMapColorType("orange_stained_hardened_clay", "Orange Stained Hardened Clay", 37));
        this.register(new SpongeMapColorType("magenta_stained_hardened_clay", "Magenta Stained Hardened Clay", 38));
        this.register(new SpongeMapColorType("light_blue_stained_hardened_clay", "Light Blue Stained Hardened_Clay", 39));
        this.register(new SpongeMapColorType("yellow_stained_hardened_clay", "Yellow Stained Hardened Clay", 40));
        this.register(new SpongeMapColorType("lime_stained_hardened_clay", "Lime Stained Hardened Clay", 41));
        this.register(new SpongeMapColorType("pink_stained_hardened_clay", "Pink Stained Hardened Clay", 42));
        this.register(new SpongeMapColorType("gray_stained_hardened_clay", "Gray Stained_Hardened Clay", 43));
        this.register(new SpongeMapColorType("silver_stained_hardened_clay", "Silver Stained Hardened Clay", 44));
        this.register(new SpongeMapColorType("cyan_stained_hardened_clay", "Cyan Stained Hardened Clay", 45));
        this.register(new SpongeMapColorType("purple_stained_hardened_clay", "Purple Stained Hardened Clay", 46));
        this.register(new SpongeMapColorType("blue_stained_hardened_clay", "Blue Stained Hardened Clay", 47));
        this.register(new SpongeMapColorType("brown_stained_hardened_clay", "Brown Stained Hardened Clay", 48));
        this.register(new SpongeMapColorType("green_stained_hardened_clay", "Green stained hardened clay", 49));
        this.register(new SpongeMapColorType("red_stained_hardened_clay", "Red Stained Hardened Clay", 50));
        this.register(new SpongeMapColorType("black_stained_hardened_clay", "Black Stained Hardened Clay", 51));
    }

    public void register(MapColorType type) {
        String key = type.getId().toLowerCase(Locale.ENGLISH);
        // Current size is what the new index will be.
        if (!mapColorMappings.containsKey(key) && mapColors.size() == ((SpongeMapColorType)type).getBaseColor()) {
            mapColorMappings.put(key, type);
            mapColors.add(type);
        }
    }

    public static Optional<MapColorType> getByColorValue(int colorIndex) {
        if (Holder.INSTANCE.mapColors.size() > colorIndex) {
            return Optional.ofNullable(Holder.INSTANCE.mapColors.get(colorIndex));
        }
        return Optional.empty();
    }

    private MapColorRegistryModule() {
    }

    static final class Holder {
        static final MapColorRegistryModule INSTANCE = new MapColorRegistryModule();
    }
}
