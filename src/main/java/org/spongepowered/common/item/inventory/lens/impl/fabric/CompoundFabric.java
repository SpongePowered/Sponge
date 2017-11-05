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

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftFabric;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CompoundFabric extends MinecraftFabric {

    private final MinecraftFabric fabric1;
    private final MinecraftFabric fabric2;
    private Translation displayName;

    public CompoundFabric(MinecraftFabric fabric1, MinecraftFabric fabric2) {
        this.fabric1 = fabric1;
        this.fabric2 = fabric2;
        this.displayName = fabric1.getDisplayName();
    }

    @Override
    public Collection<IInventory> allInventories() {
        Set<IInventory> inv = new HashSet<>();
        inv.addAll(fabric1.allInventories());
        inv.addAll(fabric2.allInventories());
        return inv;
    }

    @Override
    public IInventory get(int index) {

        if (index < this.fabric1.getSize()) {
            return fabric1.get(index);
        }
        return fabric2.get(index - fabric1.getSize());
    }

    @Override
    public ItemStack getStack(int index) {
        if (index < this.fabric1.getSize()) {
            return fabric1.getStack(index);
        }
        return fabric2.getStack(index - fabric1.getSize());
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        if (index < this.fabric1.getSize()) {
            fabric1.setStack(index, stack);
        } else {
            fabric2.setStack(index - fabric1.getSize(), stack);
        }
    }

    @Override
    public int getMaxStackSize() {
        return Math.max(this.fabric1.getMaxStackSize(), this.fabric2.getMaxStackSize());
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    @Override
    public int getSize() {
        return this.fabric1.getSize() + this.fabric2.getSize();
    }

    @Override
    public void clear() {
        this.fabric1.clear();
        this.fabric2.clear();
    }

    @Override
    public void markDirty() {
        this.fabric1.markDirty();
        this.fabric2.markDirty();
    }

}
