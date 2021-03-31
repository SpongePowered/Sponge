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

import org.spongepowered.api.block.entity.carrier.chest.Chest;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.inventory.lens.impl.RealLens;
import org.spongepowered.common.inventory.lens.impl.comp.GridInventoryLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;

/**
 * This class is only used as an adapter when explicitly requested from the API, trough
 * {@link Chest#doubleChestInventory()}
 */
public class LargeChestInventoryLens extends RealLens {

    private int upperChest;
    private int lowerChest;

    public LargeChestInventoryLens(int size, Class<? extends Inventory> clazz, SlotLensProvider slots) {
        super(0, size, clazz);
        this.upperChest = size / 2;
        this.lowerChest = size / 2;
        this.init(slots);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void init(final SlotLensProvider slots) {
        // add grids
        int base = 0;
        this.addSpanningChild(new ChestPartLens(base, 9, this.upperChest / 9, slots, true));
        base += this.upperChest;
        this.addSpanningChild(new ChestPartLens(base, 9, this.lowerChest / 9, slots, false));
        base += this.lowerChest;

        this.addChild(new GridInventoryLens(0, 9, (this.upperChest + this.lowerChest) / 9, slots));

        this.addMissingSpanningSlots(base, slots);
    }
}
