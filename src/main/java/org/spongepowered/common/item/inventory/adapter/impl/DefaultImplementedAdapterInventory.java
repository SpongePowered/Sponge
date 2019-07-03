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
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.bridge.item.inventory.InventoryAdapterBridge;
import org.spongepowered.common.bridge.item.inventory.InventoryBridge;
import org.spongepowered.common.item.inventory.EmptyInventoryImpl;
import org.spongepowered.common.item.inventory.InventoryIterator;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.CompoundSlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.CompoundLens;
import org.spongepowered.common.item.inventory.lens.impl.fabric.CompoundFabric;
import org.spongepowered.common.item.inventory.query.Query;
import org.spongepowered.common.item.inventory.query.operation.LensQueryOperation;
import org.spongepowered.common.item.inventory.query.operation.SlotLensQueryOperation;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

/**
 * Implements almost all of {@link Inventory} assuming that this is a {@link InventoryBridge} providing the {@link InventoryAdapter}.
 */
public interface DefaultImplementedAdapterInventory extends Inventory {

    /**
     * Only use on Inventories that are Fabrics themselves
     */
    interface WithClear extends DefaultImplementedAdapterInventory {

        @Override
        default void clear() {
            ((InventoryBridge) this).bridge$getAdapter().bridge$getFabric().fabric$clear();
        }
    }

    @Override
    default PluginContainer getPlugin() {
        return ((InventoryAdapterBridge) this).bridge$getPlugin();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    default Translation getName() {
        if (((InventoryBridge) this).bridge$getAdapter().bridge$getRootLens() == null) {
            return ((InventoryBridge) this).bridge$getAdapter().bridge$getFabric().fabric$getDisplayName();
        }
        return ((InventoryBridge) this).bridge$getAdapter().bridge$getRootLens().getName(((InventoryBridge) this).bridge$getAdapter().bridge$getFabric());
    }

    @Override
    default Inventory root() {
        return this.parent() == this ? this : this.parent().root();
    }

    @Override
    default Inventory parent() {
        return this; // TODO let Adapter decide instead?
    }

    @Override
    default Optional<ItemStack> poll() {
        return AdapterLogic.pollSequential(((InventoryBridge) this).bridge$getAdapter());
    }

    @Override
    default Optional<ItemStack> poll(int limit) {
        return AdapterLogic.pollSequential(((InventoryBridge) this).bridge$getAdapter(), limit);
    }

    @Override
    default Optional<ItemStack> peek() {
        return AdapterLogic.peekSequential(((InventoryBridge) this).bridge$getAdapter());
    }

    @Override
    default Optional<ItemStack> peek(int limit) {
        return AdapterLogic.peekSequential(((InventoryBridge) this).bridge$getAdapter(), limit);
    }

    @Override
    default InventoryTransactionResult offer(ItemStack stack) {
        return AdapterLogic.appendSequential(((InventoryBridge) this).bridge$getAdapter(), stack);
    }

    @Override
    default boolean canFit(ItemStack stack) {
        return AdapterLogic.canFit(((InventoryBridge) this).bridge$getAdapter(), stack);
    }

    @Override
    default InventoryTransactionResult set(ItemStack stack) {
        return AdapterLogic.insertSequential(((InventoryBridge) this).bridge$getAdapter(), stack);
    }

    @Override
    default int size() {
        return AdapterLogic.countStacks(((InventoryBridge) this).bridge$getAdapter());
    }

    @Override
    default int totalItems() {
        return AdapterLogic.countItems(((InventoryBridge) this).bridge$getAdapter());
    }

    @Override
    default int capacity() {
        return AdapterLogic.getCapacity(((InventoryBridge) this).bridge$getAdapter());
    }

    @Override
    default boolean hasChildren() {
        return ((InventoryBridge) this).bridge$getAdapter().bridge$getRootLens().getChildren().size() != 0;
    }

    @Override
    default boolean contains(ItemStack stack) {
        return AdapterLogic.contains(((InventoryBridge) this).bridge$getAdapter(), stack);
    }

    @Override
    default boolean containsAny(ItemStack stack) {
        return AdapterLogic.contains(((InventoryBridge) this).bridge$getAdapter(), stack, 1);
    }

    @Override
    default boolean contains(ItemType type) {
        return AdapterLogic.contains(((InventoryBridge) this).bridge$getAdapter(), type);
    }

    @Override
    default int getMaxStackSize() {
        return ((InventoryBridge) this).bridge$getAdapter().bridge$getRootLens().getMaxStackSize(((InventoryBridge) this).bridge$getAdapter().bridge$getFabric());
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends InventoryProperty<?, ?>> Collection<T> getProperties(Inventory child, Class<T> property) {
        return (Collection<T>) AdapterLogic.getProperties(((InventoryBridge) this).bridge$getAdapter(), child, property);
    }

    @Override
    default <T extends InventoryProperty<?, ?>> Collection<T> getProperties(Class<T> property) {
        if (this.parent() == this) {
            return AdapterLogic.getRootProperties(((InventoryBridge) this).bridge$getAdapter(), property);
        }
        return this.parent().getProperties(this, property);
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends InventoryProperty<?, ?>> Optional<T> getProperty(Inventory child, Class<T> property, Object key) {
        for (final InventoryProperty<?, ?> prop : AdapterLogic.getProperties(((InventoryBridge) this).bridge$getAdapter(), child, property)) {
            if (key.equals(prop.getKey())) {
                return Optional.of((T)prop);
            }
        }
        return Optional.empty();
    }

    @Override
    default <T extends InventoryProperty<?, ?>> Optional<T> getProperty(Class<T> property, Object key) {
        if (this.parent() == this) {
            return AdapterLogic.getRootProperty(((InventoryBridge) this).bridge$getAdapter(), property, key);
        }
        return this.parent().getProperty(this, property, key);
    }

    @Override
    default <T extends InventoryProperty<?, ?>> Optional<T> getInventoryProperty(Inventory child, Class<T> property) {
        final Object key = AbstractInventoryProperty.getDefaultKey(property);
        return this.getProperty(child, property, key);
    }

    @Override
    default <T extends InventoryProperty<?, ?>> Optional<T> getInventoryProperty(Class<T> property) {
        final Object key = AbstractInventoryProperty.getDefaultKey(property);
        return this.getProperty(property, key);
    }

    @Override
    default Iterator<Inventory> iterator() {
        return new InventoryIterator(((InventoryBridge) this).bridge$getAdapter().bridge$getRootLens(), ((InventoryBridge) this).bridge$getAdapter().bridge$getFabric(), this);
    }

    @Override
    @Deprecated
    default void setMaxStackSize(int size) {
        throw new UnsupportedOperationException("This inventory does not support stack limit adjustment");
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
    default <T extends Inventory> T query(QueryOperation<?>... queries) {
        return (T) Query.compile(((InventoryBridge) this).bridge$getAdapter(), queries).execute();
    }

    @Override
    default Inventory intersect(Inventory inventory) {
        return Query.compile(((InventoryBridge) this).bridge$getAdapter(), new SlotLensQueryOperation(ImmutableSet.of(inventory))).execute();
    }

    @Override
    default Inventory union(Inventory inventory) {
        final CompoundLens.Builder lensBuilder = CompoundLens.builder().add(((InventoryBridge) this).bridge$getAdapter().bridge$getRootLens());
        final CompoundFabric fabric = new CompoundFabric(((InventoryBridge) this).bridge$getAdapter().bridge$getFabric(), ((InventoryAdapter) inventory).bridge$getFabric());
        final CompoundSlotProvider provider = new CompoundSlotProvider().add(((InventoryBridge) this).bridge$getAdapter());
        for (final Object inv : inventory) {
            lensBuilder.add(((InventoryAdapter) inv).bridge$getRootLens());
            provider.add((InventoryAdapter) inv);
        }
        final CompoundLens lens = lensBuilder.build(provider);
        final InventoryAdapter compoundAdapter = lens.getAdapter(fabric, this);

        return Query.compile(compoundAdapter, new SlotLensQueryOperation(ImmutableSet.of((Inventory) compoundAdapter))).execute();
    }

    @Override
    default boolean containsInventory(Inventory inventory) {
        final Inventory result = Query.compile(((InventoryBridge) this).bridge$getAdapter(), new LensQueryOperation(((InventoryAdapter) inventory).bridge$getRootLens())).execute();
        return result.capacity() == inventory.capacity() && ((InventoryAdapter) result).bridge$getRootLens() == ((InventoryAdapter) inventory).bridge$getRootLens();
    }

    @Override
    default InventoryArchetype getArchetype() {
        return InventoryArchetypes.UNKNOWN;
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends Inventory> Iterable<T> slots() {
        // TODO caching
        return (Iterable<T>) SlotCollectionIterator.of(this, ((InventoryBridge) this).bridge$getAdapter());
    }

}
