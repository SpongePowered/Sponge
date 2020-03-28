package org.spongepowered.common.data.provider.block.location;

import org.spongepowered.common.data.provider.DataProviderRegistry;
import org.spongepowered.common.data.provider.DataProviderRegistryBuilder;

public class LocationDataProviders extends DataProviderRegistryBuilder {

    public LocationDataProviders(DataProviderRegistry registry) {
        super(registry);
    }

    @Override
    public void register() {
        this.register(new GroundLuminanceProvider());
        this.register(new SkyLuminanceProvider());
        this.register(new BiomeTemperatureProvider());
        this.register(new TemperatureProvider());

    }
}
