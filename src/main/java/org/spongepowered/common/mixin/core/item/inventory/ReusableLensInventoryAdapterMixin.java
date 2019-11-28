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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.item.inventory.InventoryAdapterBridge;
import org.spongepowered.common.bridge.item.inventory.InventoryBridge;
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
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.DoubleSidedInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.tileentity.LockableTileEntity;

/**
 * Implement {@link InventoryAdapter#bridge$getSlotProvider()}
 * and {@link InventoryAdapter#bridge$getRootLens()}
 * using a {@link ReusableLensProvider} or {@link LensProviderBridge}
 */
@Mixin(value = {
        LockableTileEntity.class,
        Inventory.class,
        CraftResultInventory.class,
        DoubleSidedInventory.class
}, priority = 999)
public abstract class ReusableLensInventoryAdapterMixin implements ReusableLensInventoryAdapaterBridge, InventoryAdapterBridge, InventoryBridge {

    @Nullable private ReusableLens<?> impl$reusableLens = null;

    @Override
    public ReusableLens<?> bridge$getReusableLens() {
        if (this.impl$reusableLens == null) {
            this.impl$reusableLens = ReusableLens.getLens(this);
        }
        return this.impl$reusableLens;
    }
}
