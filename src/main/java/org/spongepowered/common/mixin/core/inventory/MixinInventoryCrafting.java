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
package org.spongepowered.common.mixin.core.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.LensProvider;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.DefaultEmptyLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotLensCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.CraftingGridInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.fabric.IInventoryFabric;

@SuppressWarnings("rawtypes")
@Mixin(InventoryCrafting.class)
@Implements(value = @Interface(iface = MinecraftInventoryAdapter.class, prefix = "inventory$"))
public abstract class MixinInventoryCrafting implements IInventory, LensProvider {

    @Shadow @Final private NonNullList<ItemStack> stackList;
    @Shadow @Final private int inventoryWidth;
    @Shadow @Final private int inventoryHeight;

    protected Fabric fabric;
    protected SlotLensCollection slots;
    protected Lens lens;

    @SuppressWarnings("unchecked")
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructed(CallbackInfo ci) {
        this.fabric = new IInventoryFabric(this);
        this.slots = new SlotLensCollection.Builder().add(this.stackList.size()).build();
        this.lens = rootLens(fabric, ((InventoryAdapter) this));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Lens rootLens(Fabric fabric, InventoryAdapter adapter) {
        if (this.stackList.size() == 0) {
            return new DefaultEmptyLens(adapter);
        }
        return new CraftingGridInventoryLensImpl(0, this.inventoryWidth, this.inventoryHeight, this.slots);
    }

    @SuppressWarnings("unchecked")
    public SlotProvider inventory$getSlotProvider() {
        return this.slots;
    }

    public Lens inventory$getRootLens() {
        return this.lens;
    }

    public Fabric inventory$getFabric() {
        return this.fabric;
    }

}
