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

import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.world.ILockableContainer;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.MultiBlockCarrier;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.IMixinMultiBlockCarrier;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.ReusableLensProvider;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.ReusableLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotLensCollection;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.LargeChestInventoryLens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Mixin(InventoryLargeChest.class)
public abstract class MixinInventoryLargeChest implements MinecraftInventoryAdapter, CarriedInventory<MultiBlockCarrier>, ReusableLensProvider,
        IMixinMultiBlockCarrier {

    @Shadow @Final public ILockableContainer upperChest;
    @Shadow @Final public ILockableContainer lowerChest;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ReusableLens<?> generateLens(Fabric fabric, InventoryAdapter adapter) {
        return ReusableLens.getLens(LargeChestInventoryLens.class, this, this::generateSlotProvider, this::generateRootLens);
    }

    @SuppressWarnings("unchecked")
    private SlotProvider generateSlotProvider() {
        return new SlotLensCollection.Builder().add(this.getFabric().getSize()).build();
    }

    @SuppressWarnings("unchecked")
    private LargeChestInventoryLens generateRootLens(SlotProvider slots) {
        return new LargeChestInventoryLens(this, slots);
    }

    @Override
    public Optional<MultiBlockCarrier> getCarrier() {
        return Optional.of(this);
    }

    @Override
    public List<Location<World>> getLocations() {
        List<Location<World>> list = new ArrayList<>();
        if (this.upperChest instanceof TileEntity) {
            list.add(((TileEntity) this.upperChest).getLocation());
        }
        if (this.lowerChest instanceof TileEntity) {
            list.add(((TileEntity) this.lowerChest).getLocation());
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public CarriedInventory<? extends Carrier> getInventory() {
        return this;
    }
}
