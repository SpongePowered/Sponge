package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import net.minecraft.world.gen.feature.OreFeatureConfig;
import org.spongepowered.api.world.gen.feature.config.MinableConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(OreFeatureConfig.class)
public abstract class OreFeatureConfigMixin_API implements MinableConfig {
}
