package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import net.minecraft.world.gen.feature.FeatureRadiusConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FeatureRadiusConfig.class)
public abstract class FeatureRadiusConfigMixin_API implements org.spongepowered.api.world.gen.feature.config.FeatureRadiusConfig {
}
