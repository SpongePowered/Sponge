package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import net.minecraft.world.gen.feature.EndGatewayConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EndGatewayConfig.class)
public abstract class EndGatewayConfigMixin_API implements org.spongepowered.api.world.gen.feature.config.EndGatewayConfig {
}
