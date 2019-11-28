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
package org.spongepowered.common.data.processor.data.entity;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.GameType;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableGameModeData;
import org.spongepowered.api.data.manipulator.mutable.entity.GameModeData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeGameModeData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class GameModeDataProcessor extends
        AbstractEntitySingleDataProcessor<ServerPlayerEntity, GameMode, Value<GameMode>, GameModeData, ImmutableGameModeData> {

    public GameModeDataProcessor() {
        super(ServerPlayerEntity.class, Keys.GAME_MODE);
    }

    @Override
    protected GameModeData createManipulator() {
        return new SpongeGameModeData();
    }

    @Override
    protected boolean set(ServerPlayerEntity entity, GameMode value) {
        entity.func_71033_a((GameType) (Object) value);
        return true;
    }

    @Override
    protected Optional<GameMode> getVal(ServerPlayerEntity entity) {
        return Optional.of((GameMode) (Object) entity.field_71134_c.func_73081_b());
    }

    @Override
    protected ImmutableValue<GameMode> constructImmutableValue(GameMode value) {
        return ImmutableSpongeValue.cachedOf(Keys.GAME_MODE, GameModes.SURVIVAL, value);
    }

    @Override
    protected Value<GameMode> constructValue(GameMode actualValue) {
        return new SpongeValue<>(Keys.GAME_MODE, GameModes.SURVIVAL, actualValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
