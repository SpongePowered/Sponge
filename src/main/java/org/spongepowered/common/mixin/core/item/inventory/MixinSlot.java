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
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.inventory.IMixinSlot;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.OrderedInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftFabric;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.slots.SlotLensImpl;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;

@Mixin(Slot.class)
public abstract class MixinSlot implements org.spongepowered.api.item.inventory.Slot, IMixinSlot, MinecraftInventoryAdapter<IInventory> {

    @Shadow @Final public int slotIndex;
    @Shadow @Final public IInventory inventory;

    protected Fabric<IInventory> fabric;
    protected SlotCollection slots;
    protected Lens<IInventory, ItemStack> lens;

    private InventoryAdapter<IInventory, ItemStack> parentAdapter;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(CallbackInfo ci) {
        this.fabric = MinecraftFabric.of(this);
        this.slots = new SlotCollection.Builder().add(1).build();
        if (this.inventory instanceof InventoryAdapter) {
            Object lens = ((InventoryAdapter) this.inventory).getRootLens().getChildren().get(slotIndex);
            if (lens instanceof SlotLens) {
                this.lens = ((SlotLens) lens);
            }
        }
        if (this.lens == null) {
            this.lens = new SlotLensImpl<>(0);
        }
    }

    @Override
    public int getSlotIndex() {
        return this.slotIndex;
    }

    @Override
    public Inventory parent() {
        if (this.inventory instanceof Inventory) {
            return ((Inventory) this.inventory);
        }
        if (this.parentAdapter == null) {
            OrderedInventoryLensImpl lens = new OrderedInventoryLensImpl(0, this.fabric.getSize(), 1, new SlotCollection.Builder().add(this.fabric.getSize()).build());
            this.parentAdapter = new OrderedInventoryAdapter(this.fabric, lens);
        }
        return this.parentAdapter;
    }

    @Override
    public org.spongepowered.api.item.inventory.Slot transform(Type type) {
        return this;
    }

    @Override
    public org.spongepowered.api.item.inventory.Slot transform() {
        return this.transform(Type.INVENTORY);
    }

    @Override
    public SlotProvider<IInventory, ItemStack> getSlotProvider() {
        return this.slots;
    }

    @Override
    public Lens<IInventory, ItemStack> getRootLens() {
        return this.lens;
    }

    @Override
    public Fabric<IInventory> getFabric() {
        return this.fabric;
    }
}
