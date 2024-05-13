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
package org.spongepowered.common.data.provider.world;

import net.minecraft.world.level.GameType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.border.WorldBorder;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.generation.config.WorldGenerationConfig;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.weather.SpongeWeather;

public final class WorldPropertiesData {

    private WorldPropertiesData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(LevelData.class)
                    .create(Keys.SPAWN_POSITION)
                        .get(h -> VecHelper.toVector3i(h.getSpawnPos()))
                    .create(Keys.HARDCORE)
                        .get(LevelData::isHardcore)
                    .create(Keys.WORLD_DIFFICULTY)
                        .get(h -> (Difficulty) (Object) h.getDifficulty())
                .asMutable(WritableLevelData.class)
                    .create(Keys.SPAWN_POSITION)
                        .get(h -> VecHelper.toVector3i(h.getSpawnPos()))
                        .set((h, v) -> h.setSpawn(VecHelper.toBlockPos(v), h.getSpawnAngle()))
                .asMutable(ServerLevelData.class)
                    .create(Keys.GAME_MODE)
                        .get(h -> (GameMode) (Object) h.getGameType())
                        .set((h, v) -> h.setGameType((GameType) (Object) v))
                    .create(Keys.COMMANDS)
                        .get(ServerLevelData::isAllowCommands)
                    .create(Keys.INITIALIZED)
                        .get(ServerLevelData::isInitialized)
                    .create(Keys.WORLD_BORDER)
                        .get(h -> (WorldBorder) h.getWorldBorder())
                    .create(Keys.WEATHER)
                        .get(SpongeWeather::of)
                        .set(SpongeWeather::apply)
                .asMutable(PrimaryLevelData.class)
                    .create(Keys.WORLD_DIFFICULTY)
                        .set((h, v) -> h.setDifficulty((net.minecraft.world.Difficulty) (Object) v))
                    .create(Keys.WORLD_GEN_CONFIG)
                        .get(h -> (WorldGenerationConfig) h.worldGenOptions())
                .asMutable(PrimaryLevelDataBridge.class)
                    .create(Keys.WORLD_TYPE)
                        .get(h -> (WorldType) (Object) h.bridge$dimensionType())
                        .set((h, v) -> h.bridge$dimensionType((DimensionType) (Object) v, true))
                    .create(Keys.PVP)
                        .get(h -> h.bridge$pvp().orElseGet(() -> SpongeCommon.server().isPvpAllowed()))
                        .set(PrimaryLevelDataBridge::bridge$setPvp)
                    .create(Keys.HARDCORE)
                        .set(PrimaryLevelDataBridge::bridge$hardcore)
                    .create(Keys.COMMANDS)
                        .set(PrimaryLevelDataBridge::bridge$allowCommands)
                    .create(Keys.SERIALIZATION_BEHAVIOR)
                        .get(h -> h.bridge$serializationBehavior().orElse(SerializationBehavior.AUTOMATIC))
                        .set(PrimaryLevelDataBridge::bridge$setSerializationBehavior)
                    .create(Keys.VIEW_DISTANCE)
                        .get(h -> h.bridge$viewDistance().orElseGet(() -> SpongeCommon.server().getPlayerList().getViewDistance()))
                        .set(PrimaryLevelDataBridge::bridge$setViewDistance)
                    .create(Keys.DISPLAY_NAME)
                        .get(h -> h.bridge$displayName().orElse(null))
                        .set(PrimaryLevelDataBridge::bridge$setDisplayName)
                    .create(Keys.PERFORM_SPAWN_LOGIC)
                        .get(PrimaryLevelDataBridge::bridge$performsSpawnLogic)
                        .set(PrimaryLevelDataBridge::bridge$setPerformsSpawnLogic)
                    .create(Keys.IS_LOAD_ON_STARTUP)
                        .get(PrimaryLevelDataBridge::bridge$loadOnStartup)
                        .set(PrimaryLevelDataBridge::bridge$setLoadOnStartup)


        ;
    }
    // @formatter:on


}
