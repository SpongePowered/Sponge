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
package org.spongepowered.common.item.inventory.lens;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftFabric;
import org.spongepowered.common.item.inventory.lens.impl.fabric.ContainerFabric;

import java.util.Collection;

/**
 * A fabric is an underlying view of an indexed container, this allows raw
 * inventories and containers to be handled the same via lenses, without the
 * lenses themselves having to care about the type of the target object.
 *
 * @param <TInventory> Type of inventory being manipulated.
 */
public interface Fabric<TInventory> {

    /**
     * Return all inventories which compose this fabric, order is not guaranteed
     * or enforced.
     */
    Collection<TInventory> allInventories();

    /**
     * Return the inventory at the specified index in the fabric.
     *
     * @param index
     * @return
     */
    TInventory get(int index);

    ItemStack getStack(int index);

    void setStack(int index, ItemStack stack);

    int getMaxStackSize();

    Translation getDisplayName();

    int getSize();

    void clear();

    void markDirty();

}
