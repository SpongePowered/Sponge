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

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFuseData;
import org.spongepowered.api.data.manipulator.mutable.entity.FuseData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFuseData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeFuseData extends AbstractImmutableData<ImmutableFuseData, FuseData>
        implements ImmutableFuseData {

    private final int fuseDuration;
    private final int ticksRemaining;

    private final ImmutableSpongeValue<Integer> fuseDurationValue;
    private final ImmutableSpongeValue<Integer> ticksRemainingValue;

    public ImmutableSpongeFuseData(int fuseDuration, int ticksRemaining) {
        super(ImmutableFuseData.class);
        this.fuseDuration = fuseDuration;
        this.fuseDurationValue = new ImmutableSpongeValue<>(Keys.FUSE_DURATION, fuseDuration);
        this.ticksRemaining = ticksRemaining;
        this.ticksRemainingValue = new ImmutableSpongeValue<>(Keys.TICKS_REMAINING, ticksRemaining);
        registerGetters();
    }

    public ImmutableSpongeFuseData() {
        this(0, 0);
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.FUSE_DURATION, () -> this.fuseDuration);
        registerKeyValue(Keys.FUSE_DURATION, this::fuseDuration);
        registerFieldGetter(Keys.TICKS_REMAINING, () -> this.ticksRemaining);
        registerKeyValue(Keys.TICKS_REMAINING, this::ticksRemaining);
    }

    @Override
    public ImmutableValue<Integer> fuseDuration() {
        return this.fuseDurationValue;
    }

    @Override
    public ImmutableValue<Integer> ticksRemaining() {
        return this.ticksRemainingValue;
    }

    @Override
    public FuseData asMutable() {
        return new SpongeFuseData(this.fuseDuration, this.ticksRemaining);
    }

    @Override
    public int compareTo(ImmutableFuseData that) {
        return ComparisonChain.start()
                .compare(this.fuseDuration, (int) that.fuseDuration().get())
                .compare(this.ticksRemaining, (int) that.ticksRemaining().get())
                .result();
    }

}
