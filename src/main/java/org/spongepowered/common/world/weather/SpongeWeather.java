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
            final int thunderTime = levelData.getThunderTime();
            return new SpongeWeather((SpongeWeatherType) WeatherTypes.THUNDER.get(),
                    new SpongeTicks(thunderTime),
                    new SpongeTicks(6000 - thunderTime));
        }
        final boolean raining = levelData.isRaining();
        if (raining) {
            final int rainTime = levelData.getRainTime();
            return new SpongeWeather((SpongeWeatherType) WeatherTypes.RAIN.get(),
                    new SpongeTicks(rainTime),
                    new SpongeTicks(6000 - rainTime));
        }
        final int clearWeatherTime = levelData.getClearWeatherTime();
        return new SpongeWeather((SpongeWeatherType) WeatherTypes.CLEAR.get(),
                new SpongeTicks(clearWeatherTime),
                new SpongeTicks(6000 - clearWeatherTime));
    }

    public static void apply(final ServerLevelData levelData, final Weather weather) {
        final long time = weather.remainingDuration().ticks();
        final WeatherType type = weather.type();
        if (type == WeatherTypes.CLEAR.get()) {
            levelData.setClearWeatherTime((int) time);
            levelData.setRaining(false);
            levelData.setRainTime(0);
            levelData.setThundering(false);
            levelData.setThunderTime(0);
        } else if (type == WeatherTypes.RAIN.get()) {
            levelData.setRaining(true);
            levelData.setRainTime((int) time);
            levelData.setThundering(false);
            levelData.setThunderTime(0);
            levelData.setClearWeatherTime(0);
        } else if (type == WeatherTypes.THUNDER.get()) {
            levelData.setRaining(true);
            levelData.setRainTime((int) time);
            levelData.setThundering(true);
            levelData.setThunderTime((int) time);
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
