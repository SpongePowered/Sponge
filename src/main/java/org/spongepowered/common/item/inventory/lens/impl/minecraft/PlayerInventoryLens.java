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
package org.spongepowered.common.item.inventory.lens.impl.minecraft;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.common.entity.player.SpongeUserInventory;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.comp.EquipmentInventoryLens;
import org.spongepowered.common.item.inventory.lens.comp.MainPlayerInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.RealLens;
import org.spongepowered.common.item.inventory.lens.impl.comp.EquipmentInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.MainPlayerInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;

public class PlayerInventoryLens extends RealLens {

    private static final int EQUIPMENT = 4;
    private final ArmorEquipable player;

    private MainPlayerInventoryLensImpl main;
    private EquipmentInventoryLensImpl equipment;
    private SlotLens<IInventory, ItemStack> offhand;

    public PlayerInventoryLens(InventoryAdapter<IInventory, ItemStack> adapter, SlotProvider<IInventory, ItemStack> slots) {
        super(0, adapter.getFabric().getSize(), adapter, slots);
        if (adapter instanceof InventoryPlayer) {
            this.player = (ArmorEquipable) ((InventoryPlayer) adapter).player;
        }
        else if (adapter instanceof SpongeUserInventory) {
            this.player = ((SpongeUserInventory) adapter).player;
        } else {
            throw new IllegalArgumentException("Adapter is not a PlayerInventory");
        }
        this.init(slots);
    }

    @Override
    protected void init(SlotProvider<IInventory, ItemStack> slots) {
        // Adding slots
        for (int ord = 0, slot = this.base; ord < this.size; ord++, slot++) {
            this.addChild(slots.getSlot(slot), new SlotIndex(ord));
        }

        int base = 0;
        this.main = new MainPlayerInventoryLensImpl(base, slots, false);
        base += this.main.slotCount();
        this.equipment = new EquipmentInventoryLensImpl(this.player, base, EQUIPMENT, 1, slots, false);
        base += EQUIPMENT;
        this.offhand = slots.getSlot(base);

        // TODO Hotbar in Vanilla is part of the main inventory (first 9 slots) ; maybe wrap it in a Lens?
        this.addSpanningChild(this.main);
        this.addSpanningChild(this.equipment);
        this.addSpanningChild(this.offhand);

        // Additional Slots for bigger modded inventories
        int additionalSlots = this.size - base - 1;
        if (additionalSlots > 0) {
            this.addSpanningChild(new OrderedInventoryLensImpl(base, additionalSlots, 1, slots));
        }

    }

    public MainPlayerInventoryLens<IInventory, ItemStack> getMainLens() {
        return this.main;
    }

    public EquipmentInventoryLens<IInventory, ItemStack> getEquipmentLens() {
        return this.equipment;
    }

    public SlotLens<IInventory, ItemStack> getOffhandLens() {
        return this.offhand;
    }
}
