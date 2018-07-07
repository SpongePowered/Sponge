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
package org.spongepowered.common.item.inventory.lens.impl.comp;

import net.minecraft.entity.player.InventoryPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.EquipmentSlotAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.InvalidOrdinalException;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.slots.EquipmentSlotLens;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

/**
 * Single Slot pointing to a players {@link EquipmentTypes#MAIN_HAND} slot.
 */
public class HeldHandSlotLensImpl implements EquipmentSlotLens {

    private InventoryPlayer getInventoryPlayer(Fabric fabric) {
        return (InventoryPlayer) fabric.get(0); // Only players have this lens
    }

    @Override
    public net.minecraft.item.ItemStack getStack(Fabric fabric) {
        InventoryPlayer inv = this.getInventoryPlayer(fabric);
        return inv.getCurrentItem();
    }

    @Override
    public boolean setStack(Fabric fabric, net.minecraft.item.ItemStack stack) {
        InventoryPlayer inv = this.getInventoryPlayer(fabric);
        inv.mainInventory.set(inv.currentItem, stack);
        return true;
    }

    @Override
    public int getOrdinal(Fabric fabric) {
        InventoryPlayer inv = this.getInventoryPlayer(fabric);
        return inv.currentItem;
    }

    @Override
    public Lens getParent() {
        return null;
    }

    @Override
    public Class<? extends Inventory> getAdapterType() {
        return EquipmentSlotAdapter.class;
    }

    @Override
    public InventoryAdapter getAdapter(Fabric inv, Inventory parent) {
        return new EquipmentSlotAdapter(inv, this, parent);
    }

    @Override
    public Translation getName(Fabric inv) {
        return inv.getDisplayName();
    }

    @Override public int slotCount() {
        return 1;
    }

    @Override
    public int getMaxStackSize(Fabric inv) {
        return inv.getMaxStackSize();
    }

    @Override
    public List<Lens> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public List<Lens> getSpanningChildren() {
        return Collections.emptyList();
    }

    @Nullable @Override
    public SlotLens getSlot(int ordinal) {
        if (ordinal != 0) {
            throw new InvalidOrdinalException("Non-zero slot ordinal");
        }
        return this;
    }

    @Override
    public List<SlotLens> getSlots() {
        return Collections.singletonList(this);
    }

    @Override
    public String toString(int deep) {
        return "[Held]";
    }

    @Override
    public String toString() {
        return this.toString(0);
    }

    @Override
    public Lens getLens(int index) {
        return this;
    }

    @Override
    public Collection<InventoryProperty<?, ?>> getProperties(int index) {
        return Collections.emptyList();
    }

    @Override
    public Collection<InventoryProperty<?, ?>> getProperties(Lens lens) {
        return Collections.emptyList();
    }

    @Override
    public boolean has(Lens lens) {
        return false;
    }

    @Override
    public boolean isSubsetOf(Collection<Lens> c) {
        return false;
    }

    @Override
    public Iterator<Lens> iterator() {
        return Collections.emptyListIterator();
    }

    @Override
    public SlotLens getSlotLens(int ordinal) {
        if (ordinal != 0) {
            throw new InvalidOrdinalException("Non-zero slot ordinal");
        }
        return this;
    }

    @Override
    public Predicate<EquipmentType> getEquipmentTypeFilter() {
        return (e) -> e == EquipmentTypes.MAIN_HAND;
    }

    @Override
    public Predicate<ItemStack> getItemStackFilter() {
        return (i) -> true;
    }

    @Override
    public Predicate<ItemType> getItemTypeFilter() {
        return (i) -> true;
    }
}
