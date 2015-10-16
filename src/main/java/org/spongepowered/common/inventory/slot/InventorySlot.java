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
package org.spongepowered.common.inventory.slot;

import net.minecraft.inventory.IInventory;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionBuilder;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.translation.FixedTranslation;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.common.Sponge;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

/**
 * {@link Inventory} {@link Slot} object that contains common logic between mixin
 * Mojang's {@link net.minecraft.inventory.Slot} and the proxies to the inventory-like
 * systems found in {@link net.minecraft.entity.EntityLivingBase}.
 */
@NonnullByDefault
public interface InventorySlot extends org.spongepowered.api.item.inventory.Slot {
    IInventory getNMSInventory();
    int getSlotNumber();

    @Override
    default Optional<Inventory> parent() {
        return Optional.of((Inventory) getNMSInventory());
    }

    @Override
    default ItemStack poll() {
        return fetch(true);
    }

    @Override
    default ItemStack poll(int limit) {
        return fetchSome(true, limit);
    }

    @Override
    default ItemStack peek() {
        return fetch(false);
    }

    @Override
    default ItemStack peek(int limit) {
        return fetchSome(false, limit);
    }

    @Override
    default InventoryTransactionResult offer(ItemStack stack) {
        // TODO Correct the transaction based on how offer goes
        final net.minecraft.item.ItemStack nmsStack = getNMSInventory().getStackInSlot(getSlotNumber());
        final net.minecraft.item.ItemStack passedNmsStack = (net.minecraft.item.ItemStack) stack;
        if (!(net.minecraft.item.ItemStack.areItemsEqual(nmsStack, passedNmsStack) &&
                net.minecraft.item.ItemStack.areItemStackTagsEqual(nmsStack, passedNmsStack))) {
            return InventoryTransactionBuilder.failNoTransactions();
        }
        boolean canIncrease = getMaxStackSize() != nmsStack.stackSize;
        if (!canIncrease) {
            return InventoryTransactionBuilder.failNoTransactions();
        }
        int remaining = getMaxStackSize() - nmsStack.stackSize;
        int toBeOffered = stack.getQuantity();
        if (toBeOffered > remaining) {
            nmsStack.stackSize += toBeOffered - remaining;
            stack.setQuantity(toBeOffered - remaining);
        } else {
            nmsStack.stackSize += remaining;
            // TODO Quantity being set 0 could be a problem...
            stack.setQuantity(0);
        }
        getNMSInventory().markDirty();
        return InventoryTransactionBuilder.successNoTransactions();
    }

    @Override
    default InventoryTransactionResult set(ItemStack stack) {
        // TODO Correct the transaction based on how set goes
        if (stack.getQuantity() <= getMaxStackSize()) {
            getNMSInventory().setInventorySlotContents(getSlotNumber(), (net.minecraft.item.ItemStack) stack.copy());
        }
        return InventoryTransactionBuilder.successNoTransactions();
    }

    @Override
    default void clear() {
        getNMSInventory().setInventorySlotContents(getSlotNumber(), null);
    }

    @Override
    default int size() {
        return getNMSInventory().getStackInSlot(getSlotNumber()) != null ? 1 : 0;
    }

    @Override
    default int capacity() {
        return 1;
    }

    @Override
    default boolean isEmpty() {
        return size() == 0;
    }

    @Override
    default boolean contains(ItemStack stack) {
        return net.minecraft.item.ItemStack.areItemStacksEqual(getNMSInventory().getStackInSlot(getSlotNumber()),
                (net.minecraft.item.ItemStack) stack);
    }

    @Override
    default boolean contains(ItemType type) {
        return net.minecraft.item.ItemStack.areItemsEqual(getNMSInventory().getStackInSlot(getSlotNumber()),
                (net.minecraft.item.ItemStack) Sponge.getGame().getRegistry().createItemBuilder().itemType(type).build());
    }

    @Override
    default int getMaxStackSize() {
        return getNMSInventory().getInventoryStackLimit();
    }

    @Override
    default void setMaxStackSize(int size) {
        // TODO Generic slots can't really go over 64...revise for specific slots.
    }

    @Override
    default <T extends InventoryProperty<?, ?>> Collection<T> getProperties(Inventory child, Class<T> property) {
        return Collections.emptyList();
    }

    @Override
    default <T extends InventoryProperty<?, ?>> Collection<T> getProperties(Class<T> property) {
        return Collections.emptyList();
    }

    @Override
    default <T extends InventoryProperty<?, ?>> Optional<T> getProperty(Inventory child, Class<T> property, Object key) {
        return Optional.empty();
    }

    @Override
    default <T extends InventoryProperty<?, ?>> Optional<T> getProperty(Class<T> property, Object key) {
        return Optional.empty();
    }

    @Override
    default int getStackSize() {
        final net.minecraft.item.ItemStack nmsStack = getNMSInventory().getStackInSlot(getSlotNumber());
        return nmsStack != null ? nmsStack.stackSize : -1;
    }

    @Override
    default Iterator<Inventory> iterator() {
        // TODO Unsure if this is correct...
        return Collections.emptyIterator();
    }

    @Override
    default Translation getName() {
        return new FixedTranslation("slot " + getSlotNumber());
    }

    default ItemStack fetch(boolean remove) {
        final net.minecraft.item.ItemStack nmsStack = getNMSInventory().getStackInSlot(getSlotNumber());
        if (nmsStack == null) {
            return ItemStackSnapshot.NONE.createStack();
        }
        if (remove) {
            clear();
        }
        return (ItemStack) nmsStack.copy();
    }

    default ItemStack fetchSome(boolean remove, int limit) {
        final net.minecraft.item.ItemStack nmsStack = getNMSInventory().getStackInSlot(getSlotNumber());
        if (nmsStack == null) {
            return ItemStackSnapshot.NONE.createStack();
        }

        final net.minecraft.item.ItemStack fetchedNmsStack = nmsStack.copy();
        if (nmsStack.stackSize <= limit) {
            if (remove) {
                clear();
            }
        } else {
            fetchedNmsStack.stackSize = limit;
            if (remove) {
                nmsStack.stackSize -= limit;
                getNMSInventory().markDirty();
            }
        }

        return (ItemStack) fetchedNmsStack;
    }
}
