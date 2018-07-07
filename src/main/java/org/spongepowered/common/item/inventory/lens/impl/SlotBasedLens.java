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
package org.spongepowered.common.item.inventory.lens.impl;

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.item.inventory.adapter.impl.BasicInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.property.SlotIndexImpl;

/**
 * Lenses for inventory based on slots.
 *
 * <p>This lenses will usually return a new matching adapter (usually extending {@link BasicInventoryAdapter})</p>
 */
@SuppressWarnings("rawtypes")
public abstract class SlotBasedLens extends AbstractLens {

    protected final int stride;

    public SlotBasedLens(int base, int size, int stride, Class<? extends Inventory> adapterType, SlotProvider slots) {
        super(base, size, adapterType);
        checkArgument(stride > 0, "Invalid stride: %s", stride);
        this.stride = stride;
        this.init(slots);
    }

    private void init(SlotProvider slots) {
        for (int ord = 0, slot = this.base; ord < this.size; ord++, slot += this.stride) {
            this.addSpanningChild(slots.getSlotLens(slot), new SlotIndexImpl(ord));
        }
    }




}
