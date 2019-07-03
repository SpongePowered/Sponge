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
package org.spongepowered.common.item.inventory.lens.impl.collections;

import static com.google.common.base.Preconditions.*;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.SlotCollectionIterator;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.slots.SlotLensImpl;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class SlotCollection extends DynamicLensCollectionImpl implements SlotProvider {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static class Builder {

        private List<Tuple<Class<? extends SlotAdapter>, SlotLensProvider>> slotTypes = new ArrayList<>();
        private final SlotLensProvider defaultProvider = (i) -> new SlotLensImpl(i, this.slotTypes.get(i).getFirst());

        public Builder add() {
            return this.add(SlotAdapter.class);
        }

        public Builder add(Class<? extends SlotAdapter> type) {
            return this.add(type, this.defaultProvider);
        }

        public Builder add(Class<? extends SlotAdapter> type, SlotLensProvider provider) {
            this.slotTypes.add(Tuple.of(checkNotNull(type), provider));
            return this;
        }

        public Builder add(int count) {
            return this.add(count, SlotAdapter.class, this.defaultProvider);
        }

        public Builder add(int count, Class<? extends SlotAdapter> type) {
            return this.add(count, type, this.defaultProvider);
        }

        public Builder add(int count, Class<? extends SlotAdapter> type, SlotLensProvider provider) {
            checkNotNull(type);
            for (int i = 0; i < count; i++) {
                this.add(type, provider);
            }

            return this;
        }

        public int size() {
            return this.slotTypes.size();
        }

        SlotLensProvider getProvider(int index) {
            return this.slotTypes.get(index).getSecond();
        }

        public SlotCollection build() {
            return new SlotCollection(this.size(), this);
        }

    }

    private Builder builder;

    public SlotCollection(int size) {
        this(size, null);
    }

    private SlotCollection(int size, Builder builder) {
        super(size);
        this.builder = builder;
        this.populate();
    }

    private void populate() {
        for (int index = 0; index < this.size(); index++) {
            this.lenses[index] = this.createSlotLens(index);
        }
    }

    @SuppressWarnings("rawtypes")
    private SlotLens createSlotLens(int slotIndex) {
        return this.builder == null ? new SlotLensImpl(slotIndex, SlotAdapter.class) : this.builder.getProvider(slotIndex).createSlotLens(slotIndex);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public SlotLens getSlot(int index) {
        return (SlotLens) this.get(index);
    }
}
