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
package org.spongepowered.common.mixin.inventory.event.world.level.block.entity;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.accessor.world.level.block.entity.HopperBlockEntityAccessor;
import org.spongepowered.common.bridge.world.inventory.ViewableInventoryBridge;
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.util.InventoryUtil;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin_Inventory {



    // Call PreEvents

    @Redirect(method = "suckInItems",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;isEmptyContainer(Lnet/minecraft/world/Container;Lnet/minecraft/core/Direction;)Z"))
    private static boolean impl$throwTransferPreIfNotEmpty(final Container inventory, final Direction facing, final Level level, final Hopper hopper) {
        final boolean result = HopperBlockEntityAccessor.invoker$isEmptyContainer(inventory, facing);
        if (result || !ShouldFire.TRANSFER_INVENTORY_EVENT_PRE) {
            return result;
        }
        return InventoryEventFactory.callTransferPre(InventoryUtil.toInventory(inventory), InventoryUtil.toInventory(hopper)).isCancelled();
    }

    // Capture Transactions

    @Redirect(method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;tryMoveInItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack impl$throwEventsForInsertion(final Container source, final Container destination, final ItemStack stack,
            final int index, final Direction direction) {
        // capture Transaction
        if (!((source instanceof TrackedInventoryBridge || destination instanceof TrackedInventoryBridge) && destination instanceof InventoryAdapter)) {
            return HopperBlockEntityAccessor.invoker$tryMoveInItem(source, destination, stack, index, direction);
        }
        if (!ShouldFire.TRANSFER_INVENTORY_EVENT_POST) {
            return HopperBlockEntityAccessor.invoker$tryMoveInItem(source, destination, stack, index, direction);
        }
        TrackedInventoryBridge captureIn = InventoryUtil.forCapture(source);
        if (captureIn == null) {
            captureIn = InventoryUtil.forCapture(destination);
        }
        return InventoryEventFactory.captureTransaction(captureIn, InventoryUtil.toInventory(destination), index,
                () -> HopperBlockEntityAccessor.invoker$tryMoveInItem(source, destination, stack, index, direction));
    }

    // Post Captured Transactions

    @Inject(method = "tryTakeInItemFromSlot",
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z",
                     ordinal = 1))
    private static void imlp$throwTransferEventsWhenPullingItems(final Hopper hopper, final Container iInventory, final int index,
            final Direction direction,
            final CallbackInfoReturnable<Boolean> cir, final ItemStack itemStack, final ItemStack itemStack1, final ItemStack itemStack2) {
        // after putStackInInventoryAllSlots if the transfer worked
        if (ShouldFire.TRANSFER_INVENTORY_EVENT_POST && itemStack2.isEmpty()) {
            // Capture Insert in Origin
            final TrackedInventoryBridge capture = InventoryUtil.forCapture(hopper);
            final SlotTransaction sourceSlotTransaction = InventoryEventFactory.captureTransaction(capture, InventoryUtil.toInventory(iInventory), index, itemStack1);
            // Call event
            InventoryEventFactory.callTransferPost(capture, InventoryUtil.toInventory(iInventory), InventoryUtil.toInventory(hopper), itemStack1, sourceSlotTransaction);
        }

        // Ignore all container transactions in affected inventories
        if (hopper instanceof final ViewableInventoryBridge bridge) {
            try (final PhaseContext<?> context = BlockPhase.State.RESTORING_BLOCKS.createPhaseContext(PhaseTracker.SERVER)) {
                context.buildAndSwitch();
                for (final ServerPlayer player : bridge.viewableBridge$getViewers()) {
                    player.containerMenu.broadcastChanges();
                }
            }
        }
        if (iInventory instanceof final ViewableInventoryBridge bridge) {
            try (final PhaseContext<?> context = BlockPhase.State.RESTORING_BLOCKS.createPhaseContext(PhaseTracker.SERVER)) {
                context.buildAndSwitch();
                for (final ServerPlayer player : bridge.viewableBridge$getViewers()) {
                    player.containerMenu.broadcastChanges();
                }
            }
        }
    }

    @Redirect(method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/item/ItemEntity;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack impl$onPutStackInInventoryAllSlots(final Container source, final Container destination, final ItemStack stack, final Direction direction, final Container d2, final ItemEntity entity) {
        return InventoryEventFactory.callHopperInventoryPickupEvent(destination, entity, stack);
    }
}
