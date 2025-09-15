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
package org.spongepowered.common.mixin.core.server.level;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.server.level.ServerPlayerGameModeBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.VecHelper;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin implements ServerPlayerGameModeBridge {

    // @formatter:off
    @Shadow @Final protected ServerPlayer player;
    @Shadow protected net.minecraft.server.level.ServerLevel level;
    // @formatter:on

    private boolean impl$interactBlockLeftClickEventCancelled = false;
    private boolean impl$interactBlockRightClickEventCancelled = false;

    private @Nullable Direction impl$blockBreakActionDirection;
    private @Nullable Direction impl$delayedBlockBreakActionDirection;

    @Override
    public boolean bridge$isInteractBlockRightClickCancelled() {
        return this.impl$interactBlockRightClickEventCancelled;
    }

    @Override
    public void bridge$setInteractBlockRightClickCancelled(final boolean cancelled) {
        this.impl$interactBlockRightClickEventCancelled = cancelled;
    }

    /**
     * We have to check for cancelled left click events because they occur from different packets
     * or processing branches such that there's no clear "context" of where we can store these variables.
     * So, we store it to the interaction manager's fields, to avoid contaminating other interaction
     * manager's processes.
    */
    @WrapMethod(method = "handleBlockBreakAction")
    private void impl$cancelIfInteractBlockPrimaryCancelled(final BlockPos $$0, final ServerboundPlayerActionPacket.Action $$1,
            final Direction $$2, final int $$3, final int $$4, final Operation<Void> original) {
        if (this.impl$interactBlockLeftClickEventCancelled) {
            this.impl$interactBlockLeftClickEventCancelled = false;
            return;
        }
        try {
            this.impl$blockBreakActionDirection = $$2;
            original.call($$0, $$1, $$2, $$3, $$4);
        } finally {
            this.impl$blockBreakActionDirection = null;
        }
    }

    @Inject(method = "handleBlockBreakAction", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;delayedDestroyPos:Lnet/minecraft/core/BlockPos;"))
    private void impl$onSetDelayedDestroyPos(final BlockPos $$0, final ServerboundPlayerActionPacket.Action $$1, final Direction $$2, final int $$3, final int $$4, final CallbackInfo ci) {
        this.impl$delayedBlockBreakActionDirection = $$2;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean impl$onDelayedBlockDestruction(final ServerPlayerGameMode instance, final BlockPos $$0, final Operation<Boolean> original) {
        try {
            this.impl$blockBreakActionDirection = this.impl$delayedBlockBreakActionDirection;
            return original.call(instance, $$0);
        } finally {
            this.impl$blockBreakActionDirection = null;
        }
    }

    @WrapMethod(method = "destroyBlock")
    private boolean impl$onDestroyBlock(final BlockPos $$0, final Operation<Boolean> original) {
        final PhaseTracker tracker = PhaseTracker.getWorldInstance(this.level);
        try (final CauseStackManager.StackFrame frame = tracker.pushCauseFrame()) {
            final BlockSnapshot snapshot = ((org.spongepowered.api.world.server.ServerWorld) this.level)
                .createSnapshot(VecHelper.toVector3i($$0));
            final ItemStack heldItem = this.player.getItemInHand(InteractionHand.MAIN_HAND);
            SpongeCommonEventFactory.applyCommonInteractContext(this.player, heldItem, InteractionHand.MAIN_HAND, snapshot, null, frame);
            final InteractBlockEvent.Primary.Finish event = SpongeCommonEventFactory.callInteractBlockEventPrimaryFinish(
                snapshot, this.impl$blockBreakActionDirection);
            if (event.isCancelled()) {
                return false;
            }
            frame.pushCause(event);
            return original.call($$0);
        }
    }
}
