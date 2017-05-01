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
package org.spongepowered.common.data.manipulator.immutable.entity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableJoinData;
import org.spongepowered.api.data.manipulator.mutable.entity.JoinData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeJoinData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.time.Instant;

public class ImmutableSpongeJoinData extends AbstractImmutableData<ImmutableJoinData, JoinData> implements ImmutableJoinData {

    private final Instant firstJoined;
    private final Instant lastJoined;
    private final ImmutableSpongeValue<Instant> firstJoinedValue;
    private final ImmutableSpongeValue<Instant> lastJoinedValue;

    public ImmutableSpongeJoinData(Instant firstJoined, Instant lastJoined) {
        super(ImmutableJoinData.class);
        this.firstJoinedValue = new ImmutableSpongeValue<>(Keys.FIRST_DATE_PLAYED, Instant.EPOCH, this.firstJoined = firstJoined);
        this.lastJoinedValue = new ImmutableSpongeValue<>(Keys.LAST_DATE_PLAYED, Instant.EPOCH, this.lastJoined = lastJoined);
        registerGetters();
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.FIRST_DATE_PLAYED, () -> this.firstJoined);
        registerKeyValue(Keys.FIRST_DATE_PLAYED, this::firstPlayed);

        registerFieldGetter(Keys.LAST_DATE_PLAYED, () -> this.lastJoined);
        registerKeyValue(Keys.LAST_DATE_PLAYED, this::lastPlayed);
    }

    @Override
    public ImmutableValue<Instant> firstPlayed() {
        return this.firstJoinedValue;
    }

    @Override
    public ImmutableValue<Instant> lastPlayed() {
        return this.lastJoinedValue;
    }

    @Override
    public JoinData asMutable() {
        return new SpongeJoinData(this.firstJoined, this.lastJoined);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.FIRST_DATE_PLAYED.getQuery(), this.firstJoined.toEpochMilli())
                .set(Keys.LAST_DATE_PLAYED.getQuery(), this.lastJoined.toEpochMilli());
    }
}
