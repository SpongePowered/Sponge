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
package org.spongepowered.common.item.inventory.fabric;

import com.google.common.collect.ImmutableSet;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.text.translation.FixedTranslation;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.bridge.item.inventory.InventoryBridge;
import org.spongepowered.common.item.inventory.lens.Fabric;

import java.util.Collection;

/**
 * Provides {@link Fabric} access to an {@link IInventory}
 */
class IInventoryTranslator implements InventoryTranslator<IInventory> {
    @Override
    public Collection<InventoryBridge> allInventories(IInventory inventory) {
        return ImmutableSet.of((InventoryBridge) inventory);
    }

    @Override
    @SuppressWarnings("unchecked")
    public InventoryBridge get(IInventory inventory, int index) {
        return (InventoryBridge) inventory;
    }

    @Override
    public ItemStack getStack(IInventory inventory, int index) {
        return inventory.func_70301_a(index);
    }

    @Override
    public void setStack(IInventory inventory, int index, ItemStack stack) {
        inventory.func_70299_a(index, stack);
    }

    @Override
    public int getMaxStackSize(IInventory inventory) {
        return inventory.func_70297_j_();
    }

    @Override
    public Translation getDisplayName(IInventory inventory) {
        return new FixedTranslation(inventory.func_145748_c_().func_150260_c());
    }

    @Override
    public int getSize(IInventory inventory) {
        return inventory.func_70302_i_();
    }

    @Override
    public void clear(IInventory inventory) {
        inventory.func_174888_l();
    }

    @Override
    public void markDirty(IInventory inventory) {
        inventory.func_70296_d();
    }
}
