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

import net.minecraft.inventory.IInventory;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.query.Query;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

public interface MinecraftInventoryAdapter extends InventoryAdapter<IInventory, net.minecraft.item.ItemStack> {
    
    @Override
    default public Translation getName() {
        return this.getRootLens().getName(this.getInventory());
    }

    @Override
    default public Optional<ItemStack> poll() {
        return Adapter.Logic.pollSequential(this);
    }

    @Override
    default public Optional<ItemStack> poll(int limit) {
        return Adapter.Logic.pollSequential(this, limit);
    }

    @Override
    default public Optional<ItemStack> peek() {
        return Adapter.Logic.peekSequential(this);
    }

    @Override
    default public Optional<ItemStack> peek(int limit) {
        return Adapter.Logic.peekSequential(this, limit);
    }

    @Override
    default public InventoryTransactionResult offer(ItemStack stack) {
//        try {
            return Adapter.Logic.appendSequential(this, stack);
//        } catch (Exception ex) {
//            return false;
//        }
    }

    @Override
    default public InventoryTransactionResult set(ItemStack stack) {
        return Adapter.Logic.insertSequential(this, stack);
    }

    @Override
    default public int size() {
        return Adapter.Logic.countStacks(this);
    }

    @Override
    default public int totalItems() {
        return Adapter.Logic.countItems(this);
    }

    @Override
    default public int capacity() {
        return Adapter.Logic.getCapacity(this);
    }

    @Override
    default public boolean isEmpty() {
        return this.getRootLens().getChildren().size() == 0;
    }

    @Override
    default public boolean contains(ItemStack stack) {
        return Adapter.Logic.contains(this, stack);
    }

    @Override
    default public boolean contains(ItemType type) {
        return Adapter.Logic.contains(this, type);
    }

    @Override
    default public int getMaxStackSize() {
        return this.getRootLens().getMaxStackSize(this.getInventory());
    }

    @Override
    default public void setMaxStackSize(int size) {
        throw new UnsupportedOperationException("This inventory does not support stack limit adjustment");
    }

    @SuppressWarnings("unchecked")
    @Override
    default public <T extends InventoryProperty<?, ?>> Collection<T> getProperties(Inventory child, Class<T> property) {
        return (Collection<T>) Adapter.Logic.getProperties(this, child, property);
    }

    @Override
    default public <T extends InventoryProperty<?, ?>> Collection<T> getProperties(Class<T> property) {
        return Collections.<T>emptyList();
    }

    @Override
    default public <T extends InventoryProperty<?, ?>> Optional<T> getProperty(Inventory child, Class<T> property, Object key) {
        return Optional.<T>empty(); // TODO!
    }

    @Override
    default public <T extends InventoryProperty<?, ?>> Optional<T> getProperty(Class<T> property, Object key) {
        return Optional.<T>empty(); // TODO!
    }

    @Override
    default public Iterator<Inventory> iterator() {
        return new Adapter.Iter(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    default public <T extends Inventory> T query(Class<?>... types) {
        return (T) Query.compile(this, types).execute();
    }

    @SuppressWarnings("unchecked")
    @Override
    default public <T extends Inventory> T query(ItemType... types) {
        return (T) Query.compile(this, types).execute();
    }

    @SuppressWarnings("unchecked")
    @Override
    default public <T extends Inventory> T query(ItemStack... types) {
        return (T) Query.compile(this, types).execute();
    }

    @SuppressWarnings("unchecked")
    @Override
    default public <T extends Inventory> T query(InventoryProperty<?, ?>... props) {
        return (T) Query.compile(this, props).execute();
    }

    @SuppressWarnings("unchecked")
    @Override
    default public <T extends Inventory> T query(Translation... names) {
        return (T) Query.compile(this, names).execute();
    }

    @SuppressWarnings("unchecked")
    @Override
    default public <T extends Inventory> T query(String... args) {
        return (T) Query.compile(this, args).execute();
    }

    @SuppressWarnings("unchecked")
    @Override
    default public <T extends Inventory> T query(Object... args) {
        return (T) Query.compile(this, args).execute();
    }

}
