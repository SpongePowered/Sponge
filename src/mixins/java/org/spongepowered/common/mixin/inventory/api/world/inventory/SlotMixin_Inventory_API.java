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
package org.spongepowered.common.mixin.inventory.api.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackLike;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.inventory.adapter.impl.DefaultImplementedAdapterInventory;
import org.spongepowered.common.item.util.ItemStackUtil;

@Mixin(Slot.class)
public abstract class SlotMixin_Inventory_API implements org.spongepowered.api.item.inventory.Slot, DefaultImplementedAdapterInventory.WithClear {

    // @formatter:off
    @Shadow @Final public Container container;
    @Shadow public abstract void shadow$set(net.minecraft.world.item.ItemStack param0);
    @Shadow public abstract net.minecraft.world.item.ItemStack shadow$getItem();
    @Shadow public abstract int shadow$getMaxStackSize();
    // @formatter:on

    @Override
    public Inventory parent() {
        if (this.container instanceof Inventory) {
            return (Inventory) this.container;
        }
        // In modded the inventory could be null
        return this;
    }

    @Override
    public org.spongepowered.api.item.inventory.Slot viewedSlot() {
        return this;
    }

    @Override
    public InventoryTransactionResult set(ItemStackLike stack) {
        final InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.SUCCESS);
        final net.minecraft.world.item.ItemStack nativeStack = ItemStackUtil.fromLikeToNative(stack);

        final net.minecraft.world.item.ItemStack old = this.shadow$getItem();
        ItemStackSnapshot oldSnap = ItemStackUtil.snapshotOf(old);

        int remaining = stack.quantity();
        final int push = Math.min(remaining, this.shadow$getMaxStackSize());
        net.minecraft.world.item.ItemStack newStack = ItemStackUtil.cloneDefensiveNative(nativeStack, push);
        this.shadow$set(newStack);
        result.transaction(new SlotTransaction(this, oldSnap, ItemStackUtil.snapshotOf(newStack)));
        remaining -= push;

        if (remaining > 0) {
            result.reject(ItemStackUtil.cloneDefensive(nativeStack, remaining));
        }

        return result.build();
    }
}
