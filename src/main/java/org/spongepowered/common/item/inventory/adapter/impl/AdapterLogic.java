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

import com.google.common.collect.Streams;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.custom.CustomInventory;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AdapterLogic{

    private AdapterLogic() {}

    public static Optional<ItemStack> pollSequential(final InventoryAdapter adapter) {
        return AdapterLogic.pollSequential(adapter.bridge$getFabric(), adapter.bridge$getRootLens());
    }

    public static Optional<ItemStack> pollSequential(final Fabric inv, final Lens lens) {
        return AdapterLogic.findStack(inv, lens, true);
    }

    public static Optional<ItemStack> pollSequential(final InventoryAdapter adapter, final int limit) {
        return AdapterLogic.pollSequential(adapter.bridge$getFabric(), adapter.bridge$getRootLens(), limit);
    }

    public static Optional<ItemStack> pollSequential(final Fabric inv, final Lens lens, final int limit) {
        return AdapterLogic.findStacks(inv, lens, limit, true);
    }

    public static Optional<ItemStack> peekSequential(final InventoryAdapter adapter) {
        return AdapterLogic.peekSequential(adapter.bridge$getFabric(), adapter.bridge$getRootLens());
    }

    public static Optional<ItemStack> peekSequential(final Fabric inv, final Lens lens) {
        return AdapterLogic.findStack(inv, lens, false);
    }

    public static Optional<ItemStack> peekSequential(final InventoryAdapter adapter, final int limit) {
        return AdapterLogic.peekSequential(adapter.bridge$getFabric(), adapter.bridge$getRootLens(), limit);
    }

    public static Optional<ItemStack> peekSequential(final Fabric inv, final Lens lens, final int limit) {
        return AdapterLogic.findStacks(inv, lens, limit, false);
    }

    private static Optional<ItemStack> findStack(final Fabric inv, final Lens lens, final boolean remove) {
        if (lens == null) {
            return Optional.empty();
        }
        for (int ord = 0; ord < lens.slotCount(); ord++) {
            final net.minecraft.item.ItemStack stack = lens.getStack(inv, ord);
            if (stack.func_190926_b() || (remove && !lens.setStack(inv, ord, net.minecraft.item.ItemStack.field_190927_a))) {
                continue;
            }
            return ItemStackUtil.cloneDefensiveOptional(stack);
        }

        return Optional.empty();
    }

    private static Optional<ItemStack> findStacks(final Fabric inv, final Lens lens, int limit, final boolean remove) {

        if (lens == null) {
            return Optional.empty();
        }

        ItemStack result = null;

        for (int ord = 0; ord < lens.slotCount(); ord++) {
            final net.minecraft.item.ItemStack stack = lens.getStack(inv, ord);
            if (stack.func_190926_b() || stack.func_190916_E() < 1 || (result != null && !result.getType().equals(stack.func_77973_b()))) {
                continue;
            }

            if (result == null) {
                result = ItemStackUtil.cloneDefensive(stack, 0);
            }

            final int pull = Math.min(stack.func_190916_E(), limit);
            result.setQuantity(result.getQuantity() + pull);
            limit -= pull;

            if (!remove) {
                continue;
            }

            if (pull >= stack.func_190916_E()) {
                lens.setStack(inv, ord, net.minecraft.item.ItemStack.field_190927_a);
            } else {
                stack.func_190920_e(stack.func_190916_E() - pull);
            }
        }

        if (remove && result != null && !result.isEmpty()) {
            inv.fabric$markDirty();
        }

        return Optional.ofNullable(result);
    }

    public static InventoryTransactionResult insertSequential(final InventoryAdapter adapter, final ItemStack stack) {
        return AdapterLogic.insertSequential(adapter.bridge$getFabric(), adapter.bridge$getRootLens(), stack);
    }

    public static InventoryTransactionResult insertSequential(final Fabric inv, final Lens lens, final ItemStack stack) {
        if (lens == null) {
            return InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.FAILURE).reject(ItemStackUtil.cloneDefensive(stack)).build();
        }
        try {
            return AdapterLogic.insertStack(inv, lens, stack);
        } catch (Exception ex) {
           return InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.ERROR).reject(ItemStackUtil.cloneDefensive(stack)).build();
        }
    }

    private static InventoryTransactionResult insertStack(final Fabric inv, final Lens lens, final ItemStack stack) {
        final InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.SUCCESS);
        final net.minecraft.item.ItemStack nativeStack = ItemStackUtil.toNative(stack);

        final int maxStackSize = Math.min(lens.getMaxStackSize(inv), nativeStack.func_77976_d());
        int remaining = stack.getQuantity();

        for (int ord = 0; ord < lens.slotCount() && remaining > 0; ord++) {
            final net.minecraft.item.ItemStack old = lens.getStack(inv, ord);
            final int push = Math.min(remaining, maxStackSize);
            if (lens.setStack(inv, ord, ItemStackUtil.cloneDefensiveNative(nativeStack, push))) {
                result.replace(ItemStackUtil.fromNative(old));
                remaining -= push;
            }
        }

        if (remaining > 0) {
            result.reject(ItemStackUtil.cloneDefensive(nativeStack, remaining));
        }

        inv.fabric$markDirty();

        return result.build();
    }

    public static InventoryTransactionResult appendSequential(final InventoryAdapter adapter, final ItemStack stack) {
        return AdapterLogic.appendSequential(adapter.bridge$getFabric(), adapter.bridge$getRootLens(), stack);
    }

    public static InventoryTransactionResult appendSequential(final Fabric inv, final Lens lens, final ItemStack stack) {
        final InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.SUCCESS);
        final net.minecraft.item.ItemStack nativeStack = ItemStackUtil.toNative(stack);

        final int maxStackSize = Math.min(lens.getMaxStackSize(inv), nativeStack.func_77976_d());
        int remaining = stack.getQuantity();

        for (int ord = 0; ord < lens.slotCount() && remaining > 0; ord++) {
            final net.minecraft.item.ItemStack old = lens.getStack(inv, ord);
            int push = Math.min(remaining, maxStackSize);
            if (old.func_190926_b() && lens.setStack(inv, ord, ItemStackUtil.cloneDefensiveNative(nativeStack, push))) {
                remaining -= push;
            } else if (!old.func_190926_b() && ItemStackUtil.compareIgnoreQuantity(old, stack)) {
                push = Math.max(Math.min(maxStackSize - old.func_190916_E(), remaining), 0); // max() accounts for oversized stacks
                old.func_190920_e(old.func_190916_E() + push);
                remaining -= push;
            }
        }

        if (remaining == stack.getQuantity()) {
            // No items were consumed
            result.type(InventoryTransactionResult.Type.FAILURE).reject(ItemStackUtil.cloneDefensive(nativeStack));
        } else {
            stack.setQuantity(remaining);
            inv.fabric$markDirty();
        }

        return result.build();
    }

    public static int countStacks(final InventoryAdapter adapter) {
        return AdapterLogic.countStacks(adapter.bridge$getFabric(), adapter.bridge$getRootLens());
    }

    public static int countStacks(final Fabric inv, final Lens lens) {
        int stacks = 0;

        for (int ord = 0; ord < lens.slotCount(); ord++) {
            stacks += !lens.getStack(inv, ord).func_190926_b() ? 1 : 0;
        }

        return stacks;
    }

    public static int countItems(final InventoryAdapter adapter) {
        return AdapterLogic.countItems(adapter.bridge$getFabric(), adapter.bridge$getRootLens());
    }

    public static int countItems(final Fabric inv, final Lens lens) {
        int items = 0;

        for (int ord = 0; ord < lens.slotCount(); ord++) {
            final net.minecraft.item.ItemStack stack = lens.getStack(inv, ord);
            items += stack.func_190916_E();
        }

        return items;
    }

    public static int getCapacity(final InventoryAdapter adapter) {
        return AdapterLogic.getCapacity(adapter.bridge$getFabric(), adapter.bridge$getRootLens());
    }

    public static int getCapacity(final Fabric inv, final Lens lens) {
        return lens.getSlots().size();
    }

    public static Collection<InventoryProperty<?, ?>> getProperties(final InventoryAdapter adapter,
            final Inventory child, final Class<? extends InventoryProperty<?, ?>> property) {
        return AdapterLogic.getProperties(adapter.bridge$getFabric(), adapter.bridge$getRootLens(), child, property);
    }

    public static Collection<InventoryProperty<?, ?>> getProperties(final Fabric inv, final Lens lens,
            final Inventory child, final Class<? extends InventoryProperty<?, ?>> property) {

        if (child instanceof InventoryAdapter) {
            checkNotNull(property, "property");
            final int index = lens.getChildren().indexOf(((InventoryAdapter) child).bridge$getRootLens());
            if (index > -1) {
                return lens.getProperties(index).stream().filter(prop -> property.equals(prop.getClass()))
                        .collect(Collectors.toCollection(ArrayList::new));
            }
        }

        return Collections.emptyList();
    }

    public static <T extends InventoryProperty<?, ?>> Collection<T> getRootProperties(InventoryAdapter adapter, final Class<T> property) {
        adapter = inventoryRoot(adapter);
        if (adapter instanceof CustomInventory) {
            return ((CustomInventory) adapter).getProperties().values().stream().filter(p -> property.equals(p.getClass()))
                    .map(property::cast).collect(Collectors.toList());
        }
        return Streams.stream(findRootProperty(adapter, property)).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public static <T extends InventoryProperty<?, ?>> Optional<T> getRootProperty(InventoryAdapter adapter, final Class<T> property, final Object key) {
        adapter = inventoryRoot(adapter);
        if (adapter instanceof CustomInventory) {
            final InventoryProperty<?, ?> forKey = ((CustomInventory) adapter).getProperties().get(key);
            if (forKey != null && property.equals(forKey.getClass())) {
                return Optional.of((T) forKey);
            }
        }
        return findRootProperty(adapter, property);
    }

    @SuppressWarnings("unchecked")
    private static <T extends InventoryProperty<?, ?>> Optional<T> findRootProperty(final InventoryAdapter adapter, final Class<T> property) {
        if (property == InventoryTitle.class) {
            final Text text = Text.of(adapter.bridge$getFabric().fabric$getDisplayName());
            return (Optional<T>) Optional.of(InventoryTitle.of(text));
        }
        // TODO more properties of top level inventory
        return Optional.empty();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static InventoryAdapter inventoryRoot(InventoryAdapter adapter) {
        // Get Root Inventory
        adapter = (InventoryAdapter) ((Inventory) adapter).root();
        if (adapter instanceof Container) {
            // If Root is a Container get the viewed inventory
            final Object first = adapter.bridge$getFabric().fabric$get(0);
            if (first instanceof CustomInventory) {
                // if viewed inventory is a custom inventory get it instead
                adapter = ((InventoryAdapter) first);
            }
        }
        return adapter;
    }

    public static boolean contains(final InventoryAdapter adapter, final ItemStack stack) {
        return AdapterLogic.contains(adapter.bridge$getFabric(), adapter.bridge$getRootLens(), stack, stack.getQuantity());
    }

    public static boolean contains(final InventoryAdapter adapter, final ItemStack stack, final int quantity) {
        return AdapterLogic.contains(adapter.bridge$getFabric(), adapter.bridge$getRootLens(), stack, quantity);
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
    public static boolean contains(final Fabric inv, final Lens lens, final ItemStack stack, final int quantity) {
        final net.minecraft.item.ItemStack nonNullStack = ItemStackUtil.toNative(stack); // Handle null as empty
        int found = 0;
        for (int ord = 0; ord < lens.slotCount(); ord++) {
            final net.minecraft.item.ItemStack slotStack = lens.getStack(inv, ord);
            if (slotStack.func_190926_b()) {
                if (nonNullStack.func_190926_b()) {
                    found++; // Found an empty Slot
                    if (found >= quantity) {
                        return true;
                    }
                }
            } else {
                if (ItemStackUtil.compareIgnoreQuantity(slotStack, stack)) {
                    found += slotStack.func_190916_E(); // Found a matching stack
                    if (found >= quantity) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean contains(final InventoryAdapter adapter, final ItemType type) {
        return AdapterLogic.contains(adapter.bridge$getFabric(), adapter.bridge$getRootLens(), type);
    }

    public static boolean contains(final Fabric inv, final Lens lens, final ItemType type) {
        for (int ord = 0; ord < lens.slotCount(); ord++) {
            final net.minecraft.item.ItemStack slotStack = lens.getStack(inv, ord);
            if (slotStack.func_190926_b()) {
                if (type == null || type == ItemTypes.NONE) {
                    return true; // Found an empty Slot
                }
            } else {
                if (slotStack.func_77973_b() == type) {
                    return true; // Found a matching stack
                }
            }
        }
        return false;
    }

    public static boolean canFit(final InventoryAdapter adapter, final ItemStack stack) {

        final Fabric inv = adapter.bridge$getFabric();
        final Lens lens = adapter.bridge$getRootLens();

        final net.minecraft.item.ItemStack nativeStack = ItemStackUtil.toNative(stack);

        final int maxStackSize = Math.min(lens.getMaxStackSize(inv), nativeStack.func_77976_d());
        int remaining = stack.getQuantity();

        for (int ord = 0; ord < lens.slotCount() && remaining > 0; ord++) {
            final net.minecraft.item.ItemStack old = lens.getStack(inv, ord);
            int push = Math.min(remaining, maxStackSize);
            if (old.func_190926_b()) {
                remaining -= push;
            } else if (ItemStackUtil.compareIgnoreQuantity(old, stack)) {
                push = Math.max(Math.min(maxStackSize - old.func_190916_E(), remaining), 0); // max() accounts for oversized stacks
                remaining -= push;
            }
        }

        return remaining == 0;
    }
}
