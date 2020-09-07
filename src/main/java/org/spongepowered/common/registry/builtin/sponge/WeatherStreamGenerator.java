package org.spongepowered.common.registry.builtin.sponge;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.common.weather.SpongeWeather;

import java.util.stream.Stream;

public final class WeatherStreamGenerator {

    private WeatherStreamGenerator() {
    }

    public static Stream<Weather> stream() {
        return Stream.of(
                new SpongeWeather(ResourceKey.minecraft("clear")),
                new SpongeWeather(ResourceKey.minecraft("rain")),
                new SpongeWeather(ResourceKey.minecraft("thunder"))
        );
    }
}
