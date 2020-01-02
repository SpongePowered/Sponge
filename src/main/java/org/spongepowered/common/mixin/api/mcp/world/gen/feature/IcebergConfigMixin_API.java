package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import net.minecraft.world.gen.feature.IcebergConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IcebergConfig.class)
public abstract class IcebergConfigMixin_API implements org.spongepowered.api.world.gen.feature.config.IcebergConfig {
}
