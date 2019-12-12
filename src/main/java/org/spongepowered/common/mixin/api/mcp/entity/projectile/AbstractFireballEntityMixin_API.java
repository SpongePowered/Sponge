package org.spongepowered.common.mixin.api.mcp.entity.projectile;

import net.minecraft.entity.projectile.AbstractFireballEntity;
import org.spongepowered.api.entity.projectile.explosive.fireball.FireballEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractFireballEntity.class)
public abstract class AbstractFireballEntityMixin_API extends DamagingProjectileEntityMixin_API implements FireballEntity {
}
