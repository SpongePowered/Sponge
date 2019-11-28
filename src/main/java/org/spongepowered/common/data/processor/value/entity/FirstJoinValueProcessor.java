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

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.world.storage.SpongePlayerDataHandler;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;

public class FirstJoinValueProcessor extends AbstractSpongeValueProcessor<Identifiable, Instant, Value<Instant>> {

    public FirstJoinValueProcessor() {
        super(Identifiable.class, Keys.FIRST_DATE_PLAYED);
    }

    @Override
    protected boolean supports(Identifiable dataHolder) {
        return dataHolder instanceof PlayerEntity || dataHolder instanceof SpongeUser;
    }

    @Override
    protected Value<Instant> constructValue(Instant actualValue) {
        return new SpongeValue<>(Keys.FIRST_DATE_PLAYED, Instant.now(), actualValue);
    }

    @Override
    protected boolean set(Identifiable container, Instant value) {
        final UUID id = container.getUniqueId();
        final Instant played = SpongePlayerDataHandler.getLastPlayed(id).orElse(Instant.now());
        SpongePlayerDataHandler.setPlayerInfo(id, value, played);
        return true;
    }

    @Override
    protected Optional<Instant> getVal(Identifiable container) {
        return SpongePlayerDataHandler.getFirstJoined(container.getUniqueId());
    }

    @Override
    protected ImmutableValue<Instant> constructImmutableValue(Instant value) {
        return new ImmutableSpongeValue<>(Keys.FIRST_DATE_PLAYED, Instant.now(), value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
