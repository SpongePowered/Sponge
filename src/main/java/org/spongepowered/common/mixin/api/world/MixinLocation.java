package org.spongepowered.common.mixin.api.world;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.world.IMixinLocation;
import org.spongepowered.common.util.VecHelper;

import javax.annotation.Nullable;

@Mixin(value = Location.class, remap = false)
public abstract class MixinLocation implements IMixinLocation {

    @Shadow public abstract Vector3i getBlockPosition();

    @Nullable
    private BlockPos blockPos;

    @Override
    public BlockPos getBlockPos() {
        if (this.blockPos == null) {
            this.blockPos = VecHelper.toBlockPos(this.getBlockPosition());
        }
        return this.blockPos;
    }
}
