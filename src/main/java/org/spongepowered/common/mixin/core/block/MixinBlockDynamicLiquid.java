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

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.block.BlockUtil;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.Random;

@Mixin(BlockDynamicLiquid.class)
public abstract class MixinBlockDynamicLiquid {

    @Inject(method = "canFlowInto", at = @At("HEAD"), cancellable = true)
    public void onCanFlowInto(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!((IMixinWorld) worldIn).isFake() && ShouldFire.CHANGE_BLOCK_EVENT_PRE &&
            SpongeCommonEventFactory.callChangeBlockEventPre((IMixinWorldServer) worldIn, pos).isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "updateTick", at = @At("HEAD"), cancellable = true)
    public void onUpdateTickHead(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        if (!((IMixinWorld) worldIn).isFake() && ShouldFire.CHANGE_BLOCK_EVENT_PRE) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.LIQUID_FLOW, (org.spongepowered.api.world.World) worldIn);
            if (SpongeCommonEventFactory.callChangeBlockEventPre((IMixinWorldServer) worldIn, pos).isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "updateTick", at = @At("RETURN"), cancellable = true)
    public void onUpdateTickReturn(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        if (!((IMixinWorld) worldIn).isFake() && ShouldFire.CHANGE_BLOCK_EVENT_PRE) {
            Sponge.getCauseStackManager().removeContext(EventContextKeys.LIQUID_FLOW);
        }
    }

    // Capture Lava falling on Water forming Stone
    @Inject(method = "updateTick", cancellable = true, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z"))
    private void beforeSetBlockState(net.minecraft.world.World worldIn, BlockPos sourcePos, IBlockState state, Random rand, CallbackInfo ci) {
        if (!ShouldFire.CHANGE_BLOCK_EVENT_MODIFY) {
            return;
        }
        Location<org.spongepowered.api.world.World> loc = new Location<>(((org.spongepowered.api.world.World) worldIn), sourcePos.getX(), sourcePos.getY(), sourcePos.getZ());
        LocatableBlock source = LocatableBlock.builder().location(loc).build();
        IBlockState newState = Blocks.STONE.getDefaultState();
        ChangeBlockEvent.Modify event = SpongeCommonEventFactory.callChangeBlockEventModifyLiquidMix(worldIn, sourcePos, newState, source);
        Transaction<BlockSnapshot> transaction = event.getTransactions().get(0);
        if (event.isCancelled() || !transaction.isValid()) {
            ci.cancel();
            return;
        }
        if (!worldIn.setBlockState(sourcePos.down(), BlockUtil.toNative(transaction.getFinal().getState()))) {
            ci.cancel();
        }
    }

    // Capture Fluids flowing into other blocks
    @Inject(method = "tryFlowInto", cancellable = true, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/state/IBlockState;getMaterial()Lnet/minecraft/block/material/Material;"))
    private void afterCanFlowInto(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, int level, CallbackInfo ci) {
        IBlockState defaultState = ((Block) (Object) this).getDefaultState();
        if (!ShouldFire.CHANGE_BLOCK_EVENT_MODIFY) { // Check if we even need to fire.
            return;
        }
        // Do not call events when just flowing into air or same liquid
        if (state.getMaterial() != Material.AIR && state.getMaterial() != defaultState.getMaterial()) {
            IBlockState newState = defaultState.withProperty(BlockLiquid.LEVEL, level);
            ChangeBlockEvent.Break event = SpongeCommonEventFactory.callChangeBlockEventModifyLiquidBreak(worldIn, pos, newState, 3);

            Transaction<BlockSnapshot> transaction = event.getTransactions().get(0);
            if (event.isCancelled() || !transaction.isValid()) {
                ci.cancel();
                return;
            }

            // Transaction modified?
            if (transaction.getDefault() != transaction.getFinal()) {
                worldIn.setBlockState(pos, BlockUtil.toNative(transaction.getFinal().getState()));
                ci.cancel();
            }
            // else do vanilla logic
        }
    }

}
