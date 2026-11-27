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
package org.spongepowered.common.world.server;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Stores Sponge's map ID to UUID index as server-global SavedData, beside vanilla's maps. Before this, the default
 * world's {@code SpongeData} in {@code level.dat} kept it.
 *
 * <p>Persisted at {@code data/sponge/map_uuids.dat} in the server-level data directory.</p>
 */
public final class SpongeMapUUIDData extends SavedData {

    // NBT CompoundTag keys must be strings, so use string-based int codec for map keys
    static final Codec<Map<Integer, UUID>> MAP_INDEX_CODEC =
        Codec.unboundedMap(Codec.STRING.xmap(Integer::parseInt, String::valueOf), UUIDUtil.CODEC);

    public static final Codec<SpongeMapUUIDData> CODEC = RecordCodecBuilder.create(
        i -> i.group(
                MAP_INDEX_CODEC.optionalFieldOf("map_uuids").forGetter(data -> Optional.of(data.mapIndex())),
                // api-19's migration put the index here, under a key that api-19 never read
                MAP_INDEX_CODEC.optionalFieldOf("legacy_map_uuids").forGetter(data -> Optional.empty()),
                // The player tables api-19 kept here. These now only seed each world's own table
                MAP_INDEX_CODEC.optionalFieldOf("player_uuids", Map.of()).forGetter(data -> data.sharedPlayerUniqueIds),
                UUIDUtil.CODEC.listOf().optionalFieldOf("legacy_player_uuids", List.of()).forGetter(data -> data.legacyPlayerUniqueIds)
            )
            .apply(i, (mapIndex, legacyMapIndex, sharedPlayers, legacyPlayers) ->
                new SpongeMapUUIDData(mapIndex.or(() -> legacyMapIndex).orElse(Map.of()), sharedPlayers, legacyPlayers))
    );

    // We don't use LEVEL, since it would fix the whole file as if it were a level.dat. No fix touches the command
    // storage type
    public static final SavedDataType<SpongeMapUUIDData> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath("sponge", "map_uuids"), SpongeMapUUIDData::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final BiMap<Integer, UUID> mapUUIDIndex;
    private final Map<Integer, UUID> sharedPlayerUniqueIds;
    private final List<UUID> legacyPlayerUniqueIds;

    public SpongeMapUUIDData() {
        this(Map.of(), Map.of(), List.of());
    }

    public SpongeMapUUIDData(final Map<Integer, UUID> mapIndex, final Map<Integer, UUID> sharedPlayerUniqueIds, final List<UUID> legacyPlayerUniqueIds) {
        this.mapUUIDIndex = HashBiMap.create(mapIndex);
        this.sharedPlayerUniqueIds = sharedPlayerUniqueIds;
        this.legacyPlayerUniqueIds = legacyPlayerUniqueIds;
    }

    private Map<Integer, UUID> mapIndex() {
        return new HashMap<>(this.mapUUIDIndex);
    }

    public BiMap<Integer, UUID> mapUUIDIndex() {
        return this.mapUUIDIndex;
    }

    /**
     * Gets the players to seed a world's table with, when api-19 ran the world and it has no table of its own yet.
     * For the default world, this is the api-18 table that api-19's migration moved here, if there is one.
     * Otherwise, it is the table that api-19 shared between all worlds and tracked chunks by.
     */
    public Map<Integer, UUID> seedPlayerUniqueIds(final boolean defaultWorld) {
        if (defaultWorld && !this.legacyPlayerUniqueIds.isEmpty()) {
            return SpongeLevelMigration.indexPlayers(this.legacyPlayerUniqueIds);
        }
        return this.sharedPlayerUniqueIds;
    }
}
