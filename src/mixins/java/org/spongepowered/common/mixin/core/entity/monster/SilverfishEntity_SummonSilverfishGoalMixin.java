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
package org.spongepowered.common.mixin.core.entity.monster;

import net.minecraft.block.BlockState;
import net.minecraft.block.SilverfishBlock;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.SilverfishEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.entity.GrieferBridge;

@Mixin(targets = "net/minecraft/entity/monster/SilverfishEntity$SummonSilverfishGoal")
public abstract class SilverfishEntity_SummonSilverfishGoalMixin extends Goal {

    @Shadow(aliases = "this$0") @Final private SilverfishEntity silverfish;

    /**
     * @author gabizou - April 13th, 2018
     * @author i509VCB - February 18th, 2020 - 1.14.4
     * @reason Forge changes the gamerule method calls, so the old injection/redirect
     * would fail in forge environments. This changes the injection to a predictable
     * place where we still can forcibly call things but still cancel as needed.
     *
     * @param world The World
     * @param pos The target position
     * @param dropBlock Whether to drop the block or not
     */
    @Redirect(
        method = "tick()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;destroyBlock(Lnet/minecraft/util/math/BlockPos;ZLnet/minecraft/entity/Entity;)Z"
        )
    )
    private boolean impl$onCanGrief(final World world, final BlockPos pos, final boolean dropBlock) {
        final BlockState blockState = world.getBlockState(pos);
        return ((GrieferBridge) this.silverfish).bridge$canGrief()
               ? world.destroyBlock(pos, dropBlock)
               : world.setBlockState(pos, ((SilverfishBlock) blockState.getBlock()).getMimickedBlock().getDefaultState(), 3);
    }
}
