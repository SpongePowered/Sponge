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
package org.spongepowered.common.data.processor.value.entity;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldSettings;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class GameModeValueProcessor extends AbstractSpongeValueProcessor<GameMode, Value<GameMode>> {

    public GameModeValueProcessor() {
        super(Keys.GAME_MODE);
    }

    @Override
    public Optional<GameMode> getValueFromContainer(ValueContainer<?> container) {
        if (container instanceof EntityPlayerMP) {
            return Optional.of((GameMode) (Object) ((EntityPlayerMP) container).theItemInWorldManager.getGameType());
        }
        return Optional.empty();
    }

    @Override
    protected Value<GameMode> constructValue(GameMode defaultValue) {
        return new SpongeValue<GameMode>(getKey(), defaultValue, GameModes.NOT_SET);
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof EntityPlayerMP;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, GameMode value) {
        final ImmutableValue<GameMode> newValue = getGameModeValue(value);
        if (container instanceof EntityPlayerMP) {
            final GameMode old = (GameMode) (Object) ((EntityPlayerMP) container).theItemInWorldManager.getGameType();
            final ImmutableValue<GameMode> oldValue = getGameModeValue(old);
            try {
                ((EntityPlayerMP) container).setGameType((WorldSettings.GameType) (Object) value);
                return DataTransactionBuilder.successReplaceResult(newValue, oldValue);
            } catch (Exception e) {
                return DataTransactionBuilder.errorResult(newValue);
            }
        }
        return DataTransactionBuilder.failResult(newValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }

    private static ImmutableValue<GameMode> getGameModeValue(GameMode gameMode) {
        return ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.GAME_MODE, gameMode, GameModes.SURVIVAL);
    }
}
