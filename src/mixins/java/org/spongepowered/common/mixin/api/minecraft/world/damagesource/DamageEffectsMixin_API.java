package org.spongepowered.common.mixin.api.minecraft.world.damagesource;

import net.minecraft.world.damagesource.DamageEffects;
import org.spongepowered.api.event.cause.entity.damage.DamageEffect;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DamageEffects.class)
public abstract class DamageEffectsMixin_API implements DamageEffect {
}
