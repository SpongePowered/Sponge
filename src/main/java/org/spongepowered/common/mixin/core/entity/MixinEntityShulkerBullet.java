package org.spongepowered.common.mixin.core.entity;

import net.minecraft.entity.projectile.EntityShulkerBullet;
import org.spongepowered.api.entity.ShulkerBullet;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityShulkerBullet.class)
public abstract class MixinEntityShulkerBullet implements ShulkerBullet {

}
