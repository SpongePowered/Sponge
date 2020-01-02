package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import net.minecraft.world.gen.feature.HellLavaConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HellLavaConfig.class)
public abstract class HellLavaConfigMixin_API implements org.spongepowered.api.world.gen.feature.config.HellLavaConfig {
}
