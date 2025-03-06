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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.util.InventoryUtil;

import java.util.stream.IntStream;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin_Inventory {

    @Redirect(method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/item/ItemEntity;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack impl$transferFromEntity(
        final Container sourceNull, final Container destination, final ItemStack stack, final Direction directionNull, @Local(argsOnly = true) final ItemEntity entity) {
        return InventoryEventFactory.callHopperInventoryPickupEvent(destination, entity, stack);
    }

    // Call PreEvents

    @ModifyExpressionValue(method = "suckInItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;getSlots(Lnet/minecraft/world/Container;Lnet/minecraft/core/Direction;)[I"))
    private static int[] impl$beforeTransferFrom(int[] slots, @Local(argsOnly = true) final Hopper hopper, @Local final Container container) {
        final boolean empty = IntStream.of(slots).allMatch(slot -> container.getItem(slot).isEmpty());
        if (empty || !ShouldFire.TRANSFER_INVENTORY_EVENT_PRE) {
            return slots;
        }
        if (InventoryEventFactory.callTransferPre(InventoryUtil.toInventory(container), InventoryUtil.toInventory(hopper)).isCancelled()) {
            return new int[0];
        }
        return slots;
    }

    @ModifyExpressionValue(method = "ejectItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;isFullContainer(Lnet/minecraft/world/Container;Lnet/minecraft/core/Direction;)Z"))
    private static boolean impl$beforeTransferTo(boolean full, @Local(argsOnly = true) final HopperBlockEntity hopper, @Local final Container container) {
        if (full || !ShouldFire.TRANSFER_INVENTORY_EVENT_PRE) {
            return full;
        }
        return InventoryEventFactory.callTransferPre(InventoryUtil.toInventory(hopper), InventoryUtil.toInventory(container)).isCancelled();
    }

    // Capture Transactions

    @WrapOperation(method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;tryMoveInItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack impl$onTransfer(
        final Container source, final Container destination, final ItemStack stack, final int index, final Direction direction, final Operation<ItemStack> original
    ) {
        if (!((source instanceof TrackedInventoryBridge || destination instanceof TrackedInventoryBridge) && destination instanceof InventoryAdapter)) {
            return original.call(source, destination, stack, index, direction);
        }
        if (!ShouldFire.TRANSFER_INVENTORY_EVENT_POST) {
            return original.call(source, destination, stack, index, direction);
        }
        TrackedInventoryBridge capture = InventoryUtil.forCapture(source);
        if (capture == null) {
            capture = InventoryUtil.forCapture(destination);
        }
        return InventoryEventFactory.captureTransaction(capture, InventoryUtil.toInventory(destination), index,
            () -> original.call(source, destination, stack, index, direction));
    }

    // Post Captured Transactions

    @Inject(method = "tryTakeInItemFromSlot", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 1))
    private static void impl$afterTransferFrom(
        final Hopper hopper, final Container container, final int index, final Direction direction,
        final CallbackInfoReturnable<Boolean> cir, final ItemStack stack, final int originalCount, final ItemStack insertedStack
    ) {
        // If the transfer worked
        if (ShouldFire.TRANSFER_INVENTORY_EVENT_POST && insertedStack.isEmpty()) {
            // Capture Insert in Origin
            final TrackedInventoryBridge capture = InventoryUtil.forCapture(hopper);
            final int newCount = stack.getCount();
            stack.setCount(originalCount);
            final ItemStack originalStack = stack.copy();
            stack.setCount(newCount);
            final SlotTransaction sourceSlotTransaction = InventoryEventFactory.captureTransaction(capture, InventoryUtil.toInventory(container), index, originalStack);
            // Call event
            InventoryEventFactory.callTransferPost(capture, InventoryUtil.toInventory(container), InventoryUtil.toInventory(hopper), originalStack, sourceSlotTransaction);
        }

        InventoryUtil.updateInventoryNoEvents(container);
        InventoryUtil.updateInventoryNoEvents(hopper);
    }

    @Inject(method = "ejectItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 1))
    private static void impl$afterTransferTo(
        final Level level, final BlockPos pos, final HopperBlockEntity hopper, final CallbackInfoReturnable<Boolean> cir,
        @Local final Container container, @Local(ordinal = 0) final int index, @Local(ordinal = 1) final int originalCount,
        @Local(ordinal = 0) final ItemStack stack, @Local(ordinal = 1) final ItemStack insertedStack
    ) {
        // If the transfer worked
        if (ShouldFire.TRANSFER_INVENTORY_EVENT_POST && insertedStack.isEmpty()) {
            // Capture Insert in Origin
            final TrackedInventoryBridge capture = InventoryUtil.forCapture(hopper);
            final int newCount = stack.getCount();
            stack.setCount(originalCount);
            final ItemStack originalStack = stack.copy();
            stack.setCount(newCount);
            final SlotTransaction sourceSlotTransaction = InventoryEventFactory.captureTransaction(capture, InventoryUtil.toInventory(hopper), index, originalStack);
            // Call event
            InventoryEventFactory.callTransferPost(capture, InventoryUtil.toInventory(hopper), InventoryUtil.toInventory(container), originalStack, sourceSlotTransaction);
        }

        InventoryUtil.updateInventoryNoEvents(hopper);
        InventoryUtil.updateInventoryNoEvents(container);
    }
}
