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
package org.spongepowered.common.mixin.api.mcp.inventory;

import net.minecraft.inventory.DoubleSidedInventory;
import net.minecraft.world.ILockableContainer;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.MultiBlockCarrier;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.item.inventory.DefaultSingleBlockCarrier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Mixin(DoubleSidedInventory.class)
public abstract class InventoryLargeChestMixin_API implements CarriedInventory<MultiBlockCarrier>, MultiBlockCarrier {

    @Shadow @Final private ILockableContainer upperChest;
    @Shadow @Final private ILockableContainer lowerChest;

    @Override
    public Optional<MultiBlockCarrier> getCarrier() {
        return Optional.of(this);
    }

    @Override
    public List<Location<World>> getLocations() {
        final List<Location<World>> list = new ArrayList<>();
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

    @Override
    public Location<World> getLocation() {
        return this.getLocations().get(0);
    }

    @Override
    public Optional<Inventory> getInventory(final Location<World> at) {
        if (this.getLocations().contains(at)) {
            return Optional.of(this.getInventory());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Inventory> getInventory(final Location<World> at, final Direction from) {
        return this.getInventory(at);
    }

    @Override
    public Inventory getInventory(final Direction from) {
        return DefaultSingleBlockCarrier.getInventory(from, this);
    }
}
