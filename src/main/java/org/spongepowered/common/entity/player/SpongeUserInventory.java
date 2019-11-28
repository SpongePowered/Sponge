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
package org.spongepowered.common.entity.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Arrays;
import java.util.List;

public class SpongeUserInventory implements IInventory {

    // sourced from InventoryPlayer

    /** An array of 36 item stacks indicating the main player inventory (including the visible bar). */
    final NonNullList<ItemStack> mainInventory = NonNullList.func_191197_a(36, ItemStack.field_190927_a);
    /** An array of 4 item stacks containing the currently worn armor pieces. */
    final NonNullList<ItemStack> armorInventory = NonNullList.func_191197_a(4, ItemStack.field_190927_a);
    final NonNullList<ItemStack> offHandInventory = NonNullList.func_191197_a(1, ItemStack.field_190927_a);
    private final List<NonNullList<ItemStack>> allInventories;
    /** The index of the currently held item (0-8). */
    public int currentItem;
    /** The player whose inventory this is. */
    public SpongeUser player;
    private boolean dirty = false;

    public SpongeUserInventory(SpongeUser playerIn) {
        this.allInventories = Arrays.asList(this.mainInventory, this.armorInventory, this.offHandInventory);
        this.player = playerIn;
    }

    public ItemStack getCurrentItem() {
        return InventoryPlayer.func_184435_e(this.currentItem) ? this.mainInventory.get(this.currentItem) : ItemStack.field_190927_a;
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    @Override
    public ItemStack func_70298_a(int index, int count) {
        this.func_70296_d();
        List<ItemStack> list = null;

        for (NonNullList<ItemStack> nonnulllist : this.allInventories) {
            if (index < nonnulllist.size()) {
                list = nonnulllist;
                break;
            }

            index -= nonnulllist.size();
        }

        return list != null && !list.get(index).func_190926_b() ? ItemStackHelper.func_188382_a(list, index, count) : ItemStack.field_190927_a;
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    @Override
    public ItemStack func_70304_b(int index) {
        this.func_70296_d();
        NonNullList<ItemStack> nonnulllist = null;

        for (NonNullList<ItemStack> nonnulllist1 : this.allInventories) {
            if (index < nonnulllist1.size()) {
                nonnulllist = nonnulllist1;
                break;
            }

            index -= nonnulllist1.size();
        }

        if (nonnulllist != null && !nonnulllist.get(index).func_190926_b()) {
            ItemStack itemstack = nonnulllist.get(index);
            nonnulllist.set(index, ItemStack.field_190927_a);
            return itemstack;
        } else {
            return ItemStack.field_190927_a;
        }
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    @Override
    public void func_70299_a(int index, ItemStack stack) {
        this.func_70296_d();
        NonNullList<ItemStack> nonnulllist = null;

        for (NonNullList<ItemStack> nonnulllist1 : this.allInventories) {
            if (index < nonnulllist1.size()) {
                nonnulllist = nonnulllist1;
                break;
            }

            index -= nonnulllist1.size();
        }

        if (nonnulllist != null) {
            nonnulllist.set(index, stack);
        }
    }

    /**
     * Writes the inventory out as a list of compound tags. This is where the slot indices are used (+100 for armor, +80
     * for crafting).
     */
    public NBTTagList writeToNBT(NBTTagList nbtTagListIn) {
        for (int i = 0; i < this.mainInventory.size(); ++i) {
            if (!this.mainInventory.get(i).func_190926_b()) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.func_74774_a("Slot", (byte) i);
                this.mainInventory.get(i).func_77955_b(nbttagcompound);
                nbtTagListIn.func_74742_a(nbttagcompound);
            }
        }

        for (int j = 0; j < this.armorInventory.size(); ++j) {
            if (!this.armorInventory.get(j).func_190926_b()) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.func_74774_a("Slot", (byte) (j + 100));
                this.armorInventory.get(j).func_77955_b(nbttagcompound1);
                nbtTagListIn.func_74742_a(nbttagcompound1);
            }
        }

        for (int k = 0; k < this.offHandInventory.size(); ++k) {
            if (!this.offHandInventory.get(k).func_190926_b()) {
                NBTTagCompound nbttagcompound2 = new NBTTagCompound();
                nbttagcompound2.func_74774_a("Slot", (byte) (k + 150));
                this.offHandInventory.get(k).func_77955_b(nbttagcompound2);
                nbtTagListIn.func_74742_a(nbttagcompound2);
            }
        }

        this.dirty = false;

        return nbtTagListIn;
    }

    /**
     * Reads from the given tag list and fills the slots in the inventory with the correct items.
     */
    public void readFromNBT(NBTTagList nbtTagListIn) {
        this.mainInventory.clear();
        this.armorInventory.clear();
        this.offHandInventory.clear();

        for (int i = 0; i < nbtTagListIn.func_74745_c(); ++i) {
            NBTTagCompound nbttagcompound = nbtTagListIn.func_150305_b(i);
            int j = nbttagcompound.func_74771_c("Slot") & 255;
            ItemStack itemstack = new ItemStack(nbttagcompound);

            if (!itemstack.func_190926_b()) {
                if (j >= 0 && j < this.mainInventory.size()) {
                    this.mainInventory.set(j, itemstack);
                } else if (j >= 100 && j < this.armorInventory.size() + 100) {
                    this.armorInventory.set(j - 100, itemstack);
                } else if (j >= 150 && j < this.offHandInventory.size() + 150) {
                    this.offHandInventory.set(j - 150, itemstack);
                }
            }
        }
    }

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int func_70302_i_() {
        return this.mainInventory.size() + this.armorInventory.size() + this.offHandInventory.size();
    }

    @Override
    public boolean func_191420_l() {
        for (ItemStack itemstack : this.mainInventory) {
            if (!itemstack.func_190926_b()) {
                return false;
            }
        }

        for (ItemStack itemstack1 : this.armorInventory) {
            if (!itemstack1.func_190926_b()) {
                return false;
            }
        }

        for (ItemStack itemstack2 : this.offHandInventory) {
            if (!itemstack2.func_190926_b()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the stack in the given slot.
     */
    @Override
    public ItemStack func_70301_a(int index) {
        List<ItemStack> list = null;

        for (NonNullList<ItemStack> nonnulllist : this.allInventories) {
            if (index < nonnulllist.size()) {
                list = nonnulllist;
                break;
            }

            index -= nonnulllist.size();
        }

        return list == null ? ItemStack.field_190927_a : list.get(index);
    }

    /**
     * Get the name of this object. For players this returns their username
     */
    @Override
    public String func_70005_c_() {
        return "container.inventory";
    }

    /**
     * Returns true if this thing is named
     */
    @Override
    public boolean func_145818_k_() {
        return false;
    }

    /**
     * Get the formatted ChatComponent that will be used for the sender's username in chat
     */
    @Override
    public ITextComponent func_145748_c_() {
        return this.func_145818_k_() ? new TextComponentString(this.func_70005_c_()) : new TextComponentTranslation(this.func_70005_c_());
    }

    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     */
    @Override
    public int func_70297_j_() {
        return 64;
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    @Override
    public void func_70296_d() {
        this.dirty = true;
        this.player.markDirty();
    }

    /**
     * Don't rename this method to canInteractWith due to conflicts with Container
     */
    @Override
    public boolean func_70300_a(EntityPlayer player) {
        return true;
    }

    @Override
    public void func_174889_b(EntityPlayer player) {
    }

    @Override
    public void func_174886_c(EntityPlayer player) {
    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For
     * guis use Slot.isItemValid
     */
    @Override
    public boolean func_94041_b(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int func_174887_a_(int id) {
        return 0;
    }

    @Override
    public void func_174885_b(int id, int value) {
    }

    @Override
    public int func_174890_g() {
        return 0;
    }

    @Override
    public void func_174888_l() {
        for (List<ItemStack> list : this.allInventories) {
            list.clear();
        }
    }

}
