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

import org.spongepowered.api.Engine;
import org.spongepowered.api.util.MinecraftDayTime;
import org.spongepowered.api.util.Ticks;

import java.time.Duration;
import java.util.Objects;

public final class SpongeMinecraftDayTime implements MinecraftDayTime {

    static long getTicksFor(final long days, final long hours, final long minutes) {
        return (long) (days * Constants.TickConversions.MINECRAFT_DAY_TICKS +
                        hours * Constants.TickConversions.MINECRAFT_HOUR_TICKS +
                        minutes * Constants.TickConversions.MINECRAFT_MINUTE_TICKS);
    }

    private final long internalTime;
    private final long internalTimeWithOffset;

    public SpongeMinecraftDayTime(final long internalTime) {
        if (internalTime < 0) {
            throw new IllegalArgumentException("Internal time cannot be negative!");
        }
        this.internalTime = internalTime;
        this.internalTimeWithOffset = internalTime + Constants.TickConversions.MINECRAFT_EPOCH_OFFSET;
    }

    @Override
    public int day() {
        return (int) ((this.internalTimeWithOffset) / Constants.TickConversions.MINECRAFT_DAY_TICKS) + 1;
    }

    @Override
    public int hour() {
        return (int) (((this.internalTimeWithOffset) % Constants.TickConversions.MINECRAFT_DAY_TICKS) / Constants.TickConversions.MINECRAFT_HOUR_TICKS);
    }

    @Override
    public int minute() {
        return (int) (((this.internalTimeWithOffset) % Constants.TickConversions.MINECRAFT_HOUR_TICKS) / Constants.TickConversions.MINECRAFT_MINUTE_TICKS);
    }

    @Override
    public MinecraftDayTime add(final Ticks ticks) {
        Objects.requireNonNull(ticks);
        if (ticks.isInfinite()) {
            throw new IllegalArgumentException("Ticks cannot be infinite!");
        }

        return new SpongeMinecraftDayTime(this.internalTime + ticks.ticks());
    }

    @Override
    public MinecraftDayTime add(final int days, final int hours, final int minutes) {
        if (days < 0) {
            throw new IllegalArgumentException("Days cannot be negative!");
        }
        if (hours < 0 || hours > 23) {
            throw new IllegalArgumentException("Hours is not between 0 and 23!");
        }
        if (minutes < 0 || minutes > 59) {
            throw new IllegalArgumentException("Minutes is not between 0 and 59!");
        }
        return new SpongeMinecraftDayTime(this.internalTime + SpongeMinecraftDayTime.getTicksFor(days, hours, minutes));
    }

    @Override
    public MinecraftDayTime subtract(final Ticks ticks) {
        Objects.requireNonNull(ticks);
        if (ticks.isInfinite()) {
            throw new IllegalArgumentException("Ticks cannot be infinite!");
        }
        final long time = this.internalTime - ticks.ticks();
        if (time <= 0) {
            throw new IllegalArgumentException("ticks is larger than this day time object");
        }
        return new SpongeMinecraftDayTime(this.internalTime - ticks.ticks());
    }

    @Override
    public MinecraftDayTime subtract(final int days, final int hours, final int minutes) {
        Preconditions.checkArgument(days >= 0, "days is negative");
        Preconditions.checkArgument(hours >= 0 && hours <= 23, "hours is not between 0 and 23");
        Preconditions.checkArgument(minutes >= 0 && minutes <= 59, "minutes is not between 0 and 59");
        final long newTickTime = this.internalTime - SpongeMinecraftDayTime.getTicksFor(days, hours, minutes);
        if (newTickTime < 0) {
            throw new IllegalArgumentException("The result MinecraftDayTime would be negative.");
        }
        return new SpongeMinecraftDayTime(newTickTime);
    }

    @Override
    public Duration asInGameDuration() {
        return Duration.ofMinutes(this.day() * 24L * 60L + this.hour() * 60L + this.minute());
    }

    @Override
    public Ticks asTicks() {
        return Ticks.of(this.internalTime);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeMinecraftDayTime that = (SpongeMinecraftDayTime) o;
        return this.internalTime == that.internalTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.internalTime);
    }

    public static final class Factory implements MinecraftDayTime.Factory {

        private final MinecraftDayTime epoch;

        public Factory() {
            this.epoch = new SpongeMinecraftDayTime(0);
        }

        @Override
        public MinecraftDayTime epoch() {
            return this.epoch;
        }

        @Override
        public MinecraftDayTime of(final Engine engine, final Duration duration) {
            Objects.requireNonNull(engine);
            Objects.requireNonNull(duration);

            if (!duration.isNegative()) {
                throw new IllegalArgumentException("Duration is negative!");
            }
            return new SpongeMinecraftDayTime((long) (duration.toMinutes() * Constants.TickConversions.MINECRAFT_MINUTE_TICKS));
        }

        @Override
        public MinecraftDayTime of(final int days, final int hours, final int minutes) {
            if (days < 1) {
                throw new IllegalArgumentException("Days must be greater than 0!");
            }
            if (hours < 0 || hours > 23 || (days == 1 && hours < 6)) {
                throw new IllegalArgumentException("Hours is not between 0 and 23 (or 6 and 23 for day 1)!");
            }
            if (minutes < 0 || minutes > 59) {
                throw new IllegalArgumentException("Minutes is not between 0 and 59!");
            }
            return new SpongeMinecraftDayTime(SpongeMinecraftDayTime.getTicksFor(days, hours, minutes) - Constants.TickConversions.MINECRAFT_EPOCH_OFFSET);
        }

        @Override
        public MinecraftDayTime of(final Engine engine, final Ticks ticks) {
            Objects.requireNonNull(engine);
            Objects.requireNonNull(ticks);
            if (ticks.isInfinite()) {
                throw new IllegalArgumentException("Ticks cannot be infinite!");
            }

            return new SpongeMinecraftDayTime(ticks.ticks());
        }
    }
}
