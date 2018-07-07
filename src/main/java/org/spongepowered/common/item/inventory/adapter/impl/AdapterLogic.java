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

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.Optional;

public abstract class AdapterLogic {

    private AdapterLogic() {
    }

    public static Optional<ItemStack> pollSequential(InventoryAdapter adapter) {
        return AdapterLogic.pollSequential(adapter.getFabric(), adapter.getRootLens());
    }

    public static Optional<ItemStack> pollSequential(Fabric inv, Lens lens) {
        return AdapterLogic.findStack(inv, lens, true);
    }

    public static Optional<ItemStack> pollSequential(InventoryAdapter adapter, int limit) {
        return AdapterLogic.pollSequential(adapter.getFabric(), adapter.getRootLens(), limit);
    }

    public static Optional<ItemStack> pollSequential(Fabric inv, Lens lens, int limit) {
        return AdapterLogic.findStacks(inv, lens, limit, true);
    }

    public static Optional<ItemStack> peekSequential(InventoryAdapter adapter) {
        return AdapterLogic.peekSequential(adapter.getFabric(), adapter.getRootLens());
    }

    public static Optional<ItemStack> peekSequential(Fabric inv, Lens lens) {
        return AdapterLogic.findStack(inv, lens, false);
    }

    public static Optional<ItemStack> peekSequential(InventoryAdapter adapter, int limit) {
        return AdapterLogic.peekSequential(adapter.getFabric(), adapter.getRootLens(), limit);
    }

    public static Optional<ItemStack> peekSequential(Fabric inv, Lens lens, int limit) {
        return AdapterLogic.findStacks(inv, lens, limit, false);
    }

    private static Optional<ItemStack> findStack(Fabric inv, Lens lens, boolean remove) {
        if (lens == null) {
            return Optional.empty();
        }
        for (int ord = 0; ord < lens.slotCount(); ord++) {
            net.minecraft.item.ItemStack stack = lens.getStack(inv, ord);
            if (stack.isEmpty() || (remove && !lens.setStack(inv, ord, net.minecraft.item.ItemStack.EMPTY))) {
                continue;
            }
            return ItemStackUtil.cloneDefensiveOptional(stack);
        }

        if (lens.slotCount() > 0) {
            return Optional.of(ItemStack.empty());
        }

        return Optional.empty();
    }

    private static Optional<ItemStack> findStacks(Fabric inv, Lens lens, int limit, boolean remove) {

        if (lens == null) {
            return Optional.empty();
        }

        ItemStack result = null;

        for (int ord = 0; ord < lens.slotCount(); ord++) {
            net.minecraft.item.ItemStack stack = lens.getStack(inv, ord);
            if (stack.isEmpty() || stack.getCount() < 1 || (result != null && !result.getType().equals(stack.getItem()))) {
                continue;
            }

            if (result == null) {
                result = ItemStackUtil.cloneDefensive(stack, 0);
            }

            int pull = Math.min(stack.getCount(), limit);
            result.setQuantity(result.getQuantity() + pull);
            limit -= pull;

            if (!remove) {
                continue;
            }

            if (pull >= stack.getCount()) {
                lens.setStack(inv, ord, net.minecraft.item.ItemStack.EMPTY);
            } else {
                stack.setCount(stack.getCount() - pull);
            }
        }

        if (result == null && lens.slotCount() > 0) {
            return Optional.of(ItemStack.empty());
        }

        return Optional.ofNullable(result);
    }

    public static InventoryTransactionResult insertSequential(InventoryAdapter adapter, ItemStack stack) {
        return AdapterLogic.insertSequential(adapter.getFabric(), adapter.getRootLens(), stack);
    }

    public static InventoryTransactionResult insertSequential(Fabric inv, Lens lens, ItemStack stack) {
        if (lens == null) {
            return InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.FAILURE).reject(ItemStackUtil.cloneDefensive(stack))
                    .build();
        }
        try {
            return AdapterLogic.insertStack(inv, lens, stack);
        } catch (Exception ex) {
            return InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.ERROR).reject(ItemStackUtil.cloneDefensive(stack))
                    .build();
        }
    }

    private static InventoryTransactionResult insertStack(Fabric inv, Lens lens, ItemStack stack) {
        InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.SUCCESS);
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

        return result.build();
    }

    public static InventoryTransactionResult appendSequential(InventoryAdapter adapter, ItemStack stack) {
        return AdapterLogic.appendSequential(adapter.getFabric(), adapter.getRootLens(), stack);
    }

    public static InventoryTransactionResult appendSequential(Fabric inv, Lens lens, ItemStack stack) {
        InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.SUCCESS);
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
            result.type(InventoryTransactionResult.Type.FAILURE).reject(ItemStackUtil.cloneDefensive(nativeStack));
        } else {
            stack.setQuantity(remaining);
        }

        return result.build();
    }

    public static int countStacks(InventoryAdapter adapter) {
        return AdapterLogic.countStacks(adapter.getFabric(), adapter.getRootLens());
    }

    public static int countStacks(Fabric inv, Lens lens) {
        int stacks = 0;

        for (int ord = 0; ord < lens.slotCount(); ord++) {
            stacks += !lens.getStack(inv, ord).isEmpty() ? 1 : 0;
        }

        return stacks;
    }

    public static int countItems(InventoryAdapter adapter) {
        return AdapterLogic.countItems(adapter.getFabric(), adapter.getRootLens());
    }

    public static int countItems(Fabric inv, Lens lens) {
        int items = 0;

        for (int ord = 0; ord < lens.slotCount(); ord++) {
            net.minecraft.item.ItemStack stack = lens.getStack(inv, ord);
            items += !stack.isEmpty() ? stack.getCount() : 0;
        }

        return items;
    }

    public static int getCapacity(InventoryAdapter adapter) {
        return AdapterLogic.getCapacity(adapter.getFabric(), adapter.getRootLens());
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
                if (type == null || type == ItemTypes.NONE) {
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
}
