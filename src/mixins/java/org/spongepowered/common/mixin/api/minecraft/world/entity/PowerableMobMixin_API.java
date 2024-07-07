package org.spongepowered.common.mixin.api.minecraft.world.entity;

import net.minecraft.world.entity.PowerableMob;
import org.spongepowered.api.entity.Chargeable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PowerableMob.class)
public interface PowerableMobMixin_API extends Chargeable {
}
