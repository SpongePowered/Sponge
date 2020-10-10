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
import org.spongepowered.api.util.MinecraftDayTime;
import org.spongepowered.api.util.Ticks;

import java.time.Duration;

public final class SpongeMinecraftDayTime implements MinecraftDayTime {

    public static final MinecraftDayTime.Factory FACTORY_INSTANCE = new Factory();

    static final int MINECRAFT_DAY_TICKS = 24000;
    static final int MINECRAFT_HOUR_TICKS = SpongeMinecraftDayTime.MINECRAFT_DAY_TICKS / 24;
    static final double MINECRAFT_MINUTE_TICKS = SpongeMinecraftDayTime.MINECRAFT_HOUR_TICKS / 60.0;
    private static final int MINECRAFT_EPOCH_OFFSET = 6000;

    static long getTicksFor(final long days, final long hours, final long minutes) {
        return (long) (days * SpongeMinecraftDayTime.MINECRAFT_DAY_TICKS +
                        hours * SpongeMinecraftDayTime.MINECRAFT_HOUR_TICKS +
                        minutes * SpongeMinecraftDayTime.MINECRAFT_MINUTE_TICKS);
    }

    private final long internalTime;
    private final long internalTimeWithOffset;

    public SpongeMinecraftDayTime(final long internalTime) {
        Preconditions.checkArgument(internalTime >= 0, "internalTime cannot be zero");
        this.internalTime = internalTime;
        this.internalTimeWithOffset = internalTime + SpongeMinecraftDayTime.MINECRAFT_EPOCH_OFFSET;
    }

    @Override
    public int day() {
        return (int) ((this.internalTimeWithOffset) / SpongeMinecraftDayTime.MINECRAFT_DAY_TICKS) + 1;
    }

    @Override
    public int hour() {
        return (int) (((this.internalTimeWithOffset) % SpongeMinecraftDayTime.MINECRAFT_DAY_TICKS) / SpongeMinecraftDayTime.MINECRAFT_HOUR_TICKS);
    }

    @Override
    public int minute() {
        return (int) (((this.internalTimeWithOffset) % SpongeMinecraftDayTime.MINECRAFT_HOUR_TICKS) / SpongeMinecraftDayTime.MINECRAFT_MINUTE_TICKS);
    }

    @Override
    public MinecraftDayTime plus(final int days, final int hours, final int minutes) {
        Preconditions.checkArgument(days >= 0, "days is negative");
        Preconditions.checkArgument(hours >= 0 && hours <= 23, "hours is not between 0 and 23");
        Preconditions.checkArgument(minutes >= 0 && minutes <= 59, "minutes is not between 0 and 59");
        return new SpongeMinecraftDayTime(this.internalTime + SpongeMinecraftDayTime.getTicksFor(days, hours, minutes));
    }

    @Override
    public MinecraftDayTime minus(final int days, final int hours, final int minutes) {
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

    public static final class Factory implements MinecraftDayTime.Factory {

        @Override
        public MinecraftDayTime of(final Duration duration) {
            Preconditions.checkArgument(!duration.isNegative(), "duration is negative");
            return new SpongeMinecraftDayTime((long) (duration.toMinutes() * SpongeMinecraftDayTime.MINECRAFT_MINUTE_TICKS));
        }

        @Override
        public MinecraftDayTime of(final int days, final int hours, final int minutes) {
            Preconditions.checkArgument(days >= 1, "days is not positive");
            Preconditions.checkArgument(hours >= 0 && hours <= 23 && (days > 1 || hours >= 6),
                    "hours is not between 0 and 23 (or 6 and 23 for day 1)");
            Preconditions.checkArgument(minutes >= 0 && minutes <= 59, "minutes is not between 0 and 59");
            return new SpongeMinecraftDayTime(SpongeMinecraftDayTime.getTicksFor(days, hours, minutes) - SpongeMinecraftDayTime.MINECRAFT_EPOCH_OFFSET);
        }

        @Override
        public MinecraftDayTime of(final Ticks ticks) {
            return new SpongeMinecraftDayTime(ticks.getTicks());
        }
    }

}
