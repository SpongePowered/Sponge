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
package org.spongepowered.common.mixin.inventory.impl.bridge;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.entity.player.PlayerInventoryBridge;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;

import java.util.List;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin implements PlayerInventoryBridge, InventoryAdapter {

    @Shadow public int currentItem;
    @Shadow @Final public PlayerEntity player;
    @Shadow @Final public NonNullList<ItemStack> mainInventory;
    @Shadow @Final public NonNullList<ItemStack> armorInventory;
    @Shadow @Final public NonNullList<ItemStack> offHandInventory;
    @Shadow @Final private List<NonNullList<ItemStack>> allInventories;

    @Shadow private int timesChanged;

    private int impl$lastTimesChanged = this.timesChanged;

    private int impl$offhandIndex;

    @Inject(method = "<init>*", at = @At("RETURN"), remap = false)
    private void onConstructed(final PlayerEntity playerIn, final CallbackInfo ci) {
        // Find offhand slot
        for (final NonNullList<ItemStack> inventory : this.allInventories) {
            if (inventory == this.offHandInventory) {
                break;
            }
            this.impl$offhandIndex += inventory.size();
        }
    }

    @Override
    public int bridge$getHeldItemIndex(final Hand hand) {
        switch (hand) {
            case MAIN_HAND:
                return this.currentItem;
            case OFF_HAND:
                return this.impl$offhandIndex;
            default:
                throw new AssertionError(hand);
        }
    }

    @Override
    public void bridge$setSelectedItem(int itemIndex, final boolean notify) {
        itemIndex = itemIndex % 9;
        if (notify && this.player instanceof ServerPlayerEntity) {
            final SHeldItemChangePacket packet = new SHeldItemChangePacket(itemIndex);
            ((ServerPlayerEntity)this.player).connection.sendPacket(packet);
        }
        this.currentItem = itemIndex;
    }

    @Override
    public void bridge$cleanupDirty() {
        if (this.timesChanged != this.impl$lastTimesChanged) {
            this.player.openContainer.detectAndSendChanges();
        }
    }

    @Override
    public void bridge$markClean() {
        this.impl$lastTimesChanged = this.timesChanged;
    }
}
