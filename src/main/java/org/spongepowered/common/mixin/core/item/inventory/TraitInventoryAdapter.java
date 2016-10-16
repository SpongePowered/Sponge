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

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import org.spongepowered.api.item.inventory.EmptyInventory;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.item.inventory.EmptyInventoryImpl;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.custom.CustomContainer;
import org.spongepowered.common.item.inventory.custom.CustomInventory;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;

import java.util.ArrayList;
import java.util.List;

@Mixin({
        net.minecraft.inventory.Slot.class,
        InventoryPlayer.class,
        TileEntityChest.class,
        TileEntityFurnace.class,
        TileEntityHopper.class,
        EntityVillager.class,
        Container.class,
        InventoryLargeChest.class,
        InventoryEnderChest.class,
        CustomInventory.class
})
@Implements(@Interface(iface = Inventory.class, prefix = "inventory$"))
public abstract class TraitInventoryAdapter implements MinecraftInventoryAdapter {

    protected EmptyInventory empty;
    protected Inventory parent;
    protected Inventory next;
    protected SlotCollection slots;
    protected List<Inventory> children = new ArrayList<Inventory>();
    protected Iterable<Slot> slotIterator;

    @Override
    public Inventory parent() {
        return this.parent;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> T first() {
        return (T) this.iterator().next();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> T next() {
        return (T) this.emptyInventory();
    }

    @Override
    public SlotProvider<IInventory, net.minecraft.item.ItemStack> getSlotProvider() {
        return this.slots;
    }

    @Override
    public Inventory getChild(int index) {
        if (index < 0 || index >= this.getRootLens().getChildren().size()) {
            throw new IndexOutOfBoundsException("No child at index: " + index);
        }
        while (index >= this.children.size()) {
            this.children.add(null);
        }
        Inventory child = this.children.get(index);
        if (child == null) {
            child = this.getRootLens().getChildren().get(index).getAdapter(this.getInventory(), this);
            this.children.set(index, child);
        }
        return child;
    }

    protected final EmptyInventory emptyInventory() {
        if (this.empty == null) {
            this.empty = new EmptyInventoryImpl(this);
        }
        return this.empty;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> Iterable<T> slots() {
        if (this.slotIterator == null) {
            this.slotIterator = this.slots.getIterator(this);
        }
        return (Iterable<T>) this.slotIterator;
    }

    @Intrinsic
    public void inventory$clear() {
        this.getInventory().clear();
    }

}
