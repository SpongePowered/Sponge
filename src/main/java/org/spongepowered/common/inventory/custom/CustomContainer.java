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

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.api.item.inventory.ContainerType;
import org.spongepowered.common.bridge.world.inventory.container.TrackedMenuBridge;

public class CustomContainer extends AbstractContainerMenu {

    public CustomInventory inv;

    public CustomContainer(int id, final Player player, final CustomInventory inventory, ContainerType type) {
        super((net.minecraft.world.inventory.MenuType<?>) type, id);
        this.inv = inventory;

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            this.addSlot(new Slot(inventory, slot, 0, 0));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(player.getInventory(), (row + 1) * 9 + col, 0, 0));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(player.getInventory(), col, 0, 0));
        }

        ((TrackedMenuBridge) inventory).bridge$trackContainerMenu(this);
    }

    @Override
    public boolean stillValid(final Player playerIn) {
        return true;
    }

    @Override
    public void removed(final Player playerIn) {
        super.removed(playerIn);
        this.inv.stopOpen(playerIn);
    }

    @Override
    public ItemStack quickMoveStack(final Player playerIn, final int index) {
        // Almost 1:1 copy of ChestContainer#transferStackInSlot
        ItemStack itemstack = ItemStack.EMPTY;
        final Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            final ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < this.inv.getContainerSize()) {
                if (!this.moveItemStackTo(itemstack1, this.inv.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, this.inv.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }
}
