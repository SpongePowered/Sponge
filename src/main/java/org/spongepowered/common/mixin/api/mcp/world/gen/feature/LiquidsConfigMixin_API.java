package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import net.minecraft.world.gen.feature.LiquidsConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LiquidsConfig.class)
public abstract class LiquidsConfigMixin_API implements org.spongepowered.api.world.gen.feature.config.LiquidsConfig {
}
