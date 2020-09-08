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
package org.spongepowered.common.mixin.tracker.entity.player;

import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.item.util.ItemStackUtil;

@Mixin(net.minecraft.entity.player.PlayerInventory.class)
public abstract class PlayerInventoryMixin_Tracker implements TrackedInventoryBridge {

    @Shadow public abstract ItemStack shadow$getStackInSlot(int index);
    @Shadow protected abstract int shadow$addResource(int p_191973_1_, ItemStack p_191973_2_);


    private Slot impl$getSpongeSlotByIndex(int index) {
        final int hotbarSize = net.minecraft.entity.player.PlayerInventory.getHotbarSize();
        if (index < hotbarSize) {
            return ((PlayerInventory) this).getPrimary().getHotbar().getSlot(index).get();
        }
        index -= hotbarSize;
        return ((PlayerInventory) this).getPrimary().getStorage().getSlot(index).get();
    }

    @Inject(method = "add", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    private void impl$ifCaptureDoTransactions(final int index, final ItemStack stack, final CallbackInfoReturnable<Boolean> cir) {
        if (this.bridge$capturingInventory()) {
            // Capture "damaged" items picked up
            final Slot slot = this.impl$getSpongeSlotByIndex(index);
            this.bridge$getCapturedSlotTransactions().add(new SlotTransaction(slot, ItemStackSnapshot.empty(), ItemStackUtil.snapshotOf(stack)));
        }
    }

    @Redirect(method = "storePartialItemStack", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/entity/player/PlayerInventory;addResource(ILnet/minecraft/item/ItemStack;)I"))
    private int impl$ifCaptureDoTransactions(final net.minecraft.entity.player.PlayerInventory inv, final int index, final ItemStack stack) {
        if (this.bridge$capturingInventory()) {
            // Capture items getting picked up
            final Slot slot = index == 40 ? ((PlayerInventory) this).getOffhand() : this.impl$getSpongeSlotByIndex(index);
            final ItemStackSnapshot original = ItemStackUtil.snapshotOf(this.shadow$getStackInSlot(index));
            final int result = this.shadow$addResource(index, stack);
            final ItemStackSnapshot replacement = ItemStackUtil.snapshotOf(this.shadow$getStackInSlot(index));
            this.bridge$getCapturedSlotTransactions().add(new SlotTransaction(slot, original, replacement));
            return result;
        }
        return this.shadow$addResource(index, stack);

    }
}
