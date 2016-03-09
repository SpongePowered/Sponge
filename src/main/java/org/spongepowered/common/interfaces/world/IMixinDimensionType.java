package org.spongepowered.common.interfaces.world;

import org.spongepowered.api.service.context.Context;
import org.spongepowered.common.config.SpongeConfig;

public interface IMixinDimensionType {

    SpongeConfig<SpongeConfig.DimensionConfig> getDimensionConfig();

    Context getContext();
}
