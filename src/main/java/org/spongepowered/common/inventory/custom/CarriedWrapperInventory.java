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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.common.bridge.inventory.CarriedBridge;
import org.spongepowered.common.bridge.inventory.InventoryBridge;

import java.util.Optional;
import java.util.Set;

/**
 * A wrapper Inventory providing a carrier for an inventory
 */
public class CarriedWrapperInventory implements IInventory, CarriedBridge {

    private IInventory wrapped;
    private Carrier carrier; // shadow usage

    public CarriedWrapperInventory(IInventory wrapped, Carrier carrier) {
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
    public int getSizeInventory() {
        return this.wrapped.getSizeInventory();
    }

    @Override
    public boolean isEmpty() {
        return this.wrapped.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.wrapped.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return this.wrapped.decrStackSize(index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return this.wrapped.removeStackFromSlot(index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.wrapped.setInventorySlotContents(index, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return this.wrapped.getInventoryStackLimit();
    }

    @Override
    public void markDirty() {
        this.wrapped.markDirty();
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return this.wrapped.isUsableByPlayer(player);
    }

    @Override
    public void openInventory(PlayerEntity player) {
       this.wrapped.openInventory(player);
    }

    @Override
    public void closeInventory(PlayerEntity player) {
        this.wrapped.closeInventory(player);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return this.wrapped.isItemValidForSlot(index, stack);
    }

    @Override
    public int count(Item itemIn) {
        return this.wrapped.count(itemIn);
    }

    @Override
    public boolean hasAny(Set<Item> set) {
        return this.wrapped.hasAny(set);
    }

    @Override
    public void clear() {
        this.wrapped.clear();
    }
}
