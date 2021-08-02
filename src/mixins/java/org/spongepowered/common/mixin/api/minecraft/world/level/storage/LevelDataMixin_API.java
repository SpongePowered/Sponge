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
package org.spongepowered.common.mixin.api.minecraft.world.level.storage;

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.LevelData;
import org.spongepowered.api.util.MinecraftDayTime;
import org.spongepowered.api.world.gamerule.GameRule;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Interface.Remap;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.level.GameRulesAccessor;
import org.spongepowered.common.accessor.world.level.GameRules_ValueAccessor;
import org.spongepowered.common.util.SpongeMinecraftDayTime;
import org.spongepowered.math.vector.Vector3i;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Mixin(LevelData.class)
@Implements(@Interface(iface = WorldProperties.class, prefix = "worldProperties$", remap = Remap.NONE))
public interface LevelDataMixin_API extends WorldProperties {

    // @formatter:off
    @Shadow int shadow$getXSpawn();
    @Shadow int shadow$getYSpawn();
    @Shadow int shadow$getZSpawn();
    @Shadow long shadow$getGameTime();
    @Shadow long shadow$getDayTime();
    @Shadow boolean shadow$isHardcore();
    @Shadow GameRules shadow$getGameRules();
    @Shadow Difficulty shadow$getDifficulty();
    // @formatter:on

    @Override
    default Vector3i spawnPosition() {
        return new Vector3i(this.shadow$getXSpawn(), this.shadow$getYSpawn(), this.shadow$getZSpawn());
    }

    @Intrinsic
    default MinecraftDayTime worldProperties$gameTime() {
        return new SpongeMinecraftDayTime(this.shadow$getGameTime());
    }

    @Intrinsic
    default MinecraftDayTime worldProperties$dayTime() {
        return new SpongeMinecraftDayTime(this.shadow$getDayTime());
    }

    @Intrinsic
    default boolean worldProperties$hardcore() {
        return this.shadow$isHardcore();
    }

    @Intrinsic
    default org.spongepowered.api.world.difficulty.Difficulty worldProperties$difficulty() {
        return (org.spongepowered.api.world.difficulty.Difficulty) (Object) this.shadow$getDifficulty();
    }

    @Override
    default  <V> V gameRule(final GameRule<V> gameRule) {
        final GameRules.Value<?> value = this.shadow$getGameRules().getRule((GameRules.Key<?>) (Object) Objects.requireNonNull(gameRule,
                "gameRule"));
        if (value instanceof GameRules.BooleanValue) {
            return (V) Boolean.valueOf(((GameRules.BooleanValue) value).get());
        } else if (value instanceof GameRules.IntegerValue) {
            return (V) Integer.valueOf(((GameRules.IntegerValue) value).get());
        }
        return null;
    }

    @Override
    default  <V> void setGameRule(final GameRule<V> gameRule, final V value) {
        Objects.requireNonNull(gameRule, "gameRule");
        Objects.requireNonNull(value, "value");

        final GameRules.Value<?> mValue = this.shadow$getGameRules().getRule((GameRules.Key<?>) (Object) gameRule);
        ((GameRules_ValueAccessor) mValue).invoker$deserialize(value.toString());
    }

    @Override
    default Map<GameRule<?>, ?> gameRules() {
        final Map<GameRules.Key<?>, GameRules.Value<?>> rules =
                ((GameRulesAccessor) this.shadow$getGameRules()).accessor$rules();

        final Map<GameRule<?>, Object> apiRules = new HashMap<>();
        for (final Map.Entry<GameRules.Key<?>, GameRules.Value<?>> rule : rules.entrySet()) {
            final GameRule<?> key = (GameRule<?>) (Object) rule.getKey();
            final GameRules.Value<?> mValue = rule.getValue();
            Object value = null;
            if (mValue instanceof GameRules.BooleanValue) {
                value = ((GameRules.BooleanValue) mValue).get();
            } else if (mValue instanceof GameRules.IntegerValue) {
                value = ((GameRules.IntegerValue) mValue).get();
            }

            if (value != null) {
                apiRules.put(key, value);
            }
        }

        return apiRules;
    }
}
