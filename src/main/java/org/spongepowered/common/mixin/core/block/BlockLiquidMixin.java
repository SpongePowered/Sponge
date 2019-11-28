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

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.event.SpongeCommonEventFactory;

@Mixin(BlockLiquid.class)
public abstract class BlockLiquidMixin extends BlockMixin {

    // Capture Lava and Water mixing forming CobbleStone or Obsidian
    @Inject(
        method = "checkForMixing",
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILSOFT,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z"
        )
    )
    private void impl$CheckForLiquidMixing(final World worldIn, final BlockPos pos, final BlockState state,
        final CallbackInfoReturnable<Boolean> cir, final boolean flag, final Integer integer) {
        final BlockState newState = integer == 0 ? Blocks.field_150343_Z.func_176223_P() : Blocks.field_150347_e.func_176223_P();
        final ChangeBlockEvent.Modify event = SpongeCommonEventFactory.callChangeBlockEventModifyLiquidMix(worldIn, pos, newState, null);
        final Transaction<BlockSnapshot> transaction = event.getTransactions().get(0);
        if (event.isCancelled() || !transaction.isValid()) {
            cir.setReturnValue(false);
            return;
        }
        final boolean success = worldIn.func_175656_a(pos, (BlockState) transaction.getFinal().getState());
        if (!success) {
            cir.setReturnValue(false);
        }
    }

}
