package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import net.minecraft.world.gen.feature.BlockBlobConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockBlobConfig.class)
public abstract class BlockBlobConfigMixin_API implements org.spongepowered.api.world.gen.feature.config.BlockBlobConfig {
}
