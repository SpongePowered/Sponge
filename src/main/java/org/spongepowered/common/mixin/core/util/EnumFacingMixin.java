package org.spongepowered.common.mixin.core.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.util.EnumFacingBridge;

@Mixin(EnumFacing.class)
public class EnumFacingMixin implements EnumFacingBridge {

    @Shadow @Final private Vec3i directionVec;

    @Override
    public Vec3i bridge$getDirectionVec() {
        return this.directionVec;
    }
}
