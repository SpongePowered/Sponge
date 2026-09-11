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

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.event.tracking.PhaseTracker;

@Mixin(AbstractBedBlock.class)
public abstract class AbstractBedBlockMixin {

    // @formatter:off
    @Shadow public abstract BedRule shadow$getBedRule(final Level level, final BlockPos pos);
    // @formatter:on

    @Inject(method = "useWithoutItem", at = @At(value = "HEAD"), cancellable = true)
    private void impl$onUseBed(final BlockState param0, final Level param1, final BlockPos param2, final Player param3, final BlockHitResult param5,
            final CallbackInfoReturnable<InteractionResult> cir) {
        if (!param1.isClientSide()) {
            final Cause currentCause = PhaseTracker.getInstance().currentCause();
            final BlockPos bedLocation = param5.getBlockPos();
            final BlockSnapshot snapshot = ((ServerWorld) param1).createSnapshot(bedLocation.getX(), bedLocation.getY(), bedLocation.getZ());
            if (Sponge.eventManager().post(SpongeEventFactory.createSleepingEventPre(currentCause, snapshot, (Living) param3))) {
                cir.setReturnValue(InteractionResult.CONSUME);
            }
        }
    }

    @Inject(method = "useWithoutItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/AbstractBedBlock;destroyOnUse(Lnet/minecraft/world/level/block/state/BlockState;"
                    + "Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/InteractionResult;"),
            cancellable = true)
    private void impl$onDestroyBedOnUse(final BlockState param0, final Level param1, final BlockPos param2, final Player param3, final BlockHitResult param5,
            final CallbackInfoReturnable<InteractionResult> cir) {
        final Cause currentCause = PhaseTracker.getInstance().currentCause();
        final BlockPos bedLocation = param5.getBlockPos();
        final BlockSnapshot snapshot = ((ServerWorld) param1).createSnapshot(bedLocation.getX(), bedLocation.getY(), bedLocation.getZ());
        if (Sponge.eventManager().post(SpongeEventFactory.createSleepingEventFailed(currentCause, snapshot, (Living) param3))) {
            final AbstractBedBlock self = (AbstractBedBlock) (Object) this;
            param3.startSleepInBed(self, param0, this.shadow$getBedRule(param1, param2), param2).ifLeft((param1x) -> {
                if (param1x != null && param1x.message() != null) {
                    param3.sendOverlayMessage(param1x.message());
                }
            });
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

}
