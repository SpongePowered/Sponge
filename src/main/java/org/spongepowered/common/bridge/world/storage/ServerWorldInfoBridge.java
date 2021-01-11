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
package org.spongepowered.common.bridge.world.storage;

import net.kyori.adventure.text.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.inheritable.WorldConfig;

import java.util.UUID;

public interface ServerWorldInfoBridge {

    boolean bridge$valid();

    @Nullable
    ServerWorld bridge$world();

    @Nullable
    DimensionType bridge$dimensionType();

    void bridge$dimensionType(DimensionType dimensionType, boolean updatePlayers);

    UUID bridge$uniqueId();

    void bridge$setUniqueId(UUID uniqueId);

    boolean bridge$customDifficulty();

    void bridge$forceSetDifficulty(Difficulty difficulty);

    boolean bridge$pvp();

    void bridge$setPvp(boolean pvp);

    boolean bridge$enabled();

    void bridge$setEnabled(boolean enabled);

    boolean bridge$loadOnStartup();

    void bridge$setLoadOnStartup(boolean loadOnStartup);

    boolean bridge$performsSpawnLogic();

    void bridge$setPerformsSpawnLogic(boolean keepLoaded);

    SerializationBehavior bridge$serializationBehavior();

    void bridge$setSerializationBehavior(SerializationBehavior behavior);

    @Nullable
    Component bridge$displayName();

    void bridge$setDisplayName(@Nullable Component displayName);

    int bridge$viewDistance();

    void bridge$setViewDistance(@Nullable Integer viewDistance);

    InheritableConfigHandle<WorldConfig> bridge$configAdapter();

    void bridge$configAdapter(InheritableConfigHandle<WorldConfig> adapter);

    void bridge$populateFromDimension(Dimension dimension);
}
