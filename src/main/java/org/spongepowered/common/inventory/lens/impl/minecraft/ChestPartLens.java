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

import net.minecraft.inventory.DoubleSidedInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.tileentity.ChestTileEntity;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.accessor.inventory.DoubleSidedInventoryAccessor;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.impl.RealLens;
import org.spongepowered.common.inventory.lens.impl.comp.GridInventoryLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;

/**
 * A lens for a part of a double chest inventory
 */
public class ChestPartLens extends RealLens {

    private boolean upper;

    @SuppressWarnings("unchecked")
    public ChestPartLens(int base, int width, int height, SlotLensProvider slots, boolean upper) {
        super(base, width * height, (Class<? extends Inventory>) (Class) ChestTileEntity.class);
        this.upper = upper;
        this.addSpanningChild(new GridInventoryLens(base, width, height, slots));
    }

    @Override
    public Inventory getAdapter(Fabric fabric, Inventory parent) {
        if (fabric instanceof ChestContainer) {
            fabric = fabric.fabric$get(this.base).bridge$getAdapter().inventoryAdapter$getFabric();
        }
        if (fabric instanceof DoubleSidedInventory) {
            if (this.upper) {
                return (Inventory) ((DoubleSidedInventoryAccessor) fabric).accessor$getUpperChest();
            } else {
                return (Inventory) ((DoubleSidedInventoryAccessor) fabric).accessor$getLowerChest();
            }
        }
        return super.getAdapter(fabric, parent);
    }

}
