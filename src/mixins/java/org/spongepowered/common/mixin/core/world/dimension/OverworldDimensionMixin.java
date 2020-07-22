package org.spongepowered.common.mixin.core.world.dimension;

import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.OverworldDimension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.accessor.world.dimension.DimensionAccessor;

@Mixin(OverworldDimension.class)
public abstract class OverworldDimensionMixin extends DimensionMixin {

    /**
     * @author Zidane
     * @reason Vanilla requires the DimensionType in the constructor of Dimension yet doesn't use it and hardcodes it instead...
     */
    @Overwrite
    public DimensionType getType() {
        return ((DimensionAccessor) this).accessor$getType();
    }
}
