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

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFuseData;
import org.spongepowered.api.data.manipulator.mutable.entity.FuseData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeFuseData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class SpongeFuseData extends AbstractData<FuseData, ImmutableFuseData> implements FuseData {

    private int fuseDuration;
    private int ticksRemaining;

    public SpongeFuseData(int fuseDuration, int ticksRemaining) {
        super(FuseData.class);
        this.fuseDuration = fuseDuration;
        this.ticksRemaining = ticksRemaining;
        registerGettersAndSetters();
    }

    public SpongeFuseData() {
        this(0, 0);
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.FUSE_DURATION, () -> this.fuseDuration);
        registerFieldSetter(Keys.FUSE_DURATION, fuseDuration -> this.fuseDuration = fuseDuration);
        registerKeyValue(Keys.FUSE_DURATION, this::fuseDuration);

        registerFieldGetter(Keys.TICKS_REMAINING, () -> this.ticksRemaining);
        registerFieldSetter(Keys.TICKS_REMAINING, ticksRemaining -> this.ticksRemaining = ticksRemaining);
        registerKeyValue(Keys.TICKS_REMAINING, this::ticksRemaining);
    }

    @Override
    public Value<Integer> fuseDuration() {
        return new SpongeValue<>(Keys.FUSE_DURATION, this.fuseDuration);
    }

    @Override
    public Value<Integer> ticksRemaining() {
        return new SpongeValue<>(Keys.TICKS_REMAINING, this.ticksRemaining);
    }

    @Override
    public FuseData copy() {
        return new SpongeFuseData(this.fuseDuration, this.ticksRemaining);
    }

    @Override
    public ImmutableFuseData asImmutable() {
        return new ImmutableSpongeFuseData(this.fuseDuration, this.ticksRemaining);
    }

    @Override
    public int compareTo(FuseData that) {
        return ComparisonChain.start()
                .compare(this.fuseDuration, (int) that.fuseDuration().get())
                .compare(this.ticksRemaining, (int) that.ticksRemaining().get())
                .result();
    }

}
