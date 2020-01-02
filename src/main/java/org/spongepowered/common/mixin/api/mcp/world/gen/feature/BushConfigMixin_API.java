package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import net.minecraft.world.gen.feature.BushConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BushConfig.class)
public abstract class BushConfigMixin_API implements org.spongepowered.api.world.gen.feature.config.BushConfig {
}
