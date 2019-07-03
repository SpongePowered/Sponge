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
package org.spongepowered.common.item.inventory.adapter.impl.comp;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.type.Inventory2D;
import org.spongepowered.common.item.inventory.adapter.impl.AbstractInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.AdapterLogic;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.comp.Inventory2DLens;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;

import java.util.Optional;

public class Inventory2DAdapter extends OrderedInventoryAdapter implements Inventory2D {

    protected Inventory2DLens lens2d;

    public Inventory2DAdapter(Fabric inventory, Inventory2DLens root, Inventory parent) {
        super(inventory, root, parent);
        this.lens2d = root;
    }
    
    protected SlotLens getSlotLens(int x, int y) {
        return this.getSlotLens(SlotPos.of(x, y));
    }

    protected SlotLens getSlotLens(SlotPos pos) {
        try {
            return this.lens2d.getSlot(pos);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }

    @Override
    public Optional<Slot> getSlot(SlotPos pos) {
        return AbstractInventoryAdapter.forSlot(this.bridge$getFabric(), this.getSlotLens(pos), this);
    }

    @Override
    public Optional<ItemStack> poll(SlotPos pos) {
        return AdapterLogic.pollSequential(this.bridge$getFabric(), this.getSlotLens(pos));
    }

    @Override
    public Optional<ItemStack> poll(SlotPos pos, int limit) {
        return AdapterLogic.pollSequential(this.bridge$getFabric(), this.getSlotLens(pos), limit);
    }

    @Override
    public Optional<ItemStack> peek(SlotPos pos) {
        return AdapterLogic.peekSequential(this.bridge$getFabric(), this.getSlotLens(pos));
    }

    @Override
    public Optional<ItemStack> peek(SlotPos pos, int limit) {
        return AdapterLogic.peekSequential(this.bridge$getFabric(), this.getSlotLens(pos), limit);
    }

    @Override
    public InventoryTransactionResult set(SlotPos pos, ItemStack stack) {
        return AdapterLogic.insertSequential(this.bridge$getFabric(), this.getSlotLens(pos), stack);
    }

}
