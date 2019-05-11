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
package org.spongepowered.common.item.inventory.adapter.impl;

import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult.Type;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.Optional;

import javax.annotation.Nullable;

public abstract class AdapterLogic {

    private AdapterLogic() {
    }

    public static InventoryTransactionResult pollSequential(Fabric inv, @Nullable Lens lens, @Nullable Integer limit) {
        if (lens == null || lens.getSlots().size() <= 0) {
            return InventoryTransactionResult.builder().type(Type.NO_SLOT).build();
        }

        InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(Type.SUCCESS);

        ItemStack removedType = null; // used when polling from multiple slots

        for (SlotLens slot : lens.getSlots()) {
            net.minecraft.item.ItemStack stack = slot.getStack(inv);

            // Only remove one type of item
            if (stack.isEmpty() || (removedType != null && !ItemStackUtil.compareIgnoreQuantity(removedType, stack))) {
                continue;
            }

            // Poll up to limit items OR entire stack when no limit is set
            int pollCount = limit != null ? Math.min(stack.getCount(), limit) : stack.getCount();

            net.minecraft.item.ItemStack newStack = net.minecraft.item.ItemStack.EMPTY;
            if (pollCount < stack.getCount()) { // is stack not removed completely?
                newStack = stack.copy();
                newStack.setCount(newStack.getCount() - pollCount);
            }

            if (slot.setStack(inv, newStack)) { // Set new stack
                SlotAdapter slotAdapter = (SlotAdapter) slot.getAdapter(inv, null); // TODO parent??
                result.transaction(new SlotTransaction(slotAdapter, ItemStackUtil.snapshotOf(stack), ItemStackUtil.snapshotOf(newStack)));
                if (removedType == null) {
                    removedType = ItemStackUtil.cloneDefensive(stack, 1); // set removed type when first removing
                }
                if (limit == null) {
                    break; // no limit only polls the first non-empty slot
                }
                limit -= pollCount; // remove amount polled from slot
            }
            // else setting stack failed - do nothing

            if (limit != null && limit <= 0) { // polled all items requested
                break;
            }
        }

        if (removedType != null) { // mark dirty if items were removed
            inv.markDirty();
        }

        if (limit != null && limit > 0) { // not all items requested could be polled
            result.type(Type.FAILURE);
        }

        return result.build();
    }

    public static Optional<ItemStack> peekSequential(Fabric inv, @Nullable Lens lens) {
        return AdapterLogic.findStack(inv, lens);
    }

    private static Optional<ItemStack> findStack(Fabric inv, @Nullable Lens lens) {
        if (lens == null || lens.getSlots().size() <= 0) {
            return Optional.empty();
        }

        for (SlotLens slot : lens.getSlots()) {
            net.minecraft.item.ItemStack stack = slot.getStack(inv);
            if (!stack.isEmpty()) {
                return ItemStackUtil.cloneDefensiveOptional(stack);
            }
        }

        return Optional.of(ItemStack.empty());
    }

    public static InventoryTransactionResult insertSequential(Fabric inv, @Nullable Lens lens, ItemStack stack) {
        if (lens == null) {
            return InventoryTransactionResult.builder().type(Type.FAILURE).reject(ItemStackUtil.cloneDefensive(stack)).build();
        }
        try {
            return AdapterLogic.insertStack(inv, lens, stack);
        } catch (Exception ex) {
            return InventoryTransactionResult.builder().type(Type.ERROR).reject(ItemStackUtil.cloneDefensive(stack)).build();
        }
    }

    private static InventoryTransactionResult insertStack(Fabric inv, Lens lens, ItemStack stack) {
        InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(Type.SUCCESS);
        net.minecraft.item.ItemStack nativeStack = ItemStackUtil.toNative(stack);

        int maxStackSize = Math.min(lens.getMaxStackSize(inv), nativeStack.getMaxStackSize());
        int remaining = stack.getQuantity();

        for (int ord = 0; ord < lens.slotCount() && remaining > 0; ord++) {
            net.minecraft.item.ItemStack old = lens.getStack(inv, ord);
            ItemStackSnapshot oldSnap = ItemStackUtil.snapshotOf(old);
            int push = Math.min(remaining, maxStackSize);
            net.minecraft.item.ItemStack newStack = ItemStackUtil.cloneDefensiveNative(nativeStack, push);
            if (lens.setStack(inv, ord, newStack)) {
                InventoryAdapter adapter = lens.getAdapter(inv, null);
                SlotTransaction trans = new SlotTransaction((Slot) adapter, ItemStackUtil.snapshotOf(old), ItemStackUtil.snapshotOf(newStack));
                result.transaction(trans);
                remaining -= push;

                Slot slot = ((SlotAdapter) lens.getSlot(ord).getAdapter(inv, null));
                result.transaction(new SlotTransaction(slot, oldSnap, ItemStackUtil.snapshotOf(lens.getStack(inv, ord))));
            }
        }

        if (remaining > 0) {
            result.reject(ItemStackUtil.cloneDefensive(nativeStack, remaining));
        }

        inv.markDirty();

        return result.build();
    }

    public static InventoryTransactionResult appendSequential(Fabric inv, @Nullable Lens lens, ItemStack stack) {
        if (lens == null) {
            return InventoryTransactionResult.builder().type(Type.FAILURE).reject(ItemStackUtil.cloneDefensive(stack)).build();
        }
        InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(Type.SUCCESS);
        net.minecraft.item.ItemStack nativeStack = ItemStackUtil.toNative(stack);

        int maxStackSize = Math.min(lens.getMaxStackSize(inv), nativeStack.getMaxStackSize());
        int remaining = stack.getQuantity();

        for (int ord = 0; ord < lens.slotCount() && remaining > 0; ord++) {
            net.minecraft.item.ItemStack old = lens.getStack(inv, ord);

            int push = Math.min(remaining, maxStackSize);
            if (old.isEmpty() && lens.setStack(inv, ord, ItemStackUtil.cloneDefensiveNative(nativeStack, push))) {
                remaining -= push;
                Slot slot = ((SlotAdapter) lens.getSlot(ord).getAdapter(inv, null));
                result.transaction(new SlotTransaction(slot, ItemStackUtil.snapshotOf(old), ItemStackUtil.snapshotOf(lens.getStack(inv, ord))));
            } else if (!old.isEmpty() && ItemStackUtil.compareIgnoreQuantity(old, stack)) {
                ItemStackSnapshot oldSnap = ItemStackUtil.snapshotOf(old);
                push = Math.max(Math.min(maxStackSize - old.getCount(), remaining), 0); // max() accounts for oversized stacks
                old.setCount(old.getCount() + push);
                remaining -= push;
                Slot slot = ((SlotAdapter) lens.getSlot(ord).getAdapter(inv, null));
                result.transaction(new SlotTransaction(slot, oldSnap, ItemStackUtil.snapshotOf(lens.getStack(inv, ord))));
            }

        }

        if (remaining == stack.getQuantity()) {
            // No items were consumed
            result.type(Type.FAILURE).reject(ItemStackUtil.cloneDefensive(nativeStack));
        } else {
            stack.setQuantity(remaining);
            inv.markDirty();
        }

        return result.build();
    }

    public static int countFreeCapacity(Fabric inv, Lens lens) {
        return lens.getSlots().stream().mapToInt(slot -> slot.getStack(inv).isEmpty() ? 1 : 0).sum();
    }

    public static int countQuantity(Fabric inv, Lens lens) {
        int items = 0;

        for (int ord = 0; ord < lens.slotCount(); ord++) {
            net.minecraft.item.ItemStack stack = lens.getStack(inv, ord);
            items += !stack.isEmpty() ? stack.getCount() : 0;
        }

        return items;
    }

    public static int getCapacity(Fabric inv, Lens lens) {
        return lens.slotCount();
    }

    public static boolean contains(InventoryAdapter adapter, ItemStack stack) {
        return AdapterLogic.contains(adapter.getFabric(), adapter.getRootLens(), stack, stack.getQuantity());
    }

    public static boolean contains(InventoryAdapter adapter, ItemStack stack, int quantity) {
        return AdapterLogic.contains(adapter.getFabric(), adapter.getRootLens(), stack, quantity);
    }

    /**
     * Searches for at least <code>quantity</code> of given stack.
     *
     * @param inv The inventory to search in
     * @param lens The lens to search with
     * @param stack The stack to search with
     * @param quantity The quantity to find
     * @return true if at least <code>quantity</code> of given stack has been found in given inventory
     */
    public static boolean contains(Fabric inv, Lens lens, ItemStack stack, int quantity) {
        net.minecraft.item.ItemStack nonNullStack = ItemStackUtil.toNative(stack); // Handle null as empty
        int found = 0;
        for (int ord = 0; ord < lens.slotCount(); ord++) {
            net.minecraft.item.ItemStack slotStack = lens.getStack(inv, ord);
            if (slotStack.isEmpty()) {
                if (nonNullStack.isEmpty()) {
                    found++; // Found an empty Slot
                    if (found >= quantity) {
                        return true;
                    }
                }
            } else {
                if (ItemStackUtil.compareIgnoreQuantity(slotStack, stack)) {
                    found += slotStack.getCount(); // Found a matching stack
                    if (found >= quantity) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean contains(InventoryAdapter adapter, ItemType type) {
        return AdapterLogic.contains(adapter.getFabric(), adapter.getRootLens(), type);
    }

    public static boolean contains(Fabric inv, Lens lens, ItemType type) {
        for (int ord = 0; ord < lens.slotCount(); ord++) {
            net.minecraft.item.ItemStack slotStack = lens.getStack(inv, ord);
            if (slotStack.isEmpty()) {
                if (type == null || type == ItemTypes.AIR) {
                    return true; // Found an empty Slot
                }
            } else {
                if (slotStack.getItem() == type) {
                    return true; // Found a matching stack
                }
            }
        }
        return false;
    }

    public static boolean canFit(InventoryAdapter adapter, ItemStack stack) {

        Fabric inv = adapter.getFabric();
        Lens lens = adapter.getRootLens();

        net.minecraft.item.ItemStack nativeStack = ItemStackUtil.toNative(stack);

        int maxStackSize = Math.min(lens.getMaxStackSize(inv), nativeStack.getMaxStackSize());
        int remaining = stack.getQuantity();

        for (int ord = 0; ord < lens.slotCount() && remaining > 0; ord++) {
            net.minecraft.item.ItemStack old = lens.getStack(inv, ord);
            int push = Math.min(remaining, maxStackSize);
            if (old.isEmpty()) {
                remaining -= push;
            } else if (ItemStackUtil.compareIgnoreQuantity(old, stack)) {
                push = Math.max(Math.min(maxStackSize - old.getCount(), remaining), 0); // max() accounts for oversized stacks
                remaining -= push;
            }
        }

        return remaining == 0;
    }
}
