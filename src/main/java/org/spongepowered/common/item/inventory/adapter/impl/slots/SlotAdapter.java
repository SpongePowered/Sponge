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
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.AbstractInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.impl.fabric.ContainerFabric;
import org.spongepowered.common.item.inventory.lens.impl.fabric.SlotFabric;
import org.spongepowered.common.item.inventory.lens.impl.slots.FakeSlotLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.slots.SlotLensImpl;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.inventory.SlotAccessor;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Base SlotAdapter implementation for {@link net.minecraft.item.ItemStack} based Inventories.
 */
@SuppressWarnings("rawtypes")
public class SlotAdapter extends AbstractInventoryAdapter implements Slot {

    private final SlotLens slot;

    private final int ordinal;

    private SlotAdapter nextSlot;
    private final ImmutableList<Inventory> slots;

    // Internal use for events, will be removed soon!
    public int slotNumber = -1;

    @SuppressWarnings("rawtypes")
    private static SlotLens getLens(final net.minecraft.inventory.Slot slot) {
        if (((SlotAccessor) slot).accessor$getIndex() >= 0) { // Normal Slot?
            if (slot.inventory instanceof InventoryAdapter) { // If the inventory is an adapter we can get the existing SlotLens
                return ((InventoryAdapter) slot.inventory).bridge$getSlotProvider().getSlot(((SlotAccessor) slot).accessor$getIndex());
            }
            // otherwise fallback to a new SlotLens
            return new SlotLensImpl(((SlotAccessor) slot).accessor$getIndex());
        }
        return new FakeSlotLensImpl(slot);
    }

    public SlotAdapter(final Fabric inventory, final SlotLens lens, final Inventory parent) {
        super(inventory, lens, parent);
        this.slot = lens;
        this.ordinal = lens.getOrdinal(inventory);
        this.slots = ImmutableList.of(this);
        this.slotNumber = this.ordinal; // TODO this is used in events
    }

    public int getOrdinal() {
        return this.ordinal;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getStackSize() {
        return this.slot.getStack(this.inventory).getCount();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> Iterable<T> slots() {
        return (Iterable<T>) this.slots;
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    @Override
    public <T extends Inventory> T first() {
        return (T) this;
    }

    @Override
    public Optional<ItemStack> poll() {
        final net.minecraft.item.ItemStack stack = this.inventory.getStack(this.ordinal);
        if (stack.isEmpty()) {
            return Optional.<ItemStack>empty();
        }
        this.inventory.setStack(this.ordinal, net.minecraft.item.ItemStack.EMPTY);
        return Optional.<ItemStack>of(ItemStackUtil.fromNative(stack));
    }

    @Override
    public Optional<ItemStack> peek() {
        final net.minecraft.item.ItemStack stack = this.slot.getStack(this.inventory);
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return ItemStackUtil.cloneDefensiveOptional(stack);
    }

    @Override
    public InventoryTransactionResult offer(final ItemStack stack) {
//        // TODO Correct the transaction based on how offer goes
//        final net.minecraft.item.ItemStack old = this.inventory.getStack(this.ordinal);
//        if (!ItemStackUtil.compare(old, stack)) {
//            return InventoryTransactionResult.failNoTransactions();
//        }
//        boolean canIncrease = getMaxStackSize() != old.stackSize;
//        if (!canIncrease) {
//            return InventoryTransactionResult.failNoTransactions();
//        }
//        int remaining = getMaxStackSize() - old.stackSize;
//        int toBeOffered = stack.getQuantity();
//        if (toBeOffered > remaining) {
//            old.stackSize += toBeOffered - remaining;
//            stack.setQuantity(toBeOffered - remaining);
//        } else {
//            old.stackSize += remaining;
//            // TODO Quantity being set 0 could be a problem...
//            stack.setQuantity(0);
//        }
//        this.inventory.markDirty();
//        return InventoryTransactionResult.successNoTransactions();

        final InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.SUCCESS);
        final net.minecraft.item.ItemStack nativeStack = ItemStackUtil.toNative(stack);

        final int maxStackSize = this.slot.getMaxStackSize(this.inventory);
        int remaining = stack.getQuantity();

        final net.minecraft.item.ItemStack old = this.slot.getStack(this.inventory);
        int push = Math.min(remaining, maxStackSize);
        if (old.isEmpty() && this.slot.setStack(this.inventory, ItemStackUtil.cloneDefensiveNative(nativeStack, push))) {
            remaining -= push;
        } else if (!old.isEmpty() && ItemStackUtil.compareIgnoreQuantity(old, stack)) {
            this.inventory.markDirty();
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

        return result.build();
    }

    @Override
    public boolean canFit(final ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        final net.minecraft.item.ItemStack old = this.slot.getStack(this.inventory);
        if (old.isEmpty()) {
            return this.getMaxStackSize() >= stack.getQuantity();
        }
        return ItemStackUtil.compareIgnoreQuantity(old, stack) && this.getMaxStackSize() - old.getCount() >= stack.getQuantity();
    }

    @Override
    public InventoryTransactionResult set(final ItemStack stack) {
        final InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.SUCCESS);
        final net.minecraft.item.ItemStack nativeStack = ItemStackUtil.toNative(stack);

        final net.minecraft.item.ItemStack old = this.slot.getStack(this.inventory);
        if (stack.getType() == ItemTypes.NONE) {
            clear(); // NONE item will clear the slot
            return result.replace(ItemStackUtil.fromNative(old)).build();
        }
        int remaining = stack.getQuantity();
        final int push = Math.min(remaining, this.slot.getMaxStackSize(this.inventory));
        if (this.slot.setStack(this.inventory, ItemStackUtil.cloneDefensiveNative(nativeStack, push))) {
            result.replace(ItemStackUtil.fromNative(old));
            remaining -= push;
        }

        if (remaining > 0) {
            result.reject(ItemStackUtil.cloneDefensive(nativeStack, remaining));
        }

        return result.build();
    }

    @Override
    public void clear() {
        this.slot.setStack(this.inventory, net.minecraft.item.ItemStack.EMPTY);
    }

    @Override
    public int size() {
        return !this.slot.getStack(this.inventory).isEmpty()? 1 : 0;
    }

    @Override
    public int totalItems() {
        return this.slot.getStack(this.inventory).getCount();
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
        final net.minecraft.item.ItemStack slotStack = this.slot.getStack(this.inventory);
        return slotStack.isEmpty() ? ItemStackUtil.toNative(stack).isEmpty() :
                ItemStackUtil.compareIgnoreQuantity(slotStack, stack) && slotStack.getCount() >= stack.getQuantity();
    }

    @Override
    public boolean containsAny(final ItemStack stack) {
        final net.minecraft.item.ItemStack slotStack = this.slot.getStack(this.inventory);
        return slotStack.isEmpty() ? ItemStackUtil.toNative(stack).isEmpty() : ItemStackUtil.compareIgnoreQuantity(slotStack, stack);
    }

    @Override
    public boolean contains(final ItemType type) {
        final net.minecraft.item.ItemStack slotStack = this.slot.getStack(this.inventory);
        return slotStack.isEmpty() ? (type == null || type == ItemTypes.AIR) : slotStack.getItem().equals(type);
    }

    @Override
    public Slot transform(final Slot.Type type) {
        switch (type) {
            case INVENTORY:
                if (this.inventory instanceof SlotFabric) {
                    return ((Slot) ((SlotFabric) this.inventory).getDelegate());
                }
                if (this.inventory instanceof ContainerFabric) {
                    return ((Slot) ((ContainerFabric) this.inventory).getContainer().getSlot(this.slotNumber));
                }
                return this;
            default:
                return this;
        }
    }

    @Override
    public Slot transform() {
        return this.transform(Slot.Type.INVENTORY);
    }

    @Override
    public Iterator<Inventory> iterator() {
        return new Iterator<Inventory>() {
            private boolean iterated = false;
            @Override
            public boolean hasNext() {
                return !this.iterated;
            }

            @Override
            public Inventory next() {
                if (this.iterated) throw new NoSuchElementException("Iterator is consumed");
                this.iterated = true;
                return SlotAdapter.this;
            }
        };
    }
}
