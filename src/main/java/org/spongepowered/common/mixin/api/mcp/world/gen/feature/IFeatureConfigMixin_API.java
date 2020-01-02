package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import net.minecraft.world.gen.feature.IFeatureConfig;
import org.spongepowered.api.world.gen.FeatureConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IFeatureConfig.class)
public interface IFeatureConfigMixin_API extends FeatureConfig {
}
