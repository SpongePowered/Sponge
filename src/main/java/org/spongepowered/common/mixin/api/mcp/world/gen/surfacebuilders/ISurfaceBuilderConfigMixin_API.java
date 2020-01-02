package org.spongepowered.common.mixin.api.mcp.world.gen.surfacebuilders;

import net.minecraft.world.gen.surfacebuilders.ISurfaceBuilderConfig;
import org.spongepowered.api.world.gen.surface.SurfaceConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ISurfaceBuilderConfig.class)
public interface ISurfaceBuilderConfigMixin_API extends SurfaceConfig {
}
