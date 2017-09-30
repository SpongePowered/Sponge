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
import net.minecraft.inventory.IInventory;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.common.interfaces.inventory.IMixinSlot;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.Adapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftFabric;
import org.spongepowered.common.item.inventory.lens.impl.fabric.ContainerFabric;
import org.spongepowered.common.item.inventory.lens.impl.fabric.DelegatingFabric;
import org.spongepowered.common.item.inventory.lens.impl.slots.FakeSlotLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.slots.SlotLensImpl;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.Optional;

public class SlotAdapter extends Adapter implements Slot {

    private final SlotLens<IInventory, net.minecraft.item.ItemStack> slot;

    private final int ordinal;

    private SlotAdapter nextSlot;
    private final ImmutableList<Inventory> slots;

    // Internal use for events, will be removed soon!
    public int slotNumber = -1;

    public SlotAdapter(net.minecraft.inventory.Slot slot) {
        this(MinecraftFabric.of(slot), getLens(slot), slot.inventory instanceof Inventory ? (Inventory) slot.inventory : null);
        this.slotNumber = slot.slotNumber;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static SlotLens<IInventory, net.minecraft.item.ItemStack> getLens(net.minecraft.inventory.Slot slot) {
        if (((IMixinSlot) slot).getSlotIndex() >= 0) { // Normal Slot?
            if (slot.inventory instanceof InventoryAdapter) { // If the inventory is an adapter we can get the existing SlotLens
                return ((InventoryAdapter) slot.inventory).getSlotProvider().getSlot(((IMixinSlot) slot).getSlotIndex());
            }
            // otherwise fallback to a new SlotLens
            return new SlotLensImpl(((IMixinSlot) slot).getSlotIndex());
        }
        return new FakeSlotLensImpl(slot);
    }

    public SlotAdapter(Fabric<IInventory> inventory, SlotLens<IInventory, net.minecraft.item.ItemStack> lens, Inventory parent) {
        super(inventory, lens, parent);
        this.slot = lens;
        this.ordinal = lens.getOrdinal(inventory);
        this.slots = ImmutableList.of(this);
        this.slotNumber = this.ordinal; // TODO this is used in events
    }

    public int getOrdinal() {
        return this.ordinal;
    }

    @Override
    public int getStackSize() {
        return this.slot.getStack(this.inventory).getCount();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> Iterable<T> slots() {
        return (Iterable<T>) this.slots;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> T first() {
        return (T) this;
    }

    @Override
    public Optional<ItemStack> poll() {
        net.minecraft.item.ItemStack stack = this.inventory.getStack(this.ordinal);
        if (stack.isEmpty()) {
            return Optional.<ItemStack>empty();
        }
        this.inventory.setStack(this.ordinal, net.minecraft.item.ItemStack.EMPTY);
        return Optional.<ItemStack>of(ItemStackUtil.fromNative(stack));
    }

    @Override
    public Optional<ItemStack> poll(int limit) {
        return super.poll(limit);
    }

    @Override
    public Optional<ItemStack> peek() {
        net.minecraft.item.ItemStack stack = this.slot.getStack(this.inventory);
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return ItemStackUtil.cloneDefensiveOptional(stack);
    }

    @Override
    public Optional<ItemStack> peek(int limit) {
        return super.peek(limit);
    }

    @Override
    public InventoryTransactionResult offer(ItemStack stack) {
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

        InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.SUCCESS);
        net.minecraft.item.ItemStack nativeStack = ItemStackUtil.toNative(stack);

        int maxStackSize = this.slot.getMaxStackSize(this.inventory);
        int remaining = stack.getQuantity();

        net.minecraft.item.ItemStack old = this.slot.getStack(this.inventory);
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
    public InventoryTransactionResult set(ItemStack stack) {
        InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.SUCCESS);
        net.minecraft.item.ItemStack nativeStack = ItemStackUtil.toNative(stack);

        net.minecraft.item.ItemStack old = this.slot.getStack(this.inventory);
        if (stack.getType() == ItemTypes.NONE) {
            clear(); // NONE item will clear the slot
            return result.replace(ItemStackUtil.fromNative(old)).build();
        }
        int remaining = stack.getQuantity();
        int push = Math.min(remaining, this.slot.getMaxStackSize(this.inventory));
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
    public boolean contains(ItemStack stack) {
        net.minecraft.item.ItemStack slotStack = this.slot.getStack(this.inventory);
        return slotStack.isEmpty() ? ItemStackUtil.toNative(stack).isEmpty() :
                ItemStackUtil.compareIgnoreQuantity(slotStack, stack) && slotStack.getCount() >= stack.getQuantity();
    }

    @Override
    public boolean containsAny(ItemStack stack) {
        net.minecraft.item.ItemStack slotStack = this.slot.getStack(this.inventory);
        return slotStack.isEmpty() ? ItemStackUtil.toNative(stack).isEmpty() : ItemStackUtil.compareIgnoreQuantity(slotStack, stack);
    }

    @Override
    public boolean contains(ItemType type) {
        net.minecraft.item.ItemStack slotStack = this.slot.getStack(this.inventory);
        return slotStack.isEmpty() ? (type == null || type == ItemTypes.AIR) : slotStack.getItem().equals(type);
    }

    @Override
    public int getMaxStackSize() {
        return super.getMaxStackSize();
    }

    @Override
    public Slot transform(Type type) {
        switch (type) {
            case INVENTORY:
                if (this.inventory instanceof DelegatingFabric) {
                    return ((Slot) ((DelegatingFabric) this.inventory).getDelegate());
                }
                if (this.inventory instanceof ContainerFabric) {
                    return ((Slot) ((ContainerFabric) this.inventory).getContainer().getSlot(this.slotNumber));
                }
            default:
                return this;
        }
    }

    @Override
    public Slot transform() {
        return this.transform(Type.INVENTORY);
    }

    //    @Override
//    public Iterator<Inventory> iterator() {
//        // TODO
//        return super.iterator();
//    }
}
