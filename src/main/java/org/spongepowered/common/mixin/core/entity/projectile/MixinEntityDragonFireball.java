package org.spongepowered.common.mixin.core.entity.projectile;

import net.minecraft.entity.projectile.EntityDragonFireball;
import org.spongepowered.api.entity.projectile.explosive.DragonFireball;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityDragonFireball.class)
public abstract class MixinEntityDragonFireball extends MixinEntityFireball implements DragonFireball {

}
