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
package org.spongepowered.common.mixin.core.block;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.event.SpongeCommonEventFactory;

@Mixin(BlockRedstoneDiode.class)
public abstract class MixinBlockRedstoneDiode extends BlockHorizontal {

    protected MixinBlockRedstoneDiode(Material materialIn) {
        super(materialIn);
    }

    @Inject(method = "notifyNeighbors", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/math/BlockPos;offset(Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/util/math/BlockPos;",
            shift = At.Shift.AFTER), cancellable = true)
    public void onNotifyNeighbors(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, CallbackInfo ci) {
        EnumFacing enumfacing = state.getValue(BlockHorizontal.FACING);
        BlockPos blockpos = pos.offset(enumfacing.getOpposite());
        if (worldIn.isRemote) {
            worldIn.neighborChanged(blockpos, this, pos);
            worldIn.notifyNeighborsOfStateExcept(blockpos, this, enumfacing);
            ci.cancel();
            return;
        }

        NotifyNeighborBlockEvent event = SpongeCommonEventFactory.callNotifyNeighborEvent((World) worldIn, pos,
                java.util.EnumSet.of(enumfacing.getOpposite()));
        if (event == null || (!event.isCancelled() && !event.getNeighbors().isEmpty())) {
            worldIn.neighborChanged(blockpos, (BlockRedstoneDiode) (Object) this, pos);
            worldIn.notifyNeighborsOfStateExcept(blockpos, (BlockRedstoneDiode) (Object) this, enumfacing);
        }
        // We cancel here to avoid Forge event call in SF
        ci.cancel();
    }
}
