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
package org.spongepowered.common.util;

import com.google.common.base.Preconditions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.util.Ticks;

import java.time.Duration;
import java.time.temporal.TemporalUnit;

public final class SpongeTicks implements Ticks {

    public static final Factory FACTORY_INSTANCE = new Factory();

    public static final int TICK_DURATION_MS = 50;
    private static final Duration EFFECTIVE_MINIMUM_DURATION = Duration.ofMillis(SpongeTicks.TICK_DURATION_MS);

    private final long ticks;
    private final Duration effectiveMinimumDuration;

    public SpongeTicks(final long ticks) {
        this.ticks = ticks;
        this.effectiveMinimumDuration = SpongeTicks.EFFECTIVE_MINIMUM_DURATION.multipliedBy(this.ticks);
    }

    @Override
    @NonNull
    public Duration getExpectedDuration() {
        return this.effectiveMinimumDuration;
    }

    @Override
    public long getTicks() {
        return this.ticks;
    }

    public static final class Factory implements Ticks.Factory {

        @Override
        @NonNull
        public Ticks of(final long ticks) {
            Preconditions.checkArgument(ticks >= 0, "tick parameter must be non-negative");
            return new SpongeTicks(ticks);
        }

        @Override
        @NonNull
        public Ticks of(final long time, @NonNull final TemporalUnit temporalUnit) {
            Preconditions.checkArgument(time >= 0, "time parameter must be non-negative");
            final long target = temporalUnit.getDuration().multipliedBy(time).toMillis();
            return this.of((long) Math.ceil(target / (double) SpongeTicks.TICK_DURATION_MS));
        }

    }

}
