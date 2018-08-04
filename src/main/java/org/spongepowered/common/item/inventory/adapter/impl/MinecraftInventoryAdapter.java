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
import org.spongepowered.api.data.Property;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperation;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.property.store.common.InventoryPropertyStore;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.CompoundSlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.CompoundLens;
import org.spongepowered.common.item.inventory.lens.impl.fabric.CompoundFabric;
import org.spongepowered.common.item.inventory.lens.impl.fabric.MinecraftFabric;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;
import org.spongepowered.common.item.inventory.property.AbstractInventoryProperty;
import org.spongepowered.common.item.inventory.query.Query;
import org.spongepowered.common.item.inventory.query.operation.LensQueryOperation;
import org.spongepowered.common.item.inventory.query.operation.SlotLensQueryOperation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface MinecraftInventoryAdapter extends InventoryAdapter {

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
    default ItemStack poll() {
        return AdapterLogic.pollSequential(this).orElse(ItemStack.empty());
    }

    @Override
    default ItemStack poll(int limit) {
        return AdapterLogic.pollSequential(this, limit).orElse(ItemStack.empty());
    }

    @Override
    default ItemStack peek() {
        return AdapterLogic.peekSequential(this).orElse(ItemStack.empty());
    }

    @Override
    default ItemStack peek(int limit) {
        return AdapterLogic.peekSequential(this, limit).orElse(ItemStack.empty());
    }

    @Override
    default InventoryTransactionResult offer(ItemStack stack) {
        return AdapterLogic.appendSequential(this, stack);
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
        return (Collection<T>) InventoryPropertyStore.getProperties(this, child, property);
    }

    @Override
    default <T extends InventoryProperty<?, ?>> Collection<T> getProperties(Class<T> property) {
        if (this.parent() == this) {
            return InventoryPropertyStore.getRootProperties(this, property);
        }
        return this.parent().getProperties(this, property);
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends InventoryProperty<?, ?>> Optional<T> getProperty(Inventory child, Class<T> property, Object key) {
        for (InventoryProperty<?, ?> prop : InventoryPropertyStore.getProperties(this, child, property)) {
            if (key.equals(prop.getKey())) {
                return Optional.of((T)prop);
            }
        }
        return Optional.empty();
    }

    @Override
    default <T extends InventoryProperty<?, ?>> Optional<T> getProperty(Class<T> property, Object key) {
        if (this.parent() == this) {
            return InventoryPropertyStore.getRootProperty(this, property, key);
        }
        return this.parent().getProperty(this, property, key);
    }

    default <T extends InventoryProperty<?, ?>> Optional<T> getProperty(Inventory child, Class<T> property) {
        Object key = AbstractInventoryProperty.getDefaultKey(property);
        return this.getProperty(child, property, key);
    }

    @Override
    default <T extends Property<?, ?>> Optional<T> getProperty(Class<T> property) {
        return SpongeImpl.getPropertyRegistry().getStore(property).flatMap(p -> p.getFor(this));
    }

    @Override
    default Collection<Property<?, ?>> getApplicableProperties() {
        return SpongeImpl.getPropertyRegistry().getPropertiesFor(this);
    }

    @Override
    default List<Inventory> children() {
        return this.getRootLens().getSpanningChildren().stream()
                .map(l -> l.getAdapter(this.getFabric(), this))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    default Inventory query(QueryOperation<?>... queries) {
        return Query.compile(this, queries).execute();
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends Inventory> Optional<T> query(Class<T> inventoryType) {
        Inventory result = this.query(QueryOperationTypes.INVENTORY_TYPE.of(inventoryType));
        if (inventoryType.isAssignableFrom(result.getClass())) {
            return Optional.of((T) result);
        }
        return Optional.empty();
    }

    @Override
    default Inventory intersect(Inventory inventory) {
        return Query.compile(this, new SlotLensQueryOperation(ImmutableSet.of(inventory))).execute();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    default Inventory union(Inventory inventory) {
        CompoundLens.Builder lensBuilder = CompoundLens.builder().add(getRootLens());
        CompoundFabric fabric = new CompoundFabric(this.getFabric(), ((InventoryAdapter) inventory).getFabric());
        CompoundSlotProvider provider = new CompoundSlotProvider().add(this);
        for (Object inv : inventory.children()) {
            lensBuilder.add(((InventoryAdapter) inv).getRootLens());
            provider.add((InventoryAdapter) inv);
        }
        CompoundLens lens = lensBuilder.build(provider);
        InventoryAdapter compoundAdapter = lens.getAdapter(fabric, this);

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

    @Override
    default Optional<ViewableInventory> asViewable() {
        if (this instanceof ViewableInventory) {
            return Optional.of(((ViewableInventory) this));
        }
        // TODO Mod-Support
        return Optional.empty();
    }

    default SlotLens getSlotLens(SlotIndex index) {
        int idx = index.getValue().intValue();
        return this.getRootLens().getSlot(idx);
    }

    @Override
    default Optional<Slot> getSlot(SlotIndex index) {
        return VanillaAdapter.forSlot(this.getFabric(), this.getSlotLens(index), this);
    }

    @Override
    default Optional<org.spongepowered.api.item.inventory.ItemStack> poll(SlotIndex index) {
        return AdapterLogic.pollSequential(this.getFabric(), this.getSlotLens(index));
    }

    @Override
    default Optional<org.spongepowered.api.item.inventory.ItemStack> poll(SlotIndex index, int limit) {
        return AdapterLogic.pollSequential(this.getFabric(), this.getSlotLens(index), limit);
    }

    @Override
    default Optional<org.spongepowered.api.item.inventory.ItemStack> peek(SlotIndex index) {
        return AdapterLogic.peekSequential(this.getFabric(), this.getSlotLens(index));
    }

    @Override
    default Optional<org.spongepowered.api.item.inventory.ItemStack> peek(SlotIndex index, int limit) {
        return AdapterLogic.peekSequential(this.getFabric(), this.getSlotLens(index), limit);
    }

    @Override
    default InventoryTransactionResult set(SlotIndex index, org.spongepowered.api.item.inventory.ItemStack stack) {
        return AdapterLogic.insertSequential(this.getFabric(), this.getSlotLens(index), stack);
    }

    @Override
    default InventoryTransactionResult offer(SlotIndex index, org.spongepowered.api.item.inventory.ItemStack stack) {
        return AdapterLogic.appendSequential(this.getFabric(), this.getSlotLens(index), stack);
    }
}
