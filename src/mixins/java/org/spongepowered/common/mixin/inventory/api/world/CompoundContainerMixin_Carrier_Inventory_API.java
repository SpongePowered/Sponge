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
package org.spongepowered.common.mixin.inventory.api.world;

import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.MultiBlockCarrier;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.inventory.DefaultSingleBlockCarrier;

import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Mixin(CompoundContainer.class)
public abstract class CompoundContainerMixin_Carrier_Inventory_API implements MultiBlockCarrier {

    @Shadow @Final private Container container1;

    @Shadow @Final private Container container2;

    @Override
    public List<ServerLocation> locations() {
        final List<ServerLocation> list = new ArrayList<>();
        if (this.container1 instanceof BlockEntity) {
            list.add(((BlockEntity) this.container1).serverLocation());
        }
        if (this.container2 instanceof BlockEntity) {
            list.add(((BlockEntity) this.container2).serverLocation());
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public CarriedInventory<? extends Carrier> inventory() {
        return (CarriedInventory<? extends Carrier>) this;
    }

    @Override
    public ServerLocation location() {
        return this.locations().get(0);
    }

    @Override
    public Optional<Inventory> inventory(final ServerLocation at) {
        if (this.locations().contains(at)) {
            return Optional.of(this.inventory());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Inventory> inventory(final ServerLocation at, final Direction from) {
        return this.inventory(at);
    }

    @Override
    public Inventory inventory(final Direction from) {
        return DefaultSingleBlockCarrier.inventory(from, this);
    }
}
