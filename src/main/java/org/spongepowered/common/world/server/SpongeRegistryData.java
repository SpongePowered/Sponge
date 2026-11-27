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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.data.DataUtil;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A {@link SavedData} implementation that stores per-dimension Sponge data: the world's unique {@link UUID}, its
 * {@link ResourceKey}, the custom data of its properties, the players its chunks track, and its level data. The
 * default world's level data is not stored here, since the root {@code level.dat} holds it.
 *
 * <p>This is persisted as {@code data/sponge/registry.dat} within each dimension's
 * saved data directory.</p>
 */
public final class SpongeRegistryData extends SavedData {

    public static final Codec<SpongeRegistryData> CODEC = RecordCodecBuilder.create(
        i -> i.group(
                UUIDUtil.CODEC.fieldOf("uuid").forGetter(SpongeRegistryData::uniqueId),
                ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("key").forGetter(SpongeRegistryData::resourceKey),
                CompoundTag.CODEC.optionalFieldOf("level_data").forGetter(SpongeRegistryData::levelData),
                CompoundTag.CODEC.optionalFieldOf("custom_data").forGetter(SpongeRegistryData::customData),
                SpongeMapUUIDData.MAP_INDEX_CODEC.optionalFieldOf("player_uuids", Map.of()).forGetter(SpongeRegistryData::playerUniqueIds)
            )
            .apply(i, SpongeRegistryData::new)
    );

    // We don't use LEVEL, since it would fix the whole file as if it were a level.dat. No fix touches the command
    // storage type, and the level data inside keeps its own version and is fixed as it is read
    public static final SavedDataType<SpongeRegistryData> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath("sponge", "registry"), SpongeRegistryData::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private UUID uniqueId;
    private @Nullable ResourceKey<Level> key;
    private @Nullable CompoundTag levelData;
    private @Nullable CompoundTag customData;
    private Map<Integer, UUID> playerUniqueIds = Map.of();

    public SpongeRegistryData() {
        this.uniqueId = UUID.randomUUID();
    }

    public SpongeRegistryData(final UUID uniqueId, final Optional<ResourceKey<Level>> key, final Map<Integer, UUID> playerUniqueIds) {
        this(uniqueId, key, Optional.empty(), Optional.empty(), playerUniqueIds);
    }

    private SpongeRegistryData(final UUID uniqueId, final Optional<ResourceKey<Level>> key, final Optional<CompoundTag> levelData,
        final Optional<CompoundTag> customData, final Map<Integer, UUID> playerUniqueIds) {
        this.uniqueId = uniqueId;
        this.key = key.orElse(null);
        this.levelData = levelData.orElse(null);
        this.customData = customData.orElse(null);
        this.playerUniqueIds = playerUniqueIds;
    }

    public UUID uniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(final UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.setDirty();
    }

    public org.spongepowered.api.@Nullable ResourceKey key() {
        if (this.key == null) {
            return null;
        }
        return (org.spongepowered.api.ResourceKey) (Object) this.key.identifier();
    }

    public Optional<ResourceKey<Level>> resourceKey() {
        return Optional.ofNullable(this.key);
    }

    public void setKey(final org.spongepowered.api.ResourceKey key) {
        this.key = SpongeWorldManager.createRegistryKey(key);
        this.setDirty();
    }

    /** Gets the level data of a world other than the default one, as {@link PrimaryLevelData#createTag} writes it */
    public Optional<CompoundTag> levelData() {
        return Optional.ofNullable(this.levelData);
    }

    public Optional<CompoundTag> customData() {
        return Optional.ofNullable(this.customData);
    }

    /** Gets the players that the world's chunks track, by the index the chunks keep */
    public Map<Integer, UUID> playerUniqueIds() {
        return this.playerUniqueIds;
    }

    /**
     * Stores the world's properties: their custom data, their players, and their level data unless the root
     * {@code level.dat} holds it.
     */
    public void store(final PrimaryLevelData properties, final boolean levelData) {
        this.levelData = levelData ? properties.createTag(null) : null;
        this.customData = DataUtil.syncDataToTag(properties) ? ((DataCompoundHolder) properties).data$getCompound() : null;
        this.playerUniqueIds = Map.copyOf(((PrimaryLevelDataBridge) properties).bridge$spongeData().playerUniqueIds());
        this.setDirty();
    }

    /** Applies the custom data kept for a world to its properties, as api-18 did from its {@code level.dat} */
    public static void applyCustomData(final PrimaryLevelData properties, final CompoundTag customData) {
        final DataCompoundHolder holder = (DataCompoundHolder) properties;
        holder.data$setCompound(customData.copy());
        DataUtil.syncTagToData(holder);
        holder.data$setCompound(null);
    }
}
