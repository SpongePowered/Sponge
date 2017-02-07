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

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.comp.EquipmentInventoryLens;
import org.spongepowered.common.item.inventory.lens.comp.GridInventoryLens;
import org.spongepowered.common.item.inventory.lens.comp.HotbarLens;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftLens;
import org.spongepowered.common.item.inventory.lens.impl.comp.EquipmentInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.HotbarLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.slots.SlotLensImpl;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;

public class PlayerInventoryLens extends MinecraftLens {

    private final EntityPlayerMP player;

    private HotbarLensImpl hotbar;
    private GridInventoryLensImpl main;
    private EquipmentInventoryLensImpl equipment;
    private SlotLensImpl offhand;

    public PlayerInventoryLens(InventoryAdapter<IInventory, ItemStack> adapter, SlotProvider<IInventory, ItemStack> slots) {
        super(0, adapter.getInventory().getSize(), adapter, slots);
        this.player = (EntityPlayerMP) ((InventoryPlayer) adapter).player;
        this.init(slots);
    }

    @Override
    protected void init(SlotProvider<IInventory, ItemStack> slots) {
        this.hotbar = new HotbarLensImpl(0, InventoryPlayer.getHotbarSize(), slots);
        this.main = new GridInventoryLensImpl(9, 9, 3, 9, slots);
        this.equipment = new EquipmentInventoryLensImpl((ArmorEquipable) player, 36, 4, 1, slots);
        this.offhand = new SlotLensImpl(37);

        // TODO Hotbar in Vanilla is part of the main inventory (first 9 slots) ; maybe wrap it in a Lens?
        this.addSpanningChild(this.hotbar);
        this.addSpanningChild(this.main);
        this.addSpanningChild(this.equipment);
        this.addSpanningChild(this.offhand);
    }

    @Override
    protected boolean isDelayedInit() {
        return true; // player is needed for EquipmentInventoryLensImpl
    }

    public HotbarLens<IInventory, net.minecraft.item.ItemStack> getHotbarLens() {
        return this.hotbar;
    }

    public GridInventoryLens<IInventory, ItemStack> getMainLens() {
        return this.main;
    }

    public EquipmentInventoryLens<IInventory, ItemStack> getEquipmentLens() {
        return this.equipment;
    }

    public SlotLens<IInventory, ItemStack> getOffhandLens() {
        return this.offhand;
    }
}
