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
package org.spongepowered.common.mixin.tracker.block;

import net.minecraft.block.LeavesBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;

import java.util.Random;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin_Tracker extends BlockMixin_Tracker {

    @Shadow @Final public static BooleanProperty PERSISTENT;
    @Shadow @Final public static IntegerProperty DISTANCE;

    @Redirect(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/server/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private boolean tracker$switchContextForDecay(net.minecraft.world.server.ServerWorld serverWorld, BlockPos pos,
            net.minecraft.block.BlockState newState, int flags) {
        final PhaseTracker instance = PhaseTracker.getInstance();
        final PhaseContext<?> currentContext = instance.getPhaseContext();
        final IPhaseState<?> currentState = currentContext.state;
        try (final PhaseContext<?> context = currentState.includesDecays() ? null : BlockPhase.State.BLOCK_DECAY.createPhaseContext(instance)
                                           .source(new SpongeLocatableBlockBuilder()
                                               .world((ServerWorld) serverWorld)
                                               .position(pos.getX(), pos.getY(), pos.getZ())
                                               .state((BlockState) newState)
                                               .build())) {
            if (context != null) {
                context.buildAndSwitch();
            }
            return serverWorld.setBlockState(pos, newState, flags);
        }
    }

    /**
     * @author gabizou - February 6th, 2020 - Minecraft 1.14.3
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
    public void randomTick(net.minecraft.block.BlockState state, net.minecraft.world.server.ServerWorld worldIn, BlockPos pos, Random random) {
        if (!state.get(PERSISTENT) && state.get(DISTANCE) == 7) {
            // Sponge Start - PhaseTracker checks and phase entry
            if (!((WorldBridge) worldIn).bridge$isFake()) {
                final PhaseContext<?> peek = PhaseTracker.getInstance().getPhaseContext();
                final IPhaseState<?> currentState = peek.state;
                try (final PhaseContext<?> context = currentState.includesDecays() ? null : BlockPhase.State.BLOCK_DECAY.createPhaseContext(PhaseTracker.SERVER)
                        .source(new SpongeLocatableBlockBuilder()
                                .world((ServerWorld) worldIn)
                                .position(pos.getX(), pos.getY(), pos.getZ())
                                .state((BlockState) state)
                                .build())) {
                    if (context != null) {
                        context.buildAndSwitch();
                    }
                    shadow$spawnDrops(state, worldIn, pos);
                    worldIn.removeBlock(pos, false);
                }
                return;
            }
            // Sponge End
            shadow$spawnDrops(state, worldIn, pos);
            worldIn.removeBlock(pos, false);
        }

    }
}
