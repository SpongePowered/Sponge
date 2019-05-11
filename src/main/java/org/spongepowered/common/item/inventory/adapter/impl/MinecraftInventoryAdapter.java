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
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.query.Query;
import org.spongepowered.api.item.inventory.query.QueryType;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.UniqueCustomSlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.CompoundLens;
import org.spongepowered.common.item.inventory.lens.impl.fabric.CompoundFabric;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;
import org.spongepowered.common.item.inventory.query.SpongeQueryTypes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public interface MinecraftInventoryAdapter extends InventoryPropertyHolder, InventoryAdapter {

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
    default InventoryTransactionResult poll() {
        return AdapterLogic.pollSequential(this.getFabric(), this.getRootLens(), null);
    }

    @Override
    default InventoryTransactionResult poll(int limit) {
        return AdapterLogic.pollSequential(this.getFabric(), this.getRootLens(), limit);
    }

    @Override
    default ItemStack peek() {
        return AdapterLogic.peekSequential(this.getFabric(), this.getRootLens()).orElse(ItemStack.empty());
    }

    @Override
    default InventoryTransactionResult offer(ItemStack stack) {
        return AdapterLogic.appendSequential(this.getFabric(), this.getRootLens(), stack);
    }

    @Override
    default InventoryTransactionResult offer(ItemStack... stacks) {
        InventoryTransactionResult result = InventoryTransactionResult.successNoTransactions();
        for (ItemStack stack : stacks) {
            result = result.and(AdapterLogic.appendSequential(this.getFabric(), this.getRootLens(), stack));
        }

        return result;
    }

    @Override
    default boolean canFit(ItemStack stack) {
        return AdapterLogic.canFit(this, stack);
    }

    @Override
    default int freeCapacity() {
        return AdapterLogic.countFreeCapacity(this.getFabric(), this.getRootLens());
    }

    @Override
    default int totalQuantity() {
        return AdapterLogic.countQuantity(this.getFabric(), this.getRootLens());
    }

    @Override
    default int capacity() {
        return AdapterLogic.getCapacity(this.getFabric(), this.getRootLens());
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

//    @Override
//    default int getMaxStackSize() {
//        return this.getRootLens().getMaxStackSize(this.getFabric());
//    }

    @Override
    default List<Inventory> children() {
        return this.getRootLens().getSpanningChildren().stream()
                .map(l -> l.getAdapter(this.getFabric(), this))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    default Inventory query(Query query) {
        return query.execute(this);
    }

    @Override
    default <P> Inventory query(QueryType.OneParam<P> type, P param) {
        return type.of(param).execute(this);
    }

    @Override
    default <P1, P2> Inventory query(QueryType.TwoParam<P1, P2> type, P1 param1, P2 param2) {
        return type.of(param1, param2).execute(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends Inventory> Optional<T> query(Class<T> inventoryType) {
        Inventory result = this.query(QueryTypes.INVENTORY_TYPE.of(inventoryType));
        if (inventoryType.isAssignableFrom(result.getClass())) {
            return Optional.of((T) result);
        }
        return Optional.empty();
    }

    @Override
    default Inventory intersect(Inventory inventory) {
        return SpongeQueryTypes.SLOT_LENS.of(ImmutableSet.of(inventory)).execute(this);
    }

    @SuppressWarnings({"rawtypes"})
    @Override
    default Inventory union(Inventory inventory) {
        CompoundLens.Builder lensBuilder = CompoundLens.builder().add(getRootLens());
        CompoundFabric fabric = new CompoundFabric(this.getFabric(), ((InventoryAdapter) inventory).getFabric());
        UniqueCustomSlotProvider provider = new UniqueCustomSlotProvider().add(this);
        for (Object inv : inventory.children()) {
            lensBuilder.add(((InventoryAdapter) inv).getRootLens());
            provider.add((InventoryAdapter) inv);
        }
        CompoundLens lens = lensBuilder.build(provider);

        return lens.getAdapter(fabric, this);
    }

    @SuppressWarnings("rawtypes")
    @Override
    default boolean containsInventory(Inventory inventory) {
        // TODO fix this impl
        Inventory result = SpongeQueryTypes.LENS.of(((InventoryAdapter) inventory).getRootLens()).execute(this);
        return result.capacity() == inventory.capacity() && ((InventoryAdapter) result).getRootLens() == ((InventoryAdapter) inventory).getRootLens();
    }

    @Override
    default Optional<ViewableInventory> asViewable() {
        if (this instanceof ViewableInventory) {
            return Optional.of(((ViewableInventory) this));
        }
        // TODO Mod-Support
        return Optional.empty();
    }

    @Nullable
    default SlotLens getSlotLens(int index) {
        return this.getRootLens().getSlot(index);
    }

    @Override
    default Optional<Slot> getSlot(int index) {
        return BasicInventoryAdapter.forSlot(this.getFabric(), this.getSlotLens(index), this);
    }

    @Override
    default InventoryTransactionResult pollFrom(int index) {
        return AdapterLogic.pollSequential(this.getFabric(), this.getSlotLens(index), null);
    }

    @Override
    default InventoryTransactionResult pollFrom(int index, int limit) {
        return AdapterLogic.pollSequential(this.getFabric(), this.getSlotLens(index), limit);
    }

    @Override
    default Optional<org.spongepowered.api.item.inventory.ItemStack> peekAt(int index) {
        return AdapterLogic.peekSequential(this.getFabric(), this.getSlotLens(index));
    }

    @Override
    default InventoryTransactionResult set(int index, org.spongepowered.api.item.inventory.ItemStack stack) {
        return AdapterLogic.insertSequential(this.getFabric(), this.getSlotLens(index), stack);
    }

    @Override
    default InventoryTransactionResult offer(int index, org.spongepowered.api.item.inventory.ItemStack stack) {
        return AdapterLogic.appendSequential(this.getFabric(), this.getSlotLens(index), stack);
    }
}
