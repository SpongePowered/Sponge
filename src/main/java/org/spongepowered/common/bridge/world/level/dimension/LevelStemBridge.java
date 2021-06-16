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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.common.world.server.SpongeWorldTemplate;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;
import java.util.UUID;

public interface LevelStemBridge {

    Optional<Component> bridge$displayName();

    Optional<ResourceLocation> bridge$gameMode();

    Optional<ResourceLocation> bridge$difficulty();

    Optional<SerializationBehavior> bridge$serializationBehavior();

    Optional<Integer> bridge$viewDistance();

    Optional<Vector3i> bridge$spawnPosition();

    boolean bridge$loadOnStartup();

    boolean bridge$performsSpawnLogic();

    Optional<Boolean> bridge$hardcore();

    Optional<Boolean> bridge$commands();

    Optional<Boolean> bridge$pvp();

    boolean bridge$fromSettings();

    void bridge$setFromSettings(boolean fromSettings);

    void bridge$populateFromTemplate(SpongeWorldTemplate s);

    void bridge$populateFromLevelData(PrimaryLevelData levelData);

    SpongeWorldTemplate bridge$asTemplate();

    LevelStem bridge$decorateData(SpongeWorldTemplate.SpongeDataSection data);

    SpongeWorldTemplate.SpongeDataSection bridge$createData();
}
