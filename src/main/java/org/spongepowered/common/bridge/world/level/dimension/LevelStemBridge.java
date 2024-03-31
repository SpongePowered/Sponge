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
package org.spongepowered.common.bridge.world.level.dimension;

import net.kyori.adventure.text.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.dimension.LevelStem;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.common.world.server.SpongeWorldTemplate;
import org.spongepowered.math.vector.Vector3i;

public interface LevelStemBridge {

    @Nullable Component bridge$displayName();

    @Nullable GameType bridge$gameMode();

    @Nullable Difficulty bridge$difficulty();

    @Nullable SerializationBehavior bridge$serializationBehavior();

    Integer bridge$viewDistance();

    @Nullable Vector3i bridge$spawnPosition();

    boolean bridge$loadOnStartup();

    boolean bridge$performsSpawnLogic();

    @Nullable Boolean bridge$hardcore();

    @Nullable Boolean bridge$allowCommands();

    @Nullable Boolean bridge$pvp();

    @Nullable Long bridge$seed();

    LevelStem bridge$decorateData(SpongeWorldTemplate.SpongeDataSection data);

    LevelStem bridge$decorateData(DataManipulator data);

    SpongeWorldTemplate.SpongeDataSection bridge$createData();
}
