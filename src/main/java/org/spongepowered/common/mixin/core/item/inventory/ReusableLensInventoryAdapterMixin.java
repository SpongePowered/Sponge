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

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.tileentity.TileEntityLockable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.item.inventory.InventoryAdapterBridge;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.ReusableLensInventoryAdapaterBridge;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.bridge.inventory.LensProviderBridge;
import org.spongepowered.common.item.inventory.lens.ReusableLensProvider;
import org.spongepowered.common.item.inventory.lens.impl.DefaultEmptyLens;
import org.spongepowered.common.item.inventory.lens.impl.ReusableLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;

import javax.annotation.Nullable;

/**
 * Implement {@link InventoryAdapter#bridge$getSlotProvider()}
 * and {@link InventoryAdapter#bridge$getRootLens()}
 * using a {@link ReusableLensProvider} or {@link LensProviderBridge}
 */
@Mixin(value = {
        TileEntityLockable.class,
        InventoryBasic.class,
        InventoryCraftResult.class,
        InventoryLargeChest.class
}, priority = 999)
public abstract class ReusableLensInventoryAdapterMixin implements ReusableLensInventoryAdapaterBridge, InventoryAdapterBridge {

    @Nullable private ReusableLens<?> impl$reusableLens = null;

    @Override
    public ReusableLens<?> bridge$getReusableLens() {
        if (this.impl$reusableLens != null) {
            return this.impl$reusableLens;
        }
        if (this instanceof ReusableLensProvider) {
            this.impl$reusableLens = ((ReusableLensProvider) this).bridge$generateReusableLens(this.bridge$getFabric(), this);
            return this.impl$reusableLens;
        }
        if (this instanceof LensProviderBridge) {
            // We can set the slot provider onto itself for recycling the field usage in InventoryTraitContainerAdapterMixin
            this.bridge$setSlotProvider(((LensProviderBridge) this).bridge$slotProvider(this.bridge$getFabric(), this));
            final Lens lens = ((LensProviderBridge) this).bridge$rootLens(this.bridge$getFabric(), this);
            this.impl$reusableLens = new ReusableLens<>(this.bridge$getSlotProvider(), lens);
            this.bridge$setLens(lens);
            return this.impl$reusableLens;
        }
        final SlotCollection slots = new SlotCollection.Builder().add(this.bridge$getFabric().getSize()).build();
        final Lens lens;
        if (this.bridge$getFabric().getSize() == 0) {
            lens = new DefaultEmptyLens(this);
        } else {
            lens = new OrderedInventoryLensImpl(0, this.bridge$getFabric().getSize(), 1, slots);
        }
        this.impl$reusableLens = new ReusableLens<>(slots, lens);
        this.bridge$setSlotProvider(slots);
        this.bridge$setLens(this.impl$reusableLens.getLens());
        return this.impl$reusableLens;
    }

}
