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

import static org.spongepowered.api.data.Property.Operator.DELEGATE;

import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.DefaultIndexedLens;
import org.spongepowered.common.item.inventory.lens.impl.RealLens;
import org.spongepowered.common.item.inventory.lens.impl.slots.FuelSlotLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.slots.InputSlotLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.slots.OutputSlotLensImpl;
import org.spongepowered.common.item.inventory.property.SlotIndexImpl;

public class FurnaceInventoryLens extends RealLens {

    private InputSlotLensImpl input;
    private FuelSlotLensImpl fuel;
    private OutputSlotLensImpl output;

    public FurnaceInventoryLens(InventoryAdapter adapter, SlotProvider slots) {
        this(0, adapter, slots);
    }

    public FurnaceInventoryLens(int base, InventoryAdapter adapter, SlotProvider slots) {
        super(base, adapter.getFabric().getSize(), adapter.getClass());
        this.init(slots);
    }

    protected void init(SlotProvider slots) {
        this.addChild(new DefaultIndexedLens(0, 3, slots));

        this.input = new InputSlotLensImpl(0, (i) -> true, (i) -> true);
        this.fuel = new FuelSlotLensImpl(1, (i) -> true, (i) -> true);       // TODO SlotFurnaceFuel
        this.output = new OutputSlotLensImpl(2, (i) -> false, (i) -> false); // SlotFurnaceOutput

        this.addSpanningChild(this.input, new SlotIndexImpl(0, DELEGATE));
        this.addSpanningChild(this.fuel, new SlotIndexImpl(1, DELEGATE));
        this.addSpanningChild(this.output, new SlotIndexImpl(2, DELEGATE));
    }
}
