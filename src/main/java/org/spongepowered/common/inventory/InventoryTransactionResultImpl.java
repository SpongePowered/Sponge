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
package org.spongepowered.common.inventory;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.inventory.ItemStackLike;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.util.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class InventoryTransactionResultImpl implements InventoryTransactionResult, InventoryTransactionResult.Poll {

    private final List<SlotTransaction> slotTransactions;
    private final List<ItemStackSnapshot> rejected;
    private final List<ItemStackSnapshot> polled;

    private final Type type;

    InventoryTransactionResultImpl(org.spongepowered.common.inventory.InventoryTransactionResultImpl.Builder builder) {
        this.type = Objects.requireNonNull(builder.resultType, "Result type");
        this.rejected = builder.rejected != null ? ImmutableList.copyOf(builder.rejected) : Collections.emptyList();
        this.slotTransactions = builder.slotTransactions != null ? ImmutableList.copyOf(builder.slotTransactions) : Collections.emptyList();
        this.polled = builder.polled != null ? ImmutableList.copyOf(builder.polled) : Collections.emptyList();
    }

    @Override
    public InventoryTransactionResult and(InventoryTransactionResult other) {
        Type resultType = Type.SUCCESS;
        if (this.type == Type.ERROR || other.type() == Type.ERROR) {
            resultType = Type.ERROR;
        }
        if (this.type == Type.FAILURE || other.type() == Type.FAILURE) {
            resultType = Type.FAILURE;
        }
        InventoryTransactionResult.Builder builder =
                InventoryTransactionResult.builder().type(resultType).reject(this.rejected).reject(other.rejectedItems())
                        .transaction(this.slotTransactions).transaction(other.slotTransactions());
        this.polled.forEach(builder::poll);
        return builder.build();
    }

    @Override
    public void revert() {
        for (SlotTransaction transaction : Lists.reverse(this.slotTransactions)) {
            transaction.slot().set(transaction.original());
        }
    }

    @Override
    public boolean revertOnFailure() {
        if (this.type == Type.FAILURE) {
            this.revert();
            return true;
        }
        return false;
    }

    @Override
    public Type type() {
        return this.type;
    }

    @Override
    public List<ItemStackSnapshot> rejectedItems() {
        return this.rejected;
    }

    @Override
    public List<SlotTransaction> slotTransactions() {
        return this.slotTransactions;
    }

    @Override
    public List<ItemStackSnapshot> polledItems() {
        return this.polled;
    }

    @Override
    public ItemStackSnapshot polledItem() {
        return this.polled.get(0);
    }

    public static class Builder implements InventoryTransactionResult.Builder, InventoryTransactionResult.Builder.PollBuilder {

        InventoryTransactionResult.@Nullable Type resultType;
        @Nullable List<ItemStackSnapshot> rejected;
        @Nullable List<SlotTransaction> slotTransactions;
        @Nullable List<ItemStackSnapshot> polled;

        @Override
        public InventoryTransactionResult.Builder type(final InventoryTransactionResult.Type type) {
            this.resultType = Objects.requireNonNull(type, "Type cannot be null!");
            return this;
        }

        @Override
        public PollBuilder poll(ItemStackLike itemStack) {
            if (this.polled == null) {
                this.polled = new ArrayList<>();
            }
            this.polled.add(itemStack.asImmutable());
            return this;
        }

        @Override
        public InventoryTransactionResult.Builder reject(ItemStackLike... itemStacks) {
            if (this.rejected == null) {
                this.rejected = new ArrayList<>();
            }
            for (ItemStackLike itemStack1 : itemStacks) {
                if (!itemStack1.isEmpty()) {
                    this.rejected.add(itemStack1.asImmutable());
                }
            }
            return this;
        }

        @Override
        public InventoryTransactionResult.Builder reject(Iterable<? extends ItemStackLike> itemStacks) {
            if (this.rejected == null) {
                this.rejected = new ArrayList<>();
            }
            for (ItemStackLike itemStack1 : itemStacks) {
                if (!itemStack1.isEmpty()) {
                    this.rejected.add(itemStack1.asImmutable());
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
        public InventoryTransactionResult.Poll build() {
            Preconditions.checkState(this.resultType != null, "ResultType cannot be null!");
            return new InventoryTransactionResultImpl(this);
        }

        @Override
        public InventoryTransactionResult.Builder from(InventoryTransactionResult value) {
            Objects.requireNonNull(value, "InventoryTransactionResult cannot be null!");
            this.resultType = Objects.requireNonNull(value.type(), "ResultType cannot be null!");
            this.slotTransactions = new ArrayList<>(value.slotTransactions());
            this.rejected = new ArrayList<>(value.rejectedItems());
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
