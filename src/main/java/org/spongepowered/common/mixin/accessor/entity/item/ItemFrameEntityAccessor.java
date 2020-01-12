package org.spongepowered.common.mixin.accessor.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemFrameEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemFrameEntity.class)
public interface ItemFrameEntityAccessor {

    @Invoker("dropItemOrSelf") void accessor$dropItemOrSelf(Entity entity, boolean drop);

}
