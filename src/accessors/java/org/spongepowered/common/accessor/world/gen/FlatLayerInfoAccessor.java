package org.spongepowered.common.accessor.world.gen;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.FlatLayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FlatLayerInfo.class)
public interface FlatLayerInfoAccessor {

    @Accessor("blockState") void accessor$blockState(BlockState block);
}
