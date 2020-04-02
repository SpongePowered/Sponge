package org.spongepowered.common.mixin.accessor.item;

import net.minecraft.item.ArmorItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ArmorItem.class)
public interface ArmorItemAccessor {

    @Accessor("damageReduceAmount") int accessor$getDamageReduceAmount();
}
