package org.spongepowered.common.mixin.api.mcp.entity.projectile;

import net.minecraft.entity.projectile.ProjectileEntity;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.mixin.api.mcp.entity.EntityMixin_API;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin_API extends EntityMixin_API implements Projectile {

}
