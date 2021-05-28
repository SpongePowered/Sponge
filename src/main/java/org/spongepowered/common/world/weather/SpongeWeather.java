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
package org.spongepowered.common.world.weather;

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.WeatherType;
import org.spongepowered.common.util.Constants;

public final class SpongeWeather implements Weather {

    private final SpongeWeatherType type;
    private final Ticks remainingDuration, runningDuration;

    public SpongeWeather(final SpongeWeatherType type, final Ticks remainingDuration, final Ticks runningDuration) {
        this.type = type;
        this.remainingDuration = remainingDuration;
        this.runningDuration = runningDuration;
    }

    @Override
    public WeatherType type() {
        return this.type;
    }

    @Override
    public Ticks remainingDuration() {
        return this.remainingDuration;
    }

    @Override
    public Ticks runningDuration() {
        return this.runningDuration;
    }

    @Override
    public int contentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, this.contentVersion())
                .set(Constants.Universe.Weather.TYPE, this.type.key(RegistryTypes.WEATHER_TYPE))
                .set(Constants.Universe.Weather.REMAINING_DURATION, this.remainingDuration.ticks())
                .set(Constants.Universe.Weather.RUNNING_DURATION, this.runningDuration.ticks());
    }

    public static class FactoryImpl implements Weather.Factory {

        @Override
        public Weather of(WeatherType type, Ticks remainingDuration, Ticks runningDuration) {
            return new SpongeWeather((SpongeWeatherType) type, remainingDuration, runningDuration);
        }
    }
}
