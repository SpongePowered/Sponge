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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import java.util.Arrays;
import java.util.List;

public class SpongeUserInventory implements IInventory {

    // sourced from InventoryPlayer

    /** An array of 36 item stacks indicating the main player inventory (including the visible bar). */
    final NonNullList<ItemStack> mainInventory = NonNullList.withSize(36, ItemStack.EMPTY);
    /** An array of 4 item stacks containing the currently worn armor pieces. */
    final NonNullList<ItemStack> armorInventory = NonNullList.withSize(4, ItemStack.EMPTY);
    final NonNullList<ItemStack> offHandInventory = NonNullList.withSize(1, ItemStack.EMPTY);
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
        return PlayerInventory.isHotbar(this.currentItem) ? this.mainInventory.get(this.currentItem) : ItemStack.EMPTY;
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    @Override
    public ItemStack decrStackSize(int index, int count) {
        this.markDirty();
        List<ItemStack> list = null;

        for (NonNullList<ItemStack> nonnulllist : this.allInventories) {
            if (index < nonnulllist.size()) {
                list = nonnulllist;
                break;
            }

            index -= nonnulllist.size();
        }

        return list != null && !list.get(index).isEmpty() ? ItemStackHelper.getAndSplit(list, index, count) : ItemStack.EMPTY;
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    @Override
    public ItemStack removeStackFromSlot(int index) {
        this.markDirty();
        NonNullList<ItemStack> nonnulllist = null;

        for (NonNullList<ItemStack> nonnulllist1 : this.allInventories) {
            if (index < nonnulllist1.size()) {
                nonnulllist = nonnulllist1;
                break;
            }

            index -= nonnulllist1.size();
        }

        if (nonnulllist != null && !nonnulllist.get(index).isEmpty()) {
            ItemStack itemstack = nonnulllist.get(index);
            nonnulllist.set(index, ItemStack.EMPTY);
            return itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.markDirty();
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
    public ListNBT writeToNBT(ListNBT nbtTagListIn) {
        for (int i = 0; i < this.mainInventory.size(); ++i) {
            if (!this.mainInventory.get(i).isEmpty()) {
                CompoundNBT nbttagcompound = new CompoundNBT();
                nbttagcompound.putByte("Slot", (byte) i);
                this.mainInventory.get(i).write(nbttagcompound);
                nbtTagListIn.appendTag(nbttagcompound);
            }
        }

        for (int j = 0; j < this.armorInventory.size(); ++j) {
            if (!this.armorInventory.get(j).isEmpty()) {
                CompoundNBT nbttagcompound1 = new CompoundNBT();
                nbttagcompound1.putByte("Slot", (byte) (j + 100));
                this.armorInventory.get(j).write(nbttagcompound1);
                nbtTagListIn.appendTag(nbttagcompound1);
            }
        }

        for (int k = 0; k < this.offHandInventory.size(); ++k) {
            if (!this.offHandInventory.get(k).isEmpty()) {
                CompoundNBT nbttagcompound2 = new CompoundNBT();
                nbttagcompound2.putByte("Slot", (byte) (k + 150));
                this.offHandInventory.get(k).write(nbttagcompound2);
                nbtTagListIn.appendTag(nbttagcompound2);
            }
        }

        this.dirty = false;

        return nbtTagListIn;
    }

    /**
     * Reads from the given tag list and fills the slots in the inventory with the correct items.
     */
    public void readFromNBT(ListNBT nbtTagListIn) {
        this.mainInventory.clear();
        this.armorInventory.clear();
        this.offHandInventory.clear();

        for (int i = 0; i < nbtTagListIn.tagCount(); ++i) {
            CompoundNBT nbttagcompound = nbtTagListIn.getCompound(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            ItemStack itemstack = new ItemStack(nbttagcompound);

            if (!itemstack.isEmpty()) {
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
    public int getSizeInventory() {
        return this.mainInventory.size() + this.armorInventory.size() + this.offHandInventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.mainInventory) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        for (ItemStack itemstack1 : this.armorInventory) {
            if (!itemstack1.isEmpty()) {
                return false;
            }
        }

        for (ItemStack itemstack2 : this.offHandInventory) {
            if (!itemstack2.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the stack in the given slot.
     */
    @Override
    public ItemStack getStackInSlot(int index) {
        List<ItemStack> list = null;

        for (NonNullList<ItemStack> nonnulllist : this.allInventories) {
            if (index < nonnulllist.size()) {
                list = nonnulllist;
                break;
            }

            index -= nonnulllist.size();
        }

        return list == null ? ItemStack.EMPTY : list.get(index);
    }

    /**
     * Get the name of this object. For players this returns their username
     */
    @Override
    public String getName() {
        return "container.inventory";
    }

    /**
     * Returns true if this thing is named
     */
    @Override
    public boolean hasCustomName() {
        return false;
    }

    /**
     * Get the formatted ChatComponent that will be used for the sender's username in chat
     */
    @Override
    public ITextComponent getDisplayName() {
        return this.hasCustomName() ? new StringTextComponent(this.getName()) : new TranslationTextComponent(this.getName());
    }

    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     */
    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    @Override
    public void markDirty() {
        this.dirty = true;
        this.player.markDirty();
    }

    /**
     * Don't rename this method to canInteractWith due to conflicts with Container
     */
    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return true;
    }

    @Override
    public void openInventory(PlayerEntity player) {
    }

    @Override
    public void closeInventory(PlayerEntity player) {
    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For
     * guis use Slot.isItemValid
     */
    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        for (List<ItemStack> list : this.allInventories) {
            list.clear();
        }
    }

}
