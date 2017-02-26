package org.spongepowered.common.mixin.concurrentchecks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.tracking.CauseTracker;

import java.util.ConcurrentModificationException;

@Mixin(value = CauseTracker.class, remap = false)
public abstract class MixinCauseTracker {

    @Inject(method = "setBlockState", at = @At(value = "HEAD"))
    public void onSetBlockState(final BlockPos pos, final IBlockState newState, final int flags, CallbackInfoReturnable<Boolean> ci) {
        if (!SpongeImpl.getServer().isCallingFromMinecraftThread()) {
            throw new ConcurrentModificationException(String.format("Attempting to set block %s at pos %s with flags %s off of the main thread!", newState, pos, flags));
        }
    }

}
