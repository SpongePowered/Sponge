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
package org.spongepowered.common.bridge.world.level.storage;

import com.google.common.collect.BiMap;
import com.mojang.serialization.Dynamic;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.inheritable.WorldConfig;

import java.util.Optional;
import java.util.UUID;

public interface PrimaryLevelDataBridge {

    boolean bridge$valid();

    @Nullable ServerLevel bridge$world();

    @Nullable DimensionType bridge$dimensionType();

    void bridge$dimensionType(DimensionType dimensionType, boolean updatePlayers);

    UUID bridge$uniqueId();

    void bridge$setUniqueId(UUID uniqueId);

    boolean bridge$customDifficulty();

    boolean bridge$customGameType();

    boolean bridge$customSpawnPosition();

    void bridge$forceSetDifficulty(Difficulty difficulty);

    Optional<Boolean> bridge$pvp();

    void bridge$setPvp(@Nullable Boolean pvp);

    boolean bridge$loadOnStartup();

    void bridge$setLoadOnStartup(boolean loadOnStartup);

    boolean bridge$performsSpawnLogic();

    void bridge$setPerformsSpawnLogic(boolean keepLoaded);

    Optional<SerializationBehavior> bridge$serializationBehavior();

    void bridge$setSerializationBehavior(@Nullable SerializationBehavior behavior);

    Optional<Component> bridge$displayName();

    void bridge$setDisplayName(@Nullable Component displayName);

    Optional<Integer> bridge$viewDistance();

    void bridge$setViewDistance(@Nullable Integer viewDistance);

    InheritableConfigHandle<WorldConfig> bridge$configAdapter();

    void bridge$configAdapter(InheritableConfigHandle<WorldConfig> adapter);

    void bridge$populateFromDimension(LevelStem dimension);

    void bridge$setMapUUIDIndex(BiMap<Integer, UUID> index);

    BiMap<Integer, UUID> bridge$getMapUUIDIndex();

    int bridge$getIndexForUniqueId(UUID uuid);

    Optional<UUID> bridge$getUniqueIdForIndex(int ownerIndex);

    void bridge$readSpongeLevelData(Dynamic<Tag> impl$spongeLevelData);

    CompoundTag bridge$writeSpongeLevelData();
}
