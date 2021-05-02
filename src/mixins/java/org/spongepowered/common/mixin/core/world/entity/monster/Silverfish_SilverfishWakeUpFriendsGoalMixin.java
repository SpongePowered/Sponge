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
package org.spongepowered.common.mixin.core.world.entity.monster;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.entity.GrieferBridge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(targets = "net/minecraft/world/entity/monster/Silverfish$SilverfishWakeUpFriendsGoal")
public abstract class Silverfish_SilverfishWakeUpFriendsGoalMixin extends Goal {

    // @formatter:off
    @Shadow(aliases = "this$0") @Final private Silverfish silverfish;
    // @formatter:on

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
            target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z"
        )
    )
    private boolean impl$onCanGrief(final Level world, final BlockPos pos, final boolean dropBlock, final Entity entity) {
        final BlockState blockState = world.getBlockState(pos);
        return ((GrieferBridge) entity).bridge$canGrief()
               ? world.destroyBlock(pos, dropBlock)
               : world.setBlock(pos, ((InfestedBlock) blockState.getBlock()).getHostBlock().defaultBlockState(), 3);
    }
}
