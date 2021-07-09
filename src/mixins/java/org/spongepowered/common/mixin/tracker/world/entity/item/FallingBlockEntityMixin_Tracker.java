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
package org.spongepowered.common.mixin.tracker.world.entity.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Blocks;
import org.checkerframework.checker.nullness.qual.NonNull;

import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.tick.EntityTickContext;
import org.spongepowered.common.mixin.tracker.world.entity.EntityMixin_Tracker;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin_Tracker extends EntityMixin_Tracker {

    @Override
    public void tracker$populateFrameInTickContext(
        final CauseStackManager.StackFrame frame, final EntityTickContext context
    ) {
        context.getCreator().ifPresent(frame::pushCause);
        super.tracker$populateFrameInTickContext(frame, context);
    }

    /**
     * @author gabizou - January 9th, 2018 - 1.12.2
     * @author gabizou - May 21st, 2019 - 1.12.2
     * @author gabizou - February 23rd, 2020 - 1.14.3
     *
     * @reason Due to the need to fail fast and return out of this falling block
     * entity's update if the block change is cancelled, we need to inject after
     * the world.setBlockToAir is called and potentially tell the callback to
     * cancel. Unfortunately, there's no way to just blindly checking if the block
     * was changed, because the duality of bulk and single block tracking being
     * a possibility. So, we have to do the following:
     * - check if we're throwing the event that would otherwise trigger a block capture
     * - If the current phase state allows for bulk capturing, the block will be
     *   in the capture supplier, and we can just blindly throw the processing
     *   of that block(s)
     * - If the processing is cancelled, kill this entity
     * - If we're doing single capture throws, well, double check the block state
     *   on the world, and if it's not air, well, it's been either replaced, or
     *   cancelled. If it's replaced/cancelled, then don't allow this entity to
     *   live.
     *
     * @param ci
     */
    @Inject(method = "tick()V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void tracker$handleBlockCapture(final CallbackInfo ci) {
        final BlockPos pos = new BlockPos(this.shadow$getX(), this.shadow$getY(), this.shadow$getZ());
        // So, there's two cases here: either the world is not cared for, or the
        // ChangeBlockEvent is not being listened to. If it's not being listened to,
        // we need to specifically just proceed as normal.
        if (((WorldBridge) this.level).bridge$isFake() || !ShouldFire.CHANGE_BLOCK_EVENT) {
            return;
        }
        // Ideally, at this point we should still be in the EntityTickState and only this block should
        // be changing. What we need to do here is throw the block event specifically for setting air
        // and THEN if this one cancels, we should kill this entity off, unless we want some duplication
        // of falling blocks, but, since the world already supposedly set the block to air,
        // we don't need to re-set the block state at the position, just need to check
        // if the processing succeeded or not.
        final PhaseContext<@NonNull ?> currentContext = PhaseTracker.getInstance().getPhaseContext();
        // By this point, we should have some sort of captured block
        if (currentContext.doesBlockEventTracking()) {
            if (!TrackingUtil.processBlockCaptures(currentContext)) {
                // So, it's been cancelled, we want to absolutely remove this entity.
                // And we want to stop the entity update at this point.
                this.shadow$remove();
                ci.cancel();
            }

            // We have to check if the original set block state succeeded if we're doing
            // single captures. If we're not doing bulk capturing (for whatever reason),
            // we would simply check for the current block state on the world, if it's air,
            // then it's been captured/processed for single events. And if it's not air,
            // that means that single event was cancelled, so, the block needs to remain
            // and this entity needs to die.
        } else if (this.level.getBlockState(pos) != Blocks.AIR.defaultBlockState()) {
            this.shadow$remove();
            ci.cancel();
        }
    }


}
