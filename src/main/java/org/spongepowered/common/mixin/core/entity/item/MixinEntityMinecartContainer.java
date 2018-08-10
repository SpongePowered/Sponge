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
package org.spongepowered.common.mixin.core.entity.item;

import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.storage.loot.ILootContainer;
import org.spongepowered.api.entity.vehicle.minecart.ContainerMinecart;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.DefaultIndexedLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotLensCollection;
import org.spongepowered.common.item.inventory.lens.impl.fabric.IInventoryFabric;

@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(EntityMinecartContainer.class)
@Implements({@Interface(iface = MinecraftInventoryAdapter.class, prefix = "inventory$"),
             @Interface(iface = ContainerMinecart.class, prefix = "container$")})
public abstract class MixinEntityMinecartContainer extends MixinEntityMinecart implements ILockableContainer, ILootContainer {

    protected Fabric fabric = new IInventoryFabric(this);
    protected SlotLensCollection slots = new SlotLensCollection.Builder().add(this.getSizeInventory()).build();
    protected Lens lens = new DefaultIndexedLens(0, this.getSizeInventory(), this.slots);

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

    @SuppressWarnings("unchecked")
    public CarriedInventory<ContainerMinecart> container$getInventory() {
        return (CarriedInventory<ContainerMinecart>) this;
    }
}
