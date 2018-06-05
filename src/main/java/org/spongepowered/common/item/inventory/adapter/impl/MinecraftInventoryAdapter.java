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
import net.minecraft.inventory.IInventory;
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

public interface MinecraftInventoryAdapter<TInventory> extends InventoryAdapter<TInventory, net.minecraft.item.ItemStack> {

    @Override
    default Translation getName() {
        if (this.getRootLens() == null) {
            return this.getFabric().getDisplayName();
        }
        return this.getRootLens().getName(this.getFabric());
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
    default Optional<ItemStack> poll(int limit) {
        return AdapterLogic.pollSequential(this, limit);
    }

    @Override
    default Optional<ItemStack> peek() {
        return AdapterLogic.peekSequential(this);
    }

    @Override
    default Optional<ItemStack> peek(int limit) {
        return AdapterLogic.peekSequential(this, limit);
    }

    @Override
    default InventoryTransactionResult offer(ItemStack stack) {
        //        try {
        return AdapterLogic.appendSequential(this, stack);
        //        } catch (Exception ex) {
        //            return false;
        //        }
    }

    @Override
    default InventoryTransactionResult set(ItemStack stack) {
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
        return this.getRootLens().getChildren().size() != 0;
    }

    @Override
    default boolean contains(ItemStack stack) {
        return AdapterLogic.contains(this, stack);
    }

    @Override
    default boolean containsAny(ItemStack stack) {
        return AdapterLogic.contains(this, stack, 1);
    }

    @Override
    default boolean contains(ItemType type) {
        return AdapterLogic.contains(this, type);
    }

    @Override
    default int getMaxStackSize() {
        return this.getRootLens().getMaxStackSize(this.getFabric());
    }

    @Override
    default void setMaxStackSize(int size) {
        throw new UnsupportedOperationException("This inventory does not support stack limit adjustment");
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends InventoryProperty<?, ?>> Collection<T> getProperties(Inventory child, Class<T> property) {
        return (Collection<T>) AdapterLogic.getProperties(this, child, property);
    }

    @Override
    default <T extends InventoryProperty<?, ?>> Collection<T> getProperties(Class<T> property) {
        if (this.parent() == this) {
            return AdapterLogic.getRootProperties(this, property);
        }
        return this.parent().getProperties(this, property);
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends InventoryProperty<?, ?>> Optional<T> getProperty(Inventory child, Class<T> property, Object key) {
        for (InventoryProperty<?, ?> prop : AdapterLogic.getProperties(this, child, property)) {
            if (key.equals(prop.getKey())) {
                return Optional.of((T)prop);
            }
        }
        return Optional.empty();
    }

    @Override
    default <T extends InventoryProperty<?, ?>> Optional<T> getProperty(Class<T> property, Object key) {
        if (this.parent() == this) {
            return AdapterLogic.getRootProperty(this, property, key);
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
        return new InventoryIterator<>(this.getRootLens(), this.getFabric(), this);
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends Inventory> T query(QueryOperation<?>... queries) {
        return (T) Query.compile(this, queries).execute();
    }

    @Override
    default Inventory intersect(Inventory inventory) {
        return Query.compile(this, new SlotLensQueryOperation(ImmutableSet.of(inventory))).execute();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    default Inventory union(Inventory inventory) {
        CompoundLens.Builder lensBuilder = CompoundLens.builder().add(getRootLens());
        CompoundFabric fabric = new CompoundFabric((MinecraftFabric) getFabric(), (MinecraftFabric) ((InventoryAdapter) inventory).getFabric());
        CompoundSlotProvider provider = new CompoundSlotProvider().add(this);
        for (Object inv : inventory) {
            lensBuilder.add(((InventoryAdapter) inv).getRootLens());
            provider.add((InventoryAdapter) inv);
        }
        CompoundLens lens = lensBuilder.build(provider);
        InventoryAdapter<IInventory, net.minecraft.item.ItemStack> compoundAdapter = lens.getAdapter(fabric, this);

        return Query.compile(compoundAdapter, new SlotLensQueryOperation(ImmutableSet.of(compoundAdapter))).execute();
    }

    @SuppressWarnings("rawtypes")
    @Override
    default boolean containsInventory(Inventory inventory) {
        Inventory result = Query.compile(this, new LensQueryOperation(((InventoryAdapter) inventory).getRootLens())).execute();
        return result.capacity() == inventory.capacity() && ((InventoryAdapter) result).getRootLens() == ((InventoryAdapter) inventory).getRootLens();
    }

    @Override
    default InventoryArchetype getArchetype() {
        return InventoryArchetypes.UNKNOWN;
    }

}
