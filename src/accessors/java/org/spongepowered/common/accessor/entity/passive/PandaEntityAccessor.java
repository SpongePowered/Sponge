package org.spongepowered.common.accessor.entity.passive;

import net.minecraft.entity.passive.PandaEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PandaEntity.class)
public interface PandaEntityAccessor {

    @Invoker("func_213559_es") int accessor$getEatingTime();

    @Invoker("func_213571_t") void accessor$setEatingTime(int eatingTime);

    @Invoker("func_213585_ee") int accessor$getSneezingTime();

    @Invoker("func_213562_s") void accessor$setSneezingTime(int sneezingTime);
}
