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

import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeImmutableValue;
import org.spongepowered.common.data.value.SpongeMutableValue;
import org.spongepowered.common.world.storage.SpongePlayerDataHandler;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class FirstJoinValueProcessor extends AbstractSpongeValueProcessor<EntityPlayer, Instant> {

    public FirstJoinValueProcessor() {
        super(EntityPlayer.class, Keys.FIRST_DATE_PLAYED);
    }

    @Override
    protected Value.Mutable<Instant> constructMutableValue(Instant actualValue) {
        return new SpongeMutableValue<>(Keys.FIRST_DATE_PLAYED, actualValue);
    }

    @Override
    protected boolean set(EntityPlayer container, Instant value) {
        final UUID id = container.getUniqueID();
        final Instant played = SpongePlayerDataHandler.getLastPlayed(id).orElse(Instant.now());
        SpongePlayerDataHandler.setPlayerInfo(id, value, played);
        return true;
    }

    @Override
    protected Optional<Instant> getVal(EntityPlayer container) {
        return SpongePlayerDataHandler.getFirstJoined(container.getUniqueID());
    }

    @Override
    protected Value.Immutable<Instant> constructImmutableValue(Instant value) {
        return new SpongeImmutableValue<>(Keys.FIRST_DATE_PLAYED, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
