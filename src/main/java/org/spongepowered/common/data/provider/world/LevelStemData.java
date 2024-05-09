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

import net.minecraft.world.level.dimension.LevelStem;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.generation.ChunkGenerator;
import org.spongepowered.common.bridge.world.level.dimension.LevelStemBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

// Used as delegate for WorldTemplate
public final class LevelStemData {

    private LevelStemData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asImmutable(LevelStem.class)
                    .create(Keys.WORLD_TYPE)
                        .get(h -> (WorldType) (Object) h.type().value())
                    .create(Keys.CHUNK_GENERATOR)
                        .get(h -> (ChunkGenerator) h.generator())
                .asImmutable(LevelStemBridge.class)
                    .create(Keys.DISPLAY_NAME)
                        .get(LevelStemBridge::bridge$displayName)
                    .create(Keys.GAME_MODE)
                        .get(h -> (GameMode) (Object) h.bridge$gameMode())
                    .create(Keys.WORLD_DIFFICULTY)
                        .get(h -> (Difficulty) (Object) h.bridge$difficulty())
                    .create(Keys.SERIALIZATION_BEHAVIOR)
                        .get(LevelStemBridge::bridge$serializationBehavior)
                    .create(Keys.IS_LOAD_ON_STARTUP)
                        .get(LevelStemBridge::bridge$loadOnStartup)
                    .create(Keys.PERFORM_SPAWN_LOGIC)
                        .get(LevelStemBridge::bridge$performsSpawnLogic)
                    .create(Keys.HARDCORE)
                        .get(LevelStemBridge::bridge$hardcore)
                    .create(Keys.COMMANDS)
                        .get(LevelStemBridge::bridge$allowCommands)
                    .create(Keys.PVP)
                        .get(LevelStemBridge::bridge$pvp)
                    .create(Keys.VIEW_DISTANCE)
                        .get(LevelStemBridge::bridge$viewDistance)
                    .create(Keys.SPAWN_POSITION)
                        .get(LevelStemBridge::bridge$spawnPosition)
                    .create(Keys.SEED)
                        .get(LevelStemBridge::bridge$seed)
        ;

    }
    // @formatter:on


}
