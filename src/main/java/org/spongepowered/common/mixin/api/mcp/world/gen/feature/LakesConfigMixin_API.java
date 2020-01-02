package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import net.minecraft.world.gen.feature.LakesConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LakesConfig.class)
public abstract class LakesConfigMixin_API implements org.spongepowered.api.world.gen.feature.config.LakesConfig {
}
