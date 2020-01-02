package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import net.minecraft.world.gen.feature.SphereReplaceConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SphereReplaceConfig.class)
public abstract class SphereReplaceConfigMixin_API implements org.spongepowered.api.world.gen.feature.config.SphereReplaceConfig {
}
