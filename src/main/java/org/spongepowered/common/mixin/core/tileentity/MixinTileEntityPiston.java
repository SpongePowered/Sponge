/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.mixin.core.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.EnumFacing;
import org.spongepowered.api.block.tileentity.Piston;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.event.SpongeCommonEventFactory;

@Mixin(TileEntityPiston.class)
public abstract class MixinTileEntityPiston extends MixinTileEntity implements Piston {

    @Shadow private IBlockState pistonState;
    @Shadow private EnumFacing pistonFacing;

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z", shift = At.Shift.AFTER), cancellable = true)
    public void onUpdate(CallbackInfo ci) {
        if (this.world.isRemote) {
            this.world.neighborChanged(this.pos, this.pistonState.getBlock(), this.pos);
            ci.cancel();
            return;
        }

        NotifyNeighborBlockEvent event = SpongeCommonEventFactory.callNotifyNeighborEvent((World) this.world, this.pos, java.util.EnumSet.of(this.pistonFacing.getOpposite()));
        if (event == null || !event.isCancelled() && !event.getNeighbors().isEmpty()) {
            this.world.neighborChanged(this.pos, this.pistonState.getBlock(), this.pos);
        }
        // We cancel here to avoid Forge event call in SF
        ci.cancel();
    }

    @Inject(method = "clearPistonTileEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z", shift = At.Shift.AFTER), cancellable = true)
    public void onClearPistonTileEntity(CallbackInfo ci) {
        if (this.world.isRemote) {
            this.world.neighborChanged(this.pos, this.pistonState.getBlock(), this.pos); 
            ci.cancel();
            return;
        }

        NotifyNeighborBlockEvent event = SpongeCommonEventFactory.callNotifyNeighborEvent((World) this.world, this.pos, java.util.EnumSet.of(this.pistonFacing.getOpposite()));
        if (event == null || !event.isCancelled() && !event.getNeighbors().isEmpty()) {
            this.world.neighborChanged(this.pos, this.pistonState.getBlock(), this.pos);
        }
        // We cancel here to avoid Forge event call in SF
        ci.cancel();
    }
}
