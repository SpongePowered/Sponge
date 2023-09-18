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
package org.spongepowered.vanilla.mixin.inventory.event.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.accessor.world.level.block.entity.HopperBlockEntityAccessor;
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.inventory.util.InventoryUtil;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin_Inventory_Vanilla {

    @Redirect(method = "ejectItems",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;isFullContainer(Lnet/minecraft/world/Container;Lnet/minecraft/core/Direction;)Z"))
    private static boolean vanilla$throwTransferPreIfNotFull(
        final Container attachedContainer, final Direction direction, final Level level, final BlockPos pos, final BlockState state,
        final Container container
    ) {
        final boolean result = HopperBlockEntityAccessor.invoker$isFullContainer(attachedContainer, direction);
        if (result || !ShouldFire.TRANSFER_INVENTORY_EVENT_PRE) {
            return result;
        }
        return InventoryEventFactory.callTransferPre(InventoryUtil.toInventory(container), InventoryUtil.toInventory(attachedContainer)).isCancelled();
    }


    // Post Captured Transactions

    @Inject(method = "ejectItems", locals = LocalCapture.CAPTURE_FAILEXCEPTION,
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z",
            ordinal = 1))
    private static void vanilla$afterPutStackInSlots(
        final Level var0, final BlockPos var1, final BlockState var2, final Container var3,
        final CallbackInfoReturnable<Boolean> cir, final Container iInventory, final Direction enumFacing,
        final int i, final ItemStack itemStack, final ItemStack itemStack1
    ) {
        // after putStackInInventoryAllSlots if the transfer worked
        if (ShouldFire.TRANSFER_INVENTORY_EVENT_POST && itemStack1.isEmpty()) {
            // Capture Insert in Origin
            final TrackedInventoryBridge capture = InventoryUtil.forCapture(var3);
            final SlotTransaction sourceSlotTransaction = InventoryEventFactory.captureTransaction(capture, (Inventory) var3, i, itemStack);
            // Call event
            InventoryEventFactory.callTransferPost(capture, (Inventory) iInventory, InventoryUtil.toInventory(iInventory), itemStack, sourceSlotTransaction);
        }
    }

}
