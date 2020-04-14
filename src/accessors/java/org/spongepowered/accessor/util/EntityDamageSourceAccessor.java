package org.spongepowered.accessor.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.EntityDamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityDamageSource.class)
public interface EntityDamageSourceAccessor {

    @Accessor("damageSourceEntity") void accessor$setDamageSourceEntity(Entity entity);
}
