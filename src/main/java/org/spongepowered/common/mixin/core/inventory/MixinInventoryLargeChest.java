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
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ILockableContainer;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.ReusableLensProvider;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.ReusableLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.LargeChestInventoryLens;

import java.util.Optional;

@Mixin(InventoryLargeChest.class)
public abstract class MixinInventoryLargeChest implements MinecraftInventoryAdapter<IInventory>, CarriedInventory<TileEntityCarrier>, ReusableLensProvider<IInventory, ItemStack> {

    @Shadow @Final private ILockableContainer upperChest;
    @Shadow @Final private ILockableContainer lowerChest;

    @Override
    public ReusableLens<?> generateLens(Fabric<IInventory> fabric, InventoryAdapter<IInventory, ItemStack> adapter) {
        return ReusableLens.getLens(LargeChestInventoryLens.class, ((InventoryAdapter) this), this::generateSlotProvider, this::generateRootLens);
    }

    @SuppressWarnings("unchecked")
    private SlotProvider<IInventory, ItemStack> generateSlotProvider() {
        return new SlotCollection.Builder().add(this.getFabric().getSize()).build();
    }

    @SuppressWarnings("unchecked")
    private LargeChestInventoryLens generateRootLens(SlotProvider<IInventory, ItemStack> slots) {
        return new LargeChestInventoryLens(this, slots);
    }

    @Override
    public Inventory getChild(Lens<IInventory, ItemStack> lens) {
        return null;
    }

    @Override
    public Optional<TileEntityCarrier> getCarrier() {
        return Optional.of(((TileEntityCarrier) upperChest));
    }
}
