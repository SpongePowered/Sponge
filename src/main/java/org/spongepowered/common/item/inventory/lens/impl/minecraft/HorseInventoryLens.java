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

import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.DefaultIndexedLens;
import org.spongepowered.common.item.inventory.lens.impl.RealLens;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;

public class HorseInventoryLens extends RealLens {

    private DefaultIndexedLens horseEquipment;
    private GridInventoryLensImpl chest;

    public HorseInventoryLens(InventoryAdapter adapter, SlotProvider slots) {
        super(0, 2
                //+ 5*3  // TODO chested horse
                ,adapter.getClass());
        this.init(slots);
    }

    protected void init(SlotProvider slots) {
        this.horseEquipment = new DefaultIndexedLens(0, 2, slots); // 0-1

        this.addSpanningChild(this.horseEquipment);
        this.addMissingSpanningSlots(2, slots);

        // TODO only for "chested" horses. Will this cause problems?
        //this.chest = new GridInventoryLensImpl(2, 5, 3, 5, slots);             // 2-16
        //this.addSpanningChild(this.chest);
    }
}
