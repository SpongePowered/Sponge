package org.spongepowered.common.mixin.api.mcp.world.gen.placement;

import net.minecraft.world.gen.placement.CountConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CountConfig.class)
public abstract class CountConfigMixin_API implements org.spongepowered.api.world.gen.feature.config.CountConfig {
}
