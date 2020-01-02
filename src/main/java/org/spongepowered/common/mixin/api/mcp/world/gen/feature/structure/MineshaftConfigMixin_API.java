package org.spongepowered.common.mixin.api.mcp.world.gen.feature.structure;

import net.minecraft.world.gen.feature.structure.MineshaftConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MineshaftConfig.class)
public abstract class MineshaftConfigMixin_API implements org.spongepowered.api.world.gen.feature.config.MineshaftConfig {
}
