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

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.inventory.adapter.impl.BasicInventoryAdapter;
import org.spongepowered.common.inventory.lens.impl.RealLens;
import org.spongepowered.common.inventory.lens.impl.slot.FilteringSlotLens.ItemStackFilter;
import org.spongepowered.common.inventory.lens.impl.slot.FuelSlotLens;
import org.spongepowered.common.inventory.lens.impl.slot.InputSlotLens;
import org.spongepowered.common.inventory.lens.impl.slot.PotionSlotLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;

public class BrewingStandInventoryLens extends RealLens {

    private InputSlotLens ingredient;
    private InputSlotLens fuel;

    public BrewingStandInventoryLens(SlotLensProvider slots) {
        this(5, BasicInventoryAdapter.class, slots);
    }

    @SuppressWarnings("unchecked")
    public BrewingStandInventoryLens(int size, Class<? extends Inventory> clazz, final SlotLensProvider slots) {
        super(0, size, clazz);
        this.init(slots);
    }

    protected void init(final SlotLensProvider slots) {
        for (int i = 0; i <= 2; i++) {
            this.addSpanningChild(new PotionSlotLens(slots.getSlotLens(i), ItemStackFilter.filterIInventory(i)));
        }

        this.ingredient = new InputSlotLens(slots.getSlotLens(3), ItemStackFilter.filterIInventory(3));
        this.fuel = new FuelSlotLens(slots.getSlotLens(4), ItemStackFilter.filterIInventory(4));

        this.addSpanningChild(this.ingredient);
        this.addSpanningChild(this.fuel);
    }
}
