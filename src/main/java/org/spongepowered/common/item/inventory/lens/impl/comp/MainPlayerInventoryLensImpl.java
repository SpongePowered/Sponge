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
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.MainPlayerInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.comp.GridInventoryLens;
import org.spongepowered.common.item.inventory.lens.comp.HotbarLens;
import org.spongepowered.common.item.inventory.lens.comp.MainPlayerInventoryLens;

public class MainPlayerInventoryLensImpl extends GridInventoryLensImpl implements MainPlayerInventoryLens<IInventory, ItemStack> {

    private static final int MAIN_INVENTORY_HEIGHT = 3;

    private HotbarLensImpl hotbar;
    private GridInventoryLensImpl grid;

    public MainPlayerInventoryLensImpl(int base, int width, int xBase, int yBase, SlotProvider<IInventory, ItemStack> slots) {
        this(base, width, xBase, yBase, MainPlayerInventoryAdapter.class, slots);
    }

    public MainPlayerInventoryLensImpl(int base, int width, int xBase, int yBase, Class<? extends Inventory> adapterType, SlotProvider<IInventory, ItemStack> slots) {
        super(base, width, xBase, yBase, adapterType, slots);
    }

    @Override
    protected void init(InventoryAdapter<IInventory, ItemStack> adapter, SlotProvider<IInventory, ItemStack> slots) {
        super.init(adapter, slots);

        int base = this.base;
        int INVENTORY_WIDTH = InventoryPlayer.getHotbarSize();
        this.hotbar = new HotbarLensImpl(base, INVENTORY_WIDTH, slots);
        base += INVENTORY_WIDTH;
        this.grid = new GridInventoryLensImpl(base, INVENTORY_WIDTH, MAIN_INVENTORY_HEIGHT, INVENTORY_WIDTH, slots);

        this.addChild(this.hotbar);
        this.addChild(this.grid);
    }

    @Override
    public InventoryAdapter<IInventory, ItemStack> getAdapter(Fabric<IInventory> inv, Inventory parent) {
        return new MainPlayerInventoryAdapter(inv, this, parent);
    }

    @Override
    public HotbarLens<IInventory, ItemStack> getHotbar() {
        return this.hotbar;
    }

    @Override
    public GridInventoryLens<IInventory, ItemStack> getGrid() {
        return this.grid;
    }
}
