package org.spongepowered.common.mixin.accessor.block;

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.SkullBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSkullBlock.class)
public interface AbstractSkullBlockAccessor {

    @Accessor("skullType") SkullBlock.ISkullType accessor$getSkullType();
}
