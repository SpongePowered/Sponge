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
package org.spongepowered.common.item.inventory.adapter.impl.slots;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.item.inventory.adapter.impl.BasicInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.impl.fabric.ContainerFabric;
import org.spongepowered.common.item.inventory.lens.impl.fabric.SlotFabric;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.List;

/**
 * Base SlotAdapter implementation for {@link net.minecraft.item.ItemStack} based Inventories.
 */
public class SlotAdapter extends BasicInventoryAdapter implements Slot {

    private final int ordinal;
    private final ImmutableList<Slot> slots;
    private final SlotLens slot;

    public SlotAdapter(Fabric inventory, SlotLens lens, Inventory parent) {
        super(inventory, lens, parent);
        this.slot = lens;
        this.ordinal = lens.getOrdinal(inventory);
        this.slots = ImmutableList.of(this);
    }

    public int getOrdinal() {
        return this.ordinal;
    }

    @Override
    public int getStackSize() {
        return this.slot.getStack(this.fabric).getCount();
    }

    @Override
    public List<Slot> slots() {
        return this.slots;
    }

    @Override
    public ItemStack poll() {
        net.minecraft.item.ItemStack stack = this.fabric.getStack(this.ordinal);
        if (stack.isEmpty()) {
            return ItemStack.empty();
        }
        this.fabric.setStack(this.ordinal, net.minecraft.item.ItemStack.EMPTY);
        return ItemStackUtil.cloneDefensive(stack);
    }

    @Override
    public ItemStack peek() {
        return ItemStackUtil.cloneDefensive(this.slot.getStack(this.fabric));
    }

    @Override
    public InventoryTransactionResult offer(ItemStack stack) {

        InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.SUCCESS);
        net.minecraft.item.ItemStack nativeStack = ItemStackUtil.toNative(stack);

        int maxStackSize = this.slot.getMaxStackSize(this.fabric);
        int remaining = stack.getQuantity();

        net.minecraft.item.ItemStack old = this.slot.getStack(this.fabric);
        ItemStackSnapshot oldSnap = ItemStackUtil.snapshotOf(old);
        int push = Math.min(remaining, maxStackSize);
        if (old.isEmpty() && this.slot.setStack(this.fabric, ItemStackUtil.cloneDefensiveNative(nativeStack, push))) {
            remaining -= push;
        } else if (!old.isEmpty() && ItemStackUtil.compareIgnoreQuantity(old, stack)) {
            this.fabric.markDirty();
            push = Math.max(Math.min(maxStackSize - old.getCount(), remaining), 0); // max() accounts for oversized stacks
            old.setCount(old.getCount() + push);
            remaining -= push;
        }

        if (remaining == stack.getQuantity()) {
            // No items were consumed
            result.reject(ItemStackUtil.cloneDefensive(nativeStack));
        } else {
            stack.setQuantity(remaining);
        }
        result.transaction(new SlotTransaction(this, oldSnap, ItemStackUtil.snapshotOf(this.slot.getStack(this.fabric))));

        return result.build();
    }

    @Override
    public InventoryTransactionResult set(ItemStack stack) {
        InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.SUCCESS);
        net.minecraft.item.ItemStack nativeStack = ItemStackUtil.toNative(stack);

        net.minecraft.item.ItemStack old = this.slot.getStack(this.fabric);
        ItemStackSnapshot oldSnap = ItemStackUtil.snapshotOf(old);
        if (stack.isEmpty()) {
            this.clear();
            SlotTransaction trans = new SlotTransaction(this, oldSnap, ItemStackSnapshot.NONE);
            return result.transaction(trans).build();
        }
        int remaining = stack.getQuantity();
        int push = Math.min(remaining, this.slot.getMaxStackSize(this.fabric));
        net.minecraft.item.ItemStack newStack = ItemStackUtil.cloneDefensiveNative(nativeStack, push);
        if (this.slot.setStack(this.fabric, newStack)) {
            SlotTransaction trans = new SlotTransaction(this, oldSnap, ItemStackUtil.snapshotOf(newStack));
            result.transaction(trans);
            remaining -= push;
        }

        if (remaining > 0) {
            result.reject(ItemStackUtil.cloneDefensive(nativeStack, remaining));
        }

        return result.build();
    }

    @Override
    public void clear() {
        this.slot.setStack(this.fabric, net.minecraft.item.ItemStack.EMPTY);
    }

    @Override
    public int size() {
        return !this.slot.getStack(this.fabric).isEmpty() ? 1 : 0;
    }

    @Override
    public int totalItems() {
        return this.slot.getStack(this.fabric).getCount();
    }

    @Override
    public int capacity() {
        return 1;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public boolean contains(ItemStack stack) {
        net.minecraft.item.ItemStack slotStack = this.slot.getStack(this.fabric);
        return slotStack.isEmpty() ? ItemStackUtil.toNative(stack).isEmpty() :
                ItemStackUtil.compareIgnoreQuantity(slotStack, stack) && slotStack.getCount() >= stack.getQuantity();
    }

    @Override
    public boolean containsAny(ItemStack stack) {
        net.minecraft.item.ItemStack slotStack = this.slot.getStack(this.fabric);
        return slotStack.isEmpty() ? ItemStackUtil.toNative(stack).isEmpty() : ItemStackUtil.compareIgnoreQuantity(slotStack, stack);
    }

    @Override
    public boolean contains(ItemType type) {
        net.minecraft.item.ItemStack slotStack = this.slot.getStack(this.fabric);
        return slotStack.isEmpty() ? (type == null || type == ItemTypes.AIR) : slotStack.getItem().equals(type);
    }

    @Override
    public Slot viewedSlot() {
        if (this.fabric instanceof SlotFabric) {
            return ((Slot) ((SlotFabric) this.fabric).getDelegate());
        }
        if (this.fabric instanceof ContainerFabric) {
            return ((Slot) ((ContainerFabric) this.fabric).getContainer().getSlot(this.ordinal));
        }
        return this;
    }

}
