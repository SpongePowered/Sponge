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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

public class InventoryTransactionResultImpl implements InventoryTransactionResult {

    private final List<SlotTransaction> slotTransactions;
    private final List<ItemStackSnapshot> rejected;

    private final Type type;

    InventoryTransactionResultImpl(Builder builder) {
        this.type = checkNotNull(builder.resultType, "Result type");
        this.rejected = builder.rejected != null ? ImmutableList.copyOf(builder.rejected) : Collections.emptyList();
        this.slotTransactions = builder.slotTransactions != null ? ImmutableList.copyOf(builder.slotTransactions) : Collections.emptyList();
    }

    @Override
    public InventoryTransactionResult and(InventoryTransactionResult other) {
        Type resultType = Type.SUCCESS;
        if (this.type == Type.ERROR || other.getType() == Type.ERROR) {
            resultType = Type.ERROR;
        }
        if (this.type == Type.FAILURE || other.getType() == Type.FAILURE) {
            resultType = Type.FAILURE;
        }
        return InventoryTransactionResult.builder().type(resultType).reject(this.rejected).reject(other.getRejectedItems())
                .transaction(this.slotTransactions).transaction(other.getSlotTransactions())
                .build();
    }

    @Override
    public void revert() {
        for (SlotTransaction transaction : Lists.reverse(this.slotTransactions)) {
            transaction.getSlot().set(transaction.getOriginal().createStack());
        }
    }

    @Override
    public void revertOnFailure() {
        if (this.type == Type.FAILURE) {
            this.revert();
        }
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public List<ItemStackSnapshot> getRejectedItems() {
        return this.rejected;
    }

    @Override
    public List<SlotTransaction> getSlotTransactions() {
        return this.slotTransactions;
    }

    public static class Builder implements InventoryTransactionResult.Builder {

        @Nullable InventoryTransactionResult.Type resultType;
        @Nullable List<ItemStackSnapshot> rejected;
        @Nullable List<SlotTransaction> slotTransactions;

        @Override
        public InventoryTransactionResult.Builder type(final InventoryTransactionResult.Type type) {
            this.resultType = checkNotNull(type, "Type cannot be null!");
            return this;
        }

        @Override
        public InventoryTransactionResult.Builder reject(ItemStack... itemStacks) {
            if (this.rejected == null) {
                this.rejected = new ArrayList<>();
            }
            for (ItemStack itemStack1 : itemStacks) {
                if (!itemStack1.isEmpty()) {
                    this.rejected.add(itemStack1.createSnapshot());
                }
            }
            return this;
        }

        @Override
        public InventoryTransactionResult.Builder reject(Iterable<ItemStackSnapshot> itemStacks) {
            if (this.rejected == null) {
                this.rejected = new ArrayList<>();
            }
            for (ItemStackSnapshot itemStack1 : itemStacks) {
                if (!itemStack1.isEmpty()) {
                    this.rejected.add(itemStack1);
                }
            }
            return this;
        }

        @Override
        public InventoryTransactionResult.Builder transaction(SlotTransaction... slotTransactions) {
            return this.transaction(Arrays.asList(slotTransactions));
        }

        @Override
        public InventoryTransactionResult.Builder transaction(Iterable<SlotTransaction> slotTransactions) {
            if (this.slotTransactions == null) {
                this.slotTransactions = new ArrayList<>();
            }
            for (SlotTransaction transaction : slotTransactions) {
                this.slotTransactions.add(transaction);
            }
            return this;
        }

        @Override
        public InventoryTransactionResult build() {
            checkState(this.resultType != null, "ResultType cannot be null!");
            return new InventoryTransactionResultImpl(this);
        }

        @Override
        public InventoryTransactionResult.Builder from(InventoryTransactionResult value) {
            checkNotNull(value, "InventoryTransactionResult cannot be null!");
            this.resultType = checkNotNull(value.getType(), "ResultType cannot be null!");
            this.slotTransactions = new ArrayList<>(value.getSlotTransactions());
            this.rejected = new ArrayList<>(value.getRejectedItems());
            return this;
        }

        @Override
        public InventoryTransactionResult.Builder reset() {
            this.resultType = null;
            this.rejected = null;
            this.slotTransactions = null;
            return this;
        }

    }


}
