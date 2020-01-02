package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import net.minecraft.world.gen.feature.NoFeatureConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NoFeatureConfig.class)
public abstract class NoFeatureConfigMixin_API implements org.spongepowered.api.world.gen.feature.config.NoFeatureConfig {
}
