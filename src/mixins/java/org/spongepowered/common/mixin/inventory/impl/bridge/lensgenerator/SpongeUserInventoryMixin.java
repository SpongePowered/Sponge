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

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.inventory.LensGeneratorBridge;
import org.spongepowered.common.entity.player.SpongeUserInventory;
import org.spongepowered.common.inventory.adapter.impl.slots.EquipmentSlotAdapter;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.minecraft.PlayerInventoryLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensCollection;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;

@Mixin(SpongeUserInventory.class)
public abstract class SpongeUserInventoryMixin implements LensGeneratorBridge {

    @Shadow(remap = false) @Final NonNullList<ItemStack> mainInventory;
    @Shadow(remap = false) @Final NonNullList<ItemStack> armorInventory;
    @Shadow(remap = false) @Final NonNullList<ItemStack> offHandInventory;

    @Shadow public abstract int getSizeInventory();

    @Override
    public SlotLensProvider lensGeneratorBridge$generateSlotLensProvider() {
        return new SlotLensCollection.Builder()
            .add(this.mainInventory.size())
            .add(this.offHandInventory.size())
            .add(this.armorInventory.size(), EquipmentSlotAdapter.class)
            .add(this.getSizeInventory() - this.mainInventory.size() - this.offHandInventory.size() - this.armorInventory.size())
            .build();
    }

    @Override
    public Lens lensGeneratorBridge$generateLens(SlotLensProvider slotLensProvider) {
        return new PlayerInventoryLens(this.getSizeInventory(), this.getClass(), slotLensProvider);
    }



}
