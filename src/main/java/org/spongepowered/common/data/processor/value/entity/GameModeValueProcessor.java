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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldSettings;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.player.gamemode.GameMode;
import org.spongepowered.api.entity.player.gamemode.GameModes;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class GameModeValueProcessor extends AbstractSpongeValueProcessor<GameMode, Value<GameMode>> {

    public GameModeValueProcessor() {
        super(Keys.GAME_MODE);
    }

    @Override
    public Optional<GameMode> getValueFromContainer(ValueContainer<?> container) {
        if (container instanceof EntityPlayerMP) {
            return Optional.of((GameMode) ((Object) ((EntityPlayerMP) container).theItemInWorldManager.getGameType()));
        }
        return Optional.absent();
    }

    @Override
    public Optional<Value<GameMode>> getApiValueFromContainer(ValueContainer<?> container) {
        if (container instanceof EntityPlayerMP) {
            return Optional.<Value<GameMode>>of(
                    new SpongeValue<GameMode>(Keys.GAME_MODE, (GameMode) ((Object) ((EntityPlayerMP)
                            container).theItemInWorldManager.getGameType())));
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof EntityPlayerMP;
    }

    @Override
    public DataTransactionResult transform(ValueContainer<?> container, Function<GameMode, GameMode> function) {
        if (container instanceof EntityPlayerMP) {
            final GameMode old = (GameMode) ((Object) ((EntityPlayerMP) container).theItemInWorldManager.getGameType());
            final ImmutableValue<GameMode> oldValue = ImmutableDataCachingUtil
                    .getValue(ImmutableSpongeValue.class, Keys.GAME_MODE, old, GameModes.SURVIVAL);
            final GameMode newMode = checkNotNull(function.apply(old));
            final ImmutableValue<GameMode> newValue = ImmutableDataCachingUtil
                    .getValue(ImmutableSpongeValue.class, Keys.GAME_MODE, newMode, GameModes.SURVIVAL);
            try {
                ((EntityPlayerMP) container).setGameType((WorldSettings.GameType) ((Object) newMode));
                return DataTransactionBuilder.successReplaceResult(newValue, oldValue);
            } catch (Exception e) {
                return DataTransactionBuilder.errorResult(newValue);
            }
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, BaseValue<?> value) {
        return offerToStore(container, ((GameMode) value.get()));
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, GameMode value) {
        if (container instanceof EntityPlayerMP) {
            final GameMode old = (GameMode) ((Object) ((EntityPlayerMP) container).theItemInWorldManager.getGameType());
            final ImmutableValue<GameMode> oldValue = ImmutableDataCachingUtil
                    .getValue(ImmutableSpongeValue.class, Keys.GAME_MODE, old, GameModes.SURVIVAL);
            final ImmutableValue<GameMode> newValue = ImmutableDataCachingUtil
                    .getValue(ImmutableSpongeValue.class, Keys.GAME_MODE, value, GameModes.SURVIVAL);
            try {
                ((EntityPlayerMP) container).setGameType((WorldSettings.GameType) ((Object) value));
                return DataTransactionBuilder.successReplaceResult(newValue, oldValue);
            } catch (Exception e) {
                return DataTransactionBuilder.errorResult(newValue);
            }
        }
        return DataTransactionBuilder.failResult(ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys
                .GAME_MODE, value, GameModes.SURVIVAL));
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}
