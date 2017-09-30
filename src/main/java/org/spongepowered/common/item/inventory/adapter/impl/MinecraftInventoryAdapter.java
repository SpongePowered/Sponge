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
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.AbstractInventoryProperty;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.item.inventory.InventoryIterator;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.query.Query;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

public interface MinecraftInventoryAdapter extends InventoryAdapter<IInventory, net.minecraft.item.ItemStack> {

    @Override
    default Translation getName() {
        return this.getRootLens().getName(this.getInventory());
    }

    @Override
    default Inventory root() {
        return this.parent() == this ? this : this.parent().root();
    }

    @Override
    default Optional<ItemStack> poll() {
        return Adapter.Logic.pollSequential(this);
    }

    @Override
    default Optional<ItemStack> poll(int limit) {
        return Adapter.Logic.pollSequential(this, limit);
    }

    @Override
    default Optional<ItemStack> peek() {
        return Adapter.Logic.peekSequential(this);
    }

    @Override
    default Optional<ItemStack> peek(int limit) {
        return Adapter.Logic.peekSequential(this, limit);
    }

    @Override
    default InventoryTransactionResult offer(ItemStack stack) {
        //        try {
        return Adapter.Logic.appendSequential(this, stack);
        //        } catch (Exception ex) {
        //            return false;
        //        }
    }

    @Override
    default InventoryTransactionResult set(ItemStack stack) {
        return Adapter.Logic.insertSequential(this, stack);
    }

    @Override
    default int size() {
        return Adapter.Logic.countStacks(this);
    }

    @Override
    default int totalItems() {
        return Adapter.Logic.countItems(this);
    }

    @Override
    default int capacity() {
        return Adapter.Logic.getCapacity(this);
    }

    @Override
    default boolean hasChildren() {
        return this.getRootLens().getChildren().size() != 0;
    }

    @Override
    default boolean contains(ItemStack stack) {
        return Adapter.Logic.contains(this, stack);
    }

    @Override
    default boolean containsAny(ItemStack stack) {
        return Adapter.Logic.contains(this, stack, 1);
    }

    @Override
    default boolean contains(ItemType type) {
        return Adapter.Logic.contains(this, type);
    }

    @Override
    default int getMaxStackSize() {
        return this.getRootLens().getMaxStackSize(this.getInventory());
    }

    @Override
    default void setMaxStackSize(int size) {
        throw new UnsupportedOperationException("This inventory does not support stack limit adjustment");
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends InventoryProperty<?, ?>> Collection<T> getProperties(Inventory child, Class<T> property) {
        return (Collection<T>) Adapter.Logic.getProperties(this, child, property);
    }

    @Override
    default <T extends InventoryProperty<?, ?>> Collection<T> getProperties(Class<T> property) {
        if (this.parent() == this) {
            return Collections.emptyList(); // TODO top level inventory properties
        }
        return this.parent().getProperties(this, property);
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends InventoryProperty<?, ?>> Optional<T> getProperty(Inventory child, Class<T> property, Object key) {
        for (InventoryProperty<?, ?> prop : Adapter.Logic.getProperties(this, child, property)) {
            if (key.equals(prop.getKey())) {
                return Optional.of(((T) prop));
            }
        }
        return Optional.empty();
    }

    @Override
    default <T extends InventoryProperty<?, ?>> Optional<T> getProperty(Class<T> property, Object key) {
        if (this.parent() == this) {
            return Optional.empty(); // TODO top level inventory properties
        }
        return this.parent().getProperty(this, property, key);
    }

    @Override
    default <T extends InventoryProperty<?, ?>> Optional<T> getInventoryProperty(Inventory child, Class<T> property) {
        Object key = AbstractInventoryProperty.getDefaultKey(property);
        return this.getProperty(child, property, key);
    }

    @Override
    default <T extends InventoryProperty<?, ?>> Optional<T> getInventoryProperty(Class<T> property) {
        Object key = AbstractInventoryProperty.getDefaultKey(property);
        return this.getProperty(property, key);
    }

    @Override
    default Iterator<Inventory> iterator() {
        return new InventoryIterator<>(this.getRootLens(), this.getInventory(), this);
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends Inventory> T query(Class<?>... types) {
        return (T) Query.compile(this, types).execute();
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends Inventory> T query(ItemType... types) {
        return (T) Query.compile(this, types).execute();
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends Inventory> T query(ItemStack... types) {
        return (T) Query.compileExact(this, types).execute();
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends Inventory> T queryAny(ItemStack... types) {
        return (T) Query.compile(this, types).execute();
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends Inventory> T query(InventoryProperty<?, ?>... props) {
        return (T) Query.compile(this, props).execute();
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends Inventory> T query(Translation... names) {
        return (T) Query.compile(this, names).execute();
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends Inventory> T query(String... args) {
        return (T) Query.compile(this, args).execute();
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends Inventory> T query(Object... args) {
        return (T) Query.compile(this, args).execute();
    }

    @Override
    default Inventory intersect(Inventory inventory) {
        return Query.intersect(this, inventory).execute();
    }

    @Override
    default Inventory union(Inventory inventory) {
        return Query.union(this, inventory).execute();
    }

    @Override
    default boolean containsInventory(Inventory inventory) {
        Inventory result = Query.compile(this, ((InventoryAdapter) inventory).getRootLens()).execute();
        return result.capacity() == inventory.capacity() && ((InventoryAdapter) result).getRootLens() == ((InventoryAdapter) inventory).getRootLens();
    }

    @Override
    default InventoryArchetype getArchetype() {
        return InventoryArchetypes.UNKNOWN;
    }

}
