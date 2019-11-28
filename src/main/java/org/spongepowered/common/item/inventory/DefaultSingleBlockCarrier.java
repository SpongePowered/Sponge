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
package org.spongepowered.common.item.inventory;

import net.minecraft.inventory.ISidedInventory;
import org.spongepowered.api.item.inventory.BlockCarrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.SingleBlockCarrier;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;

public interface DefaultSingleBlockCarrier extends SingleBlockCarrier {

    @Override
    default Inventory getInventory(Direction from) {
        return getInventory(from, this);
    }

    @SuppressWarnings("deprecation")
    static Inventory getInventory(Direction from, BlockCarrier thisThing) {
        if (thisThing instanceof ISidedInventory) {
            net.minecraft.util.Direction facing = DirectionFacingProvider.getInstance().get(from).get();
            int[] slots = ((ISidedInventory) thisThing).func_180463_a(facing);
            SlotIndex[] indices = new SlotIndex[slots.length];
            for (int i = 0; i < slots.length; i++) {
                indices[i] = SlotIndex.of(slots[i]);
            }
            return thisThing.getInventory().query(indices);
        }
        return thisThing.getInventory();
    }
}
