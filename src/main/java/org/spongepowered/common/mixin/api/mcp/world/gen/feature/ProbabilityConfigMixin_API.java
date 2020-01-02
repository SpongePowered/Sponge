package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import net.minecraft.world.gen.feature.ProbabilityConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ProbabilityConfig.class)
public abstract class ProbabilityConfigMixin_API implements org.spongepowered.api.world.gen.config.ProbabilityConfig {
    @Shadow @Final public float probability;

    @Override
    public float getProbability() {
        return this.probability;
    }
}
