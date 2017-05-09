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
package org.spongepowered.common.data.processor.multi.entity;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableJoinData;
import org.spongepowered.api.data.manipulator.mutable.entity.JoinData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeJoinData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.world.storage.SpongePlayerDataHandler;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class JoinDataProcessor extends AbstractEntityDataProcessor<EntityPlayer, JoinData, ImmutableJoinData> {

    public JoinDataProcessor() {
        super(EntityPlayer.class);
    }

    @Override
    protected boolean doesDataExist(EntityPlayer dataHolder) {
        return true;
    }

    @Override
    protected boolean set(EntityPlayer dataHolder, Map<Key<?>, Object> keyValues) {
        UUID uuid = dataHolder.getUniqueID();
        Instant instant = (Instant) keyValues.get(Keys.FIRST_DATE_PLAYED);
        Instant played = (Instant) keyValues.get(Keys.LAST_DATE_PLAYED);
        SpongePlayerDataHandler.setPlayerInfo(uuid, instant, played);
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(EntityPlayer dataHolder) {
        final UUID uuid = dataHolder.getUniqueID();
        Instant first = SpongePlayerDataHandler.getFirstJoined(uuid).get();
        Instant played = SpongePlayerDataHandler.getLastPlayed(uuid).get();
        return ImmutableMap.of(Keys.FIRST_DATE_PLAYED, first, Keys.LAST_DATE_PLAYED, played);
    }

    @Override
    protected JoinData createManipulator() {
        return new SpongeJoinData();
    }

    @Override
    public Optional<JoinData> fill(DataContainer container, JoinData joinData) {
        if (!container.contains(Keys.FIRST_DATE_PLAYED, Keys.LAST_DATE_PLAYED)) {
            return Optional.empty();
        }
        final long joined = container.getLong(Keys.FIRST_DATE_PLAYED.getQuery()).get();
        final long played = container.getLong(Keys.LAST_DATE_PLAYED.getQuery()).get();
        joinData.set(Keys.FIRST_DATE_PLAYED, Instant.ofEpochMilli(joined));
        joinData.set(Keys.LAST_DATE_PLAYED, Instant.ofEpochMilli(played));
        return Optional.of(joinData);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}
