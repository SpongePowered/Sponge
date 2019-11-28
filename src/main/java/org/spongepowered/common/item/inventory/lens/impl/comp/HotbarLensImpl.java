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
package org.spongepowered.common.item.inventory.lens.impl.comp;

import net.minecraft.util.Hand;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.bridge.entity.player.InventoryPlayerBridge;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.HotbarAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.comp.HotbarLens;

public class HotbarLensImpl extends InventoryRowLensImpl implements HotbarLens {

    public HotbarLensImpl(int base, int width, SlotProvider slots) {
        this(base, width, 0, 0, HotbarAdapter.class, slots);
    }

    private HotbarLensImpl(int base, int width, int xBase, int yBase, Class<? extends Inventory> adapterType, SlotProvider slots) {
        super(base, width, xBase, yBase, adapterType, slots);
    }

    @Override
    public InventoryAdapter getAdapter(Fabric inv, Inventory parent) {
        return new HotbarAdapter(inv, this, parent);
    }

    @Override
    public int getSelectedSlotIndex(Fabric inv) {
        for (Object inner : inv.fabric$allInventories()) {
            if (inner instanceof InventoryPlayerBridge) {
                return ((InventoryPlayerBridge) inner).bridge$getHeldItemIndex(Hand.MAIN_HAND);
            }
        }
        return 0;
    }

    @Override
    public void setSelectedSlotIndex(Fabric inv, int index) {
        inv.fabric$allInventories().stream().filter(inner -> inner instanceof InventoryPlayerBridge).forEach(inner -> {
            ((InventoryPlayerBridge) inner).bridge$setSelectedItem(index, true);
        });
    }

}
