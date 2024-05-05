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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Engine;
import org.spongepowered.api.util.Ticks;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Objects;

public final class SpongeTicks implements Ticks {

    private final long ticks;
    private final Duration effectiveMinimumDuration;

    public SpongeTicks(final long ticks) {
        this.ticks = ticks;
        this.effectiveMinimumDuration = Constants.TickConversions.EFFECTIVE_MINIMUM_DURATION.multipliedBy(this.ticks);
    }

    @Override
    public long minecraftSeconds(final @NonNull Engine engine) {
        // We do this to try to ensure we get the most accurate number of seconds we can.
        // We know the hour rate is 1000 ticks, we can get an accurate hour count. This reduces the potential
        // for error.
        //
        // We get the number of in-game seconds this object fulfils, there may be a few in-game milliseconds.
        return 60 * 60 * this.ticks / Constants.TickConversions.MINECRAFT_HOUR_TICKS + // 3600 seconds in an hour
                (long) ((this.ticks % Constants.TickConversions.MINECRAFT_HOUR_TICKS) / Constants.TickConversions.MINECRAFT_SECOND_TICKS);
    }

    @Override
    public @NonNull Duration minecraftDayTimeDuration(final @NonNull Engine engine) {
        return Duration.of(this.minecraftSeconds(engine), ChronoUnit.SECONDS);
    }

    @Override
    public @NonNull Duration expectedDuration(final @NonNull Engine engine) {
        return this.effectiveMinimumDuration;
    }

    @Override
    public long ticks() {
        return this.ticks;
    }

    @Override
    public boolean isInfinite() {
        return false;
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

    public static int toSaturatedIntOrInfinite(final Ticks ticks) {
        return SpongeTicks.toSaturatedIntOrInfinite(ticks, Constants.TickConversions.INFINITE_TICKS);
    }

    public static int toSaturatedIntOrInfinite(final Ticks ticks, final int infiniteTicksMarker) {
        if (ticks.isInfinite()) {
            return infiniteTicksMarker;
        }
        long ticksValue = ticks.ticks();
        if (ticksValue > Integer.MAX_VALUE) {
            ticksValue = Integer.MAX_VALUE;
        }
        if (infiniteTicksMarker > 0 && ticksValue >= infiniteTicksMarker) {
            ticksValue = infiniteTicksMarker - 1;
        }
        return (int) ticksValue;
    }

    public static Ticks ticksOrInfinite(final long ticks) {
        return SpongeTicks.ticksOrInfinite(ticks, Constants.TickConversions.INFINITE_TICKS);
    }

    public static Ticks ticksOrInfinite(final long ticks, final int infiniteTicksMarker) {
        if (ticks == infiniteTicksMarker) {
            return SpongeInfiniteTicks.INSTANCE;
        }
        return new SpongeTicks(ticks);
    }

    public static final class Factory implements Ticks.Factory {

        private final Ticks zero;
        private final Ticks single;
        private final Ticks minecraftHour;
        private final Ticks minecraftDay;

        public Factory() {
            this.zero = new SpongeTicks(0);
            this.single = new SpongeTicks(1);
            this.minecraftHour = new SpongeTicks(Constants.TickConversions.MINECRAFT_HOUR_TICKS);
            this.minecraftDay = new SpongeTicks(Constants.TickConversions.MINECRAFT_DAY_TICKS);
        }

        @Override
        public @NonNull Ticks of(final long ticks) {
            if (ticks < 0) {
                throw new IllegalArgumentException("Tick must be greater than 0!");
            }
            return new SpongeTicks(ticks);
        }

        @Override
        public @NonNull Ticks ofWallClockTime(final @NonNull Engine engine, final long time, final @NonNull TemporalUnit temporalUnit) {
            Objects.requireNonNull(engine);
            if (time < 0) {
                throw new IllegalArgumentException("Time must be greater than 0!");
            }
            Objects.requireNonNull(temporalUnit);

            final long target = temporalUnit.getDuration().multipliedBy(time).toMillis();
            return this.of((long) Math.ceil(target / (double) Constants.TickConversions.TICK_DURATION_MS));
        }

        @Override
        public @NonNull Ticks ofMinecraftSeconds(final @NonNull Engine engine, final long seconds) {
            Objects.requireNonNull(engine);
            if (seconds < 0) {
                throw new IllegalArgumentException("Seconds must be greater than 0!");
            }
            return this.of((long) Math.ceil(seconds * Constants.TickConversions.MINECRAFT_SECOND_TICKS));
        }

        @Override
        public @NonNull Ticks ofMinecraftHours(final @NonNull Engine engine, final long hours) {
            Objects.requireNonNull(engine);
            if (hours < 0) {
                throw new IllegalArgumentException("Hours must be greater than 0!");
            }
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

        @Override
        public Ticks infinite() {
            return SpongeInfiniteTicks.INSTANCE;
        }

    }

    private static final class SpongeInfiniteTicks implements Ticks {

        private static final SpongeInfiniteTicks INSTANCE = new SpongeInfiniteTicks();

        @Override
        public Duration expectedDuration(final Engine engine) {
            throw this.isInfiniteException();
        }

        @Override
        public long ticks() {
            throw this.isInfiniteException();
        }

        @Override
        public long minecraftSeconds(final Engine engine) {
            throw this.isInfiniteException();
        }

        @Override
        public Duration minecraftDayTimeDuration(final Engine engine) {
            throw this.isInfiniteException();
        }

        @Override
        public boolean isInfinite() {
            return true;
        }

        private IllegalStateException isInfiniteException() {
            throw new IllegalStateException("This instance represent infinite time");
        }
    }

}
