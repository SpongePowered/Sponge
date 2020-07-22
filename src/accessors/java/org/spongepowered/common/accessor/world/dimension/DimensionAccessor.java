package org.spongepowered.common.accessor.world.dimension;

import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Dimension.class)
public interface DimensionAccessor {

    @Accessor("type") DimensionType accessor$getType();
}
