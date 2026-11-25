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
package org.spongepowered.common.mixin.inventory.event.core.dispenser;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.InsertIntoContainerDispenseBehavior;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.event.tracking.ContainerTransfer;
import org.spongepowered.common.event.tracking.PhaseTracker;

@Mixin(InsertIntoContainerDispenseBehavior.class)
public abstract class InsertIntoContainerDispenseBehaviorMixin_Inventory {

    @WrapOperation(method = "execute", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;"
    ))
    private ItemStack impl$beforeDispense(
        final Container dispenser, final Container container, final ItemStack stackToInsert, final Direction direction,
        final Operation<ItemStack> original
    ) {
        if (ShouldFire.TRANSFER_INVENTORY_EVENT_PRE && InventoryEventFactory.callTransferPre(((Inventory) dispenser), ((Inventory) container)).isCancelled()) {
            return stackToInsert;
        }
        // Picked up by DispenserBlockMixin_Inventory once the dispenser slot is updated
        final @Nullable ContainerTransfer transfer = PhaseTracker.getInstance().currentTransfer(dispenser);
        if (transfer != null) {
            transfer.target(container);
        }
        return original.call(dispenser, container, stackToInsert, direction);
    }
}
