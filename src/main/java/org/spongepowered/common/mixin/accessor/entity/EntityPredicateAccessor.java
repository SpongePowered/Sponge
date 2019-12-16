package org.spongepowered.common.mixin.accessor.entity;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Predicate;

@Mixin(EntityPredicate.class)
public interface EntityPredicateAccessor {

    @Accessor("customPredicate") Predicate<LivingEntity> accessor$getCustomPredicate();
}
