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
package org.spongepowered.common.mixin.inventory.impl.bridge.lensgenerator;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.inventory.LensGeneratorBridge;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.LensRegistrar;
import org.spongepowered.common.inventory.lens.impl.slot.BasicSlotLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.lens.slots.SlotLens;

@Mixin(Slot.class)
public abstract class SlotMixin implements InventoryAdapter, LensGeneratorBridge {

    @Shadow @Final private int slotIndex;
    @Shadow @Final public IInventory inventory;

    @Override
    public SlotLensProvider lensGeneratorBridge$generateSlotLensProvider() {
        return new LensRegistrar.BasicSlotLensProvider(1);
    }

    @Override
    public Lens lensGeneratorBridge$generateLens(SlotLensProvider slotLensProvider) {

        // TODO register slot lenses
//        CraftingResultSlot.class;
//        new CraftingOutputSlotLens(slotLensProvider.getSlotLens(0), (fabric, item) -> false);
//        FurnaceResultSlot.class;
//        FurnaceFuelSlot.class;
        // etc...

        try {
            final Lens rootLens = ((InventoryAdapter) this.inventory).inventoryAdapter$getRootLens();
            SlotLens lens = rootLens.getSlotLens(this.slotIndex);
            if (lens != null) {
                return lens;
            }
        } catch (Exception ignored) {
        }
        // TODO figure out how to make it always work with existing lenses
        // this works as a fallback but removes Inventory Property Support completely
        return new BasicSlotLens(0);
    }

}
