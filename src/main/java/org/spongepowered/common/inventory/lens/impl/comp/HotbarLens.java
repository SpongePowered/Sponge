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
package org.spongepowered.common.inventory.lens.impl.comp;

import net.minecraft.world.InteractionHand;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.bridge.world.entity.player.PlayerInventoryBridge;
import org.spongepowered.common.inventory.adapter.impl.comp.HotbarAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;

public class HotbarLens extends InventoryRowLens {

    public HotbarLens(int base, int width, SlotLensProvider slots) {
        this(base, width, 0, 0, HotbarAdapter.class, slots);
    }

    private HotbarLens(int base, int width, int xBase, int yBase, Class<? extends Inventory> adapterType, SlotLensProvider slots) {
        super(base, width, xBase, yBase, adapterType, slots);
    }

    @Override
    public Inventory getAdapter(Fabric fabric, Inventory parent) {
        return new HotbarAdapter(fabric, this, parent);
    }

    public int getSelectedSlotIndex(Fabric fabric) {
        for (Object inner : fabric.fabric$allInventories()) {
            if (inner instanceof PlayerInventoryBridge) {
                return ((PlayerInventoryBridge) inner).bridge$getHeldItemIndex(InteractionHand.MAIN_HAND);
            }
        }
        return 0;
    }

    public void setSelectedSlotIndex(Fabric fabric, int index) {
        fabric.fabric$allInventories().stream()
                .filter(PlayerInventoryBridge.class::isInstance)
                .map(PlayerInventoryBridge.class::cast)
                .forEach(inner -> inner.bridge$setSelectedItem(index, true));
    }

}
