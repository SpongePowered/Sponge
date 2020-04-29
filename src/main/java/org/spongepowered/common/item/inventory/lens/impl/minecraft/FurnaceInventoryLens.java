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
package org.spongepowered.common.item.inventory.lens.impl.minecraft;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.RealLens;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.slots.FuelSlotLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.slots.InputSlotLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.slots.OutputSlotLensImpl;

public class FurnaceInventoryLens extends RealLens {

    private InputSlotLensImpl input;
    private FuelSlotLensImpl fuel;
    private OutputSlotLensImpl output;

    public FurnaceInventoryLens(final InventoryAdapter adapter, final SlotProvider slots) {
        this(0, adapter, slots);
    }

    @SuppressWarnings("unchecked")
    public FurnaceInventoryLens(final int base, final InventoryAdapter adapter, final SlotProvider slots) {
        super(base, adapter.bridge$getFabric().fabric$getSize(), (Class<? extends Inventory>) adapter.getClass());
        this.init(slots);
    }

    protected void init(final SlotProvider slots) {
        this.addChild(new OrderedInventoryLensImpl(0, 3, slots));

        this.input = new InputSlotLensImpl(0, (i) -> true, (i) -> true);
        this.fuel = new FuelSlotLensImpl(1, (i) -> true, (i) -> true);       // TODO SlotFurnaceFuel

        // TODO represent the filtering in the API somehow
        this.output = new OutputSlotLensImpl(2, (i) -> true, (i) -> true); // SlotFurnaceOutput

        this.addSpanningChild(this.input, new SlotIndex(0));
        this.addSpanningChild(this.fuel, new SlotIndex(1));
        this.addSpanningChild(this.output, new SlotIndex(2));
    }
}
