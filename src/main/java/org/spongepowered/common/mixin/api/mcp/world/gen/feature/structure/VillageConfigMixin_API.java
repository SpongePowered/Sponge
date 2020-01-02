package org.spongepowered.common.mixin.api.mcp.world.gen.feature.structure;

import net.minecraft.world.gen.feature.structure.VillageConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(VillageConfig.class)
public abstract class VillageConfigMixin_API implements org.spongepowered.api.world.gen.feature.config.VillageConfig {
}
