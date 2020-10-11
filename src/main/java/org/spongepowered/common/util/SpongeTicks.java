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
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Objects;

public final class SpongeTicks implements Ticks {

    public static final Factory FACTORY_INSTANCE = new Factory();

    private final long ticks;
    private final Duration effectiveMinimumDuration;

    public SpongeTicks(final long ticks) {
        this.ticks = ticks;
        this.effectiveMinimumDuration = Constants.TickConversions.EFFECTIVE_MINIMUM_DURATION.multipliedBy(this.ticks);
    }

    @Override
    public long getMinecraftSeconds() {
        // We do this to try to ensure we get the most accurate number of seconds we can.
        // We know the hour rate is 1000 ticks, we can get an accurate hour count. This reduces the potential
        // for error.
        //
        // We get the number of in-game seconds this object fulfils, there may be a few in-game milliseconds.
        return 60 * 60 * this.ticks / Constants.TickConversions.MINECRAFT_HOUR_TICKS + // 3600 seconds in an hour
                (long) ((this.ticks % Constants.TickConversions.MINECRAFT_HOUR_TICKS) / Constants.TickConversions.MINECRAFT_SECOND_TICKS);
    }

    @Override
    public Duration getMinecraftDayTimeDuration() {
        return Duration.of(this.getMinecraftSeconds(), ChronoUnit.SECONDS);
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeTicks that = (SpongeTicks) o;
        return this.ticks == that.ticks;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.ticks);
    }

    public static final class Factory implements Ticks.Factory {

        private final Ticks zero = new SpongeTicks(0);
        private final Ticks single = new SpongeTicks(1);
        private final Ticks minecraftHour = new SpongeTicks(Constants.TickConversions.MINECRAFT_HOUR_TICKS);
        private final Ticks minecraftDay = new SpongeTicks(Constants.TickConversions.MINECRAFT_DAY_TICKS);

        @Override
        @NonNull
        public Ticks of(final long ticks) {
            Preconditions.checkArgument(ticks >= 0, "tick parameter must be non-negative");
            return new SpongeTicks(ticks);
        }

        @Override
        @NonNull
        public Ticks ofWallClockTime(final long time, @NonNull final TemporalUnit temporalUnit) {
            Preconditions.checkArgument(time >= 0, "time parameter must be non-negative");
            final long target = temporalUnit.getDuration().multipliedBy(time).toMillis();
            return this.of((long) Math.ceil(target / (double) Constants.TickConversions.TICK_DURATION_MS));
        }

        @Override
        @NonNull
        public Ticks ofMinecraftSeconds(final long seconds) {
            Preconditions.checkArgument(seconds >= 0, "time parameter must be non-negative");
            return this.of((long) Math.ceil(seconds * Constants.TickConversions.MINECRAFT_SECOND_TICKS));
        }

        @Override
        @NonNull
        public Ticks ofMinecraftHours(final long hours) {
            Preconditions.checkArgument(hours >= 0, "time parameter must be non-negative");
            return this.of(hours * Constants.TickConversions.MINECRAFT_HOUR_TICKS);
        }

        @Override
        public Ticks zero() {
            return this.zero;
        }

        @Override
        public Ticks single() {
            return this.single;
        }

        @Override
        public Ticks minecraftHour() {
            return this.minecraftHour;
        }

        @Override
        public Ticks minecraftDay() {
            return this.minecraftDay;
        }

    }

}
