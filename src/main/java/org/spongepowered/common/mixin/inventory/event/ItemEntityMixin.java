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
package org.spongepowered.common.mixin.inventory.event;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.inventory.InventoryEventFactory;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Shadow private int pickupDelay;

    @Inject(
        method = "onCollideWithPlayer",
        at = @At(
            value = "INVOKE",
            ordinal = 0,
            target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"),
        cancellable = true
    )
    private void spongeImpl$ThrowPickupEvent(final PlayerEntity entityIn, final CallbackInfo ci) {
        if (!InventoryEventFactory.callPlayerChangeInventoryPickupPreEvent(entityIn, (ItemEntity) (Object) this, this.pickupDelay)) {
            ci.cancel();
        }
    }

    @Redirect(method = "onCollideWithPlayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerInventory;addItemStackToInventory(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean spongeImpl$throwPikcupEventForAddItem(final PlayerInventory inventory, final ItemStack itemStack, final PlayerEntity player) {
        final TrackedInventoryBridge inv = (TrackedInventoryBridge) inventory;
        inv.bridge$setCaptureInventory(true);
        final boolean added = inventory.addItemStackToInventory(itemStack);
        inv.bridge$setCaptureInventory(false);
        inv.bridge$getCapturedSlotTransactions();
        if (!InventoryEventFactory.callPlayerChangeInventoryPickupEvent(player, inv)) {
            return false;
        }
        return added;
    }

}
