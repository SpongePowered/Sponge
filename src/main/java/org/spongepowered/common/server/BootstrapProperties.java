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

import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.WorldSettingsImport;
import net.minecraft.world.GameType;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.difficulty.Difficulty;

public final class BootstrapProperties {

    public static DimensionGeneratorSettings dimensionGeneratorSettings;
    public static RegistryReference<GameMode> gameMode;
    public static RegistryReference<Difficulty> difficulty;
    public static boolean pvp;
    public static boolean hardcore;
    public static int viewDistance;
    public static DynamicRegistries registries;
    public static WorldSettingsImport<?> worldSettingsAdapter;
    public static boolean isNewLevel = false;

    public static void init(final DimensionGeneratorSettings dimensionGeneratorSettings, final GameType gameType, final net.minecraft.world.Difficulty difficulty,
            final boolean pvp, final boolean hardcore, final int viewDistance, final DynamicRegistries registries) {
        BootstrapProperties.dimensionGeneratorSettings = dimensionGeneratorSettings;
        BootstrapProperties.gameMode = RegistryKey.of(RegistryTypes.GAME_MODE, ResourceKey.sponge(gameType.getName())).asDefaultedReference(() -> Sponge.getGame().registries());
        BootstrapProperties.difficulty = RegistryKey.of(RegistryTypes.DIFFICULTY, ResourceKey.sponge(difficulty.getKey())).asDefaultedReference(() -> Sponge.getGame().registries());
        BootstrapProperties.pvp = pvp;
        BootstrapProperties.hardcore = hardcore;
        BootstrapProperties.viewDistance = viewDistance;
        BootstrapProperties.registries = registries;
    }

    public static <T> void worldSettingsAdapter(final WorldSettingsImport<T> worldSettingsAdapter) {
        BootstrapProperties.worldSettingsAdapter = worldSettingsAdapter;
    }

    public static void setIsNewLevel(final boolean isNewLevel) {
        BootstrapProperties.isNewLevel = isNewLevel;
    }
}
