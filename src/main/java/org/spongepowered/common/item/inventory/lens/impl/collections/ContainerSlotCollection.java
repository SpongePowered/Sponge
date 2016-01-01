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

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;

import java.util.ArrayList;
import java.util.List;

public class ContainerSlotCollection extends SlotCollection {
    
    public class Builder {
        
        private List<Class<? extends SlotAdapter>> slotTypes = new ArrayList<Class<? extends SlotAdapter>>();
        
        public Builder add() {
            return this.add(SlotAdapter.class);
        }
        
        public Builder add(Class<? extends SlotAdapter> type) {
            this.slotTypes.add(checkNotNull(type));
            return this;
        }
        
        public Builder add(int count) {
            return this.add(count, SlotAdapter.class);
        }
        
        public Builder add(int count, Class<? extends SlotAdapter> type) {
            checkNotNull(type);
            for (int i = 0; i < count; i++) {
                this.slotTypes.add(type);
            }
            
            return this;
        }
        
        public int size() {
            return this.slotTypes.size();
        }
        
        Class<? extends Inventory> getType(int index) {
            return this.slotTypes.get(index);
        }
        
        public ContainerSlotCollection build() {
            return new ContainerSlotCollection(this);
        }

    }
    
    private final Builder builder;

    ContainerSlotCollection(Builder builder) {
        super(builder.size());
        this.builder = builder;
    }
    
    @Override
    protected Class<? extends Inventory> getSlotAdapterType(int slotIndex) {
        return this.builder.getType(slotIndex);
    }

}
