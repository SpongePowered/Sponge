package org.spongepowered.common.mixin.api.mcp.world.gen.feature.structure;

import net.minecraft.world.gen.feature.structure.OceanRuinConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(OceanRuinConfig.class)
public abstract class OceanRuinConfigMixin_API implements org.spongepowered.api.world.gen.feature.config.OceanRuinConfig {
}
