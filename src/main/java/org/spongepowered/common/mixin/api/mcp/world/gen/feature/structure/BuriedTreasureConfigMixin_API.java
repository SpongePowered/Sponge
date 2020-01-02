package org.spongepowered.common.mixin.api.mcp.world.gen.feature.structure;

import net.minecraft.world.gen.feature.structure.BuriedTreasureConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BuriedTreasureConfig.class)
public abstract class BuriedTreasureConfigMixin_API implements org.spongepowered.api.world.gen.feature.config.BuriedTreasureConfig {
}
