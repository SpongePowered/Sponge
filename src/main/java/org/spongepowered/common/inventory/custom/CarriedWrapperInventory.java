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
package org.spongepowered.common.inventory.custom;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.common.bridge.world.inventory.CarriedBridge;
import org.spongepowered.common.bridge.world.inventory.InventoryBridge;

import java.util.Optional;
import java.util.Set;

/**
 * A wrapper Inventory providing a carrier for an inventory
 */
public class CarriedWrapperInventory implements Container, CarriedBridge {

    private Container wrapped;
    private Carrier carrier; // shadow usage

    public CarriedWrapperInventory(Container wrapped, Carrier carrier) {
        this.wrapped = wrapped;
        this.carrier = carrier;
    }

    public InventoryBridge getWrapped() {
        return ((InventoryBridge) this.wrapped);
    }

    @Override
    public Optional<Carrier> bridge$getCarrier() {
        return Optional.ofNullable(this.carrier);
    }

    // Delegation

    @Override
    public int getContainerSize() {
        return this.wrapped.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return this.wrapped.isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return this.wrapped.getItem(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return this.wrapped.removeItem(index, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return this.wrapped.removeItemNoUpdate(index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        this.wrapped.setItem(index, stack);
    }

    @Override
    public int getMaxStackSize() {
        return this.wrapped.getMaxStackSize();
    }

    @Override
    public void setChanged() {
        this.wrapped.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.wrapped.stillValid(player);
    }

    @Override
    public void startOpen(Player player) {
       this.wrapped.startOpen(player);
    }

    @Override
    public void stopOpen(Player player) {
        this.wrapped.stopOpen(player);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return this.wrapped.canPlaceItem(index, stack);
    }

    @Override
    public int countItem(Item itemIn) {
        return this.wrapped.countItem(itemIn);
    }

    @Override
    public boolean hasAnyOf(Set<Item> set) {
        return this.wrapped.hasAnyOf(set);
    }

    @Override
    public void clearContent() {
        this.wrapped.clearContent();
    }
}
