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

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.AbstractInventoryProperty;
import org.spongepowered.api.item.inventory.query.QueryOperation;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.item.inventory.EmptyInventoryImpl;
import org.spongepowered.common.item.inventory.InventoryIterator;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.CompoundSlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.CompoundLens;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftFabric;
import org.spongepowered.common.item.inventory.lens.impl.fabric.CompoundFabric;
import org.spongepowered.common.item.inventory.query.Query;
import org.spongepowered.common.item.inventory.query.operation.LensQueryOperation;
import org.spongepowered.common.item.inventory.query.operation.SlotLensQueryOperation;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

public interface DefaultImplementedInventoryAdapter extends InventoryAdapter, Inventory {

    @Override
    default Translation getName() {
        if (this.bridge$getRootLens() == null) {
            return this.bridge$getFabric().getDisplayName();
        }
        return this.bridge$getRootLens().getName(this.bridge$getFabric());
    }

    @Override
    default Inventory root() {
        return this.parent() == this ? this : this.parent().root();
    }

    @Override
    default Optional<ItemStack> poll() {
        return AdapterLogic.pollSequential(this);
    }

    @Override
    default Optional<ItemStack> poll(final int limit) {
        return AdapterLogic.pollSequential(this, limit);
    }

    @Override
    default Optional<ItemStack> peek() {
        return AdapterLogic.peekSequential(this);
    }

    @Override
    default Optional<ItemStack> peek(final int limit) {
        return AdapterLogic.peekSequential(this, limit);
    }

    @Override
    default InventoryTransactionResult offer(final ItemStack stack) {
        //        try {
        return AdapterLogic.appendSequential(this, stack);
        //        } catch (Exception ex) {
        //            return false;
        //        }
    }

    @Override
    default boolean canFit(final ItemStack stack) {
        return AdapterLogic.canFit(this, stack);
    }

    @Override
    default InventoryTransactionResult set(final ItemStack stack) {
        return AdapterLogic.insertSequential(this, stack);
    }

    @Override
    default int size() {
        return AdapterLogic.countStacks(this);
    }

    @Override
    default int totalItems() {
        return AdapterLogic.countItems(this);
    }

    @Override
    default int capacity() {
        return AdapterLogic.getCapacity(this);
    }

    @Override
    default boolean hasChildren() {
        return this.bridge$getRootLens().getChildren().size() != 0;
    }

    @Override
    default boolean contains(final ItemStack stack) {
        return AdapterLogic.contains(this, stack);
    }

    @Override
    default boolean containsAny(final ItemStack stack) {
        return AdapterLogic.contains(this, stack, 1);
    }

    @Override
    default boolean contains(final ItemType type) {
        return AdapterLogic.contains(this, type);
    }

    @Override
    default int getMaxStackSize() {
        return this.bridge$getRootLens().getMaxStackSize(this.bridge$getFabric());
    }

    @Override
    default void setMaxStackSize(final int size) {
        throw new UnsupportedOperationException("This inventory does not support stack limit adjustment");
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends InventoryProperty<?, ?>> Collection<T> getProperties(final Inventory child, final Class<T> property) {
        return (Collection<T>) AdapterLogic.getProperties(this, child, property);
    }

    @Override
    default <T extends InventoryProperty<?, ?>> Collection<T> getProperties(final Class<T> property) {
        if (this.parent() == this) {
            return AdapterLogic.getRootProperties(this, property);
        }
        return this.parent().getProperties(this, property);
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends InventoryProperty<?, ?>> Optional<T> getProperty(final Inventory child, final Class<T> property, final Object key) {
        for (final InventoryProperty<?, ?> prop : AdapterLogic.getProperties(this, child, property)) {
            if (key.equals(prop.getKey())) {
                return Optional.of((T)prop);
            }
        }
        return Optional.empty();
    }

    @Override
    default <T extends InventoryProperty<?, ?>> Optional<T> getProperty(final Class<T> property, final Object key) {
        if (this.parent() == this) {
            return AdapterLogic.getRootProperty(this, property, key);
        }
        return this.parent().getProperty(this, property, key);
    }

    @Override
    default <T extends InventoryProperty<?, ?>> Optional<T> getInventoryProperty(final Inventory child, final Class<T> property) {
        final Object key = AbstractInventoryProperty.getDefaultKey(property);
        return this.getProperty(child, property, key);
    }

    @Override
    default <T extends InventoryProperty<?, ?>> Optional<T> getInventoryProperty(final Class<T> property) {
        final Object key = AbstractInventoryProperty.getDefaultKey(property);
        return this.getProperty(property, key);
    }

    @Override
    default Iterator<Inventory> iterator() {
        return new InventoryIterator(this.bridge$getRootLens(), this.bridge$getFabric(), this);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Deprecated
    default <T extends Inventory> T first() {
        return (T) this.iterator().next();
    }

    @SuppressWarnings("unchecked")
    @Override
    @Deprecated
    default <T extends Inventory> T next() {
        return (T) new EmptyInventoryImpl(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends Inventory> T query(final QueryOperation<?>... queries) {
        return (T) Query.compile(this, queries).execute();
    }

    @Override
    default Inventory intersect(final Inventory inventory) {
        return Query.compile(this, new SlotLensQueryOperation(ImmutableSet.of(inventory))).execute();
    }

    @SuppressWarnings("Duplicates")
    @Override
    default Inventory union(final Inventory inventory) {
        final CompoundLens.Builder lensBuilder = CompoundLens.builder().add(this.bridge$getRootLens());
        final CompoundFabric fabric = new CompoundFabric((MinecraftFabric) this.bridge$getFabric(), (MinecraftFabric) ((InventoryAdapter) inventory).bridge$getFabric());
        final CompoundSlotProvider provider = new CompoundSlotProvider().add(this);
        for (final Object inv : inventory) {
            lensBuilder.add(((InventoryAdapter) inv).bridge$getRootLens());
            provider.add((InventoryAdapter) inv);
        }
        final CompoundLens lens = lensBuilder.build(provider);
        final InventoryAdapter compoundAdapter = lens.getAdapter(fabric, this);

        return Query.compile(compoundAdapter, new SlotLensQueryOperation(ImmutableSet.of((Inventory) compoundAdapter))).execute();
    }

    @Override
    default boolean containsInventory(final Inventory inventory) {
        final Inventory result = Query.compile(this, new LensQueryOperation(((InventoryAdapter) inventory).bridge$getRootLens())).execute();
        return result.capacity() == inventory.capacity() && ((InventoryAdapter) result).bridge$getRootLens() == ((InventoryAdapter) inventory).bridge$getRootLens();
    }

    @Override
    default InventoryArchetype getArchetype() {
        return InventoryArchetypes.UNKNOWN;
    }

}
