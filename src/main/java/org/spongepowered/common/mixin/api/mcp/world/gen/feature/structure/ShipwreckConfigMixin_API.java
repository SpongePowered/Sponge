package org.spongepowered.common.mixin.api.mcp.world.gen.feature.structure;

import net.minecraft.world.gen.feature.structure.ShipwreckConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShipwreckConfig.class)
public abstract class ShipwreckConfigMixin_API implements org.spongepowered.api.world.gen.feature.config.ShipwreckConfig {
}
