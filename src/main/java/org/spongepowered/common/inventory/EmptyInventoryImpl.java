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
package org.spongepowered.common.inventory;

import com.google.common.collect.ImmutableSet;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.KeyValueMatcher;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.EmptyInventory;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackLike;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.query.Query;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult.Type;
import org.spongepowered.api.item.inventory.type.ViewableInventory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;


/**
 * Bottom type / empty results set for inventory queries.
 */
public class EmptyInventoryImpl implements EmptyInventory {

    private final Inventory parent;

    public EmptyInventoryImpl(final @Nullable Inventory parent) {
        this.parent = parent == null ? this : parent;
    }

    @Override
    public List<Slot> slots() {
        return Collections.emptyList();
    }

    public InventoryTransactionResult.Poll poll() {
        return InventoryTransactionResult.builder().type(Type.FAILURE).poll(ItemStackSnapshot.empty()).build();
    }

    public InventoryTransactionResult.Poll poll(int limit) {
        return InventoryTransactionResult.builder().type(Type.FAILURE).poll(ItemStackSnapshot.empty()).build();
    }

    @Override
    public ItemStack peek() {
        return ItemStack.empty();
    }

    @Override
    public List<Inventory> children() {
        return Collections.emptyList();
    }

    @Override
    public void clear() {
    }

    @Override
    public int freeCapacity() {
        return 0;
    }

    @Override
    public int totalQuantity() {
        return 0;
    }

    @Override
    public int capacity() {
        return 0;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public boolean contains(ItemStackLike stack) {
        return false;
    }

    @Override
    public boolean containsAny(ItemStackLike stack) {
        return false;
    }

    @Override
    public <V> Optional<V> get(Inventory child, Key<? extends Value<V>> key) {
        return Optional.empty();
    }

    @Override
    public <V> Optional<V> get(Key<? extends Value<V>> key) {
        return Optional.empty();
    }

    @Override
    public <E, V extends Value<E>> Optional<V> getValue(Key<V> key) {
        return Optional.empty();
    }

    @Override
    public boolean supports(Key<?> key) {
        return false;
    }

    @Override
    public Set<Key<?>> getKeys() {
        return ImmutableSet.of();
    }

    @Override
    public Set<Value.Immutable<?>> getValues() {
        return ImmutableSet.of();
    }

    @Override
    public boolean contains(ItemType type) {
        return false;
    }

    @Override
    public Inventory query(KeyValueMatcher<?> matcher) {
        return this;
    }

    public Inventory query(Query query) {
        return query.execute(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Inventory> Optional<T> query(Class<T> inventoryType) {
        if (EmptyInventory.class == inventoryType) {
            return Optional.of((T) this);
        }
        return Optional.empty();
    }

    @Override
    public Inventory intersect(Inventory inventory) {
        return this; // Shortcut
    }

    @Override
    public Inventory union(Inventory inventory) {
        return inventory; // Shortcut
    }

    @Override
    public boolean containsInventory(Inventory inventory) {
        return this == inventory;
    }

    @Override
    public Inventory parent() {
        return this.parent;
    }

    @Override
    public Inventory root() {
        return this.parent == this ? this : this.parent.root();
    }

    @Override
    public InventoryTransactionResult offer(ItemStackLike... stacks) {
        return InventoryTransactionResult.builder().type(Type.FAILURE).reject(stacks).build();
    }

    public InventoryTransactionResult.Poll pollFrom(int index) {
        return InventoryTransactionResult.builder().type(Type.NO_SLOT).poll(ItemStackSnapshot.empty()).build();
    }

    public InventoryTransactionResult.Poll pollFrom(int index, int limit) {
        return InventoryTransactionResult.builder().type(Type.NO_SLOT).poll(ItemStackSnapshot.empty()).build();
    }

    public Optional<ItemStack> peekAt(int index) {
        return Optional.empty();
    }

    public InventoryTransactionResult offer(int index, ItemStackLike stack) {
        return InventoryTransactionResult.builder().type(Type.NO_SLOT).reject(stack).build();
    }

    public InventoryTransactionResult set(int index, ItemStackLike stack) {
        return InventoryTransactionResult.builder().type(Type.NO_SLOT).reject(stack).build();
    }

    @Override
    public boolean canFit(ItemStackLike stack) {
        return false;
    }

    public Optional<Slot> slot(int index) {
        return Optional.empty();
    }

    @Override
    public boolean containsChild(Inventory child) {
        return this == child;
    }

    @Override
    public Optional<ViewableInventory> asViewable() {
        return Optional.empty();
    }
}
