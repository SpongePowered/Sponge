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
package org.spongepowered.common.item.inventory;

import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.EmptyInventory;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.query.QueryOperation;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult.Type;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.item.inventory.EmptyInventoryImpl.EmptyIterator;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Bottom type / empty results set for inventory queries.
 */
public class EmptyInventoryImpl implements EmptyInventory {

    public static final Translation EMPTY_NAME = new SpongeTranslation("inventory.empty.title");

    static final class EmptyIterator implements Iterator<Inventory> {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Inventory next() {
            throw new NoSuchElementException("Attempted to iterate over an empty Inventory");
        }

        @Override
        public void remove() {
            throw new NoSuchElementException("Attempted to remove an element from an empty collection");
        }

    }

    private final Inventory parent;

    public EmptyInventoryImpl(Inventory parent) {
        this.parent = parent;
    }

    @Override
    public <T extends Inventory> Iterable<T> slots() {
        return Collections.<T>emptyList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> T first() {
        return (T) this;
    }

    @Override
    public Optional<ItemStack> poll() {
        return Optional.<ItemStack>empty();
    }

    @Override
    public Optional<ItemStack> poll(int limit) {
        return Optional.<ItemStack>empty();
    }

    @Override
    public Optional<ItemStack> peek() {
        return Optional.<ItemStack>empty();
    }

    @Override
    public Optional<ItemStack> peek(int limit) {
        return Optional.<ItemStack>empty();
    }

    @Override
    public void clear() {
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int totalItems() {
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
    public boolean contains(ItemStack stack) {
        return false;
    }

    @Override
    public boolean containsAny(ItemStack stack) {
        return false;
    }

    @Override
    public boolean contains(ItemType type) {
        return false;
    }

    @Override
    public int getMaxStackSize() {
        return 0;
    }

    @Override
    public void setMaxStackSize(int size) {
    }

    @Override
    public <T extends InventoryProperty<?, ?>> Collection<T> getProperties(Inventory child, Class<T> property) {
        return Collections.<T>emptyList();
    }

    @Override
    public <T extends InventoryProperty<?, ?>> Collection<T> getProperties(Class<T> property) {
        return Collections.<T>emptyList();
    }

    @Override
    public <T extends InventoryProperty<?, ?>> Optional<T> getProperty(Inventory child, Class<T> property, Object key) {
        return Optional.<T>empty();
    }

    @Override
    public <T extends InventoryProperty<?, ?>> Optional<T> getProperty(Class<T> property, Object key) {
        return Optional.<T>empty();
    }

    @Override
    public <T extends InventoryProperty<?, ?>> Optional<T> getInventoryProperty(Inventory child, Class<T> property) {
        return Optional.empty();
    }

    @Override
    public <T extends InventoryProperty<?, ?>> Optional<T> getInventoryProperty(Class<T> property) {
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> T query(QueryOperation<?>... operations) {
        return (T) this;
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
    public Iterator<Inventory> iterator() {
        return new EmptyIterator();
    }

    @Override
    public Inventory parent() {
        return this.parent;
    }

    @Override
    public Inventory root() {
        return this.parent == this ? this : this.parent.root();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> T next() {
        return (T) this;
    }

    @Override
    public InventoryTransactionResult offer(ItemStack stack) {
        return InventoryTransactionResult.builder().type(Type.FAILURE).reject(stack).build();
    }

    @Override
    public boolean canFit(ItemStack stack) {
        return false;
    }

    @Override
    public InventoryTransactionResult set(ItemStack stack) {
        return InventoryTransactionResult.builder().type(Type.FAILURE).reject(stack).build();
    }

    @Override
    public Translation getName() {
        return EmptyInventoryImpl.EMPTY_NAME;
    }

    @Override
    public PluginContainer getPlugin() {
        return this.parent.getPlugin();
    }

    @Override
    public InventoryArchetype getArchetype() {
        return InventoryArchetypes.UNKNOWN;
    }
}
