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
package org.spongepowered.common.mixin.inventory.event.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.inventory.util.InventoryUtil;

@Mixin(DropperBlock.class)
public abstract class DropperBlockMixin_Inventory {

    @WrapOperation(method = "dispenseFrom", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;"
    ))
    private ItemStack impl$beforeDispense(
        final Container dispenser, final Container container, final ItemStack stackToInsert, final Direction direction,
        final Operation<ItemStack> original, @Share("container") LocalRef<Container> containerRef
    ) {
        if (ShouldFire.TRANSFER_INVENTORY_EVENT_PRE && InventoryEventFactory.callTransferPre(((Inventory) dispenser), ((Inventory) container)).isCancelled()) {
            return stackToInsert;
        }
        containerRef.set(container);
        return original.call(dispenser, container, stackToInsert, direction);
    }

    @Inject(method = "dispenseFrom", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/entity/DispenserBlockEntity;setItem(ILnet/minecraft/world/item/ItemStack;)V",
        shift = At.Shift.AFTER
    ))
    private void impl$afterDispense(
        final CallbackInfo ci, @Local final DispenserBlockEntity dispenser, @Local final int index,
        @Local(ordinal = 0) final ItemStack stack, @Local(ordinal = 1) final ItemStack remainingStack,
        @Share("container") final LocalRef<Container> containerRef
    ) {
        final Container container = containerRef.get();
        if (ShouldFire.TRANSFER_INVENTORY_EVENT_POST && container != null) {
            // Transfer worked if remainder is one less than the original stack
            if (remainingStack.getCount() == stack.getCount() - 1) {
                final TrackedInventoryBridge capture = InventoryUtil.forCapture(dispenser);
                final SlotTransaction sourceSlotTransaction = InventoryEventFactory.captureTransaction(capture, (Inventory) dispenser, index, stack);
                InventoryEventFactory.callTransferPost(capture, (Inventory) dispenser, (Inventory) container, stack, sourceSlotTransaction);
            }
        }

        InventoryUtil.updateInventoryNoEvents(dispenser);
        if (container != null) {
            InventoryUtil.updateInventoryNoEvents(container);
        }
    }
}
