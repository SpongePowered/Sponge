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

import net.minecraft.tileentity.ChestTileEntity;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.OrderedInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.RealLens;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.slots.SlotLensImpl;
import org.spongepowered.common.mixin.core.inventory.InventoryLargeChestAccessor;

/**
 * This class is only used as an adapter when explicitly requested from the API, trough
 * {@link Chest#getDoubleChestInventory()}
 */
public class LargeChestInventoryLens extends RealLens {

    private int upperChest;
    private int lowerChest;

    public LargeChestInventoryLens(final InventoryAdapter adapter, final SlotProvider slots) {
        super(0, adapter.bridge$getFabric().fabric$getSize(), OrderedInventoryAdapter.class);
        final InventoryLargeChestAccessor inventory = (InventoryLargeChestAccessor) adapter;
        this.upperChest = inventory.accessor$getUpperChest().func_70302_i_();
        this.lowerChest = inventory.accessor$getLowerChest().func_70302_i_();
        this.initLargeChest(slots);
    }

    public LargeChestInventoryLens(final int base, final InventoryAdapter adapter, final SlotProvider slots) {
        super(base, adapter.bridge$getFabric().fabric$getSize(), OrderedInventoryAdapter.class);
        final InventoryLargeChestAccessor inventory = (InventoryLargeChestAccessor) adapter.bridge$getFabric().fabric$get(0);
        this.upperChest = inventory.accessor$getUpperChest().func_70302_i_();
        this.lowerChest = inventory.accessor$getLowerChest().func_70302_i_();
        this.initLargeChest(slots);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void initLargeChest(final SlotProvider slots) {
        // add grids
        int base = 0;
        this.addSpanningChild(new GridInventoryLensImpl(base, 9, this.upperChest / 9, 9, (Class) ChestTileEntity.class, slots));
        base += this.upperChest;
        this.addSpanningChild(new GridInventoryLensImpl(base, 9, this.lowerChest / 9, 9, (Class) ChestTileEntity.class, slots));
        base += this.lowerChest;

        this.addChild(new GridInventoryLensImpl(0, 9, (this.upperChest + this.lowerChest) / 9, 9, slots));

        // add slot childs for grids
        for (int ord = 0, slot = this.base; ord < base; ord++, slot ++) {
            this.addChild(slots.getSlot(slot), new SlotIndex(ord));
        }

        // handle possible extension by mods:
        for (int i = base; i < this.slotCount(); i++) {
            this.addSpanningChild(new SlotLensImpl(i), SlotIndex.of(i));
        }
    }
}
