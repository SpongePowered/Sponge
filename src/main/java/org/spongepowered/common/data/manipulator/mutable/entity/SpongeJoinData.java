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
package org.spongepowered.common.data.manipulator.mutable.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableJoinData;
import org.spongepowered.api.data.manipulator.mutable.entity.JoinData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeJoinData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.time.Instant;

public class SpongeJoinData extends AbstractData<JoinData, ImmutableJoinData> implements JoinData {

    private Instant firstJoined;
    private Instant lastJoined;

    public SpongeJoinData() {
        this(Instant.now(), Instant.now());
    }

    public SpongeJoinData(Instant firstJoined, Instant lastJoined) {
        super(JoinData.class);
        this.firstJoined = firstJoined;
        this.lastJoined = lastJoined;
        registerGettersAndSetters();
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.FIRST_DATE_PLAYED, () -> this.firstJoined);
        registerFieldSetter(Keys.FIRST_DATE_PLAYED, instant -> this.firstJoined = checkNotNull(instant, "First join instant cannot be null!"));
        registerKeyValue(Keys.FIRST_DATE_PLAYED, this::firstPlayed);

        registerFieldGetter(Keys.LAST_DATE_PLAYED, () -> this.lastJoined);
        registerFieldSetter(Keys.LAST_DATE_PLAYED, instant -> this.lastJoined = checkNotNull(instant, "Last join instant cannot be null!"));
        registerKeyValue(Keys.LAST_DATE_PLAYED, this::lastPlayed);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.FIRST_DATE_PLAYED.getQuery(), this.firstJoined.toEpochMilli())
                .set(Keys.LAST_DATE_PLAYED.getQuery(), this.lastJoined.toEpochMilli());
    }

    @Override
    public Value<Instant> firstPlayed() {
        return new SpongeValue<>(Keys.FIRST_DATE_PLAYED, Instant.EPOCH, this.firstJoined);
    }

    @Override
    public Value<Instant> lastPlayed() {
        return new SpongeValue<>(Keys.LAST_DATE_PLAYED, Instant.EPOCH, this.lastJoined);
    }

    @Override
    public JoinData copy() {
        return new SpongeJoinData(this.firstJoined, this.lastJoined);
    }

    @Override
    public ImmutableJoinData asImmutable() {
        return new ImmutableSpongeJoinData(this.firstJoined, this.lastJoined);
    }


}
