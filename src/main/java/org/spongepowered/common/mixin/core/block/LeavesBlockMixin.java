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

import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin extends BlockMixin {

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void impl$UpdateTickRandomlyFromWorldConfig(final CallbackInfo ci) {
        this.setTickRandomly(SpongeImpl.getGlobalConfigAdapter().getConfig().getWorld().getLeafDecay());
    }

    @Redirect(method = "updateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z"))
    private boolean onUpdateDecayState(final net.minecraft.world.World worldIn, final BlockPos pos, final net.minecraft.block.BlockState state, final int flags) {
        final PhaseContext<?> currentContext = PhaseTracker.getInstance().getCurrentContext();
        final IPhaseState<?> currentState = currentContext.state;
        try (final PhaseContext<?> context = currentState.includesDecays() ? null : BlockPhase.State.BLOCK_DECAY.createPhaseContext(PhaseTracker.SERVER)
                                           .source(new SpongeLocatableBlockBuilder()
                                               .world((World) worldIn)
                                               .position(pos.getX(), pos.getY(), pos.getZ())
                                               .state((BlockState) state)
                                               .build())) {
            if (context != null) {
                context.buildAndSwitch();
            }
            return worldIn.setBlockState(pos, state, flags);
        }
    }

    /**
     * @author gabizou - August 2nd, 2016
     * @reason Rewrite to handle both drops and the change state for leaves
     * that are considered to be decaying, so the drops do not leak into
     * whatever previous phase is being handled in. Since the issue is that
     * the block change takes place in a different phase (more than likely),
     * the drops are either "lost" or not considered for drops because the
     * blocks didn't change according to whatever previous phase.
     *
     * @param worldIn The world in
     * @param pos The position
     */
    @Overwrite
    private void destroy(final net.minecraft.world.World worldIn, final BlockPos pos) {
        final net.minecraft.block.BlockState state = worldIn.getBlockState(pos);
        // Sponge Start - Cause tracking
        if (worldIn.isRemote) {
            PhaseTracker.CLIENT.getSidedThread() == Thread.currentThread()
        }
        if (!((WorldBridge) worldIn).bridge$isFake()) {
            final PhaseContext<?> peek = PhaseTracker.getInstance().getCurrentContext();
            final IPhaseState<?> currentState = peek.state;
            try (final PhaseContext<?> context = currentState.includesDecays() ? null : BlockPhase.State.BLOCK_DECAY.createPhaseContext(PhaseTracker.SERVER)
                .source(new SpongeLocatableBlockBuilder()
                    .world((World) worldIn)
                    .position(pos.getX(), pos.getY(), pos.getZ())
                    .state((BlockState) state)
                    .build())) {
                if (context != null) {
                    context.buildAndSwitch();
                }
                this.dropBlockAsItem(worldIn, pos, state, 0);
                worldIn.setBlockToAir(pos);
            }
            return;
        }
        // Sponge End
        this.dropBlockAsItem(worldIn, pos, state , 0);
        worldIn.setBlockToAir(pos);

    }
}
