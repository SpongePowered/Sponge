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
package org.spongepowered.common.mixin.core.item.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityLockable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.LensProvider;
import org.spongepowered.common.item.inventory.lens.ReusableLensProvider;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.DefaultEmptyLens;
import org.spongepowered.common.item.inventory.lens.impl.ReusableLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;

import javax.annotation.Nullable;

/**
 * Implement {@link InventoryAdapter#getSlotProvider()} and {@link InventoryAdapter#getRootLens()} using a {@link ReusableLensProvider} or {@link LensProvider}
 */
@Mixin(value = {
        TileEntityLockable.class,
        InventoryBasic.class,
        InventoryCraftResult.class,
        InventoryLargeChest.class
}, priority = 999)
public abstract class MixinTraitAdapter implements MinecraftInventoryAdapter<IInventory> {

    @Nullable private ReusableLens<?> reusableLens = null;
    @Nullable private SlotProvider<IInventory, ItemStack> slots = null;

    @Override
    public SlotProvider<IInventory, ItemStack> getSlotProvider() {
        if (this.slots != null) {
            return this.slots;
        }
        return this.getReusableLens().getSlots();
    }

    @Override
    public Lens<IInventory, ItemStack> getRootLens() {
        return this.getReusableLens().getLens();
    }

    @SuppressWarnings("unchecked")
    private ReusableLens<?> getReusableLens()
    {
        if (this.reusableLens != null) {
            return this.reusableLens;
        }
        if (this instanceof ReusableLensProvider) {
            return ((ReusableLensProvider<IInventory, ItemStack>) this).generateLens(this.getFabric(), this);
        }
        if (this instanceof LensProvider) {
            this.slots = ((LensProvider) this).slotProvider(this.getFabric(), this);
            Lens lens = ((LensProvider) this).rootLens(this.getFabric(), this);
            return new ReusableLens<>(this.slots, lens);
        }
        SlotCollection slots = new SlotCollection.Builder().add(this.getFabric().getSize()).build();
        Lens<IInventory, ItemStack> lens;
        if (this.getFabric().getSize() == 0) {
            lens = new DefaultEmptyLens<>(this);
        } else {
            lens = new OrderedInventoryLensImpl(0, this.getFabric().getSize(), 1, slots);
        }
        return new ReusableLens<>(slots, lens);
    }

}
