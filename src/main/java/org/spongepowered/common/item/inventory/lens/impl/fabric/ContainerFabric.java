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
package org.spongepowered.common.item.inventory.lens.impl.fabric;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.text.translation.FixedTranslation;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftFabric;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ContainerFabric extends MinecraftFabric {

    private Translation displayName;
    private final Container container;
    private final Set<IInventory> all;

    public ContainerFabric(Container container) {
        this(ContainerFabric.getFirstDisplayName(container), container);
    }

    private ContainerFabric(Translation displayName, Container container) {
        this.displayName = displayName;
        this.container = container;

        List<Slot> slots = this.container.inventorySlots;

        Builder<IInventory> builder = ImmutableSet.<IInventory>builder();
        for (Slot slot : slots) {
            if (slot.inventory != null) {
                builder.add(slot.inventory);
            }
        }
        this.all = builder.build();
    }

    @Override
    public Collection<IInventory> allInventories() {
        return this.all;
    }

    @Override
    public IInventory get(int index) {
        return this.container.getSlot(index).inventory;
    }

    @Override
    public ItemStack getStack(int index) {
        return this.container.getSlot(index).getStack();
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        this.container.getSlot(index).putStack(stack);
    }

    @Override
    public int getMaxStackSize() {
        int max = 0;
        for (IInventory inv : this.all) {
            max = Math.max(max, inv.getInventoryStackLimit());
        }
        return max;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    @Override
    public int getSize() {
        return this.container.inventorySlots.size();
    }

    @Override
    public void clear() {
        this.all.forEach(IInventory::clear);
    }

    @Override
    public void markDirty() {
        this.container.detectAndSendChanges();
    }

    static Translation getFirstDisplayName(Container container) {
        if (container.inventorySlots.size() == 0) {
            return new FixedTranslation("Container");
        }

        try
        {
            Slot slot = container.getSlot(0);
            return slot.inventory != null && slot.inventory.getDisplayName() != null ?
                    new FixedTranslation(slot.inventory.getDisplayName().getUnformattedText()) :
                    new FixedTranslation("UNKNOWN: " + container.getClass().getName());
        }
        catch (AbstractMethodError e)
        {
            SpongeImpl.getLogger().warn("AbstractMethodError! Could not find displayName for " +
                    container.getSlot(0).inventory.getClass().getName(), e);
            return new FixedTranslation("UNKNOWN: " + container.getClass().getName());
        }
    }

    public Container getContainer() {
        return this.container;
    }
}
