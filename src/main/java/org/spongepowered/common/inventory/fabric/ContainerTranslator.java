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
package org.spongepowered.common.inventory.fabric;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.common.bridge.world.inventory.InventoryBridge;

import java.util.Collection;

/**
 * Provides {@link Fabric} access to an {@link Container}
 */
class ContainerTranslator implements InventoryTranslator<Container> {
    @Override
    public Collection<InventoryBridge> allInventories(Container inventory) {
        return ImmutableSet.of((InventoryBridge) inventory);
    }

    @Override
    @SuppressWarnings("unchecked")
    public InventoryBridge get(Container inventory, int index) {
        return (InventoryBridge) inventory;
    }

    @Override
    public ItemStack getStack(Container inventory, int index) {
        return inventory.getItem(index);
    }

    @Override
    public void setStack(Container inventory, int index, ItemStack stack) {
        inventory.setItem(index, stack);
    }

    @Override
    public int getMaxStackSize(Container inventory) {
        return inventory.getMaxStackSize();
    }

    @Override
    public int getSize(Container inventory) {
        return inventory.getContainerSize();
    }

    @Override
    public void clear(Container inventory) {
        inventory.clearContent();
    }

    @Override
    public void markDirty(Container inventory) {
        inventory.setChanged();
    }
}
