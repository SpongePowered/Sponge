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

import com.google.common.collect.ImmutableList;
import net.minecraft.inventory.container.Container;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.inventory.adapter.impl.BasicInventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.slots.SlotLens;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.List;

/**
 * Base SlotAdapter implementation for {@link net.minecraft.item.ItemStack} based Inventories.
 */
@SuppressWarnings("rawtypes")
public class SlotAdapter extends BasicInventoryAdapter implements Slot {

    private final int ordinal;
    private final ImmutableList<Slot> slots;
    private final SlotLens slot;

    public SlotAdapter(Fabric fabric, SlotLens lens, Inventory parent) {
        super(fabric, lens, parent);
        this.slot = lens;
        this.ordinal = lens.getOrdinal(fabric);
        this.slots = ImmutableList.of(this);
    }

    public int getOrdinal() {
        return this.ordinal;
    }

    @Override
    public List<Slot> slots() {
        return this.slots;
    }

    @Override
    public InventoryTransactionResult.Poll poll() {
        final net.minecraft.item.ItemStack stack = this.bridge$getFabric().fabric$getStack(this.ordinal);
        if (stack.isEmpty()) {
            return InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.SUCCESS).poll(ItemStackSnapshot.empty()).build();
        }

        this.bridge$getFabric().fabric$setStack(this.ordinal, net.minecraft.item.ItemStack.EMPTY);
        return InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.SUCCESS)
                .transaction(new SlotTransaction(this, ItemStackUtil.snapshotOf(stack), ItemStackSnapshot.empty()))
                .poll(ItemStackUtil.snapshotOf(stack)).build();
    }


    @Override
    public ItemStack peek() {
        final net.minecraft.item.ItemStack stack = this.slot.getStack(this.bridge$getFabric());
        return ItemStackUtil.cloneDefensive(stack);
    }

    @Override
    public InventoryTransactionResult offer(final ItemStack... stacks) {

        final InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.SUCCESS);
        for (ItemStack stack : stacks) {
            final net.minecraft.item.ItemStack nativeStack = ItemStackUtil.toNative(stack);

            final int maxStackSize = this.slot.getMaxStackSize(this.bridge$getFabric());
            int remaining = stack.getQuantity();

            final net.minecraft.item.ItemStack old = this.slot.getStack(this.bridge$getFabric());
            ItemStackSnapshot oldStack = ItemStackUtil.snapshotOf(old);
            ItemStackSnapshot newStack = oldStack;
            int push = Math.min(remaining, maxStackSize);
            if (old.isEmpty() && this.slot.setStack(this.bridge$getFabric(), ItemStackUtil.cloneDefensiveNative(nativeStack, push))) {
                remaining -= push;
                newStack = ItemStackUtil.snapshotOf(stack);
            } else if (!old.isEmpty() && ItemStackUtil.compareIgnoreQuantity(old, stack)) {
                this.bridge$getFabric().fabric$markDirty();
                push = Math.max(Math.min(maxStackSize - old.getCount(), remaining), 0); // max() accounts for oversized stacks
                old.setCount(old.getCount() + push);
                remaining -= push;
                newStack = ItemStackUtil.snapshotOf(old);
            }

            result.transaction(new SlotTransaction(this, oldStack, newStack));
            if (remaining == stack.getQuantity()) {
                // No items were consumed
                result.reject(ItemStackUtil.cloneDefensive(nativeStack));
                result.type(InventoryTransactionResult.Type.FAILURE);
            }
        }

        return result.build();
    }

    @Override
    public boolean canFit(final ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        int maxStackSize = this.bridge$getFabric().fabric$getMaxStackSize();
        final net.minecraft.item.ItemStack old = this.slot.getStack(this.bridge$getFabric());
        if (old.isEmpty()) {
            return maxStackSize >= stack.getQuantity();
        }
        return ItemStackUtil.compareIgnoreQuantity(old, stack) && maxStackSize - old.getCount() >= stack.getQuantity();
    }

    @Override
    public InventoryTransactionResult set(final ItemStack stack) {
        final InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.SUCCESS);
        final net.minecraft.item.ItemStack nativeStack = ItemStackUtil.toNative(stack);

        final net.minecraft.item.ItemStack old = this.slot.getStack(this.bridge$getFabric());
        ItemStackSnapshot oldSnap = ItemStackUtil.snapshotOf(old);
        if (stack.isEmpty()) {
            this.clear(); // NONE item will clear the slot
            SlotTransaction trans = new SlotTransaction(this, oldSnap, ItemStackSnapshot.empty());
            return result.transaction(trans).build();
        }
        int remaining = stack.getQuantity();
        final int push = Math.min(remaining, this.slot.getMaxStackSize(this.bridge$getFabric()));
        net.minecraft.item.ItemStack newStack = ItemStackUtil.cloneDefensiveNative(nativeStack, push);
        if (this.slot.setStack(this.bridge$getFabric(), newStack)) {
            result.transaction(new SlotTransaction(this, oldSnap, ItemStackUtil.snapshotOf(newStack)));
            remaining -= push;
        }

        if (remaining > 0) {
            result.reject(ItemStackUtil.cloneDefensive(nativeStack, remaining));
        }

        return result.build();
    }

    @Override
    public void clear() {
        this.slot.setStack(this.bridge$getFabric(), net.minecraft.item.ItemStack.EMPTY);
    }

    @Override
    public int freeCapacity() {
        return !this.slot.getStack(this.bridge$getFabric()).isEmpty()? 1 : 0;
    }

    @Override
    public int totalQuantity() {
        return this.slot.getStack(this.bridge$getFabric()).getCount();
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
    public boolean contains(final ItemStack stack) {
        final net.minecraft.item.ItemStack slotStack = this.slot.getStack(this.bridge$getFabric());
        return slotStack.isEmpty() ? ItemStackUtil.toNative(stack).isEmpty() :
                ItemStackUtil.compareIgnoreQuantity(slotStack, stack) && slotStack.getCount() >= stack.getQuantity();
    }

    @Override
    public boolean containsAny(final ItemStack stack) {
        final net.minecraft.item.ItemStack slotStack = this.slot.getStack(this.bridge$getFabric());
        return slotStack.isEmpty() ? ItemStackUtil.toNative(stack).isEmpty() : ItemStackUtil.compareIgnoreQuantity(slotStack, stack);
    }

    @Override
    public boolean contains(final ItemType type) {
        final net.minecraft.item.ItemStack slotStack = this.slot.getStack(this.bridge$getFabric());
        return slotStack.isEmpty() ? (type == null || type == ItemTypes.AIR) : slotStack.getItem().equals(type);
    }

    @Override
    public Slot viewedSlot() {
        Fabric fabric = this.bridge$getFabric();
        if (fabric instanceof net.minecraft.inventory.container.Slot) {
            return (Slot) fabric;
        }
        if (fabric instanceof Container) {
            return (Slot)((Container) fabric).getSlot(this.ordinal);
        }
        return this;
    }
}
