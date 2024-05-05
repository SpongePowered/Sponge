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

import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.WeatherType;
import org.spongepowered.api.world.weather.WeatherTypes;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeTicks;

public final class SpongeWeather implements Weather {

    private final SpongeWeatherType type;
    private final Ticks remainingDuration, runningDuration;

    public SpongeWeather(final SpongeWeatherType type, final Ticks remainingDuration, final Ticks runningDuration) {
        this.type = type;
        this.remainingDuration = remainingDuration;
        this.runningDuration = runningDuration;
    }

    public static Weather of(final ServerLevelData levelData) {
        final boolean thundering = levelData.isThundering();
        if (thundering) {
            final Ticks thunderTime = SpongeTicks.ticksOrInfinite(levelData.getThunderTime());
            return new SpongeWeather((SpongeWeatherType) WeatherTypes.THUNDER.get(),
                    thunderTime,
                    thunderTime.isInfinite()
                            ? thunderTime
                            : new SpongeTicks(6000 - thunderTime.ticks()));
        }
        final boolean raining = levelData.isRaining();
        if (raining) {
            final Ticks rainTime = SpongeTicks.ticksOrInfinite(levelData.getRainTime());
            return new SpongeWeather((SpongeWeatherType) WeatherTypes.RAIN.get(),
                    rainTime,
                    rainTime.isInfinite()
                            ? rainTime
                            : new SpongeTicks(6000 - rainTime.ticks()));
        }
        final Ticks clearWeatherTime = SpongeTicks.ticksOrInfinite(levelData.getClearWeatherTime());
        return new SpongeWeather((SpongeWeatherType) WeatherTypes.CLEAR.get(),
                clearWeatherTime,
                clearWeatherTime.isInfinite()
                        ? clearWeatherTime
                        : new SpongeTicks(6000 - clearWeatherTime.ticks()));
    }

    public static void apply(final ServerLevelData levelData, final Weather weather) {
        final int time = SpongeTicks.toSaturatedIntOrInfinite(weather.remainingDuration());
        final WeatherType type = weather.type();
        if (type == WeatherTypes.CLEAR.get()) {
            levelData.setClearWeatherTime(time);
            levelData.setRaining(false);
            levelData.setRainTime(0);
            levelData.setThundering(false);
            levelData.setThunderTime(0);
        } else if (type == WeatherTypes.RAIN.get()) {
            levelData.setRaining(true);
            levelData.setRainTime(time);
            levelData.setThundering(false);
            levelData.setThunderTime(0);
            levelData.setClearWeatherTime(0);
        } else if (type == WeatherTypes.THUNDER.get()) {
            levelData.setRaining(true);
            levelData.setRainTime(time);
            levelData.setThundering(true);
            levelData.setThunderTime(time);
            levelData.setClearWeatherTime(0);
        }
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
