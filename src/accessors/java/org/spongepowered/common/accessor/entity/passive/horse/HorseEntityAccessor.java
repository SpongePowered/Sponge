package org.spongepowered.common.accessor.entity.passive.horse;

import net.minecraft.entity.passive.horse.HorseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HorseEntity.class)
public interface HorseEntityAccessor {

    @Invoker("getTypeVariant") int accessor$getTypeVariant();

    @Invoker("setTypeVariant") void accessor$setTypeVariant(int variant);
}
