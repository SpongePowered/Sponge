package org.spongepowered.common.accessor.entity.passive.horse;

import net.minecraft.entity.passive.horse.HorseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HorseEntity.class)
public interface HorseEntityAccessor {

    @Invoker("func_234242_w_") void accessor$func_234242_w_(int variant);

    @Invoker("func_234241_eS_") int accessor$func_234241_eS_();
}
