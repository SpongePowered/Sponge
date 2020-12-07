package org.spongepowered.common.accessor.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(FishingBobberEntity.class)
public interface FishingBobberEntityAccessor {

    @Accessor("caughtEntity") void accessor$setCaughtEntity(@Nullable Entity caughtEntity);
}
