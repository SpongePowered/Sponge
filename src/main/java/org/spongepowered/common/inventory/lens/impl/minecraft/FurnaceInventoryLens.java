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
package org.spongepowered.common.inventory.lens.impl.minecraft;

import org.spongepowered.common.inventory.adapter.impl.BasicInventoryAdapter;
import org.spongepowered.common.inventory.lens.impl.DefaultIndexedLens;
import org.spongepowered.common.inventory.lens.impl.RealLens;
import org.spongepowered.common.inventory.lens.impl.slot.FilteringSlotLens.ItemStackFilter;
import org.spongepowered.common.inventory.lens.impl.slot.FuelSlotLens;
import org.spongepowered.common.inventory.lens.impl.slot.InputSlotLens;
import org.spongepowered.common.inventory.lens.impl.slot.OutputSlotLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.property.KeyValuePair;

public class FurnaceInventoryLens extends RealLens {

    private InputSlotLens input;
    private FuelSlotLens fuel;
    private OutputSlotLens output;

    public FurnaceInventoryLens(SlotLensProvider sp) {
        super(0, 3, BasicInventoryAdapter.class);
        this.init(sp);
    }

    public FurnaceInventoryLens(int size, Class clazz, SlotLensProvider slotLensProvider) {
        super(0, size, clazz);
        this.init(slotLensProvider);
    }

    protected void init(final SlotLensProvider slots) {
        this.addChild(new DefaultIndexedLens(0, 3, slots));

        this.input = new InputSlotLens(slots.getSlotLens(0), ItemStackFilter.filterIInventory(0));
        this.fuel = new FuelSlotLens(slots.getSlotLens(1), ItemStackFilter.filterIInventory(1));
        this.output = new OutputSlotLens(slots.getSlotLens(2), ItemStackFilter.filterIInventory(2));

        this.addSpanningChild(this.input, KeyValuePair.slotIndex(0));
        this.addSpanningChild(this.fuel, KeyValuePair.slotIndex(1));
        this.addSpanningChild(this.output, KeyValuePair.slotIndex(2));
    }

}
