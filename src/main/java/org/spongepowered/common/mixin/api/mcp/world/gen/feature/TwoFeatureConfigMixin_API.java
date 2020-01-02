package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import net.minecraft.world.gen.feature.TwoFeatureChoiceConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TwoFeatureChoiceConfig.class)
public abstract class TwoFeatureConfigMixin_API implements org.spongepowered.api.world.gen.feature.config.TwoFeatureChoiceConfig {
}
