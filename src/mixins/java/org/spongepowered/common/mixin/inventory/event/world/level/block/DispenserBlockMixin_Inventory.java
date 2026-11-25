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
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseBehavior;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.event.tracking.ContainerTransfer;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.inventory.util.InventoryUtil;

/**
 * Fires the transfer post event for an item a dropper (or any dispenser with
 * an {@code insert_into_container} behavior) moved into a container. The pre
 * event and the container hand-off are in
 * {@code InsertIntoContainerDispenseBehaviorMixin_Inventory}.
 */
@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin_Inventory {

    @WrapOperation(method = "dispenseFrom", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/core/dispenser/DispenseBehavior;dispense(Ljava/lang/Iterable;Lnet/minecraft/core/dispenser/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"
    ))
    private ItemStack impl$trackTransfer(
        final Iterable<DispenseBehavior> behaviors, final BlockSource source, final ItemStack stack,
        final Operation<ItemStack> original, @Share("container") final LocalRef<Container> containerRef
    ) {
        // Open only while the behaviors run; InsertIntoContainerDispenseBehaviorMixin_Inventory reports the target to it
        try (final ContainerTransfer transfer = PhaseTracker.getInstance().pushTransfer(source.blockEntity())) {
            final ItemStack remainder = original.call(behaviors, source, stack);
            containerRef.set(transfer.target());
            return remainder;
        }
    }

    @WrapOperation(method = "dispenseFrom", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/entity/DispenserBlockEntity;setItem(ILnet/minecraft/world/item/ItemStack;)V"
    ))
    private void impl$afterDispense(
        final DispenserBlockEntity dispenser, final int index, final ItemStack remainingStack, final Operation<Void> original,
        @Local final ItemStack stack, @Share("container") final LocalRef<Container> containerRef
    ) {
        original.call(dispenser, index, remainingStack);

        final Container container = containerRef.get();
        if (container == null && !((Object) this instanceof DropperBlock)) {
            return;
        }
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
