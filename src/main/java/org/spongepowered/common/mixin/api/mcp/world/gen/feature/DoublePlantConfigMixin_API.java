package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import net.minecraft.world.gen.feature.DoublePlantConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DoublePlantConfig.class)
public abstract class DoublePlantConfigMixin_API implements org.spongepowered.api.world.gen.feature.config.DoublePlantConfig {
}
