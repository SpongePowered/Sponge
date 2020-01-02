package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import net.minecraft.world.gen.feature.ReplaceBlockConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ReplaceBlockConfig.class)
public abstract class ReplaceBlockConfigMixin_API implements org.spongepowered.api.world.gen.feature.config.ReplaceBlockConfig {
}
