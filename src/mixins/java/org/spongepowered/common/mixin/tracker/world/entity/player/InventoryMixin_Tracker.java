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
package org.spongepowered.common.mixin.tracker.world.entity.player;

import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.TransactionalCaptureSupplier;

@Mixin(net.minecraft.world.entity.player.Inventory.class)
public abstract class InventoryMixin_Tracker {

    @Shadow public abstract ItemStack shadow$getItem(int index);
    @Shadow protected abstract int shadow$addResource(int p_191973_1_, ItemStack p_191973_2_);

    private Slot impl$getSpongeSlotByIndex(int index) {
        final int hotbarSize = net.minecraft.world.entity.player.Inventory.getSelectionSize();
        if (index < hotbarSize) {
            return ((PlayerInventory) this).primary().hotbar().slot(index).get();
        }
        index -= hotbarSize;
        return ((PlayerInventory) this).primary().storage().slot(index).get();
    }

    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    private void impl$ifCaptureDoTransactions(final int index, final ItemStack stack, final CallbackInfoReturnable<Boolean> cir) {
        final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
        final TransactionalCaptureSupplier transactor = context.getTransactor();
        // Capture "damaged" items picked up
        final Slot slot = this.impl$getSpongeSlotByIndex(index);
        transactor.logInventorySlotTransaction(context, slot, ItemStack.EMPTY, stack, (Inventory) this);
    }

    @Redirect(method = "addResource(Lnet/minecraft/world/item/ItemStack;)I", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/entity/player/Inventory;addResource(ILnet/minecraft/world/item/ItemStack;)I"))
    private int impl$ifCaptureDoTransactions(final net.minecraft.world.entity.player.Inventory inv, final int index, final ItemStack stack) {
        final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
        final TransactionalCaptureSupplier transactor = context.getTransactor();

        // Capture items getting picked up
        final Slot slot = index == 40 ? ((PlayerInventory) this).offhand() : this.impl$getSpongeSlotByIndex(index);
        final ItemStack original = this.shadow$getItem(index).copy();
        final int result = this.shadow$addResource(index, stack);
        transactor.logInventorySlotTransaction(context, slot, original, this.shadow$getItem(index), (Inventory) this);
        return result;
    }
}
