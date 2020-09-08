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
package org.spongepowered.common.mixin.tracker.server.management;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.inventory.container.ContainerBridge;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

@Mixin(PlayerInteractionManager.class)
public class PlayerInteractionManagerMixin_Tracker {

    @Shadow public ServerPlayerEntity player;
    @Shadow public net.minecraft.world.server.ServerWorld world;

    // Handle Spectator opening a Container
    @Inject(method = "func_219441_a", cancellable = true,
            at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/entity/player/PlayerEntity;openContainer(Lnet/minecraft/inventory/container/INamedContainerProvider;)Ljava/util/OptionalInt;"))
    public void afterSpectatorOpenContainer(PlayerEntity playerIn, World worldIn, ItemStack stackIn, Hand handIn, BlockRayTraceResult blockRaytraceResultIn, CallbackInfoReturnable<ActionResultType> cir) {
        final Vector3i pos = VecHelper.toVector3i(blockRaytraceResultIn.getPos());
        final ServerLocation location = ServerLocation.of((ServerWorld) worldIn, pos);
        try (CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(playerIn);
            frame.addContext(EventContextKeys.BLOCK_HIT, ((ServerWorld)(worldIn)).createSnapshot(pos));
            ((ContainerBridge) playerIn.openContainer).bridge$setOpenLocation(location);
            if (!InventoryEventFactory.callInteractContainerOpenEvent(((ServerPlayerEntity) playerIn))) {
                cir.setReturnValue(ActionResultType.PASS); // Container Open was cancelled
            }
        }
    }

    // Handle non-Spectator opening a Container
    @Redirect(method = "func_219441_a",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;onBlockActivated(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/math/BlockRayTraceResult;)Lnet/minecraft/util/ActionResultType;"))
    public ActionResultType afterOpenContainer(BlockState blockState, World worldIn, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        final Container lastOpenContainer = player.openContainer;
        final ActionResultType result = blockState.onBlockActivated(worldIn, player, handIn, hit);
        if (result.isSuccess() && lastOpenContainer != player.openContainer) {
            final Vector3i pos = VecHelper.toVector3i(hit.getPos());
            final ServerLocation location = ServerLocation.of((ServerWorld) worldIn, pos);
            try (CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(player);
                frame.addContext(EventContextKeys.BLOCK_HIT, ((ServerWorld) (worldIn)).createSnapshot(pos));
                ((ContainerBridge) player.openContainer).bridge$setOpenLocation(location);
                if (!InventoryEventFactory.callInteractContainerOpenEvent((ServerPlayerEntity) player)) {
                    return ActionResultType.FAIL;
                }
            }
        }
        return result;
    }


}
