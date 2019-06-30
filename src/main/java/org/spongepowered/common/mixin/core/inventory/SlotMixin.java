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
import net.minecraft.inventory.Slot;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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

import javax.annotation.Nullable;

@Mixin(Slot.class)
public abstract class SlotMixin implements org.spongepowered.api.item.inventory.Slot, MinecraftInventoryAdapter {

    @Shadow @Final public int slotIndex;
    @Shadow @Final public IInventory inventory;

    protected Fabric impl$fabric;
    protected SlotCollection impl$slots;
    protected Lens impl$lens;

    @Nullable private InventoryAdapter parentAdapter;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$setUpFabric(final CallbackInfo ci) {
        this.impl$fabric = MinecraftFabric.of(this);
        this.impl$slots = new SlotCollection.Builder().add(1).build();
        try {
            final Lens rootLens = ((InventoryAdapter) this.inventory).bridge$getRootLens();
            this.impl$lens = rootLens.getSlotLens(this.slotIndex);
        } catch (Exception ignored) {
            // TODO figure out how to make it always work with existing lenses
            // this works as a fallback but removes Inventory Property Support completely
            this.impl$lens = new SlotLensImpl(0);
        }
    }

    @Override
    public Inventory parent() {
        if (this.inventory instanceof Inventory) {
            return ((Inventory) this.inventory);
        }
        if (this.parentAdapter == null) {
            final OrderedInventoryLensImpl lens = new OrderedInventoryLensImpl(0, this.impl$fabric
                .getSize(), 1, new SlotCollection.Builder().add(this.impl$fabric.getSize()).build());
            this.parentAdapter = new OrderedInventoryAdapter(this.impl$fabric, lens);
        }
        return this.parentAdapter;
    }

    @Override
    public org.spongepowered.api.item.inventory.Slot transform(final Type type) {
        return this;
    }

    @Override
    public org.spongepowered.api.item.inventory.Slot transform() {
        return this.transform(Type.INVENTORY);
    }

    @Override
    public SlotProvider bridge$getSlotProvider() {
        return this.impl$slots;
    }

    @Override
    public Lens bridge$getRootLens() {
        return this.impl$lens;
    }

    @Override
    public Fabric bridge$getFabric() {
        return this.impl$fabric;
    }
}
