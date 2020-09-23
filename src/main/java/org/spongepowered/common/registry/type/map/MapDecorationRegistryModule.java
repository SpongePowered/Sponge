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
import net.minecraft.world.storage.MapDecoration;
import org.spongepowered.api.map.decoration.MapDecorationType;
import org.spongepowered.api.map.decoration.MapDecorationTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.map.decoration.SpongeMapDecorationType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class MapDecorationRegistryModule implements CatalogRegistryModule<MapDecorationType> {

    @RegisterCatalog(MapDecorationTypes.class)
    private final Map<String, MapDecorationType> mapDecorationTypeMappings = new HashMap<>();

    private final Map<MapDecoration.Type, MapDecorationType> mcToSpongeTypeMappings = new HashMap<>();
    @Override
    public Optional<MapDecorationType> getById(String id) {
        return Optional.ofNullable(mapDecorationTypeMappings.get(id));
    }

    @Override
    public Collection<MapDecorationType> getAll() {
        return ImmutableSet.copyOf(mapDecorationTypeMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.register(new SpongeMapDecorationType(MapDecoration.Type.PLAYER, "player_marker", "Player Marker"));
        this.register(new SpongeMapDecorationType(MapDecoration.Type.FRAME, "green_marker", "Green Marker"));
        this.register(new SpongeMapDecorationType(MapDecoration.Type.RED_MARKER, "red_marker", "Red Marker"));
        this.register(new SpongeMapDecorationType(MapDecoration.Type.BLUE_MARKER, "blue_marker", "Blue Marker"));
        this.register(new SpongeMapDecorationType(MapDecoration.Type.TARGET_X, "target_x", "Target X"));
        this.register(new SpongeMapDecorationType(MapDecoration.Type.TARGET_POINT, "target_point", "Target Point"));
        this.register(new SpongeMapDecorationType(MapDecoration.Type.PLAYER_OFF_MAP, "player_off_map", "Player Off Map"));
        this.register(new SpongeMapDecorationType(MapDecoration.Type.PLAYER_OFF_LIMITS, "player_off_limits", "Player Off Limits"));
        this.register(new SpongeMapDecorationType(MapDecoration.Type.MANSION, "mansion", "Mansion"));
        this.register(new SpongeMapDecorationType(MapDecoration.Type.MONUMENT, "monument", "Monument"));
    }

    public void register(MapDecorationType type) {
        String key = type.getId().toLowerCase(Locale.ENGLISH);
        MapDecoration.Type mcType = ((SpongeMapDecorationType)type).getType();
        if (!mapDecorationTypeMappings.containsKey(key)
                && !mcToSpongeTypeMappings.containsKey(mcType)) {
            mapDecorationTypeMappings.put(key, type);
            mcToSpongeTypeMappings.put(mcType, type);
        }

    }

    private MapDecorationRegistryModule() {}

    public static Optional<MapDecorationType> getByMcType(MapDecoration.Type type) {
        return Optional.ofNullable(Holder.INSTANCE.mcToSpongeTypeMappings.get(type));
    }

    public static MapDecorationRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    static final class Holder {
        static final MapDecorationRegistryModule INSTANCE = new MapDecorationRegistryModule();
    }
}
