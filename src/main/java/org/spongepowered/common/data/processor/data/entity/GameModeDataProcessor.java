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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldSettings;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableGameModeData;
import org.spongepowered.api.data.manipulator.mutable.entity.GameModeData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeGameModeData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Optional;

public class GameModeDataProcessor extends AbstractEntitySingleDataProcessor<EntityPlayer, GameMode, Value<GameMode>, GameModeData, ImmutableGameModeData> {

    public GameModeDataProcessor() {
        super(EntityPlayer.class, Keys.GAME_MODE);
    }

    @Override
    protected GameModeData createManipulator() {
        return new SpongeGameModeData();
    }

    @Override
    protected boolean set(EntityPlayer entity, GameMode value) {
        if (Sponge.getGame().getPlatform().getType().isServer()) {
            entity.setGameType((WorldSettings.GameType) (Object) value);
            return true;
        } else if (Sponge.getGame().getPlatform().getType().isClient()) {
            // ((AbstractClientPlayer) entity).getPlayerInfo().getGa // TODO do we really need to handle this?
        }
        return false;
    }

    @Override
    protected Optional<GameMode> getVal(EntityPlayer entity) {
        if (entity instanceof EntityPlayerMP) {
            return Optional.of((GameMode) (Object) ((EntityPlayerMP) entity).theItemInWorldManager.getGameType());
        }
        if (Sponge.getGame().getPlatform().getType().isClient()) {
            /* TODO: This will only compile on the client (in SpongeForge)
            if (entity instanceof AbstractClientPlayer) {
                return Optional.of((GameMode) (Object) ((AbstractClientPlayer) entity).getPlayerInfo().getGameType());
            }*/
        }
        return Optional.empty();
    }

    @Override
    protected ImmutableValue<GameMode> constructImmutableValue(GameMode value) {
        return ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.GAME_MODE, value, GameModes.SURVIVAL);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.failNoData();
    }

}
