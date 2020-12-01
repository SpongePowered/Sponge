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
package org.spongepowered.common.mixin.optimization.mcp.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin_DisablePersistentScheduledUpdate {

    // @formatter:off
    @Shadow
    private static int shadow$getDistance(BlockState neighbor) {
        return 0;
    }

    @Shadow @Final public static IntegerProperty DISTANCE;
    @Shadow @Final public static BooleanProperty PERSISTENT;
    // @formatter:on

    /**
     * @author gabizou - November 29th, 2020 - Minecraft 1.15.2
     *
     * @reason We prevent the updatePlacement logic from scheduling a tick for a leaf block
     * that is persistent. It'd be expensive to schedule several leaf updates that
     * are placed by players instead of providing the updated block. So, we short
     * circuit the placement
     */
    @Overwrite
    public BlockState updatePostPlacement(final BlockState stateIn, final Direction facing, final BlockState facingState, final IWorld worldIn, final BlockPos currentPos, final BlockPos facingPos) {
        final int i = shadow$getDistance(facingState) + 1;

        if (i != 1 || stateIn.get(LeavesBlockMixin_DisablePersistentScheduledUpdate.DISTANCE) != i) {
            // Sponge Start - Directly provide the updated distance instead of scheduling an update
            if (facingState.get(LeavesBlockMixin_DisablePersistentScheduledUpdate.PERSISTENT)) {
                return facingState.with(LeavesBlockMixin_DisablePersistentScheduledUpdate.DISTANCE, i);
            }
            // Sponge End
            worldIn.getPendingBlockTicks().scheduleTick(currentPos, (LeavesBlock) (Object) this, 1);
        }

        return stateIn;
    }

    /**
     * Don't perform any updates on a persistent leaf block. There's no need to do so and just adds extra
     * noise.
     *
     * @param state The state
     * @param worldIn The world
     * @param pos The position of the block
     * @param rand The world random usually
     * @param ci The callback info, to cancel if the leaf state is persistent
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void optimization$disableTickForPersistentLeaves(final BlockState state, final ServerWorld worldIn, final BlockPos pos,
        final Random rand, final CallbackInfo ci) {
        if (state.get(LeavesBlockMixin_DisablePersistentScheduledUpdate.PERSISTENT)) {
            ci.cancel();
        }
    }
}
