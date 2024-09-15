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
package org.spongepowered.common.inventory.adapter.impl.slots;

import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackLike;
import org.spongepowered.api.item.inventory.slot.FilteringSlot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.impl.slot.FilteringSlotLens;

public class FilteringSlotAdapter extends SlotAdapter implements FilteringSlot {

    protected final FilteringSlotLens filteringSlot;

    public FilteringSlotAdapter(Fabric fabric, FilteringSlotLens lens, Inventory parent) {
        super(fabric, lens, parent);
        this.filteringSlot = lens;
    }

    @Override
    public boolean isValidItem(ItemStackLike stack) {
        FilteringSlotLens.ItemStackFilter filter = this.filteringSlot.getItemStackFilter();
        return filter == null || filter.test(this.impl$getFabric(), stack);
    }

    @Override
    public boolean isValidItem(ItemType type) {
        return this.isValidItem(ItemStack.of(type));
    }

    /*
    @Override
    public InventoryTransactionResult offer(ItemStack stack) {
        final boolean canOffer = isValidItem(stack);
        if (!canOffer) {
            final InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.FAILURE);
            result.reject(ItemStackUtil.cloneDefensive(stack));
            return result.build();
        }

        return super.offer(stack);
    }
    */

    @Override
    public InventoryTransactionResult set(ItemStackLike stack) {
        final boolean canSet = this.isValidItem(stack);
        if (!canSet) {
            final InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.FAILURE);
            result.reject(stack);
            return result.build();
        }

        return super.set(stack);
    }
}
