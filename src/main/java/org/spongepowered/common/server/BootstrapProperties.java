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
package org.spongepowered.common.server;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.difficulty.Difficulty;

public final class BootstrapProperties {

    public static WorldGenSettings worldGenSettings;
    public static RegistryReference<GameMode> gameMode;
    public static RegistryReference<Difficulty> difficulty;
    public static SerializationBehavior serializationBehavior;
    public static boolean pvp;
    public static boolean hardcore;
    public static boolean commands;
    public static int viewDistance = 10;
    public static RegistryAccess registries;
    public static RegistryReadOps<?> worldSettingsAdapter;
    public static boolean isNewLevel = false;

    public static void init(final WorldGenSettings worldGenSettings, final GameType gameType, final net.minecraft.world.Difficulty difficulty,
            final boolean pvp, final boolean hardcore, final boolean commands, final int viewDistance, final RegistryAccess registries) {
        BootstrapProperties.worldGenSettings = worldGenSettings;
        BootstrapProperties.gameMode = RegistryKey.of(RegistryTypes.GAME_MODE, ResourceKey.sponge(gameType.getName())).asDefaultedReference(() -> Sponge.game().registries());
        BootstrapProperties.difficulty = RegistryKey.of(RegistryTypes.DIFFICULTY, ResourceKey.sponge(difficulty.getKey())).asDefaultedReference(() -> Sponge.game().registries());
        BootstrapProperties.pvp = pvp;
        BootstrapProperties.hardcore = hardcore;
        BootstrapProperties.commands = commands;
        BootstrapProperties.viewDistance = viewDistance;
        BootstrapProperties.registries = registries;
        BootstrapProperties.serializationBehavior = SerializationBehavior.AUTOMATIC;
    }

    public static <T> void worldSettingsAdapter(final RegistryReadOps<T> worldSettingsAdapter) {
        BootstrapProperties.worldSettingsAdapter = worldSettingsAdapter;
    }

    public static void setIsNewLevel(final boolean isNewLevel) {
        BootstrapProperties.isNewLevel = isNewLevel;
    }
}
