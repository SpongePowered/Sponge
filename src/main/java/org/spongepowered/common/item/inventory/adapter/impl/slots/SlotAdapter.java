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
import net.minecraft.inventory.Container;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.common.item.inventory.adapter.impl.AbstractInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

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
        return this.slot.getStack(this.bridge$getFabric()).func_190916_E();
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
        final net.minecraft.item.ItemStack stack = this.bridge$getFabric().fabric$getStack(this.ordinal);
        if (stack.func_190926_b()) {
            return Optional.<ItemStack>empty();
        }
        this.bridge$getFabric().fabric$setStack(this.ordinal, net.minecraft.item.ItemStack.field_190927_a);
        return Optional.<ItemStack>of(ItemStackUtil.fromNative(stack));
    }

    @Override
    public Optional<ItemStack> peek() {
        final net.minecraft.item.ItemStack stack = this.slot.getStack(this.bridge$getFabric());
        if (stack.func_190926_b()) {
            return Optional.empty();
        }
        return ItemStackUtil.cloneDefensiveOptional(stack);
    }

    @Override
    public InventoryTransactionResult offer(final ItemStack stack) {
        final InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.SUCCESS);
        final net.minecraft.item.ItemStack nativeStack = ItemStackUtil.toNative(stack);

        final int maxStackSize = this.slot.getMaxStackSize(this.bridge$getFabric());
        int remaining = stack.getQuantity();

        final net.minecraft.item.ItemStack old = this.slot.getStack(this.bridge$getFabric());
        int push = Math.min(remaining, maxStackSize);
        if (old.func_190926_b() && this.slot.setStack(this.bridge$getFabric(), ItemStackUtil.cloneDefensiveNative(nativeStack, push))) {
            remaining -= push;
        } else if (!old.func_190926_b() && ItemStackUtil.compareIgnoreQuantity(old, stack)) {
            this.bridge$getFabric().fabric$markDirty();
            push = Math.max(Math.min(maxStackSize - old.func_190916_E(), remaining), 0); // max() accounts for oversized stacks
            old.func_190920_e(old.func_190916_E() + push);
            remaining -= push;
        }
        // TODO transaction failure

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
        final net.minecraft.item.ItemStack old = this.slot.getStack(this.bridge$getFabric());
        if (old.func_190926_b()) {
            return this.getMaxStackSize() >= stack.getQuantity();
        }
        return ItemStackUtil.compareIgnoreQuantity(old, stack) && this.getMaxStackSize() - old.func_190916_E() >= stack.getQuantity();
    }

    @Override
    public InventoryTransactionResult set(final ItemStack stack) {
        final InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.SUCCESS);
        final net.minecraft.item.ItemStack nativeStack = ItemStackUtil.toNative(stack);

        final net.minecraft.item.ItemStack old = this.slot.getStack(this.bridge$getFabric());
        if (stack.getType() == ItemTypes.NONE) {
            clear(); // NONE item will clear the slot
            return result.replace(ItemStackUtil.fromNative(old)).build();
        }
        int remaining = stack.getQuantity();
        final int push = Math.min(remaining, this.slot.getMaxStackSize(this.bridge$getFabric()));
        if (this.slot.setStack(this.bridge$getFabric(), ItemStackUtil.cloneDefensiveNative(nativeStack, push))) {
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
        this.slot.setStack(this.bridge$getFabric(), net.minecraft.item.ItemStack.field_190927_a);
    }

    @Override
    public int size() {
        return !this.slot.getStack(this.bridge$getFabric()).func_190926_b()? 1 : 0;
    }

    @Override
    public int totalItems() {
        return this.slot.getStack(this.bridge$getFabric()).func_190916_E();
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
        return slotStack.func_190926_b() ? ItemStackUtil.toNative(stack).func_190926_b() :
                ItemStackUtil.compareIgnoreQuantity(slotStack, stack) && slotStack.func_190916_E() >= stack.getQuantity();
    }

    @Override
    public boolean containsAny(final ItemStack stack) {
        final net.minecraft.item.ItemStack slotStack = this.slot.getStack(this.bridge$getFabric());
        return slotStack.func_190926_b() ? ItemStackUtil.toNative(stack).func_190926_b() : ItemStackUtil.compareIgnoreQuantity(slotStack, stack);
    }

    @Override
    public boolean contains(final ItemType type) {
        final net.minecraft.item.ItemStack slotStack = this.slot.getStack(this.bridge$getFabric());
        return slotStack.func_190926_b() ? (type == null || type == ItemTypes.AIR) : slotStack.func_77973_b().equals(type);
    }

    @Override
    public Slot transform(final Slot.Type type) {
        switch (type) {
            case INVENTORY:
                if (this.bridge$getFabric() instanceof net.minecraft.inventory.Slot) {
                    return (Slot) this.bridge$getFabric();
                }
                if (this.bridge$getFabric() instanceof Container) {
                    return (Slot) ((Container) this.bridge$getFabric()).func_75139_a(this.slotNumber);
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
