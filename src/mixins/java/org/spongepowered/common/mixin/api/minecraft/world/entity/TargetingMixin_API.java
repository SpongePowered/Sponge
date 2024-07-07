package org.spongepowered.common.mixin.api.minecraft.world.entity;

import org.spongepowered.api.entity.Targeting;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(net.minecraft.world.entity.Targeting.class)
public interface TargetingMixin_API extends Targeting {
}
