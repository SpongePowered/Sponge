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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryProperties;
import org.spongepowered.api.item.inventory.slot.SlotIndex;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

public class CustomInventory implements IInventory {

    private final List<Inventory> inventories;

    // shadow usage
    private SlotProvider slots;
    private Lens lens;
    private Fabric fabric;

    private Map<Property<?>, Object> properties = new HashMap<>();
    private final int size;
    private Carrier carrier;

    private PluginContainer plugin;

    private ITextComponent customInventory = new TextComponentString("Custom Inventory");

    public CustomInventory(int size, Lens lens, SlotProvider provider, List<Inventory> inventories, @Nullable UUID identity, @Nullable Carrier carrier) {
        this.size = size;
        this.properties.put(InventoryProperties.UNIQUE_ID, identity);
        this.carrier = carrier;
        this.lens = lens;
        this.slots = provider;
        this.inventories = inventories;
        this.plugin = SpongeImplHooks.getActiveModContainer();
    }

    // IInventory delegation

    @Nullable
    @Override
    public ITextComponent getCustomName() {
        return this.customInventory;
    }

    @Override
    public ITextComponent getName() {
        return this.customInventory;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public int getSizeInventory() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size != 0;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        int offset = 0;
        for (Inventory inv : this.inventories) {
            if (inv.capacity() > index - offset) {
                offset += inv.capacity();
                continue;
            }
            return inv.peek(SlotIndex.of(index - offset)).map(ItemStackUtil::toNative).orElse(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        int offset = 0;
        for (Inventory inv : this.inventories) {
            if (inv.capacity() > index - offset) {
                offset += inv.capacity();
                continue;
            }
            return inv.poll(SlotIndex.of(index - offset), count).map(ItemStackUtil::toNative).orElse(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        int offset = 0;
        for (Inventory inv : this.inventories) {
            if (inv.capacity() > index - offset) {
                offset += inv.capacity();
                continue;
            }
            return inv.poll(SlotIndex.of(index - offset)).map(ItemStackUtil::toNative).orElse(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        int offset = 0;
        for (Inventory inv : this.inventories) {
            if (inv.capacity() > index - offset) {
                offset += inv.capacity();
                continue;
            }
            inv.set(SlotIndex.of(index - offset), ItemStackUtil.fromNative(stack));
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        for (Inventory inventory : this.inventories) {
            if (inventory instanceof IInventory) {
                ((IInventory) inventory).markDirty();
            }
        }
    }

    @Override
    public void clear() {
        for (Inventory inventory : this.inventories) {
            inventory.clear();
        }
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

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

    // Custom Inventory

    public Map<Property<?>, ?> getProperties() {
        return this.properties;
    }

    public Carrier getCarrier() {
        return this.carrier;
    }
}
