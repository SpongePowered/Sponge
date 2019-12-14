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
package org.spongepowered.common.inventory.lens.impl.slot;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.inventory.lens.slots.SlotLens;

import java.util.ArrayList;
import java.util.List;

public class SlotLensCollection implements SlotLensProvider {

    @SuppressWarnings({"rawtypes"})
    public static class Builder {

        private List<Tuple<Class<? extends SlotAdapter>, SlotLensProvider>> slotTypes = new ArrayList<>();
        private final SlotLensProvider defaultProvider = (i) -> new BasicSlotLens(i, this.slotTypes.get(i).getFirst());

        public SlotLensCollection.Builder add() {
            return this.add(SlotAdapter.class);
        }

        public SlotLensCollection.Builder add(Class<? extends SlotAdapter> type) {
            return this.add(type, this.defaultProvider);
        }

        public SlotLensCollection.Builder add(Class<? extends SlotAdapter> type, SlotLensProvider provider) {
            this.slotTypes.add(Tuple.of(checkNotNull(type), provider));
            return this;
        }

        public SlotLensCollection.Builder add(int count) {
            return this.add(count, SlotAdapter.class, this.defaultProvider);
        }

        public SlotLensCollection.Builder add(int count, Class<? extends SlotAdapter> type) {
            return this.add(count, type, this.defaultProvider);
        }

        public SlotLensCollection.Builder add(int count, Class<? extends SlotAdapter> type, SlotLensProvider provider) {
            checkNotNull(type);
            for (int i = 0; i < count; i++) {
                this.add(type, provider);
            }

            return this;
        }

        public SlotLensCollection build() {
            final List<SlotLens> lenses = new ArrayList<>();

            for (int i = 0; i < this.slotTypes.size(); i++) {
                lenses.add(this.slotTypes.get(i).getSecond().getSlotLens(i));
            }

            return new SlotLensCollection(lenses);
        }

        public int size() {
            return this.slotTypes.size();
        }
    }

    private final List<SlotLens> lenses;

    public SlotLensCollection(List<SlotLens> lenses) {
        this.lenses = lenses;
    }

    @Override
    public SlotLens getSlotLens(int index) {
        return this.lenses.get(index);
    }

}
