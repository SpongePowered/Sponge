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

import com.google.common.collect.MapMaker;
import net.kyori.adventure.text.Component;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.world.server.SpongeServerLevelData;
import org.spongepowered.common.world.server.SpongeWorldManager;

import java.util.Map;
import java.util.Optional;

public interface ServerLevelDataBridge {
    Map<ServerLevelDataBridge, SpongeServerLevelData> spongeDataMap = new MapMaker().weakKeys().makeMap();

    // overridden for better performance in known subclasses.
    default SpongeServerLevelData bridge$spongeData() {
        return ServerLevelDataBridge.spongeDataMap.computeIfAbsent(this, k -> new SpongeServerLevelData());
    }

    default boolean bridge$valid() {
        return this.bridge$spongeData().key() != null;
    }

    default @Nullable ServerLevel bridge$level() {
        if (!Sponge.isServerAvailable()) {
            return null;
        }

        final ResourceKey key = this.bridge$spongeData().key();
        if (key == null) {
            return null;
        }

        final ServerLevel level = SpongeCommon.server().getLevel(SpongeWorldManager.createRegistryKey(key));
        if (level == null) {
            return null;
        }

        final LevelData levelData = level.getLevelData();
        if (levelData != this) {
            throw new IllegalStateException(String.format("The reference for the data for key '%s' does not match this object. This object is stale.", key));
        }

        return level;
    }

    default @Nullable DimensionType bridge$dimensionType() {
        return null;
    }

    default @Nullable ChunkGenerator bridge$chunkGenerator() {
        return null;
    }

    default LevelStem bridge$levelStem() {
        return new LevelStem(Holder.direct(this.bridge$dimensionType()), this.bridge$chunkGenerator());
    }

    default boolean bridge$customDifficulty() {
        return false;
    }

    default boolean bridge$customGameType() {
        return false;
    }

    default boolean bridge$customSpawnPosition() {
        return false;
    }

    default Optional<Boolean> bridge$pvp() {
        return Optional.empty();
    }

    default boolean bridge$loadOnStartup() {
        return false;
    }

    default boolean bridge$performsSpawnLogic() {
        return true;
    }

    default Optional<SerializationBehavior> bridge$serializationBehavior() {
        return Optional.empty();
    }

    default Optional<Component> bridge$displayName() {
        return Optional.empty();
    }

    default Optional<Integer> bridge$viewDistance() {
        return Optional.empty();
    }

    default void bridge$triggerViewDistanceLogic() {
        final ServerLevel level = this.bridge$level();
        if (level != null) {
            final int distance = this.bridge$viewDistance().orElseGet(() -> level.getServer().getPlayerList().getViewDistance());
            level.getChunkSource().setViewDistance(distance);
            final ClientboundSetChunkCacheRadiusPacket packet = new ClientboundSetChunkCacheRadiusPacket(distance);
            level.players().forEach(p -> p.connection.send(packet));
        }
    }
}
