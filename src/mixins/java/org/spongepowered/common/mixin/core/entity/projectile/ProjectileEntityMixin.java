package org.spongepowered.common.mixin.core.entity.projectile;

import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.mixin.core.entity.EntityMixin;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin extends EntityMixin {

    @Shadow protected abstract void shadow$onImpact(RayTraceResult result);
}
