package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import net.minecraft.world.gen.feature.SeaGrassConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SeaGrassConfig.class)
public abstract class SeaGrassConfigMixin_API implements org.spongepowered.api.world.gen.feature.config.SeaGrassConfig {
}
