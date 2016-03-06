package org.spongepowered.common.mixin.core.entity.projectile;

import net.minecraft.entity.projectile.EntitySpectralArrow;
import org.spongepowered.api.entity.projectile.arrow.SpectralArrow;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntitySpectralArrow.class)
public abstract class MixinEntitySpectralArrow extends MixinEntityArrow implements SpectralArrow {

}
