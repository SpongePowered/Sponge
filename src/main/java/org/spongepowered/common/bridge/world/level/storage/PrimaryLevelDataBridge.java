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
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.SerializationBehavior;

import java.util.Optional;
import java.util.UUID;

public interface PrimaryLevelDataBridge extends ServerLevelDataBridge {

    void bridge$dimensionType(DimensionType dimensionType, boolean updatePlayers);

    void bridge$forceSetDifficulty(Difficulty difficulty);

    void bridge$setPvp(@Nullable Boolean pvp);

    void bridge$setLoadOnStartup(boolean loadOnStartup);

    void bridge$setPerformsSpawnLogic(boolean keepLoaded);

    void bridge$setSerializationBehavior(@Nullable SerializationBehavior behavior);

    void bridge$setDisplayName(@Nullable Component displayName);

    void bridge$setViewDistance(@Nullable Integer viewDistance);

    void bridge$populateFromLevelStem(LevelStem dimension);

    BiMap<Integer, UUID> bridge$getMapUUIDIndex();

    int bridge$getIndexForUniqueId(UUID uuid);

    Optional<UUID> bridge$getUniqueIdForIndex(int ownerIndex);

    void bridge$hardcore(boolean hardcore);

    void bridge$allowCommands(boolean commands);

    void bridge$readSpongeLevelData(Dynamic<Tag> impl$spongeLevelData);

    CompoundTag bridge$writeSpongeLevelData();
}
