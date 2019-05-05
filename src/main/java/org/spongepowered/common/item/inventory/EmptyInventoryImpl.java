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

import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.data.property.PropertyMatcher;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.EmptyInventory;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.query.InventoryTransformation;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.query.Query;
import org.spongepowered.api.item.inventory.slot.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult.Type;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import javax.annotation.Nullable;

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

    public EmptyInventoryImpl(@Nullable Inventory parent) {
        this.parent = parent == null ? this : parent;
    }

    @Override
    public List<Slot> slots() {
        return Collections.emptyList();
    }

    @Override
    public ItemStack poll() {
        return ItemStack.empty();
    }

    @Override
    public ItemStack poll(int limit) {
        return ItemStack.empty();
    }

    @Override
    public ItemStack peek() {
        return ItemStack.empty();
    }

    @Override
    public ItemStack peek(int limit) {
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
    public <V> Optional<V> getProperty(Inventory child, Property<V> property) {
        return Optional.empty();
    }

    @Override
    public <V> Optional<V> getProperty(Property<V> property) {
        return Optional.empty();
    }

    @Override
    public OptionalInt getIntProperty(Property<Integer> property) {
        return OptionalInt.empty();
    }

    @Override
    public OptionalDouble getDoubleProperty(Property<Double> property) {
        return OptionalDouble.empty();
    }

    @Override
    public Map<Property<?>, ?> getProperties() {
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Inventory query(Query<?>... operations) {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
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
    public InventoryTransactionResult offer(ItemStack stack) {
        return InventoryTransactionResult.builder().type(Type.FAILURE).reject(stack).build();
    }

    @Override
    public InventoryTransactionResult set(ItemStack stack) {
        return InventoryTransactionResult.builder().type(Type.FAILURE).reject(stack).build();
    }

    @Override
    public Optional<ItemStack> poll(SlotIndex index) {
        return Optional.empty();
    }

    @Override
    public Optional<ItemStack> poll(SlotIndex index, int limit) {
        return Optional.empty();
    }

    @Override
    public Optional<ItemStack> peek(SlotIndex index) {
        return Optional.empty();
    }

    @Override
    public Optional<ItemStack> peek(SlotIndex index, int limit) {
        return Optional.empty();
    }

    @Override
    public InventoryTransactionResult offer(SlotIndex index, ItemStack stack) {
        return InventoryTransactionResult.builder().type(Type.FAILURE).reject(stack).build();
    }

    @Override
    public InventoryTransactionResult set(SlotIndex index, ItemStack stack) {
        return InventoryTransactionResult.builder().type(Type.FAILURE).reject(stack).build();
    }

    @Override
    public InventoryTransactionResult offer(ItemStack... stack) {
        return InventoryTransactionResult.builder().type(Type.FAILURE).reject(stack).build();
    }

    @Override
    public Inventory query(PropertyMatcher<?> propertyMatcher) {
        return this;
    }

    @Override
    public Inventory transform(InventoryTransformation transformation) {
        return this;
    }

    @Override
    public boolean canFit(ItemStack stack) {
        return false;
    }

    @Override
    public Optional<Slot> getSlot(SlotIndex index) {
        return Optional.empty();
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
    public Optional<ViewableInventory> asViewable() {
        return Optional.empty();
    }
}
