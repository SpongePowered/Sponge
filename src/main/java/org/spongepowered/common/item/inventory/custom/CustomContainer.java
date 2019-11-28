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
package org.spongepowered.common.item.inventory.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class CustomContainer extends Container {

    public CustomInventory inv;

    public CustomContainer(final PlayerEntity player, final CustomInventory inventory) {
        this.inv = inventory;

        // TODO what significance has the x/y coord on the Slots?
        for (int slot = 0; slot < inventory.func_70302_i_(); slot++) {
            this.func_75146_a(new Slot(inventory, slot, 0, 0));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.func_75146_a(new Slot(player.field_71071_by, (row + 1) * 9 + col, 0, 0));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.func_75146_a(new Slot(player.field_71071_by, col, 0, 0));
        }
    }

    @Override
    public boolean func_75145_c(final PlayerEntity playerIn) {
        return true;
    }

    @Override
    public void func_75134_a(final PlayerEntity playerIn) {
        super.func_75134_a(playerIn);
        this.inv.func_174886_c(playerIn);
    }

    @Override
    public ItemStack func_82846_b(final PlayerEntity playerIn, final int index) {
        ItemStack itemstack = ItemStack.field_190927_a;
        final Slot slot = this.field_75151_b.get(index);

        if (slot != null && slot.func_75216_d())
        {
            final ItemStack itemstack1 = slot.func_75211_c();
            itemstack = itemstack1.func_77946_l();

            if (index < this.inv.func_70302_i_())
            {
                if (!this.func_75135_a(itemstack1, this.inv.func_70302_i_(), this.field_75151_b.size(), true))
                {
                    return ItemStack.field_190927_a;
                }
            }
            else if (!this.func_75135_a(itemstack1, 0, this.inv.func_70302_i_(), false))
            {
                return ItemStack.field_190927_a;
            }

            if (itemstack1.func_190916_E() == 0)
            {
                slot.func_75215_d(ItemStack.field_190927_a);
            }
            else
            {
                slot.func_75218_e();
            }
        }

        return itemstack;
    }

    @Override
    public void func_75142_b() {
        super.func_75142_b();
        // Resend the whole inventory to prevent visual glitches due to client-prediction
        // This would not be needed if the Container enforces the same restrictions on slots as vanilla

        // Cursor Item
        /*for (IContainerListener crafter : listeners) {
            crafter.updateCraftingInventory(this, this.getInventory());
        }
        // Inventory
        for (int i = 0; i < this.inventorySlots.size(); ++i) {
            for (IContainerListener crafter : listeners) {
                crafter.sendSlotContents(this, i, this.inventorySlots.get(i).getStack());
            }
        }*/
    }
}
