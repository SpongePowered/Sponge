package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegisterCatalog;
import org.spongepowered.common.weather.SpongeWeather;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class WeatherRegistryModule implements CatalogRegistryModule<Weather> {

    @RegisterCatalog(Weathers.class)
    private final Map<String, Weather> weatherMappings = Maps.newHashMap();

    @Override
    public Optional<Weather> getById(String id) {
        return Optional.ofNullable(this.weatherMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<Weather> getAll() {
        return ImmutableList.copyOf(this.weatherMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.weatherMappings.put("clear", new SpongeWeather("clear"));
        this.weatherMappings.put("rain", new SpongeWeather("rain"));
        this.weatherMappings.put("thunder_storm", new SpongeWeather("thunder_storm"));
    }
}
