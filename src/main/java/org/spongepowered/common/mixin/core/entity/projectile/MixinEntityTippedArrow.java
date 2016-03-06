package org.spongepowered.common.mixin.core.entity.projectile;

import net.minecraft.entity.projectile.EntityTippedArrow;
import org.spongepowered.api.entity.projectile.arrow.TippedArrow;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityTippedArrow.class)
public abstract class MixinEntityTippedArrow extends MixinEntityArrow implements TippedArrow {

}
