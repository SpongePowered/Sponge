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

import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.entity.GrieferBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(targets = "net/minecraft/world/entity/monster/EnderMan$EndermanLeaveBlockGoal")
public abstract class EnderMan_EndermanLeaveBlockGoalMixin extends Goal {

    // @formatter:off
    @Shadow @Final private EnderMan enderman;
    // @formatter:on

    /**
     * @author gabizou - April 13th, 2018
     *  @reason - Due to Forge's changes, there's no clear redirect or injection
     *  point where Sponge can add the griefer checks. The original redirect aimed
     *  at the gamerule check, but this can suffice for now.
     * @author gabizou - July 26th, 2018
     * @reason Adds sanity check for calling a change block event pre
     *
     * @param endermanEntity The enderman doing griefing
     * @return The block state that can be placed, or null if the enderman can't grief
     */
    @Redirect(
        method = "canUse()Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/monster/EnderMan;getCarriedBlock()Lnet/minecraft/world/level/block/state/BlockState;"
        )
    )
    @Nullable
    private BlockState impl$onCanGrief(final EnderMan endermanEntity) {
        final BlockState heldBlockState = endermanEntity.getCarriedBlock();
        return ((GrieferBridge) this.enderman).bridge$canGrief() ? heldBlockState : null;
    }

    /**
     * @reason Makes Endermen check for block changes before they can place their blocks.
     * This allows plugins to cancel the event regardless without issue.
     */
    @Redirect(method = "canPlaceBlock(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)Z",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;isCollisionShapeFullBlock(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean impl$onPlaceBlockCancel(BlockState blockState, BlockGetter blockReaderIn, BlockPos blockPosIn) {
        if (blockState.isCollisionShapeFullBlock(blockReaderIn, blockPosIn)) {
            // Sponge start
            if (ShouldFire.CHANGE_BLOCK_EVENT_PRE) {
                final ServerLocation location = ServerLocation.of((ServerWorld) blockReaderIn, blockPosIn.getX(), blockPosIn.getY(), blockPosIn.getZ());
                final List<ServerLocation> list = new ArrayList<>(1);
                list.add(location);
                final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
                final ChangeBlockEvent.Pre event = SpongeEventFactory.createChangeBlockEventPre(cause, list,
                    ((ServerWorld) this.enderman.level));
                return !SpongeCommon.post(event);
            }
            // Sponge end
            return true;
        }
        return false;
    }
}
