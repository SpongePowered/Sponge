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
package org.spongepowered.common.registry.builtin.sponge;

import net.minecraft.util.datafix.codec.DatapackCodec;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.dimension.DimensionType;
import org.spongepowered.api.world.dimension.DimensionTypes;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;

import java.util.function.Supplier;
import java.util.stream.Stream;

public final class WorldArchetypeStreamGenerator {

    private WorldArchetypeStreamGenerator() {
    }

    public static Stream<WorldArchetype> stream() {
        return Stream.of(
                WorldArchetypeStreamGenerator.newArchetype(ResourceKey.sponge("overworld"), DimensionTypes.OVERWORLD),
                WorldArchetypeStreamGenerator.newArchetype(ResourceKey.sponge("the_nether"), DimensionTypes.THE_NETHER),
                WorldArchetypeStreamGenerator.newArchetype(ResourceKey.sponge("the_end"), DimensionTypes.THE_END)
        );
    }

    private static WorldArchetype newArchetype(final ResourceKey key, final Supplier<DimensionType> dimensionType) {
        final WorldSettings archetype = new WorldSettings(key.getValue(), GameType.SURVIVAL, false,
            Difficulty.NORMAL, true, new GameRules(), DatapackCodec.DEFAULT);
        ((ResourceKeyBridge) (Object) archetype).bridge$setKey(key);
        ((WorldSettingsBridge) (Object) archetype).bridge$setDimensionType((net.minecraft.world.DimensionType) dimensionType.get());
        ((WorldSettingsBridge) (Object) archetype).bridge$setDifficulty(Difficulty.NORMAL);
        if (dimensionType.get() == DimensionTypes.OVERWORLD.get()) {
            ((WorldSettingsBridge) (Object) archetype).bridge$setGenerateSpawnOnLoad(true);
        }
        return (WorldArchetype) (Object)  archetype;
    }
}
