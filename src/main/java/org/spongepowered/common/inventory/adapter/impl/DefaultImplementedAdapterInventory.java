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
package org.spongepowered.common.inventory.adapter.impl;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackLike;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.query.Query;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.common.bridge.world.inventory.InventoryBridge;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.property.InventoryDataHolder;
import org.spongepowered.common.inventory.query.SpongeQueryTypes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implements almost all of {@link Inventory} assuming that this is a {@link InventoryBridge} providing the {@link InventoryAdapter}.
 */
public interface DefaultImplementedAdapterInventory extends InventoryDataHolder {

    // Helpers
    default Lens impl$getLens()
    {
        return ((InventoryBridge) this).bridge$getAdapter().inventoryAdapter$getRootLens();
    }

    default Fabric impl$getFabric()
    {
        return ((InventoryBridge) this).bridge$getAdapter().inventoryAdapter$getFabric();
    }

    /**
     * Only use on Inventories that are Fabrics themselves
     */
    interface WithClear extends DefaultImplementedAdapterInventory {

        @Override
        default void clear() {
            ((InventoryBridge) this).bridge$getAdapter().inventoryAdapter$getFabric().fabric$clear();
        }
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
    default InventoryTransactionResult.Poll poll() {
        return AdapterLogic.pollSequential(this.impl$getFabric(), this.impl$getLens(), null);
    }

    @Override
    default InventoryTransactionResult.Poll poll(int limit) {
        return AdapterLogic.pollSequential(this.impl$getFabric(), this.impl$getLens(), limit);
    }

    @Override
    default ItemStack peek() {
        return AdapterLogic.peekSequential(this.impl$getFabric(), this.impl$getLens()).orElse(ItemStack.empty());
    }

    @Override
    default InventoryTransactionResult offer(ItemStackLike... stacks) {
        InventoryTransactionResult result = InventoryTransactionResult.successNoTransactions();
        for (ItemStackLike stack : stacks) {
            result = result.and(AdapterLogic.appendSequential(this.impl$getFabric(), this.impl$getLens(), stack.asMutable()));
        }
        return result;
    }

    @Override
    default boolean canFit(ItemStackLike stack) {
        return AdapterLogic.canFit(this.impl$getFabric(), this.impl$getLens(), stack.asMutable());
    }

    @Override
    default int freeCapacity() {
        return AdapterLogic.countFreeCapacity(this.impl$getFabric(), this.impl$getLens());
    }

    @Override
    default int totalQuantity() {
        return AdapterLogic.countQuantity(this.impl$getFabric(), this.impl$getLens());
    }

    @Override
    default int capacity() {
        return AdapterLogic.getCapacity(this.impl$getFabric(), this.impl$getLens());
    }

    @Override
    default boolean hasChildren() {
        return this.impl$getLens().getChildren().size() != 0;
    }

    @Override
    default boolean contains(ItemStackLike stack) {
        return AdapterLogic.contains(((InventoryBridge) this).bridge$getAdapter(), stack.asMutable());
    }

    @Override
    default boolean containsAny(ItemStackLike stack) {
        return AdapterLogic.contains(((InventoryBridge) this).bridge$getAdapter(), stack.asMutable(), 1);
    }

    @Override
    default boolean contains(ItemType type) {
        return AdapterLogic.contains(((InventoryBridge) this).bridge$getAdapter(), type);
    }

    default List<Inventory> children() {
        return this.impl$generateChildren();
    }

    default List<Inventory> impl$generateChildren() {
        return this.impl$getLens().getSpanningChildren().stream()
                .map(l -> l.getAdapter(this.impl$getFabric(), this))
                .map(Inventory.class::cast)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends Inventory> Optional<T> query(Class<T> inventoryType) {
        Inventory result = this.query(QueryTypes.INVENTORY_TYPE.get().of(inventoryType));
        if (inventoryType.isAssignableFrom(result.getClass())) {
            return Optional.of((T) result);
        }
        return Optional.empty();
    }

    @Override
    default Inventory intersect(Inventory inventory) {
        return SpongeQueryTypes.SLOT_LENS.get().of(ImmutableSet.of(inventory)).execute(this);
    }

    @Override
    default Inventory union(Inventory inventory) {
        return this.query(SpongeQueryTypes.UNION.get().of(inventory));
    }

    @Override
    default boolean containsInventory(Inventory inventory) {
        Inventory result = SpongeQueryTypes.LENS.get().of(((InventoryBridge) inventory).bridge$getAdapter().inventoryAdapter$getRootLens()).execute(this);
        return result.capacity() == inventory.capacity() && ((InventoryAdapter) result).inventoryAdapter$getRootLens() == ((InventoryAdapter) inventory).inventoryAdapter$getRootLens();
    }

    @Override
    default boolean containsChild(Inventory child) {
        return this.impl$getLens().getSpanningChildren().contains(((InventoryBridge) child).bridge$getAdapter().inventoryAdapter$getRootLens());
    }

    @Override
    default Optional<Slot> slot(int index) {
        return ((InventoryBridge) this).bridge$getAdapter().inventoryAdapter$getSlot(index);
    }

    @Override
    default InventoryTransactionResult.Poll pollFrom(int index) {
        return AdapterLogic.pollSequential(this.impl$getFabric(), this.impl$getLens().getSlotLens(this.impl$getFabric(), index), null);
    }

    @Override
    default InventoryTransactionResult.Poll pollFrom(int index, int limit) {
        return AdapterLogic.pollSequential(this.impl$getFabric(), this.impl$getLens().getSlotLens(this.impl$getFabric(), index), limit);
    }

    @Override
    default Optional<ItemStack> peekAt(int index) {
        return AdapterLogic.peekSequential(this.impl$getFabric(), this.impl$getLens().getSlotLens(this.impl$getFabric(), index));
    }

    @Override
    default InventoryTransactionResult set(int index, ItemStackLike stack) {
        return AdapterLogic.insertSequential(this.impl$getFabric(), this.impl$getLens().getSlotLens(this.impl$getFabric(), index), stack.asMutable());
    }

    @Override
    default InventoryTransactionResult offer(int index, ItemStackLike stack) {
        return AdapterLogic.appendSequential(this.impl$getFabric(), this.impl$getLens().getSlotLens(this.impl$getFabric(), index), stack.asMutable());
    }

    @Override
    default Optional<ViewableInventory> asViewable() {
        if (this instanceof ViewableInventory) {
            return Optional.of(((ViewableInventory) this));
        }
        // TODO Mod-Support
        return Optional.empty();
    }

    @Override
    default Inventory query(Query query) {
        return query.execute(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    default List<Slot> slots() {
        // TODO caching and not an iterator
        return SlotCollection.of(this, ((InventoryBridge) this).bridge$getAdapter()).slots();
    }

}
